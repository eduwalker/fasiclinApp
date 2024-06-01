package com.example.fasipemobilej;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fasipemobilej.model.request.AnamneseObsRequest;
import com.example.fasipemobilej.model.request.StatusAnamneseRequest;
import com.example.fasipemobilej.model.response.AnamnePerguntaResposta;
import com.example.fasipemobilej.model.response.AnamneseDetailResponse;
import com.example.fasipemobilej.model.response.PacienteResponse;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.example.fasipemobilej.network.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnamneseDetailSupervisorActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnGeneratePdf, btnReprovar, btnAprovar;
    private ImageButton buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_anamnesepdf_sup);

        webView = findViewById(R.id.webviewAnamneseSup);
        webView.getSettings().setJavaScriptEnabled(true);
        getSupportActionBar().hide();


        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        btnReprovar = findViewById(R.id.btnReprovar);
        btnAprovar = findViewById(R.id.btnAprovar);
        buttonReturn = findViewById(R.id.buttonReturn);

        long anamneseId = getIntent().getLongExtra("EXTRA_ANAMNESE_ID", -1);
        if (anamneseId != -1) {
            loadAnamneseDetails(anamneseId);
        } else {
            Toast.makeText(this, "ID da Anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnGeneratePdf.setOnClickListener(v -> createPdfFromWebView());
        btnReprovar.setOnClickListener(v -> showObservationsDialog(anamneseId));
        btnAprovar.setOnClickListener(v -> updateAnamneseStatusAprov(anamneseId, "valid", "Aprovada"));
        buttonReturn.setOnClickListener(v -> finish());
    }

    private void loadAnamneseDetails(long anamneseId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(buildHttpClient())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<AnamneseDetailResponse> call = apiService.getAnamneseById("Bearer " + token, anamneseId);

        call.enqueue(new Callback<AnamneseDetailResponse>() {
            @Override
            public void onResponse(Call<AnamneseDetailResponse> call, Response<AnamneseDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnamneseDetailResponse anamnese = response.body();
                    webView.loadData(buildHtml(anamnese), "text/html; charset=utf-8", "UTF-8");
                } else {
                    Log.e("API Error", "Response not successful or null: " + response.errorBody());
                    Toast.makeText(AnamneseDetailSupervisorActivity.this, "Erro ao carregar anamnese", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseDetailResponse> call, Throwable t) {
                Toast.makeText(AnamneseDetailSupervisorActivity.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    private OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    private void createPdfFromWebView() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("AnamneseDetails");

        String jobName = getString(R.string.app_name) + " Document";

        // Define os atributos de impressão
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        builder.setResolution(new PrintAttributes.Resolution("id", "Print", 300, 300));
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
        builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

        printManager.print(jobName, printAdapter, builder.build());
    }

    private String buildHtml(AnamneseDetailResponse anamnese) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<h1>Detalhes da Anamnese</h1>");

        PacienteResponse paciente = anamnese.paciente();
        if (paciente != null) {
            htmlBuilder.append("<p>Nome: ").append(paciente.nome_pac()).append("</p>");
            htmlBuilder.append("<p>CPF: ").append(paciente.cpf_pac()).append("</p>");
            htmlBuilder.append("<p>Data de Nascimento: ").append(paciente.data_nasc_pac()).append("</p>");
        } else {
            htmlBuilder.append("<p>Informações do paciente não disponíveis.</p>");
        }

        htmlBuilder.append("<p>Data da Anamnese: ").append(anamnese.dataAnamnese()).append("</p>");
        htmlBuilder.append("<p>Status da Anamnese: ").append(anamnese.statusAnamnese()).append("</p>");

        htmlBuilder.append("<h2>Perguntas e Respostas</h2>");
        for (AnamnePerguntaResposta resposta : anamnese.perguntasRespostas()) {
            htmlBuilder.append("<p><strong>").append(resposta.pergunta()).append(":</strong> ").append(resposta.resposta()).append("</p>");
        }

        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    private void showObservationsDialog(long anamneseId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_reprovar, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText editObservations = dialogView.findViewById(R.id.editTextObservations);
        Button btnSubmitObservations = dialogView.findViewById(R.id.buttonSubmitObservations);

        btnSubmitObservations.setOnClickListener(v -> {
            String observacoes = editObservations.getText().toString();
            if (!observacoes.isEmpty()) {
                updateAnamneseStatus(anamneseId, "Reprovada", observacoes);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Por favor, insira observações.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateAnamneseStatus(long anamneseId, String status, String observacoes) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(buildHttpClient())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        AnamneseObsRequest request = new AnamneseObsRequest(anamneseId, status, observacoes);
        Call<Void> call = apiService.updateAnamneseObservations("Bearer " + token, request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnamneseDetailSupervisorActivity.this, "Status e observações atualizados com sucesso.", Toast.LENGTH_SHORT).show();
                    finish(); // Voltar à tela anterior
                } else {
                    Toast.makeText(AnamneseDetailSupervisorActivity.this, "Erro ao atualizar status e observações.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AnamneseDetailSupervisorActivity.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    private void updateAnamneseStatusAprov(long anamneseId, String status, String statusFn) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(buildHttpClient())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        StatusAnamneseRequest request = new StatusAnamneseRequest(anamneseId, status, statusFn);
        Call<Void> call = apiService.updateAnamneseStatus("Bearer " + token, request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnamneseDetailSupervisorActivity.this, "Status atualizados com sucesso, Anamnese APROVAD!", Toast.LENGTH_SHORT).show();
                    finish(); // Voltar à tela anterior
                } else {
                    Toast.makeText(AnamneseDetailSupervisorActivity.this, "Erro ao aprovar Anamnese...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AnamneseDetailSupervisorActivity.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
