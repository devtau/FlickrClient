package com.devtau.flickrclient.rest.response

class TokenResponse(
    val callbackConfirmed: Boolean?,
    val tempToken: String?,
    val tempTokenSecret: String?
)