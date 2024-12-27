package com.Tms.TMS.Model.DTO

class StateDistrictTalukaCities {

    data class StateRequest(
        val states: List<String>? = null
    )

    data class DistrictRequest(
        val states: List<String>? = null,
        val districts: List<String>? = null
    )

    data class CityRequest(
        val states: List<String>? = null,
        val districts: List<String>? = null,
        val talukas: List<String>? = null
    )

    data class Location(
        val name: String,
        val districts: List<District>
    )

    data class District(
        val name: String,
        val talukas: List<Taluka>
    )

    data class Taluka(
        val name: String,
        val cities: List<String>
    )

    data class Locations(
        val locations: List<Location>
    )
}