package com.Tms.TMS.service

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.Party
import com.Tms.TMS.repository.PartyRepository
import org.springframework.core.annotation.MergedAnnotations.Search
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service

@Service
class PartyService(private val partyRepository: PartyRepository) {

    // List of all location
    fun getLocation(
        search: String,
        status: List<String>,
        page: Int,
        size: Int,
        getAll: Boolean
    ): List<Party> {
        return partyRepository.getAlllocation(search, status, page, size, getAll)
    }

    // Get location by Id
    fun getLocationById(id: String): Party {
        return partyRepository.getLocationById(id) ?: throw Exception("Location not found")

    }

    // Create new location
    fun createLocation(party: Party): Boolean {
        return partyRepository.createLocation(party)
    }

    fun updateLocation(id: String, party: Party): Party {
        return try {
            val updatedRows = partyRepository.updateLocation(id, party) ?: throw Exception("Location not found")
            if(updatedRows > 0) {
                return partyRepository.getLocationById(id) ?: throw Exception("Location not found")
            } else {
                throw Exception("Location not found")
            }
        } catch (ex: Exception) {
            throw Exception("Location not found")
        }
    }

    // delete location
    fun deleteLocation(id: String): Boolean {
        return partyRepository.deleteLocation(id)
    }

    fun deactivateParty(id: String): Party {
        val party = partyRepository.getLocationById(id) ?: throw ChangeSetPersister.NotFoundException()
        if (party.status == "inactive") {
            throw IllegalStateException("Party is already inactive")
        }
        return partyRepository.deactivateParty(id)

    }

    fun activateParty(id: String): Party {
        val party = partyRepository.getLocationById(id) ?: throw ChangeSetPersister.NotFoundException()
        if (party.status == "active") {
            throw IllegalStateException("Party is already active")
        }
        return partyRepository.activateParty(id)
    }
}