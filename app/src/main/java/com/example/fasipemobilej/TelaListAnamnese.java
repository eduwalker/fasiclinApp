package com.example.fasipemobilej;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fasipemobilej.model.response.AnamneseListPage;
import com.example.fasipemobilej.model.response.AnamneseResponse;
import com.example.fasipemobilej.network.AnamneseAdapter;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.example.fasipemobilej.network.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaListAnamnese extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnamneseAdapter anamneseAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int currentPage = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anamnese_list);

        recyclerView = findViewById(R.id.recyclerViewAnamneses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        anamneseAdapter = new AnamneseAdapter(new ArrayList<>(), this::showOptionsDialog);
        recyclerView.setAdapter(anamneseAdapter);
        getSupportActionBar().hide();

        fetchAnamneses(currentPage);
        setupPaginationButtons();
        setupSwipeRefresh();
        backButton();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchAnamneses(currentPage));
    }

    private void setupPaginationButtons() {
        findViewById(R.id.buttonPreviousPage).setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                fetchAnamneses(currentPage);
            }
        });

        findViewById(R.id.buttonNextPage).setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                fetchAnamneses(currentPage);
            }
        });
    }

    private void fetchAnamneses(int page) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService service = retrofit.create(ApiService.class);

        Call<AnamneseListPage> call = service.listAnamnesesPaged(token, page, 10);

        call.enqueue(new Callback<AnamneseListPage>() {
            @Override
            public void onResponse(Call<AnamneseListPage> call, Response<AnamneseListPage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnamneseListPage anamneseListPage = response.body();
                    Log.d("API Response", "Received " + anamneseListPage.content().size() + " anamneses");
                    List<AnamneseResponse> anamneses = anamneseListPage.content();
                    anamneseAdapter.updateData(anamneses);
                    totalPages = anamneseListPage.totalPages();
                    updatePaginationInfo();
                } else {
                    Toast.makeText(TelaListAnamnese.this, "Erro ao buscar anamneses", Toast.LENGTH_LONG).show();
                    Log.e("API Error", "Error response code: " + response.code());
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<AnamneseListPage> call, Throwable t) {
                Toast.makeText(TelaListAnamnese.this, "Falha na comunicação", Toast.LENGTH_LONG).show();
                Log.e("Retrofit", "Erro na comunicação: " + t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updatePaginationInfo() {
        TextView textPageInfo = findViewById(R.id.textPageInfo);
        textPageInfo.setText(String.format("%d/%d", currentPage + 1, totalPages));
    }

    public void backButton() {
        ImageButton backButton = findViewById(R.id.buttonReturn);
        backButton.setOnClickListener(v -> finish());
    }

    private void showOptionsDialog(AnamneseResponse anamnese) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_anamnese_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaListAnamnese.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        popupView.findViewById(R.id.btVisualizar).setOnClickListener(v -> {
            Intent intent = new Intent(TelaListAnamnese.this, AnamneseDetailActivity.class);
            intent.putExtra("EXTRA_ANAMNESE_ID", anamnese.idAnamnese());
            startActivity(intent);
            dialog.dismiss();
        });

        popupView.findViewById(R.id.btEditar).setOnClickListener(v -> {
            if ("Reprovada".equals(anamnese.statusAnamneseFn())) {
                Intent intent = new Intent(TelaListAnamnese.this, TelaEditarAnamnese.class);
                intent.putExtra("EXTRA_ANAMNESE_ID", anamnese.idAnamnese());
                intent.putExtra("EXTRA_NOME", anamnese.pacienteResponseDTO().nome_pac());
                intent.putExtra("EXTRA_CPF", anamnese.pacienteResponseDTO().cpf_pac());
                intent.putExtra("EXTRA_DATA_NASCIMENTO", anamnese.pacienteResponseDTO().data_nasc_pac().toString());
                startActivity(intent);
            } else {
                Toast.makeText(TelaListAnamnese.this, "Apenas anamneses REPROVADAS podem ser editadas.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        popupView.findViewById(R.id.btObs).setOnClickListener(v -> {
            if ("Reprovada".equals(anamnese.statusAnamneseFn())) {
                showObservacoesDialog(anamnese.observacoes());
            } else {
                Toast.makeText(TelaListAnamnese.this, "Observações disponíveis apenas para anamneses REPROVADAS!.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showObservacoesDialog(String observacoes) {
        LayoutInflater inflater = getLayoutInflater();
        View observacoesView = inflater.inflate(R.layout.dialog_observacoes, null);

        TextView textObservacoes = observacoesView.findViewById(R.id.textObservacoes);
        textObservacoes.setText(observacoes);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaListAnamnese.this);
        builder.setView(observacoesView);

        AlertDialog dialog = builder.create();

        observacoesView.findViewById(R.id.buttonFechar).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
