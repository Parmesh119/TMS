package com.Tms.TMS.Model.DTO

class StateDTCRequest {

    data class TalukaFilterRequest(
        val state: String?,
        val district: String?
    )

    data class CityFilterRequest(
        val state: String?,
        val district: String?,
        val taluka: String?
    )

    data class DistrictFilterRequest(
        val state: String?
    )
}