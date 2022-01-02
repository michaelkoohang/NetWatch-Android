package com.michaelkoohang.netwatch.model

object Constants {
    private val env = "prod"
    val apiUrl = if (env == "prod") "https://netwatch.rnoc.gatech.edu" else "https://3e3d-2600-8807-a740-7f00-351a-1528-aa4e-adae.ngrok.io"
}