package com.Tms.TMS.controller

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.Location
import com.Tms.TMS.model.LocationListRequest
import com.Tms.TMS.model.Party
import com.Tms.TMS.service.LocationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/v1/locations")
class LocationController(private val locationService: LocationService) {
    // Define user endpoints here

    // List all location
    @PostMapping("/list")
    fun listLocations(
        @RequestBody locationListRequest: LocationListRequest
    ): ResponseEntity<List<Location>> {
        return ResponseEntity.ok(locationService.getLocation(locationListRequest.search, locationListRequest.districts, locationListRequest.talukas, locationListRequest.statuses, locationListRequest.getAll, locationListRequest.page, locationListRequest.size))
    }

    //    Get location by id
    @GetMapping("/get/{id}")
    fun getLocationById(@PathVariable id: String): ResponseEntity<Location> {

        return ResponseEntity.ok(locationService.getLocationById(id))
    }

    //    Create location
    @PostMapping("/create")
    fun createLocation(@RequestBody location: Location): ResponseEntity<Location> {
        return ResponseEntity.ok(locationService.createLocation(location))
    }

    //    Update location
    @PostMapping("/update")
    fun updateLocation(@RequestBody location: Location): ResponseEntity<Location> {
        return try {
            val id = location.id!!
            ResponseEntity.ok(locationService.updateLocation(id, location))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }

    }

    //    Delete location
    @DeleteMapping("/delete/{id}")
    fun deleteLocation(@PathVariable id: String): Boolean {
        return locationService.deleteLocation(id)
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Location> {
        return ResponseEntity.ok(locationService.deactivateLocation(id))
    }

    @GetMapping("/activate/{id}")
    fun activateEmployee(@PathVariable id: String): ResponseEntity<Location> {
        return ResponseEntity.ok(locationService.activateLocation(id))
    }
}