package com.fafadiatech.newscout

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import java.util.*

class UrlEncodeInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val body = original.body()
        var requestBody: RequestBody? = null

        if (body != null) {
            val postBody = bodyToString(original.body()!!)
            val newPostBody = URLDecoder.decode(postBody, "UTF-8")

            requestBody = RequestBody.create(original.body()!!.contentType(), newPostBody);
        }

        var request: Request
        if (original.method().equals("post")) {
            request = original.newBuilder()
                    .method(original.method(), original.body())
                    .post(requestBody)
                    .build();
        } else if (original.method().equals("put")) {
            request = original.newBuilder()
                    .method(original.method(), original.body())
                    .put(requestBody)
                    .build();
        } else {
            var stringUrl = original.url().toString()
            stringUrl = stringUrl.replace("%26", "&").replace("%3D", "=").replace("%20", " ")
            val time = Date()
            request = original.newBuilder().url(stringUrl)
                    .build()
        }
        return chain.proceed(request)
    }

    fun bodyToString(request: RequestBody): String {
        try {
            val copy: RequestBody = request
            val buffer: okio.Buffer = okio.Buffer()
            if (copy != null)
                copy.writeTo(buffer);
            else {
                val stringType: String = ""
                return stringType
            }
            return buffer.readUtf8()
        } catch (e: IOException) {
            val failError = "Error Occured"
            return failError
        }
    }
}