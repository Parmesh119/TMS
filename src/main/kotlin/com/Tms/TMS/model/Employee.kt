package com.Tms.TMS.model

import org.springframework.data.annotation.Id
import java.util.*

data class Employee(
    @Id val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val contactNumber: String? = null, // Mark as nullable
    val role: String,
    val status: String? = "active"
)

data class EmployeeListRequest(
    val search: String,
    val roles: List<String> = emptyList(),
    val statuses: List<String> = emptyList(),
    val page: Int = 1,
    val size: Int = 10,
)
