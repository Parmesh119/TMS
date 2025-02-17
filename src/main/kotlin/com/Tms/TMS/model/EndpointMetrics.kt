package com.Tms.TMS.model

data class EndpointMetrics(
    val totalCalls: Double,
    val successCalls: Double,
    val failedCalls: Double,
    val activeRequests: Double
)