package com.streamvault.app.di

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit exige una baseUrl fija al construirse, pero el servidor Xtream
 * del usuario es dinámico (se configura/cambia en runtime desde Ajustes).
 *
 * Este interceptor reescribe el scheme/host/puerto de cada request saliente
 * para que apunte al servidor activo, manteniendo el path (player_api.php)
 * y los query params (username, password, action, etc.) intactos.
 *
 * XtreamRepository actualiza `baseUrl` cada vez que se activa un servidor.
 */
@Singleton
class DynamicUrlInterceptor @Inject constructor() : Interceptor {

    @Volatile
    var baseUrl: String = "http://placeholder.com"

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val target = baseUrl.toHttpUrlOrNull()
            ?: return chain.proceed(original)

        val newUrl = original.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
