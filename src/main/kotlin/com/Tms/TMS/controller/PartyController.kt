package com.Tms.TMS.controller

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Model.Party
import com.Tms.TMS.Service.LocationService
import com.Tms.TMS.Service.PartyService
import jakarta.servlet.http.Part
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
    fun listLocations(): ResponseEntity<List<Party>> {
        return ResponseEntity.ok(partyService.getLocation())
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

}