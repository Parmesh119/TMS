package com.Tms.TMS.Service

import com.Tms.TMS.Model.Role
import com.Tms.TMS.Repository.RoleRepository
import org.springframework.stereotype.Service

@Service
class RoleService(private val roleRepository: RoleRepository) {
    fun getAllRoles(): List<Role> {
        TODO()
    }

    fun createRole(role: Role): Role {
        TODO()
    }

    fun updateRole(id: String, role: Role): Role {
        TODO()
    }

    fun deleteRole(id: String): Role {
        TODO()
    }
}