package com.Tms.TMS.service

import com.Tms.TMS.model.Material
import com.Tms.TMS.repository.MaterialRepository
import org.springframework.stereotype.Service

@Service
class MaterialService(private val materialRepository: MaterialRepository) {

    fun listAllMaterials(): List<Material> {
        return materialRepository.listAllMaterial()
    }

    fun getMaterialById(id: String): Material {
        return materialRepository.getMaterialById(id)?: throw Exception("Material not found")
    }

    fun createMaterial(material: Material): Material {
        return materialRepository.createMaterial(material)
    }

    fun updateMaterial(id: String, material: Material): Material {
        return try {
            val updatedRows = materialRepository.updateMaterial(id, material)
            if(updatedRows > 0) {
                return materialRepository.getMaterialById(id) ?: throw Exception("Material not found")
            } else {
                throw Exception("Material not found")
            }
        } catch (ex: Exception) {
            throw Exception("Material not found")
        }
    }
}