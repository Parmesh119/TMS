package com.Tms.TMS.Repository

import com.Tms.TMS.Model.Employee
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
            role = rs.getString("roles").split(",") ?: emptyList()
        )
    }
    // Employee Repository operations
    fun getAllEmployee(): List<Employee> {
        val sql = """
            SELECT e.id, e.name, e.email, e.contactNumber, GROUP_CONCAT(r.role_name) AS roles
            FROM employees e
            LEFT JOIN employee_roles er ON e.id = er.id
            LEFT JOIN roles r ON er.role_id = r.id
            GROUP BY e.id
        """
        return jdbcTemplate.query(sql, rowMapper)
    }

    fun getEmployeeById(id: String): Employee? {
        val sql = """
            SELECT e.id, e.name, e.email, e.contactNumber, GROUP_CONCAT(r.role_name) AS roles
            FROM employees e
            LEFT JOIN employee_roles er ON e.id = er.id
            LEFT JOIN roles r ON er.role_id = r.id
            WHERE e.id = ?
            GROUP BY e.id
        """
        return jdbcTemplate.queryForObject(sql, rowMapper, id)
    }

    // Create an employee and assign roles
    fun createEmployee(employee: Employee): Employee {
        val sqlEmployee = "INSERT INTO employees (id, name, email, contactNumber) VALUES (?, ?, ?, ?)"
        jdbcTemplate.update(sqlEmployee, employee.id, employee.name, employee.email, employee.contactNumber)

        val sqlRole = "INSERT INTO employee_roles (id, role_id) VALUES (?, ?)"
        employee.role.forEach { roleId ->
            jdbcTemplate.update(sqlRole, employee.id, roleId)
        }

        return getEmployeeById(employee.id!!) ?: throw Exception("Failed to retrieve created employee")
    }

    // Update an employee and their roles
    fun updateEmployee(id: String, employee: Employee): Int {
        val sqlEmployee = "UPDATE employees SET name = ?, email = ?, contactNumber = ? WHERE id = ?"
        jdbcTemplate.update(sqlEmployee, employee.name, employee.email, employee.contactNumber, id)

        val sqlDeleteRoles = "DELETE FROM employee_roles WHERE id = ?"
        jdbcTemplate.update(sqlDeleteRoles, id)

        val sqlInsertRoles = "INSERT INTO employee_roles (id, role_id) VALUES (?, ?)"
        employee.role.forEach { roleId ->
            jdbcTemplate.update(sqlInsertRoles, id, roleId)
        }

        return 1
    }

    // Delete an employee
    fun deleteEmployee(id: String): Boolean {
        val sql = "DELETE FROM employees WHERE id = ?"
        return jdbcTemplate.update(sql, id) > 0
    }
}