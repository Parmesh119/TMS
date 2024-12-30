package com.Tms.TMS.model

import java.util.*

data class Employee (
    val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val contactNumber: String?,
    val role: String
)