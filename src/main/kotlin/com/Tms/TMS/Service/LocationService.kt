package com.Tms.TMS.Service

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Repository.LocationRepository
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
    fun createLocation(location: Location): Boolean {
        return locationRepository.createLocation(location)
    }

    // update location
    fun updateLocation(id: String): Location {
        TODO()
    }

    // delete location
    fun deleteLocation(id: String): Boolean {
        TODO()
    }
}