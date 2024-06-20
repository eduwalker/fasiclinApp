package com.example.fasipemobilej;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fasipemobilej.databinding.ActivityTelaPrincipalBinding;
import com.example.fasipemobilej.model.request.AnamneseRequest;
import com.example.fasipemobilej.model.response.AnamneseResponseID;
import com.example.fasipemobilej.model.request.PacienteRequest;
import com.example.fasipemobilej.model.response.PacienteResponse;
import com.example.fasipemobilej.model.response.UserResponse;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.example.fasipemobilej.network.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
        listaAnamnese();
        btlogout();

        binding.searchAnamnese.setOnClickListener(v -> binding.searchAnamnese.clearFocus());

        // Configure o ViewPager2
        List<String> titles = new ArrayList<>();
        titles.add("Total");
        titles.add("Aprovadas");
        titles.add("Reprovadas");
        titles.add("Canceladas");

        List<String> contents = new ArrayList<>();
        contents.add("Detalhes de Total");
        contents.add("Detalhes de Aprovadas");
        contents.add("Detalhes de Reprovadas");
        contents.add("Detalhes de Canceladas");

        CarouselAdapter carouselAdapter = new CarouselAdapter(titles, contents);
        binding.viewPager.setAdapter(carouselAdapter);
    }

    private void btlogout() {
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Limpa SharedPreferences
            SharedPreferences preferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();  // Remove dados armazenados
            editor.apply();  //Aplica todas mudanças de estado

            // Inicia a tela de login
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();  // Finaliza a atividade atual
        });
    }

    private void fetchUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("TOKEN", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        Call<UserResponse> call = service.getUserInfo("Bearer " + token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userInfo = response.body();

                    // Atualiza tela com nome do Profissional
                    runOnUiThread(() -> {
                        TextView tvUserName = binding.tvUserName;
                        tvUserName.setText(userInfo.nome());
                    });

                    // Salva nome e tipo do Profissional em SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("NOME_USUARIO", userInfo.nome());
                    editor.putInt("TIPO_USUARIO", userInfo.tipo());
                    editor.apply();

                    // Controla a visibilidade dos botões
                    runOnUiThread(() -> controlButtonVisibility(userInfo.tipo()));
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
                searchCpf(cpf, dialog);
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
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
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

        textViewCpf.setText(formatarCPF(pacienteInfo.cpf_pac()));
        textViewNome.setText(pacienteInfo.nome_pac());

        // Cria o construtor do diálogo sem os botões padrão
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaPrincipal.this);
        builder.setView(dialogView);

        // Adiciona ação aos botões personalizados do seu layout
        Button buttonSim = dialogView.findViewById(R.id.buttonSim);
        Button buttonNao = dialogView.findViewById(R.id.buttonNao);

        final AlertDialog resultDialog = builder.create();

        buttonSim.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String token = sharedPreferences.getString("TOKEN", "");


            String status = "valid";
            String statusfn = "Analise";

            AnamneseRequest anamneseRequest = new AnamneseRequest(pacienteInfo.cpf_pac(), status, statusfn);
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            Gson gson = new GsonBuilder()
                    .create();

            // Configura o Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            ApiService service = retrofit.create(ApiService.class);

            service.criarAnamnesePorCpf("Bearer " + token, anamneseRequest).enqueue(new Callback<AnamneseResponseID>() {
                @Override
                public void onResponse(Call<AnamneseResponseID> call, Response<AnamneseResponseID> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AnamneseResponseID anamneseResponseID = response.body();

                        // Salva o ID da Anamnese criada
                        Long idAnamnese = anamneseResponseID.idAnamnese();
                        getSharedPreferences("MySharedPref", MODE_PRIVATE).edit()
                                .putLong("ANAMNESE_ID", idAnamnese)
                                .apply();

                        // Navega para a próxima tela com o ID da anamnese
                        Intent intent = new Intent(TelaPrincipal.this, TelaQuestions.class);
                        intent.putExtra("EXTRA_ANAMNESE_ID", anamneseResponseID.idAnamnese());
                        intent.putExtra("EXTRA_NOME", pacienteInfo.nome_pac());
                        intent.putExtra("EXTRA_CPF", pacienteInfo.cpf_pac());
                        intent.putExtra("EXTRA_DATA_NASCIMENTO", pacienteInfo.data_nasc_pac());
                        startActivity(intent);
                    } else {
                        // Tratar os casos de erro
                        Log.e("AnamneseCreation", "Erro ao criar anamnese: " + response.errorBody());
                    }
                    resultDialog.dismiss();
                }

                @Override
                public void onFailure(Call<AnamneseResponseID> call, Throwable t) {
                    Log.e("AnamneseCreation", "Falha ao criar anamnese", t);
                    resultDialog.dismiss();
                }
            });
        });

        buttonNao.setOnClickListener(v -> resultDialog.dismiss());

        // Cria e mostra o diálogo
        resultDialog.show();
    }

    public static String formatarCPF(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public void listaAnamnese() {
        binding.btAnamanesesList.setOnClickListener(view -> {
            Intent intent = new Intent(TelaPrincipal.this, TelaListAnamnese.class);
            startActivity(intent);
        });
    }

    private void controlButtonVisibility(int userType) {
        Button specialButton = findViewById(R.id.specialButton);
        Button btProntAnamnese = findViewById(R.id.btProntAnamnese);

        if (userType == 3) { // Tipo 3 indica que o usuário é supervisor
            specialButton.setVisibility(View.VISIBLE);
            btProntAnamnese.setVisibility(View.VISIBLE);

            // Adiciona o OnClickListener para abrir a tela de lista de anamneses de supervisor
            specialButton.setOnClickListener(view -> {
                Intent intent = new Intent(TelaPrincipal.this, AnamneseSupervisorActivity.class);
                startActivity(intent);
            });

        } else {
            specialButton.setVisibility(View.GONE);
            btProntAnamnese.setVisibility(View.GONE);
        }
    }
}
