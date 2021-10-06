package com.michaelkoohang.netwatch.model

object Constants {
    private val env = "prod"
    val apiUrl = if (env == "prod") "https://netwatch.rnoc.gatech.edu" else "https://82d7b9406956.ngrok.io"
}