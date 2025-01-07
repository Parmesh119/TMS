package com.Tms.TMS.model

data class Transpotation(
    val id: String?,
    val companyName: String,
    val pointOfContact: String?,
    val contactNumber: String?,
    val email: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val state: String?,
    val city: String?,
    val pinCode: String?,
    val status: String,
    val vehicles: List<Vehicles> = emptyList(),
    val drivers: List<Driver> = emptyList()
)


data class Vehicles(
    val id: String?,
    val vehicleNumber: String,
    val type: String?,
    val rcBookUrl: String?,
    val status: String
)

data class Driver(
    val id: String?,
    val name: String,
    val contactNumber: String?,
    val drivingLicenseUrl: String?,
    val status: String
)

data class ListTransportationCompaniesInput(
    val search: String = "",
    val page: Int = 1,
    val size: Int = 10,
    val getAll: Boolean = false,
    val statuses: List<String> = emptyList()
)