package com.example.fasipemobilej;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fasipemobilej.model.response.AnamneseListResponse;
import com.example.fasipemobilej.model.response.AnamneseResponse;
import com.example.fasipemobilej.model.response.AnamneseResponseID;
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anamnese_list);

        recyclerView = findViewById(R.id.recyclerViewAnamneses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        anamneseAdapter = new AnamneseAdapter(new ArrayList<>(), this::showOptionsDialog);
        recyclerView.setAdapter(anamneseAdapter);
        getSupportActionBar().hide();


        fetchAnamneses();
        backButton();
    }




    private void fetchAnamneses() {
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

        Call<List<AnamneseResponse>> call = service.listAnamneses(token);

        call.enqueue(new Callback<List<AnamneseResponse>>() {
            @Override
            public void onResponse(Call<List<AnamneseResponse>> call, Response<List<AnamneseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnamneseResponse> anamneses = response.body();
                    anamneseAdapter.updateData(anamneses);
                } else {
                    Toast.makeText(TelaListAnamnese.this, "Erro ao buscar anamneses", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnamneseResponse>> call, Throwable t) {
                Toast.makeText(TelaListAnamnese.this, "Falha na comunicação", Toast.LENGTH_LONG).show();
                Log.e("Retrofit", "Erro na comunicação: " + t.getMessage());
            }
        });
    }


    public void backButton() {
        ImageButton backButton = findViewById(R.id.buttonReturn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Encerra a atividade
            }
        });

    }


    private void showOptionsDialog(AnamneseResponse anamnese) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_anamnese_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaListAnamnese.this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();


        popupView.findViewById(R.id.btVisualizar).setOnClickListener(v -> {
            Intent intent = new Intent(TelaListAnamnese.this, AnamneseDetailActivity.class);
            intent.putExtra("EXTRA_ANAMNESE_ID", anamnese.idAnamnese());  // Assumindo que há um método idAnamnese() que retorna o ID da anamnese
            startActivity(intent);
            dialog.dismiss();
        });

        popupView.findViewById(R.id.btEditar).setOnClickListener(v -> {
            Toast.makeText(TelaListAnamnese.this, "Editar: " + anamnese.pacienteResponseDTO().nome_pac(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        popupView.findViewById(R.id.btCancelar).setOnClickListener(v -> {
            Toast.makeText(TelaListAnamnese.this, "Cancelar", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }


}