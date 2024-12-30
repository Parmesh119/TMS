package com.Tms.TMS.model

import java.util.UUID

data class Location (
    val id: String? = UUID.randomUUID().toString(),
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
)