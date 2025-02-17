package com.Tms.TMS.controller

import com.Tms.TMS.model.Material
import com.Tms.TMS.service.MaterialService
import com.Tms.TMS.util.MetricsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.annotation.PostConstruct
import java.util.concurrent.TimeUnit

@RestController
@CrossOrigin
@RequestMapping("/api/v1/materials")
class MaterialController(private val materialService: MaterialService) {

    @Autowired
    private lateinit var metricsUtil: MetricsUtil

    private val baseUrl = "/api/v1/materials"
    private val listMaterialsEndpoint = "$baseUrl/list"
    private val getMaterialByIdEndpoint = "$baseUrl/get/{id}"
    private val createMaterialEndpoint = "$baseUrl/create"
    private val updateMaterialEndpoint = "$baseUrl/update"

    @PostConstruct
    fun init() {
        metricsUtil.registerEndpoint(listMaterialsEndpoint)
        metricsUtil.registerEndpoint(getMaterialByIdEndpoint)
        metricsUtil.registerEndpoint(createMaterialEndpoint)
        metricsUtil.registerEndpoint(updateMaterialEndpoint)
    }

    // Material Controller operations
    // List all materials
    @GetMapping("/list")
    fun listAllMaterials(): ResponseEntity<List<Material>> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = listMaterialsEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val materials = materialService.listAllMaterials()
            ResponseEntity.ok(materials)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // Get by id
    @GetMapping("/get/{id}")
    fun getMaterialById(@PathVariable id: String): ResponseEntity<Material> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = getMaterialByIdEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val material = materialService.getMaterialById(id)
            ResponseEntity.ok(material)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // Create material
    @PostMapping("/create")
    fun createMaterial(@RequestBody material: Material): ResponseEntity<Material> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = createMaterialEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val createdMaterial = materialService.createMaterial(material)
            ResponseEntity.ok(createdMaterial)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // Update material
    @PostMapping("/update")
    fun updateMaterial(@RequestBody material: Material): ResponseEntity<Material> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = updateMaterialEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val id = material.id!!
            val updatedMaterial = materialService.updateMaterial(id, material)
            ResponseEntity.ok(updatedMaterial)
        } catch (ex: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }
}