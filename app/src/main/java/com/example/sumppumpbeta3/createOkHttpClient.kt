package com.example.sumppumpbeta3

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class createOkHttpClient {

    val client = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}
