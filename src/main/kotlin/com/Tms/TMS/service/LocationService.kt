package com.Tms.TMS.service

import com.Tms.TMS.model.Location
import com.Tms.TMS.repository.LocationRepository
import org.springframework.stereotype.Service

@Service
class LocationService(private val locationRepository: LocationRepository) {

    // List of all location
    fun getLocation(): List<Location> {
        return locationRepository.getAlllocation()
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
}