package com.Tms.TMS.Model

class StateDTC {
    data class locations(
        val locations: List<State>
    )

    data class State(
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
}