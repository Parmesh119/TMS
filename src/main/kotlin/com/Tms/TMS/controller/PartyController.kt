package com.Tms.TMS.controller

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Model.Party
import com.Tms.TMS.Service.LocationService
import com.Tms.TMS.Service.PartyService
import jakarta.servlet.http.Part
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/user/party")
class PartyController(private val partyService: PartyService) {
    // Define user endpoints here

    // List all location
    @GetMapping("/location/list")
    fun listLocations(): ResponseEntity<List<Party>> {
        return ResponseEntity.ok(partyService.getLocation())
    }

    //    Get location by id
    @GetMapping("/location/{id}")
    fun getLocationById(@PathVariable id: String): ResponseEntity<Party> {
        return ResponseEntity.ok(partyService.getLocationById(id))
    }

    //    Create location
    @PostMapping("/location/create")
    fun createLocation(@RequestBody party: Party): Boolean {
        return partyService.createLocation(party)
    }

    //    Update location
    @PostMapping("/location/update/{id}")
    fun updateLocation(@PathVariable id: String): ResponseEntity<Party> {
        return ResponseEntity.ok(partyService.updateLocation(id))
    }

    //    Delete location
    @DeleteMapping("/location/delete/{id}")
    fun deleteLocation(@PathVariable id: String): Boolean {
        return partyService.deleteLocation(id)
    }

}