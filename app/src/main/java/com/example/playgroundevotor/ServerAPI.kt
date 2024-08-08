package com.example.playgroundevotor

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface ServerAPI {
    @PATCH
    suspend fun changeQueueState(@Url url: String, @Body body: ChangeQueueStateRequest): Response<Void>

    @PATCH
    suspend fun changeQueueStateWithContent(@Url url: String, @Body body: ChangeQueueStateRequest): Response<ResponseBody>

    @GET
    suspend fun greet(@Url url: String): Response<GreetResponse>

    companion object {

        private val BASE_URL = "http://example.com"

        val GSON = GsonBuilder().create()

        val CLIENT = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
//            .addInterceptor(ChuckerInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(CLIENT)
            .addConverterFactory(GsonConverterFactory.create(GSON))
            .build()

        val API = retrofit.create(ServerAPI::class.java)
    }
}


class ChangeQueueStateRequest(
    var queue: RemoteQueue
) {
    class RemoteQueue(
        var state: QueueState
    )
}

enum class QueueState {
    pending;
}

data class GreetResponse(
    val message: String
)