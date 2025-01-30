package com.Tms.TMS.repository

import com.Tms.TMS.model.Employee
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import kotlin.math.ceil

@Component
@Repository
class EmployeeRepository(private val jdbcTemplate: JdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        Employee(
            id = rs.getString("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            contactNumber = rs.getString("contactNumber"),
            role = rs.getString("role"),
            status = rs.getString("status")
        )
    }
    // Employee Repository operations

    fun getAllEmployee(
        search: String,
        roles: List<String>,
        status: List<String>,
        page: Int,
        size: Int
    ): List<Employee> {
        return try {
            val sqlBuilder = StringBuilder("SELECT * FROM employee WHERE 1=1")

            if (roles.isNotEmpty()) {
                sqlBuilder.append(" AND role IN (${roles.joinToString(",") { "'$it'" }})")
            }

            if (status.isNotEmpty()) {
                sqlBuilder.append(" AND status IN (${status.joinToString(",") { "'$it'" }})")
            }

            if (!search.isNullOrBlank()) {
                sqlBuilder.append(" AND name ILIKE ?")
            }

            sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?")

            val sql = sqlBuilder.toString()
            val offset = (page - 1) * size

// Prepare arguments for the query
            val queryParams = mutableListOf<Any>()

            if (!search.isNullOrBlank()) {
                queryParams.add("%$search%")
            }
            queryParams.add(size)
            queryParams.add(offset)

// Use a valid rowMapper for Employee
            val employees = jdbcTemplate.query(sql, { rs, _ ->
                Employee(
                    id = rs.getString("id"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    contactNumber = rs.getString("contactNumber"),
                    role = rs.getString("role"),
                    status = rs.getString("status")
                )
            }, *queryParams.toTypedArray())

            employees

        } catch (e: Exception) {
            throw e
        }
    }

    fun getEmployeeById(id: String): Employee? {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM employee WHERE id =?", rowMapper, id)
        } catch (ex: Exception) {
            println("Employee not found" + ex.message)
            throw ex
        }
    }

    fun createEmployee(employee: Employee): Employee {
        try {
            val sql = """
        INSERT INTO employee (id, name, email, contactNumber, role, status)
        VALUES (?, ?, ?, ?, ?, ?)
    """
            val contactNumber = employee.contactNumber ?: "" // Default to an empty string if null
            val status = employee.status ?: "active" // Default to "Active" if null
            val rowsAffected = jdbcTemplate.update(
                sql,
                employee.id,
                employee.name,
                employee.email,
                contactNumber,
                employee.role,
                status
            )
            if (rowsAffected > 0) {
                return getEmployeeById(employee.id!!) ?: throw Exception("Failed to retrieve created employee")
            } else {
                throw Exception("Failed to create employee")
            }
        } catch (ex: Exception) {
            throw ex
        }
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

    fun deactivateEmployee(id: String): Employee {
        try {
            val updateCount = jdbcTemplate.update(
                "UPDATE employee SET status = 'inactive' WHERE id = ?",
                id
            )

            if (updateCount == 0) {
                throw ChangeSetPersister.NotFoundException()
            }

            return getEmployeeById(id) ?: throw Exception("Failed to retrieve updated employee")
        } catch (ex: Exception) {
            throw ex
        }
    }
}