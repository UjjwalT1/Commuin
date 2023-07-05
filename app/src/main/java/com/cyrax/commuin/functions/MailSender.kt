package com.cyrax.commuin.functions

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface Send{
    suspend fun sendMail(recEmail:String ,subject:String,msg : String)
}
class Ssend():Send{
    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun sendMail(recEmail: String, subject: String, msg: String) {

        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, "{\r\n    \"personalizations\": [\r\n        {\r\n            \"to\": [\r\n                {\r\n                    \"email\": \"$recEmail\"\r\n                }\r\n            ],\r\n            \"subject\": \"$subject\"\r\n        }\r\n    ],\r\n    \"from\": {\r\n        \"email\": \"commuin@cyrax.com\"\r\n    },\r\n    \"content\": [\r\n        {\r\n            \"type\": \"text/plain\",\r\n            \"value\": \"$msg\"\r\n        }\r\n    ]\r\n}")
        val request = Request.Builder()
            .url("https://rapidprod-sendgrid-v1.p.rapidapi.com/mail/send")
            .post(body)
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", "ba62643559mshd76de80b1e2bacfp1c6943jsn3d9cc846503e")
            .addHeader("X-RapidAPI-Host", "rapidprod-sendgrid-v1.p.rapidapi.com")
            .build()

        GlobalScope.launch (Dispatchers.IO + coroutineExceptionHandler){
            client.newCall(request).execute()
        }

    }

}
val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
    throwable.printStackTrace()
}







