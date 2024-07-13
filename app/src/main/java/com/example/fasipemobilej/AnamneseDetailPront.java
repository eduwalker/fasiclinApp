package com.example.fasipemobilej;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fasipemobilej.model.response.AnamnePerguntaResposta;
import com.example.fasipemobilej.model.response.AnamneseDetailResponse;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.example.fasipemobilej.network.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnamneseDetailPront extends AppCompatActivity {

    private WebView webView;
    private Button btnGeneratePdf;
    private ImageButton buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_anamneseprontpdf);

        webView = findViewById(R.id.webviewAnamnese);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);  // Habilitar armazenamento DOM
        webView.setWebViewClient(new WebViewClient());
        getSupportActionBar().hide();

        long anamneseId = getIntent().getLongExtra("EXTRA_ANAMNESE_ID", -1);
        if (anamneseId != -1) {
            loadAnamneseDetails(anamneseId);
        } else {
            Toast.makeText(this, "ID da Anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        btnGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPdfFromWebView();
            }
        });

        buttonReturn = findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    String htmlContent = buildHtml(anamnese);
                    Log.d("AnamneseDetailPront", "HTML content: " + htmlContent);
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
                } else {
                    Log.e("API Error", "Response not successful or null: " + response.errorBody());
                    Toast.makeText(AnamneseDetailPront.this, "Erro ao carregar anamnese", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseDetailResponse> call, Throwable t) {
                Log.e("API Error", "Failed to load anamnese details: " + t.getMessage());
                Toast.makeText(AnamneseDetailPront.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        htmlBuilder.append("<html><head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                .append("h1 { color: #2E7D32; text-align: center; }")
                .append("h2 { color: #4CAF50; }")
                .append("p { font-size: 16px; line-height: 1.6; }")
                .append("strong { color: #388E3C; }")
                .append("</style>")
                .append("</head><body>");
        htmlBuilder.append("<h1>Perguntas e Respostas</h1>");
        for (AnamnePerguntaResposta resposta : anamnese.perguntasRespostas()) {
            htmlBuilder.append("<p><strong>").append(resposta.pergunta()).append(":</strong> ").append(resposta.resposta()).append("</p>");
        }
        htmlBuilder.append("</body></html>");
        Log.d("AnamneseDetailPront", "Generated HTML: " + htmlBuilder.toString());
        return htmlBuilder.toString();
    }
}
