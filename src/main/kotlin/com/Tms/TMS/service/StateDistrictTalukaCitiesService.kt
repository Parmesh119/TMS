package com.Tms.TMS.service
import com.Tms.TMS.model.DTO.StateDistrictTalukaCities
import com.Tms.TMS.model.DTO.StateDistrictTalukaCities.Locations
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

@Service
class StateDistrictTalukaCitiesService {
    private val locations: Locations

    init {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val resource = ClassPathResource("taluka.yml")
        locations = mapper.readValue(resource.inputStream)
    }

    fun listStates(): List<String> {
        return locations.locations.map { location -> location.name }
    }

    fun listDistricts(stateRequest: StateDistrictTalukaCities.StateRequest): List<String> {
        return when {
            stateRequest.states.isNullOrEmpty() -> locations.locations.flatMap { location ->
                location.districts.map { district -> district.name }
            }
            else -> locations.locations
                .filter { location -> location.name in stateRequest.states }
                .flatMap { location -> location.districts.map { district -> district.name } }
                .distinct()
        }
    }

    fun listTalukas(request: StateDistrictTalukaCities.DistrictRequest): List<String> {
        return when {
            // Both states and districts are null - return all talukas
            request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts.flatMap { district ->
                        district.talukas.map { taluka -> taluka.name }
                    }
                }
            }
            // Only states are provided - return all talukas in those states
            !request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() -> {
                locations.locations
                    .filter { location -> location.name in request.states }
                    .flatMap { location ->
                        location.districts.flatMap { district ->
                            district.talukas.map { taluka -> taluka.name }
                        }
                    }
                    .distinct()
            }
            // Only districts are provided - return all talukas in those districts across all states
            request.states.isNullOrEmpty() && !request.districts.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts
                        .filter { district -> district.name in request.districts }
                        .flatMap { district ->
                            district.talukas.map { taluka -> taluka.name }
                        }
                }
                    .distinct()
            }
            // Both states and districts are provided
            else -> {
                locations.locations
                    .filter { location -> location.name in request.states!! }
                    .flatMap { location ->
                        location.districts
                            .filter { district -> district.name in request.districts!! }
                            .flatMap { district ->
                                district.talukas.map { taluka -> taluka.name }
                            }
                    }
                    .distinct()
            }
        }
    }

    fun listCities(request: StateDistrictTalukaCities.CityRequest): List<String> {
        return when {
            // All parameters are null - return all cities
            request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() && request.talukas.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts.flatMap { district ->
                        district.talukas.flatMap { taluka -> taluka.cities }
                    }
                }
            }
            // Only states are provided
            !request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() && request.talukas.isNullOrEmpty() -> {
                locations.locations
                    .filter { location -> location.name in request.states }
                    .flatMap { location ->
                        location.districts.flatMap { district ->
                            district.talukas.flatMap { taluka -> taluka.cities }
                        }
                    }
                    .distinct()
            }
            // Only districts are provided
            request.states.isNullOrEmpty() && !request.districts.isNullOrEmpty() && request.talukas.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts
                        .filter { district -> district.name in request.districts }
                        .flatMap { district ->
                            district.talukas.flatMap { taluka -> taluka.cities }
                        }
                }
                    .distinct()
            }
            // Only talukas are provided
            request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() && !request.talukas.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts.flatMap { district ->
                        district.talukas
                            .filter { taluka -> taluka.name in request.talukas }
                            .flatMap { taluka -> taluka.cities }
                    }
                }
                    .distinct()
            }
            // States and districts are provided
            !request.states.isNullOrEmpty() && !request.districts.isNullOrEmpty() && request.talukas.isNullOrEmpty() -> {
                locations.locations
                    .filter { location -> location.name in request.states }
                    .flatMap { location ->
                        location.districts
                            .filter { district -> district.name in request.districts }
                            .flatMap { district ->
                                district.talukas.flatMap { taluka -> taluka.cities }
                            }
                    }
                    .distinct()
            }
            // States and talukas are provided
            !request.states.isNullOrEmpty() && request.districts.isNullOrEmpty() && !request.talukas.isNullOrEmpty() -> {
                locations.locations
                    .filter { location -> location.name in request.states }
                    .flatMap { location ->
                        location.districts.flatMap { district ->
                            district.talukas
                                .filter { taluka -> taluka.name in request.talukas }
                                .flatMap { taluka -> taluka.cities }
                        }
                    }
                    .distinct()
            }
            // Districts and talukas are provided
            request.states.isNullOrEmpty() && !request.districts.isNullOrEmpty() && !request.talukas.isNullOrEmpty() -> {
                locations.locations.flatMap { location ->
                    location.districts
                        .filter { district -> district.name in request.districts }
                        .flatMap { district ->
                            district.talukas
                                .filter { taluka -> taluka.name in request.talukas }
                                .flatMap { taluka -> taluka.cities }
                        }
                }
                    .distinct()
            }
            // All parameters are provided
            else -> {
                locations.locations
                    .filter { location -> location.name in request.states!! }
                    .flatMap { location ->
                        location.districts
                            .filter { district -> district.name in request.districts!! }
                            .flatMap { district ->
                                district.talukas
                                    .filter { taluka -> taluka.name in request.talukas!! }
                                    .flatMap { taluka -> taluka.cities }
                            }
                    }
                    .distinct()
            }
        }
    }
}