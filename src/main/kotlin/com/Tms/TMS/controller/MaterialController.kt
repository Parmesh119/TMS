package com.Tms.TMS.controller

import com.Tms.TMS.model.Material
import com.Tms.TMS.service.MaterialService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/api/v1/materials")
class MaterialController(private val materialService: MaterialService) {
    // Material Controller operations
    // List all materials

    @PostMapping("/list")
    fun listAllMaterials(): ResponseEntity<List<Material>> {
        // Implement logic to list all materials
        return ResponseEntity.ok(materialService.listAllMaterials())
    }

    // get by id
    @PostMapping("/get/{id}")
    fun getMaterialById(@PathVariable id: String): ResponseEntity<Material> {
        // Implement logic to get material by id
        return ResponseEntity.ok(materialService.getMaterialById(id))
    }

    // create material
    @PostMapping("/create")
    fun createMaterial(@RequestBody material: Material): ResponseEntity<Material> {
        // Implement logic to create a new material
        return ResponseEntity.ok(materialService.createMaterial(material))
    }

    // update material
    @PostMapping("/update")
    fun updateMaterial(@RequestBody material: Material): ResponseEntity<Material> {
        // Implement logic to update an existing material
        return try {
            val id = material.id!!
            ResponseEntity.ok(materialService.updateMaterial(id, material))
            ResponseEntity.ok(materialService.getMaterialById(id))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}