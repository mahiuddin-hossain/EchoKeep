package com.suitexen.ecokeep.network;

import com.suitexen.ecokeep.models.GroqChatRequest;
import com.suitexen.ecokeep.models.GroqChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GroqApiService {
    @POST("v1/chat/completions")
    Call<GroqChatResponse> processReceipt(
            @Header("Authorization") String authHeader,
            @Body GroqChatRequest request
    );
}