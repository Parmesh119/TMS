package com.Tms.TMS.model

import org.springframework.data.annotation.Id
import java.util.UUID

data class Party (
    @Id val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val pointOfContact: String?,
    val contactNumber: String?,
    val email: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val state: String?,
    val district: String?,
    val taluka: String?,
    val city: String?,
    val pincode: Int?,
    val status: String
)

data class ListPartiesInput(
    val search: String = "",
    val page: Int = 1,
    val size: Int = 10,
    val getAll: Boolean = false,
    val statuses: List<String> = emptyList()
)