package com.Tms.TMS.service

import com.Tms.TMS.config.StateDTCConfig
import com.Tms.TMS.model.DTO.StateDTCRequest
import org.springframework.stereotype.Service

@Service
class StateDTCService(private val stateDTCConfig: StateDTCConfig) {

    fun listStates(): List<String> {
        return stateDTCConfig.state.map { it.name }
    }

    fun listDistricts(districtFilterRequest: StateDTCRequest.DistrictFilterRequest): List<String> {
        return if (districtFilterRequest.state.isNullOrEmpty()) {
            stateDTCConfig.state.flatMap { it.districts }.map { it.name }
        } else {
            stateDTCConfig.state.filter { it.name in districtFilterRequest.state }.flatMap { it.districts }.map { it.name }
        }
    }

    fun listTalukas(talukaFilterRequest: StateDTCRequest.TalukaFilterRequest): List<String> {
        println("State Config: ${stateDTCConfig.state}")
        println("Filter Request: $talukaFilterRequest")

        return stateDTCConfig.state
            .filter { state ->
                talukaFilterRequest.state.isNullOrEmpty() || state.name.equals(talukaFilterRequest.state, ignoreCase = true)
            }
            .flatMap { state ->
                // If district is provided and found, return the talukas for that district
                if (!talukaFilterRequest.district.isNullOrEmpty()) {
                    state.districts
                        .filter { district -> district.name.equals(talukaFilterRequest.district, ignoreCase = true) }
                        .flatMap { it.talukas } // Get talukas for the found district
                } else {
                    // If district is not provided, return all talukas for all districts
                    state.districts.flatMap { it.talukas }
                }
            }
            .map { it.name }
    }

    fun listCities(CityFilterRequest: StateDTCRequest.CityFilterRequest): List<String> {
        return stateDTCConfig.state
            .filter { state ->
                // Filter by state if provided, otherwise include all states
                CityFilterRequest.state.isNullOrEmpty() || state.name.equals(CityFilterRequest.state, ignoreCase = true)
            }
            .flatMap { state ->
                state.districts.filter { district ->
                    // Filter by district if provided, otherwise include all districts
                    CityFilterRequest.district.isNullOrEmpty() || district.name.equals(CityFilterRequest.district, ignoreCase = true)
                }
            }
            .flatMap { district ->
                district.talukas.filter { taluka ->
                    // Filter by taluka if provided, otherwise include all talukas
                    CityFilterRequest.taluka.isNullOrEmpty() || taluka.name.equals(CityFilterRequest.taluka, ignoreCase = true)
                }
            }
            .flatMap { it.cities }
    }

}