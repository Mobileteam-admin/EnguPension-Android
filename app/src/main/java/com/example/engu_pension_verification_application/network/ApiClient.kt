package com.example.engu_pension_verification_application.network



import com.example.engu_pension_verification_application.util.SharedPref
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    var prefs = SharedPref

    var BASE_URL: String = "https://pension-distributor.demoserver.work"
    @Volatile private var apiInterface:ApiInterface? =null

    fun getApiInterface(): ApiInterface {
        if (apiInterface == null) {
            synchronized(this) {
                if (apiInterface == null) {
                    apiInterface = createRetrofit()
                }
            }
        }
        return apiInterface!!
    }
    private fun createRetrofit(): ApiInterface {
        var interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        var client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addNetworkInterceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .build()
                chain.proceed(request)
            }.build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiInterface::class.java)
    }
}