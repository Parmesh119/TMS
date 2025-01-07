package com.Tms.TMS.service

import com.Tms.TMS.model.Location
import com.Tms.TMS.repository.LocationRepository
import org.springframework.stereotype.Service
import org.springframework.data.crossstore.ChangeSetPersister

@Service
class LocationService(private val locationRepository: LocationRepository) {

    // List of all location
    fun getLocation(
        search: String,
        districts: List<String>,
        talukas: List<String>,
        statuses: List<String>,
        getAll: Boolean,
        page: Int,
        size: Int,
    ): List<Location> {
        return locationRepository.getAlllocation(
            search = search,
            districts = districts,
            talukas = talukas,
            statuses = statuses,
            getAll = getAll,
            page = page,
            size = size,
        )
    }

    // Get location by Id
    fun getLocationById(id: String): Location {
        return locationRepository.getLocationById(id) ?: throw Exception("Location not found")

    }

    // Create new location
    fun createLocation(location: Location): Location {
        return locationRepository.createLocation(location)
    }

    // update location
    fun updateLocation(id: String, location: Location): Location {
        return try {
            val updatedRows = locationRepository.updateLocation(id, location) ?: throw Exception("Location not found")
            if(updatedRows > 0) {
                return locationRepository.getLocationById(id) ?: throw Exception("Location not found")
            } else {
                throw Exception("Location not found")
            }
        } catch (ex: Exception) {
            throw Exception("Location not found")
        }
    }

    // delete location
    fun deleteLocation(id: String): Boolean {
        return locationRepository.deleteLocation(id)
    }

    // deactivate location
    fun deactivateLocation(id: String): Location {

        val location = locationRepository.getLocationById(id) ?: throw ChangeSetPersister.NotFoundException()
        if (location.status == "inactive") {
            throw IllegalStateException("Location is already inactive")
        }
        return locationRepository.deactivateLocation(id)

    }

    fun activateLocation(id: String): Location {
        val location = locationRepository.getLocationById(id) ?: throw ChangeSetPersister.NotFoundException()
        if (location.status == "active") {
            throw IllegalStateException("Location is already active")
        }
        return locationRepository.activateLocation(id)
    }
}