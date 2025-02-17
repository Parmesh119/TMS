package com.Tms.TMS.controller

import com.Tms.TMS.model.ListLocationsInput
import com.Tms.TMS.model.Location
import com.Tms.TMS.service.LocationService
import com.Tms.TMS.util.MetricsUtil  // Import MetricsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.annotation.PostConstruct
import java.util.concurrent.TimeUnit

@CrossOrigin
@RestController
@RequestMapping("/api/v1/locations")
class LocationController(private val locationService: LocationService) {

    @Autowired
    private lateinit var metricsUtil: MetricsUtil  // Inject MetricsUtil

    private val baseUrl = "/api/v1/locations"
    private val listLocationsEndpoint = "$baseUrl/list"
    private val getLocationByIdEndpoint = "$baseUrl/get/{id}"
    private val createLocationEndpoint = "$baseUrl/create"
    private val updateLocationEndpoint = "$baseUrl/update"
    private val deleteLocationEndpoint = "$baseUrl/delete/{id}"
    private val deactivateLocationEndpoint = "$baseUrl/deactivate/{id}"
    private val activateLocationEndpoint = "$baseUrl/activate/{id}"

    @PostConstruct
    fun init() {
        metricsUtil.registerEndpoint(listLocationsEndpoint)
        metricsUtil.registerEndpoint(getLocationByIdEndpoint)
        metricsUtil.registerEndpoint(createLocationEndpoint)
        metricsUtil.registerEndpoint(updateLocationEndpoint)
        metricsUtil.registerEndpoint(deleteLocationEndpoint)
        metricsUtil.registerEndpoint(deactivateLocationEndpoint)
        metricsUtil.registerEndpoint(activateLocationEndpoint)
    }

    // List all location
    @PostMapping("/list")
    fun listLocations(@RequestBody listLocationsInput: ListLocationsInput): ResponseEntity<List<Location>> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = listLocationsEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val locations = locationService.getLocation(
                listLocationsInput.search,
                listLocationsInput.districts,
                listLocationsInput.talukas,
                listLocationsInput.statuses,
                listLocationsInput.getAll,
                listLocationsInput.page,
                listLocationsInput.size
            )
            ResponseEntity.ok(locations)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    //    Get location by id
    @GetMapping("/get/{id}")
    fun getLocationById(@PathVariable id: String): ResponseEntity<Location> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = getLocationByIdEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val location = locationService.getLocationById(id)
            ResponseEntity.ok(location)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    //    Create location
    @PostMapping("/create")
    fun createLocation(@RequestBody location: Location): ResponseEntity<Location> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = createLocationEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val createdLocation = locationService.createLocation(location)
            ResponseEntity.ok(createdLocation)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    //    Update location
    @PostMapping("/update")
    fun updateLocation(@RequestBody location: Location): ResponseEntity<Location> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = updateLocationEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val id = location.id!!
            val updatedLocation = locationService.updateLocation(id, location)
            ResponseEntity.ok(updatedLocation)
        } catch (ex: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    //    Delete location
    @DeleteMapping("/delete/{id}")
    fun deleteLocation(@PathVariable id: String): ResponseEntity<Boolean> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = deleteLocationEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val deleted = locationService.deleteLocation(id)
            ResponseEntity.ok(deleted)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateLocation(@PathVariable id: String): ResponseEntity<Location> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = deactivateLocationEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val deactivatedLocation = locationService.deactivateLocation(id)
            ResponseEntity.ok(deactivatedLocation)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @GetMapping("/activate/{id}")
    fun activateLocation(@PathVariable id: String): ResponseEntity<Location> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = activateLocationEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val activatedLocation = locationService.activateLocation(id)
            ResponseEntity.ok(activatedLocation)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }
}