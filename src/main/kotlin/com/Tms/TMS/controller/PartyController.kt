package com.Tms.TMS.controller

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.Party
import com.Tms.TMS.model.PartyListRequest
import com.Tms.TMS.service.PartyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/v1/parties")
class PartyController(private val partyService: PartyService) {
    // Define user endpoints here

    // List all location
    @PostMapping("/list")
    fun listLocations(@RequestBody partyListRequest: PartyListRequest): ResponseEntity<List<Party>> {
        return ResponseEntity.ok(partyService.getLocation(partyListRequest.search, partyListRequest.statuses, partyListRequest.page, partyListRequest.size, partyListRequest.getAll))
    }

    //    Get location by id
    @GetMapping("/get/{id}")
    fun getLocationById(@PathVariable id: String): ResponseEntity<Party> {
        return ResponseEntity.ok(partyService.getLocationById(id))
    }

    //    Create location
    @PostMapping("/create")
    fun createLocation(@RequestBody party: Party): Boolean {
        return partyService.createLocation(party)
    }

    //    Update location
    @PostMapping("/update")
    fun updateLocation(@RequestBody party: Party): ResponseEntity<Party> {
        return try {
            val id = party.id!!
            return ResponseEntity.ok(partyService.updateLocation(id, party))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(null)
        }
    }

    //    Delete location
    @DeleteMapping("/location/delete/{id}")
    fun deleteLocation(@PathVariable id: String): Boolean {
        return partyService.deleteLocation(id)
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Party> {
        return ResponseEntity.ok(partyService.deactivateParty(id))
    }

    @GetMapping("/activate/{id}")
    fun activateEmployee(@PathVariable id: String): ResponseEntity<Party> {
        return ResponseEntity.ok(partyService.activateParty(id))
    }
}