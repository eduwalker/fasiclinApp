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
import com.example.fasipemobilej.model.response.AnamneseAnswerResponse;
import com.example.fasipemobilej.model.response.AnamneseStatusResponse;
import com.example.fasipemobilej.model.request.RespostaDTO;
import com.example.fasipemobilej.model.request.StatusAnamneseRequest;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaQuestions extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        String nome = intent.getStringExtra("EXTRA_NOME");
        String cpf = intent.getStringExtra("EXTRA_CPF");
        Long idAnamnese = intent.getLongExtra("EXTRA_ANAMNESE_ID", -1);
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
    }


    public static String formatarCPF(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupCancelButton();
            }
        });
    }



    private void setupCancelButton() {
        Button cancelButton = findViewById(R.id.buttonCancelar);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });
    }

    private void setupButtonConfirmar() {
        Button buttonConfirmar = findViewById(R.id.buttonConfirmar);
        buttonConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verificarCampos()) {
                    Toast.makeText(TelaQuestions.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                } else {
                    confirmarAnamnese();
                }
            }
        });
    }

    private void confirmarAnamnese() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");
        long anamneseId = sharedPreferences.getLong("ANAMNESE_ID", -1);

        if (anamneseId == -1) {
            Toast.makeText(TelaQuestions.this, "Erro: ID da anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<RespostaDTO> respostas = coletarDadosDeRespostas();
        if (respostas.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        AnamneseAnswerRequest request = new AnamneseAnswerRequest(anamneseId, respostas);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<AnamneseAnswerResponse> call = service.enviarAnamneseRespostas("Bearer " + token, request);

        call.enqueue(new Callback<AnamneseAnswerResponse>() {
            @Override
            public void onResponse(Call<AnamneseAnswerResponse> call, Response<AnamneseAnswerResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TelaQuestions.this, "Anamnese enviada com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d("API Response", "Anamnese enviada com sucesso.");
                    Intent intent = new Intent(TelaQuestions.this, TelaPrincipal.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    finish();
                    startActivity(intent);

                } else {
                    try {
                        Log.e("API Error", "Erro ao enviar anamnese: " + response.code() + " - " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("API Error", "Erro ao extrair a mensagem de erro", e);
                    }
                    Toast.makeText(TelaQuestions.this, "Erro ao enviar anamnese!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseAnswerResponse> call, Throwable t) {
                Log.e("Retrofit", "Erro na comunicação: " + t.getMessage());
                Toast.makeText(TelaQuestions.this, "Erro no envio da anamnese", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(TelaQuestions.this, TelaPrincipal.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                finish();
//                startActivity(intent);
            }
        });
    }




    private List<RespostaDTO> coletarDadosDeRespostas() {
        List<RespostaDTO> respostas = new ArrayList<>();

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
        String sexo = ((Spinner) findViewById(R.id.spinnerSexo)).getSelectedItem().toString();
        String estadoCivil = ((Spinner) findViewById(R.id.spinnerEstadoCivil)).getSelectedItem().toString();


        respostas.add(new RespostaDTO(1, nome));
        respostas.add(new RespostaDTO(2, idade));
        respostas.add(new RespostaDTO(3, rg));
        respostas.add(new RespostaDTO(4, cpf));
        respostas.add(new RespostaDTO(5, cartaoSus));
        respostas.add(new RespostaDTO(6, leito));
        respostas.add(new RespostaDTO(7, profissao));
        respostas.add(new RespostaDTO(8, escolaridade));
        respostas.add(new RespostaDTO(9, estadoCivil));
        respostas.add(new RespostaDTO(10, diagMedico));
        respostas.add(new RespostaDTO(11, motivInter));
        respostas.add(new RespostaDTO(12, doencaCronica));
        respostas.add(new RespostaDTO(99, sexo));


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

                // Verificar se os campos estão vazios
                if (nome.isEmpty() || cpf.isEmpty() || idade.isEmpty() || rg.isEmpty() || cartaoSus.isEmpty() || leito.isEmpty() ||
                        profissao.isEmpty() || escolaridade.isEmpty() || diagMedico.isEmpty() || motivInter.isEmpty() || doencaCronica.isEmpty()) {
                    return false; // Algum campo está vazio
                }

                // Verificações adicionais para os Spinners
                Spinner spinnerSexo = findViewById(R.id.spinnerSexo);
                if (spinnerSexo.getSelectedItemPosition() == 0) {
                    return false; // Nenhuma seleção feita no spinner de sexo
                }

                Spinner spinnerEstadoCivil = findViewById(R.id.spinnerEstadoCivil);
                if (spinnerEstadoCivil.getSelectedItemPosition() == 0) {
                    return false;
                }

                return true;
            }


    private void invalidateAnamneseAndFinish() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");
        Long anamneseId = sharedPreferences.getLong("ANAMNESE_ID", -1);

        if (anamneseId == -1) {
            Toast.makeText(TelaQuestions.this, "Erro: ID da anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StatusAnamneseRequest statusAnamneseRequest = new StatusAnamneseRequest(anamneseId, "invalid", "Cancelada");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<AnamneseStatusResponse> call = service.atualizarAnamnese("Bearer " + token, statusAnamneseRequest);
        call.enqueue(new Callback<AnamneseStatusResponse>() {
            @Override
            public void onResponse(Call<AnamneseStatusResponse> call, Response<AnamneseStatusResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TelaQuestions.this, "Anamnese invalidada com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TelaQuestions.this, "Falha ao invalidar anamnese.", Toast.LENGTH_SHORT).show();
                }
                finish(); // Finaliza a atividade independentemente do resultado
            }

            @Override
            public void onFailure(@NonNull Call<AnamneseStatusResponse> call, Throwable t) {
                Toast.makeText(TelaQuestions.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish(); // Finaliza a atividade em caso de falha na rede
            }
        });
    }

    private void showCancelDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_anamnese, null);

        Button buttonSim = dialogView.findViewById(R.id.buttonSimCancel);
        Button buttonNao = dialogView.findViewById(R.id.buttonNaoCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaQuestions.this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create(); // Cria o AlertDialog a partir do builder

        buttonSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Função para invalidar a anamnese e finalizar a atividade
                invalidateAnamneseAndFinish();
                dialog.dismiss();
                finish();
            }
        });

        buttonNao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Simplesmente fecha o diálogo
            }
        });

        dialog.show(); // Exibe o diálogo na tela
    }




    private void atualizarStatusAnamnese(Long idAnamnese, String status, String statusfn){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");
        Long anamneseId = sharedPreferences.getLong("ANAMNESE_ID", -1);

        if (anamneseId == -1) {
            Toast.makeText(TelaQuestions.this, "Erro: ID da anamnese não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StatusAnamneseRequest request = new StatusAnamneseRequest(idAnamnese, status, statusfn);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        ApiService service = retrofit.create(ApiService.class);


        service.atualizarAnamnese("Bearer " + token, request).enqueue(new Callback<AnamneseStatusResponse>() {
            @Override
            public void onResponse(Call<AnamneseStatusResponse> call, Response<AnamneseStatusResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Status atualizado com sucesso!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao atualizar status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnamneseStatusResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Erro na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }



}