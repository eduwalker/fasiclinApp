package com.example.fasipemobilej;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.example.fasipemobilej.databinding.ActivityMainBinding;
import com.example.fasipemobilej.model.request.LoginRequest;
import com.example.fasipemobilej.model.response.LoginResponse;
import com.example.fasipemobilej.network.ApiEnvironment;
import com.example.fasipemobilej.network.ApiService;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        SharedPreferences prefs = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String savedUsername = prefs.getString("USERNAME", "");
        String savedPassword = prefs.getString("PASSWORD", "");

        if (!savedUsername.equals("") && !savedPassword.equals("")) {
            binding.editUser.setText(savedUsername);
            binding.editPassword.setText(savedPassword);
            binding.checkboxRememberPassword.setChecked(true); // Apenas se você quiser que o checkbox esteja marcado automaticamente
        }

        binding.btEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });
    }

    private void login(final View view) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btEntrar.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEnvironment.DEVELOPMENT.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        LoginRequest loginRequest = new LoginRequest(
                binding.editUser.getText().toString(),
                binding.editPassword.getText().toString()
        );

        service.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btEntrar.setEnabled(true);
                if (response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String token = response.body() != null ? response.body().token() : "";
                            getSharedPreferences("MySharedPref", MODE_PRIVATE).edit()
                                    .putString("TOKEN", token)
                                    .apply();
                            navigateToMainScreen();
                        }
                    }, 1000);
                    Snackbar.make(view, "Login efetuado com sucesso!", Snackbar.LENGTH_SHORT).show();
                    if (binding.checkboxRememberPassword.isChecked()) {
                        getSharedPreferences("MySharedPref", MODE_PRIVATE).edit()
                                .putString("USERNAME", binding.editUser.getText().toString())
                                .putString("PASSWORD", binding.editPassword.getText().toString())
                                .apply();
                    }
                } else {
                    Snackbar.make(view, "Código de Aluno ou Senha Inválidos!", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btEntrar.setEnabled(true);
                Snackbar.make(view, "Erro ao conectar com o servidor: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                Log.e("LoginError", "Erro ao conectar com o servidor", t);
            }
        });
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, TelaPrincipal.class);
        startActivity(intent);
        finish();
    }
}