package com.Tms.TMS.Service

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Model.Party
import com.Tms.TMS.Repository.LocationRepository
import com.Tms.TMS.Repository.PartyRepository
import org.springframework.stereotype.Service

@Service
class PartyService(private val partyRepository: PartyRepository) {

    // List of all location
    fun getLocation(): List<Party> {
        return partyRepository.getAlllocation()
    }

    // Get location by Id
    fun getLocationById(id: String): Party {
        return partyRepository.getLocationById(id) ?: throw Exception("Location not found")

    }

    // Create new location
    fun createLocation(party: Party): Boolean {
        return partyRepository.createLocation(party)
    }

    // update location
    fun updateLocation(id: String): Party {
        TODO()
    }

    // delete location
    fun deleteLocation(id: String): Boolean {
        TODO()
    }
}