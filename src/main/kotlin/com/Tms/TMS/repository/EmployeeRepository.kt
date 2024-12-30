package com.Tms.TMS.repository

import com.Tms.TMS.model.Employee
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
class EmployeeRepository(private val jdbcTemplate: JdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        Employee(
            id = rs.getString("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            contactNumber = rs.getString("contactNumber"),
            role = rs.getString("role")
        )
    }
    // Employee Repository operations

    fun getAllEmployee(): List<Employee> {
        return jdbcTemplate.query("SELECT * FROM employee", rowMapper)
    }

    fun getEmployeeById(id: String): Employee? {
        return jdbcTemplate.queryForObject("SELECT * FROM employee WHERE id =?", rowMapper, id)
    }

    fun createEmployee(employee: Employee): Employee {
        val sql =
            "INSERT INTO employee (id, name, email, contactNumber, role) " +
                    "VALUES (?, ?, ?, ?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            employee.id,
            employee.name,
            employee.contactNumber,
            employee.email,
            employee.role
        ) > 0
        val result = getEmployeeById(employee.id!!)
        return result ?: throw Exception("Failed to retrieve created location")
    }

    fun updateEmployee(id: String, employee: Employee): Int {
        return try {
            val sql = """
            UPDATE employee
            SET name = ?, contactNumber = ?, email = ?, role = ?
            WHERE id = ?
        """.trimIndent()
            jdbcTemplate.update(sql, employee.name, employee.contactNumber, employee.email, employee.role, id)
        } catch (ex: Exception) {
            throw Exception("Location not found")
        }
    }

    fun deleteEmployee(id: String): Boolean {
        return try {
            val sql = "DELETE FROM employee WHERE id = ?"
            jdbcTemplate.update(sql, id)
            true
        } catch (ex: Exception) {
            throw Exception("Employee not found")
        }
    }


}