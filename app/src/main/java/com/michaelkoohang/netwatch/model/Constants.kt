package com.michaelkoohang.netwatch.model

object Constants {
    private val env = "prod"
    val apiUrl = if (env == "prod") "https://hikernet.rnoc.gatech.edu" else "https://0c25-2601-1c0-6100-3570-13f-830f-ed58-b881.ngrok.io"
}