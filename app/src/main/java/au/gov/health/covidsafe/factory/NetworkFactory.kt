package au.gov.health.covidsafe.factory

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.networking.service.AwsClient

interface NetworkFactory {
    companion object {
        private val logging = HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)

        val awsClient: AwsClient by lazy {
            RetrofitServiceGenerator.createService(AwsClient::class.java)
        }

        val okHttpClient: OkHttpClient by lazy {
            val builder = OkHttpClient.Builder()
            if (!builder.interceptors().contains(logging) && BuildConfig.DEBUG) {
                builder.addInterceptor(logging)
            }
            builder.build()
        }
    }
}

object RetrofitServiceGenerator {
    private val builder = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

    private var retrofit = builder.build()

    fun <S> createService(
            serviceClass: Class<S>): S {
        builder.client(NetworkFactory.okHttpClient)
        retrofit = builder.build()

        return retrofit.create(serviceClass)
    }
}