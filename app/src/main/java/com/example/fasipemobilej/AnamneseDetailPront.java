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
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
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
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
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
                .append(".container { background-color: rgba(255, 255, 255, 0.8); padding: 20px; border-radius: 10px; }")
                .append("</style>")
                .append("</head><body>")
                .append("<div class='container'>")
                .append("<h1>Anamnese Fasiclin</h1>");

        for (AnamnePerguntaResposta resposta : anamnese.perguntasRespostas()) {
            String pergunta = resposta.pergunta();
            String respostaTexto = resposta.resposta() != null ? resposta.resposta() : "";

            switch (pergunta) {
                case "Nome":
                    htmlBuilder.append("<p><strong>Nome:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Idade":
                    htmlBuilder.append("<p><strong>Idade:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "RG":
                    htmlBuilder.append("<p><strong>RG:</strong> ").append(formatarRG(respostaTexto)).append("</p>");
                    break;
                case "CPF":
                    htmlBuilder.append("<p><strong>CPF:</strong> ").append(formatarCPF(respostaTexto)).append("</p>");
                case "Sexo":
                    htmlBuilder.append("<p><strong>Sexo:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Cartão SUS":
                    htmlBuilder.append("<p><strong>Cartão SUS:</strong> ").append(formatarSUS(respostaTexto)).append("</p>");
                    break;
                case "Leito":
                    htmlBuilder.append("<p><strong>Leito:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Profissão":
                    htmlBuilder.append("<p><strong>Profissão:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Escolaridade":
                    htmlBuilder.append("<p><strong>Escolaridade:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Diagnóstico Médico":
                    htmlBuilder.append("<p><strong>Diagnóstico Médico:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Motivo da internação":
                    htmlBuilder.append("<p><strong>Motivo da internação:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Doenças Crônicas":
                    htmlBuilder.append("<p><strong>Doenças Crônicas:</strong> ").append(respostaTexto).append("</p>");
                    break;
                case "Estado Civil":
                    htmlBuilder.append("<p><strong>Estado Civil:</strong> ").append(respostaTexto).append("</p>");
                    break;
                default:
                    htmlBuilder.append("<p><strong>").append(pergunta).append(":</strong> ").append(respostaTexto).append("</p>");
                    break;
            }
        }

        htmlBuilder.append("</div></body></html>");
        Log.d("AnamneseDetailPront", "Generated HTML: " + htmlBuilder.toString());
        return htmlBuilder.toString();
    }

    private String formatarRG(String rg) {
        return rg.length() == 8 ? rg.replaceAll("(\\d{2})(\\d{3})(\\d{3})", "$1.$2.$3") : rg;
    }

    private String formatarCPF(String cpf) {
        return cpf.length() == 11 ? cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4") : cpf;
    }

    private String formatarSUS(String sus) {
        return sus.length() == 15 ? sus.replaceAll("(\\d{3})(\\d{4})(\\d{4})(\\d{4})", "$1 $2 $3 $4") : sus;
    }
}
