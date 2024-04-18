package com.example.fasipemobilej;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fasipemobilej.databinding.ActivityTelaPrincipalBinding;
import com.example.fasipemobilej.model.PacienteRequest;
import com.example.fasipemobilej.model.PacienteResponse;
import com.example.fasipemobilej.model.UserResponse;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelaPrincipal extends AppCompatActivity {


    private ActivityTelaPrincipalBinding binding;
    private static final String TAG = "MinhaAplicacao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTelaPrincipalBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        fetchUserInfo();

        binding.btCriarAnamnese.setOnClickListener(view -> showSearchCpfDialog());


//        binding.btCriarAnamnese.setOnClickListener(view -> {
//            Intent intent = new Intent(TelaPrincipal.this, TelaQuestions.class);
//            startActivity(intent);
//        });



    }



    private void fetchUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://192.168.100.113:8443/")
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        // Adicione um header de Autorização com o token JWT
        Call<UserResponse> call = service.getUserInfo("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userInfo = response.body();

                    // Atualize a UI com o nome do usuário
                    runOnUiThread(() -> {
                        TextView tvUserName = binding.tvUserName;
                        tvUserName.setText(userInfo.nome());
                    });

                    // Salve o nome do usuário nas SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("NOME_USUARIO", userInfo.nome());
                    editor.apply();
                } else {
                    // Tratar erro, por exemplo, mostrando uma mensagem ao usuário
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Tratar falha na chamada, por exemplo, mostrando uma mensagem ao usuário
            }
        });
    }

    private void showSearchCpfDialog() {
        // Infla o layout do pop-up
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.popup_cpf_search, null);  // Certifique-se de que 'popup_cpf_search' é o nome correto do layout do diálogo

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)  // Permite que o usuário feche o diálogo tocando fora dele
                .create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_popup_search_cpf);

        // Inicializa os componentes do diálogo
        EditText editTextCpf = view.findViewById(R.id.editTextCpf);
        Button buttonSearch = view.findViewById(R.id.buttonSearch);

        // Configura o listener para o botão de pesquisa
        buttonSearch.setOnClickListener(v -> {
            String cpf = editTextCpf.getText().toString();
            if (!cpf.isEmpty()) {
                searchCpf(cpf, dialog); // Modificado para passar 'dialog' como argumento
            } else {
                Toast.makeText(this, "Por favor, insira um CPF válido.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(); // Mostra o diálogo
    }



    private void searchCpf(String cpf, AlertDialog dialog) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        // Configuração do Gson e Retrofit continua igual...
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://192.168.100.113:8443/")
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService service = retrofit.create(ApiService.class);
        PacienteRequest request = new PacienteRequest(cpf);

        Call<PacienteResponse> call = service.buscarPacientePorCpf("Bearer " + token, request);

        call.enqueue(new Callback<PacienteResponse>() {
            @Override
            public void onResponse(Call<PacienteResponse> call, Response<PacienteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PacienteResponse pacienteInfo = response.body();
                    runOnUiThread(() -> {
                        dialog.dismiss(); // Fecha o diálogo após a resposta bem-sucedida
                        showResultDialog(pacienteInfo); // Exibe o resultado em um novo diálogo
                    });
                } else {
                    String errorMessage = "Erro desconhecido";
                    if (response.code() == 404) {
                        errorMessage = "Paciente não encontrado.";
                    } else if (response.code() == 500) {
                        errorMessage = "Erro interno no servidor.";
                    }
                    final String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> {
                        Toast.makeText(TelaPrincipal.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                        dialog.dismiss(); // Fecha o diálogo também em caso de erro
                    });
                }
            }

            @Override
            public void onFailure(Call<PacienteResponse> call, Throwable t) {
                Log.e(TAG, "Falha na comunicação: ", t);
                runOnUiThread(() -> {
                    Toast.makeText(TelaPrincipal.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss(); // Fecha o diálogo em caso de falha na comunicação
                });
            }
        });
    }

    private void showResultDialog(PacienteResponse pacienteInfo) {
        // Infla o layout personalizado para o diálogo
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cpf_search_confirm, null);

        // Configura os campos do layout com as informações do paciente
        TextView textViewCpf = dialogView.findViewById(R.id.editTextCpf);
        TextView textViewNome = dialogView.findViewById(R.id.editTextNome2);

        // Supondo que o layout 'dialog_patient_info' tem os TextViews com IDs 'editTextCpf' e 'editTextNome2'
        textViewCpf.setText(pacienteInfo.cpf_pac());
        textViewNome.setText(pacienteInfo.nome_pac());
        // Cria o construtor do diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaPrincipal.this);
        builder.setView(dialogView);

        // Adiciona botões ao diálogo, se necessário
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Executar ação ao clicar no botão Confirmar
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        // Cria e mostra o diálogo
        AlertDialog resultDialog = builder.create();
        resultDialog.show();
    }


}
