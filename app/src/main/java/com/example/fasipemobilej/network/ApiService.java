package com.example.fasipemobilej.network;

import com.example.fasipemobilej.model.LoginRequest;
import com.example.fasipemobilej.model.LoginResponse;
import com.example.fasipemobilej.model.PacienteRequest;
import com.example.fasipemobilej.model.PacienteResponse;
import com.example.fasipemobilej.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("user/userinfo")
    Call<UserResponse> getUserInfo(@Header("Authorization") String authToken);

    @POST("fasiclin/paciente/info")
    Call<PacienteResponse> buscarPacientePorCpf(@Header("Authorization") String token, @Body PacienteRequest cpfRequest);

}
