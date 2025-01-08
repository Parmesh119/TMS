package com.Tms.TMS.model

import java.util.*

data class Transpotation (
    val id: String? = UUID.randomUUID().toString(),
    val companyName: String,
    val pointOfContact: String?,
    val contactNumber: String?,
    val email: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val state: String?,
    val city: String?,
    val pinCode: String?,
    val status: String = "active",
    val vehicles: List<Vehicles> = emptyList(),
    val drivers: List<Driver> = emptyList(),
    val created_at: Long?,
    val updated_at: Long?
)


data class Vehicles (
    val id: String? = UUID.randomUUID().toString(),
    val vehicleNumber: String,
    val type: String?,
    val rcBookUrl: String?
)

data class Driver (
    val id: String? = UUID.randomUUID().toString(),
    val name: String,
    val contactNumber: String?,
    val drivingLicenseUrl: String?
)

data class ListTransportationCompaniesInput(
    val search: String = "",
    val statuses: List<String> = emptyList(),
    val page: Int = 1,
    val size: Int = 10,
    val getAll: Boolean = false,
)