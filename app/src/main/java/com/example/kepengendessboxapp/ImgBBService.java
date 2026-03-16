// ImgBBService.java
package com.example.kepengendessboxapp;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImgBBService {
    @Multipart
    @POST("api/1/upload")
    Call<ResponseBody> uploadImage(
            @Part("key") RequestBody apiKey,
            @Part MultipartBody.Part image
    );
}