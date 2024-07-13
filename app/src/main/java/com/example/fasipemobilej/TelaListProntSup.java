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

import com.example.fasipemobilej.model.response.AnamneseListPage;
import com.example.fasipemobilej.model.response.AnamneseResponse;
import com.example.fasipemobilej.network.AnamneseProntAdapter;
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

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaListProntSup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnamneseProntAdapter anamneseAdapter;
    private int currentPage = 0;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anamnese_list_sup);

        recyclerView = findViewById(R.id.recyclerViewAnamneses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        anamneseAdapter = new AnamneseProntAdapter(new ArrayList<>(), this::showOptionsDialog);
        recyclerView.setAdapter(anamneseAdapter);
        getSupportActionBar().hide();

        setupPaginationButtons();
        fetchAnamneses(currentPage);
        backButton();
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

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(client)
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService service = retrofit.create(ApiService.class);

        Call<AnamneseListPage> call = service.listApprovedAnamnesesBySupervisor("Bearer " + token, page, 10);

        call.enqueue(new Callback<AnamneseListPage>() {
            @Override
            public void onResponse(Call<AnamneseListPage> call, Response<AnamneseListPage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnamneseListPage anamneseListPage = response.body();
                    List<AnamneseResponse> approvedAnamneses = anamneseListPage.content();
                    Log.d("TelaListProntSup", "Dados recebidos: " + approvedAnamneses.size() + " anamneses");
                    anamneseAdapter.updateData(approvedAnamneses);
                    totalPages = anamneseListPage.totalPages();
                    updatePaginationInfo();
                } else {
                    Log.e("FetchAnamneses", "Erro na resposta: " + response.message());
                    Toast.makeText(TelaListProntSup.this, "Erro ao buscar anamneses", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseListPage> call, Throwable t) {
                Log.e("FetchAnamneses", "Falha na comunicação: " + t.getMessage());
                Toast.makeText(TelaListProntSup.this, "Falha na comunicação", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePaginationInfo() {
        TextView textPageInfo = findViewById(R.id.textPageInfo);
        textPageInfo.setText(String.format("%d/%d", currentPage + 1, totalPages));
    }

    private void backButton() {
        ImageButton backButton = findViewById(R.id.buttonReturn);
        backButton.setOnClickListener(v -> finish());
    }

    private void showOptionsDialog(AnamneseResponse anamnese) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_anamnese_options_sup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaListProntSup.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        popupView.findViewById(R.id.btVisualizarSup).setOnClickListener(v -> {
            Intent intent = new Intent(TelaListProntSup.this, AnamneseDetailSupervisorActivity.class);
            intent.putExtra("EXTRA_ANAMNESE_ID", anamnese.idAnamnese());
            startActivity(intent);
            dialog.dismiss();
        });

        popupView.findViewById(R.id.btObservacoes).setOnClickListener(v -> {
            Toast.makeText(TelaListProntSup.this, "Observações: " + anamnese.pacienteResponseDTO().nome_pac(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
