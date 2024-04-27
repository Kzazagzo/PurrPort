package pl.put.swWrotaDlaKota.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import pl.put.swWrotaDlaKota.network.KotApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ip {
    companion object {
        const val address = "https://glowing-cowbird-easy.ngrok-free.app/"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl(ip.address + '/')
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideKotApi(retrofit: Retrofit): KotApi =
        retrofit.create(KotApi::class.java)

}