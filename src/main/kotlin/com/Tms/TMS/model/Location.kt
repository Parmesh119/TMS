package com.Tms.TMS.model

import org.springframework.data.annotation.Id
import java.util.UUID

data class Location (
    @Id val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val pointOfContact: String?,
    val contactNumber: String?,
    val email: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val state: String?,
    val district: String,
    val taluka: String,
    val city: String?,
    val pincode: String?,
    val status: String
)

data class LocationListRequest(
    val search: String,
    val districts: List<String> = emptyList(),
    val talukas: List<String> = emptyList(),
    val statuses: List<String> = emptyList(),
    val getAll: Boolean,
    val page: Int,
    val size: Int
)