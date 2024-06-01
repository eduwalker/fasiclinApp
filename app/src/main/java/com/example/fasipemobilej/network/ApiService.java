package com.example.fasipemobilej.network;

import com.example.fasipemobilej.AnamneseDetailActivity;
import com.example.fasipemobilej.model.request.AnamneseAnswerRequest;
import com.example.fasipemobilej.model.request.AnamneseObsRequest;
import com.example.fasipemobilej.model.request.AnamneseViewRequest;
import com.example.fasipemobilej.model.response.AnamneseAnswerResponse;
import com.example.fasipemobilej.model.request.AnamneseRequest;
import com.example.fasipemobilej.model.response.AnamneseDetailResponse;
import com.example.fasipemobilej.model.response.AnamneseListResponse;
import com.example.fasipemobilej.model.response.AnamneseResponse;
import com.example.fasipemobilej.model.response.AnamneseResponseID;
import com.example.fasipemobilej.model.response.AnamneseStatusResponse;
import com.example.fasipemobilej.model.request.LoginRequest;
import com.example.fasipemobilej.model.response.LoginResponse;
import com.example.fasipemobilej.model.request.PacienteRequest;
import com.example.fasipemobilej.model.response.PacienteResponse;
import com.example.fasipemobilej.model.request.StatusAnamneseRequest;
import com.example.fasipemobilej.model.response.StringResponse;
import com.example.fasipemobilej.model.response.UserResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("user/userinfo")
    Call<UserResponse> getUserInfo(@Header("Authorization") String authToken);

    @POST("fasiclin/paciente/info")
    Call<PacienteResponse> buscarPacientePorCpf(@Header("Authorization") String token, @Body PacienteRequest cpfRequest);
    @POST("fasiclin/anamnese")
    Call<AnamneseResponseID> criarAnamnesePorCpf(@Header("Authorization") String token, @Body AnamneseRequest anamneseRequest);

    @POST("fasiclin/anamnese-status")
    Call<AnamneseStatusResponse> atualizarAnamnese(@Header("Authorization") String token, @Body StatusAnamneseRequest anamneseStatus);

    @POST("fasiclin/anamnese/respostas")
    Call<AnamneseAnswerResponse> enviarAnamneseRespostas(@Header("Authorization") String token, @Body AnamneseAnswerRequest anamneseAnswerRequest);

    @GET("fasiclin/anamneses")
    Call<List<AnamneseResponse>> listAnamneses(@Header("Authorization") String token);

    @GET("fasiclin/anamneses/supervisor")
    Call<List<AnamneseResponse>> listAnamnesesBySupervisor(@Header("Authorization") String token);

    @GET("fasiclin/anamnese/{id}")
    Call<AnamneseDetailResponse> getAnamneseById(@Header("Authorization") String token, @Path("id") Long idAnamnese);

    @POST("fasiclin/anamnese/update-observations")
    Call<Void> updateAnamneseObservations(@Header("Authorization") String token, @Body AnamneseObsRequest request);


    @PUT("fasiclin/anamnese/update-respostas")
    Call<ResponseBody> updateRespostas(@Header("Authorization") String token, @Body AnamneseAnswerRequest request);




}



