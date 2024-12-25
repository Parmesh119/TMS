package com.Tms.TMS.controller

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Service.LocationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/user")
class LocationController(private val locationService: LocationService) {
    // Define user endpoints here

    // List all location
    @GetMapping("/location/list")
    fun listLocations(): ResponseEntity<List<Location>> {
        return ResponseEntity.ok(locationService.getLocation())
    }

    //    Get location by id
    @GetMapping("/location/{id}")
    fun getLocationById(@PathVariable id: String): ResponseEntity<Location> {
        return ResponseEntity.ok(locationService.getLocationById(id))
    }

    //    Create location
    @PostMapping("/location/create")
    fun createLocation(@RequestBody location: Location): Boolean {
        return locationService.createLocation(location)
    }

    //    Update location
    @PostMapping("/location/update/{id}")
    fun updateLocation(@PathVariable id: String): ResponseEntity<Location> {
        return ResponseEntity.ok(locationService.updateLocation(id))
    }

    //    Delete location
    @DeleteMapping("/location/delete/{id}")
    fun deleteLocation(@PathVariable id: String): Boolean {
        return locationService.deleteLocation(id)
    }

}