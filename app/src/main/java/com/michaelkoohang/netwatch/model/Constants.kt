package com.michaelkoohang.netwatch.model

object Constants {
    private val env = "prod"
    val apiUrl = if (env == "prod") "https://hikernet.rnoc.gatech.edu" else "https://a8db-2601-1c0-6100-3570-312a-d2a3-9f9c-cf89.ngrok.io"
}