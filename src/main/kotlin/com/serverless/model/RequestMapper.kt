package com.serverless.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RequestMapper (
        @JsonProperty val content: String,
        @JsonProperty val file: String
)