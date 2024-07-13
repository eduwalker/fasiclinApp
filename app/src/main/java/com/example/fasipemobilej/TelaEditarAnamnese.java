package com.example.fasipemobilej;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fasipemobilej.model.request.AnamneseAnswerRequest;
import com.example.fasipemobilej.model.response.AnamnePerguntaResposta;
import com.example.fasipemobilej.model.response.AnamneseAnswerResponse;
import com.example.fasipemobilej.model.response.AnamneseDetailResponse;
import com.example.fasipemobilej.model.request.RespostaDTO;
import com.example.fasipemobilej.model.request.StatusAnamneseRequest;
import com.example.fasipemobilej.model.response.AnamneseStatusResponse;
import com.example.fasipemobilej.model.response.StringResponse;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.example.fasipemobilej.network.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaEditarAnamnese extends AppCompatActivity {

    private Long anamneseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_edit);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        String nome = intent.getStringExtra("EXTRA_NOME");
        String cpf = intent.getStringExtra("EXTRA_CPF");
        anamneseId = intent.getLongExtra("EXTRA_ANAMNESE_ID", -1);
        String dataNascimentoStr = intent.getStringExtra("EXTRA_DATA_NASCIMENTO");

        EditText editNome = findViewById(R.id.editNome);
        editNome.setText(nome);

        EditText editCpf = findViewById(R.id.editCpf);
        editCpf.setText(formatarCPF(cpf));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dataNascimento = LocalDate.parse(dataNascimentoStr, formatter);
        LocalDate agora = LocalDate.now();
        int idade = Period.between(dataNascimento, agora).getYears();

        EditText editIdade = findViewById(R.id.editIdade);
        editIdade.setText(String.valueOf(idade));

        setupSpinnerSexo();
        setupSpinnerEstadoCivil();
        setupBackButton();
        setupButtonConfirmar();
        setupCancelButton();

        if (anamneseId != -1) {
            carregarAnamnese(anamneseId);
        }
    }

    private void carregarAnamnese(Long anamneseId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();


        ApiService apiService = retrofit.create(ApiService.class);
        Call<AnamneseDetailResponse> call = apiService.getAnamneseById("Bearer " + token, anamneseId);

        call.enqueue(new Callback<AnamneseDetailResponse>() {
            @Override
            public void onResponse(Call<AnamneseDetailResponse> call, Response<AnamneseDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    preencherCampos(response.body());
                } else {
                    Log.e("API Error", "Response not successful or null: " + response.errorBody());
                    Toast.makeText(TelaEditarAnamnese.this, "Erro ao carregar anamnese", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseDetailResponse> call, Throwable t) {
                Toast.makeText(TelaEditarAnamnese.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }


    private void preencherCampos(AnamneseDetailResponse anamnese) {
        Log.d("PreencherCampos", "Iniciando preenchimento dos campos");
        for (AnamnePerguntaResposta resposta : anamnese.perguntasRespostas()) {
            String respostaTexto = resposta.resposta() != null ? resposta.resposta() : "";
            Log.d("PreencherCampos", "Pergunta: " + resposta.pergunta() + ", Resposta: " + respostaTexto);

            switch (resposta.pergunta()) {
                case "Nome":
                    ((EditText) findViewById(R.id.editNome)).setText(respostaTexto);
                    break;
                case "Idade":
                    ((EditText) findViewById(R.id.editIdade)).setText(respostaTexto);
                    break;
                case "RG":
                    ((EditText) findViewById(R.id.editRG)).setText(respostaTexto);
                    break;
                case "CPF":
                    ((EditText) findViewById(R.id.editCpf)).setText(formatarCPF(respostaTexto));
                    break;
                case "Cartão SUS":
                    ((EditText) findViewById(R.id.editCartaoSus)).setText(respostaTexto);
                    break;
                case "Leito":
                    ((EditText) findViewById(R.id.editLeito)).setText(respostaTexto);
                    break;
                case "Profissão":
                    ((EditText) findViewById(R.id.editProfissao)).setText(respostaTexto);
                    break;
                case "Escolaridade":
                    ((EditText) findViewById(R.id.editEscolaridade)).setText(respostaTexto);
                    break;
                case "Diagnóstico Médico":
                    ((EditText) findViewById(R.id.editDiagMedico)).setText(respostaTexto);
                    break;
                case "Motivo da internação":
                    Log.d("PreencherCampos", "Configurando Motivo da Internação: " + respostaTexto);
                    ((EditText) findViewById(R.id.editMotivInter)).setText(respostaTexto);
                    break;
                case "Doenças Crônicas":
                    Log.d("PreencherCampos", "Configurando Doenças Crônicas: " + respostaTexto);
                    ((EditText) findViewById(R.id.editDoencaCronica)).setText(respostaTexto);
                    break;
                case "Sexo":
                    Spinner spinnerSexo = findViewById(R.id.spinnerSexo);
                    ArrayAdapter<CharSequence> adapterSexo = (ArrayAdapter<CharSequence>) spinnerSexo.getAdapter();
                    int positionSexo = adapterSexo.getPosition(respostaTexto);
                    if (positionSexo >= 0) {
                        spinnerSexo.setSelection(positionSexo);
                    } else {
                        Log.w("PreencherCampos", "Resposta de Sexo não encontrada no Spinner: " + respostaTexto);
                    }
                    break;
                case "Estado Cívil":
                    Log.d("PreencherCampos", "Configurando Estado Civil: " + respostaTexto);
                    Spinner spinnerEstadoCivil = findViewById(R.id.spinnerEstadoCivil);
                    ArrayAdapter<CharSequence> adapterEstadoCivil = (ArrayAdapter<CharSequence>) spinnerEstadoCivil.getAdapter();
                    int positionEstadoCivil = adapterEstadoCivil.getPosition(respostaTexto);
                    if (positionEstadoCivil >= 0) {
                        spinnerEstadoCivil.setSelection(positionEstadoCivil);
                    } else {
                        Log.w("PreencherCampos", "Resposta de Estado Civil não encontrada no Spinner: " + respostaTexto);
                    }
                    break;
                default:
                    Log.w("PreencherCampos", "Pergunta não tratada: " + resposta.pergunta());
                    break;
            }
        }
        Log.d("PreencherCampos", "Finalizado preenchimento dos campos");
    }






    private void setupSpinnerSexo() {
        Spinner spinnerSexo = findViewById(R.id.spinnerSexo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sexo_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapter);
    }

    private void setupSpinnerEstadoCivil() {
        Spinner spinnerEstadoCivil = findViewById(R.id.spinnerEstadoCivil);
        ArrayAdapter<CharSequence> adapterEstadoCivil = ArrayAdapter.createFromResource(this,
                R.array.estado_civil_array, android.R.layout.simple_spinner_item);
        adapterEstadoCivil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoCivil.setAdapter(adapterEstadoCivil);
    }


    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonReturn);
        backButton.setOnClickListener(v -> {
            Toast.makeText(TelaEditarAnamnese.this, "Todas as alterações realizadas foram desfeitas.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupCancelButton() {
        Button cancelButton = findViewById(R.id.buttonCancelar);
        cancelButton.setOnClickListener(v -> showCancelDialog());
    }

    private void setupButtonConfirmar() {
        Button buttonConfirmar = findViewById(R.id.buttonConfirmar);
        buttonConfirmar.setOnClickListener(v -> {
            if (!verificarCampos()) {
                Toast.makeText(TelaEditarAnamnese.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                confirmarAnamnese();
            }
        });
    }

    private void confirmarAnamnese() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        if (anamneseId == -1) {
            Toast.makeText(TelaEditarAnamnese.this, "Erro: ID da anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<RespostaDTO> respostas = coletarDadosDeRespostas();
        if (respostas.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        AnamneseAnswerRequest request = new AnamneseAnswerRequest(anamneseId, respostas);

        // Log para depuração
        Log.d("AnamneseRequest", new Gson().toJson(request));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<ResponseBody> call = service.updateRespostas("Bearer " + token, request);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String message = response.body().string();
                        Toast.makeText(TelaEditarAnamnese.this, message, Toast.LENGTH_SHORT).show();
                        Log.d("API Response", "Anamnese enviada com sucesso: " + message);
                        Intent intent = new Intent(TelaEditarAnamnese.this, TelaPrincipal.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        finish();
                        startActivity(intent);
                    } catch (IOException e) {
                        Log.e("API Error", "Erro ao ler a resposta: " + e.getMessage(), e);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error", "Erro ao enviar anamnese: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e("API Error", "Erro ao extrair a mensagem de erro", e);
                    }
                    Toast.makeText(TelaEditarAnamnese.this, "Erro ao enviar anamnese!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Retrofit", "Erro na comunicação: " + t.getMessage());
                Toast.makeText(TelaEditarAnamnese.this, "Erro no envio da anamnese", Toast.LENGTH_LONG).show();
            }
        });
    }


    private List<RespostaDTO> coletarDadosDeRespostas() {
        List<RespostaDTO> respostas = new ArrayList<>();

        respostas.add(new RespostaDTO(1, ((EditText) findViewById(R.id.editNome)).getText().toString()));
        respostas.add(new RespostaDTO(2, ((EditText) findViewById(R.id.editIdade)).getText().toString()));
        respostas.add(new RespostaDTO(3, ((EditText) findViewById(R.id.editRG)).getText().toString()));
        respostas.add(new RespostaDTO(4, ((EditText) findViewById(R.id.editCpf)).getText().toString()));
        respostas.add(new RespostaDTO(5, ((EditText) findViewById(R.id.editCartaoSus)).getText().toString()));
        respostas.add(new RespostaDTO(6, ((EditText) findViewById(R.id.editLeito)).getText().toString()));
        respostas.add(new RespostaDTO(7, ((EditText) findViewById(R.id.editProfissao)).getText().toString()));
        respostas.add(new RespostaDTO(8, ((EditText) findViewById(R.id.editEscolaridade)).getText().toString()));
        respostas.add(new RespostaDTO(9, ((Spinner) findViewById(R.id.spinnerEstadoCivil)).getSelectedItem().toString()));
        respostas.add(new RespostaDTO(10, ((EditText) findViewById(R.id.editDiagMedico)).getText().toString()));
        respostas.add(new RespostaDTO(11, ((EditText) findViewById(R.id.editMotivInter)).getText().toString()));
        respostas.add(new RespostaDTO(12, ((EditText) findViewById(R.id.editDoencaCronica)).getText().toString()));
        respostas.add(new RespostaDTO(99, ((Spinner) findViewById(R.id.spinnerSexo)).getSelectedItem().toString()));

        return respostas;
    }

    private boolean verificarCampos() {
        String nome = ((EditText) findViewById(R.id.editNome)).getText().toString();
        String cpf = ((EditText) findViewById(R.id.editCpf)).getText().toString();
        String idade = ((EditText) findViewById(R.id.editIdade)).getText().toString();
        String rg = ((EditText) findViewById(R.id.editRG)).getText().toString();
        String cartaoSus = ((EditText) findViewById(R.id.editCartaoSus)).getText().toString();
        String leito = ((EditText) findViewById(R.id.editLeito)).getText().toString();
        String profissao = ((EditText) findViewById(R.id.editProfissao)).getText().toString();
        String escolaridade = ((EditText) findViewById(R.id.editEscolaridade)).getText().toString();
        String diagMedico = ((EditText) findViewById(R.id.editDiagMedico)).getText().toString();
        String motivInter = ((EditText) findViewById(R.id.editMotivInter)).getText().toString();
        String doencaCronica = ((EditText) findViewById(R.id.editDoencaCronica)).getText().toString();

        if (nome.isEmpty() || cpf.isEmpty() || idade.isEmpty() || rg.isEmpty() || cartaoSus.isEmpty() || leito.isEmpty() ||
                profissao.isEmpty() || escolaridade.isEmpty() || diagMedico.isEmpty() || motivInter.isEmpty() || doencaCronica.isEmpty()) {
            return false;
        }

        Spinner spinnerSexo = findViewById(R.id.spinnerSexo);
        if (spinnerSexo.getSelectedItemPosition() == 0) {
            return false;
        }

        Spinner spinnerEstadoCivil = findViewById(R.id.spinnerEstadoCivil);
        if (spinnerEstadoCivil.getSelectedItemPosition() == 0) {
            return false;
        }

        return true;
    }

    private void showCancelDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_anamnese, null);

        Button buttonSim = dialogView.findViewById(R.id.buttonSimCancel);
        Button buttonNao = dialogView.findViewById(R.id.buttonNaoCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaEditarAnamnese.this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        buttonSim.setOnClickListener(v -> {
            invalidateAnamneseAndFinish();
            dialog.dismiss();
        });

        buttonNao.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void invalidateAnamneseAndFinish() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        if (anamneseId == -1) {
            Toast.makeText(TelaEditarAnamnese.this, "Erro: ID da anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StatusAnamneseRequest statusAnamneseRequest = new StatusAnamneseRequest(anamneseId, "invalid", "Cancelada");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();


        ApiService service = retrofit.create(ApiService.class);
        Call<AnamneseStatusResponse> call = service.atualizarAnamnese("Bearer " + token, statusAnamneseRequest);
        call.enqueue(new Callback<AnamneseStatusResponse>() {
            @Override
            public void onResponse(Call<AnamneseStatusResponse> call, Response<AnamneseStatusResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TelaEditarAnamnese.this, "Anamnese invalidada com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TelaEditarAnamnese.this, "Falha ao invalidar anamnese.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<AnamneseStatusResponse> call, Throwable t) {
                Toast.makeText(TelaEditarAnamnese.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public static String formatarCPF(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("HTTP", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

}
