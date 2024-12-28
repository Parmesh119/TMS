package com.Tms.TMS.controller

import com.Tms.TMS.Model.Role
import com.Tms.TMS.Service.RoleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/roles")
class RoleController(private val roleService: RoleService) {

    @PostMapping("/list")
    fun getRoles(): ResponseEntity<List<Role>> {
        return ResponseEntity.ok(roleService.getAllRoles())
    }

    @PostMapping("/create")
    fun createRole(@RequestBody role: Role): ResponseEntity<Role> {
        return ResponseEntity.ok(roleService.createRole(role))
    }

    @PostMapping("/update")
    fun updateRole(@RequestBody role: Role): ResponseEntity<Role> {
        return try {
            val id = role.id
            ResponseEntity.ok(roleService.updateRole(id, role))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteRole(@PathVariable id: String): ResponseEntity<Role> {
        return ResponseEntity.ok(roleService.deleteRole(id))
    }
}