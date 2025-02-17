package com.Tms.TMS.model

data class MetricsResponse(
    val data: String,
    val metrics: Map<String, EndpointMetrics>,
    val latencyMs: Double
)