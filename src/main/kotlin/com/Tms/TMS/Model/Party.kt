package com.Tms.TMS.Model

import java.util.UUID

data class Party (
    val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val point_of_contact: String,
    val phone: Int,
    val email: String,
    val address1: String,
    val address2: String?,
    val state: String,
    val district: String,
    val taluka: String,
    val city: String,
    val zipcode: Int,
)