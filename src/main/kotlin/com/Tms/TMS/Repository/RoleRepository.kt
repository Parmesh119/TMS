package com.Tms.TMS.Repository

import com.Tms.TMS.Model.Role
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class RoleRepository(private val jdbcTemplate: JdbcTemplate){

    fun getAllRoles(): List<Role> {
        val sql = "SELECT * FROM role"
        return jdbcTemplate.query(sql, rowMapper)
    }

    fun getRoleById(id: String): Role? {
        return jdbcTemplate.queryForObject("SELECT * FROM role WHERE id = ?", rowMapper, id)
    }

    fun createRole(role: Role): Role {
        val sql =
            "INSERT INTO role (id, role_name) " +
                    "VALUES (?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            role.id,
            role.role_name
        ) > 0
        val result = getRoleById(role.id!!)
        return result ?: throw Exception("Failed to retrieve created role")
    }

    fun updateRole(id: String, role: Role): Int {
        val sql =
            "UPDATE role SET role_name = ? WHERE id = ?"
        return jdbcTemplate.update(
            sql,
            role.role_name,
            id
        )
    }

    fun deleteRole(id: String): Boolean {
        val sql = "DELETE FROM role WHERE id = ?"
        return jdbcTemplate.update(sql, id) > 0
    }
}