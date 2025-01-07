package com.Tms.TMS.service

import com.Tms.TMS.model.Location
import com.Tms.TMS.model.Party
import com.Tms.TMS.repository.LocationRepository
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service

@Service
class LocationService(private val locationRepository: LocationRepository) {

    // List of all location
    fun getLocation(
        search: String,
        district: List<String>,
        taluka: List<String>,
        statuses: List<String>,
        getAll: Boolean,
        page: Int,
        size: Int
    ): List<Location> {
        return locationRepository.getAlllocation(search, district, taluka, statuses, getAll, page, size)
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