package com.mediassist.app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryService {

    @Multipart
    @POST("v1_1/dudjacpdy/image/upload")  // ✅ MUST BE image/upload
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody  // ✅ MUST BE upload_preset
    ): Response<Map<String, Any>>
}