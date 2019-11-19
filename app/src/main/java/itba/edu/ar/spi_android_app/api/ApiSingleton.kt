package ar.edu.itba.spi_android_app.api

import android.content.Context
import android.util.Base64
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.utils.SingletonHolder
import io.jsonwebtoken.Jwts
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec


const val API_BASE_URL = "https://pf-itba-spi.herokuapp.com"

/**
 * API singleton which is lazily initialized with a [Context] when necessary.
 */
class ApiSingleton private constructor(context: Context) {
    private val jwt: String

    init {
        val jwtString = context.getString(R.string.api_private_key)
                .replace("\n", "")                              // Trim newlines
                .replace("-----BEGIN RSA PRIVATE KEY-----", "") // Trim beginning comment
                .replace("-----END RSA PRIVATE KEY-----", "")   // Trim end comment
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.decode( jwtString, Base64.DEFAULT))
        val privateKey = KeyFactory.getInstance("RSA", "BC").generatePrivate(keySpecPKCS8)

        jwt = Jwts.builder()
                .signWith(privateKey)
                .setPayload(context.getString(R.string.app_name))
                .compact()
    }

    /**
     * Lazily-initialized singleton of default HTTP client. **Use this instance whenever possible!**
     */
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(JwtInterceptor()).build()
    }

    /**
     * Request interceptor that adds an Authorization HTTP header with the app's JWT
     */
    private inner class JwtInterceptor : okhttp3.Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val authorizedRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer $jwt").build()
            return chain.proceed(authorizedRequest)
        }
    }

    /**
     * Default Retrofit builder. Uses [API_BASE_URL], [RxJava2CallAdapterFactory] to work with observables,
     * and [ScalarsConverterFactory] to work with scalars (plain strings, etc.) and [GsonConverterFactory]
     * to work with JSON.  Also uses [the default HTTP client][httpClient].
     */
    private val defaultRetrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // Adapt calls to RXJava (observables, etc.)
                .addConverterFactory(ScalarsConverterFactory.create())      // Work with plain strings, ints, etc.
                .addConverterFactory(GsonConverterFactory.create())        // Work with JSON
                .client(httpClient)
    }

    /**
     * Default Retrofit instance.  Uses defaults defined by [defaultRetrofitBuilder].
     *
     * @see defaultRetrofitBuilder
     */
    val defaultRetrofitInstance: Retrofit by lazy {
        defaultRetrofitBuilder.build()
    }

    // Actual object that holds the singleton instance of this class
    companion object : SingletonHolder<ApiSingleton, Context>(::ApiSingleton)
}
