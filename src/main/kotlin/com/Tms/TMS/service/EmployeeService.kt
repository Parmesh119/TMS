package com.Tms.TMS.service

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.RegisterRequest
import com.Tms.TMS.repository.AuthRepository
import com.Tms.TMS.repository.EmployeeRepository
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val authService: AuthService,
    private val authRepository: AuthRepository
) {

    fun getAllEmployee(
        search: String,
        roles: List<String>,
        status: List<String>,
        page: Int,
        size: Int
    ): List<Employee> {
        return employeeRepository.getAllEmployee(search, roles, status, page, size)
    }

    fun getEmployeeById(id: String): Employee {
        return employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
    }

    @Transactional
    fun createEmployee(employee: Employee): Employee {
        val password: String = "123456"
        val createUser = RegisterRequest(
            username = employee.email,
            email = employee.email,
            role = employee.role,
            password = password
        )
        try {
            authService.register(createUser)
            return  employeeRepository.createEmployee(employee)
        } catch (ex: Exception) {
            print(ex.message)
            throw Exception("Failed to create user")
        }
    }

    fun updateEmployee(id: String, employee: Employee): Employee {
        return try {
            val updatedRows = employeeRepository.updateEmployee(id, employee)?: throw Exception("Employee not found")
            if(updatedRows > 0) {
                return employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
            } else {
                throw Exception("Employee not found")
            }
        } catch (ex: Exception) {
            throw Exception("Employee not found")
        }
    }

    fun deleteEmployee(id: String): Boolean {
        return employeeRepository.deleteEmployee(id)
    }

    fun deactivateEmployee(id: String): Employee {
        val employee = employeeRepository.getEmployeeById(id) ?: throw ChangeSetPersister.NotFoundException()
        if (employee.status == "inactive") {
            throw IllegalStateException("Employee is already inactive")
        }
        val res = employeeRepository.deactivateEmployee(id)
        if(res.status == "inactive") {
            val response = authRepository.findByEmail(employee.email) ?: throw Exception("Failed to deactivate employee")
            if (response != null) {
                return employee
            } else {
                throw Exception("Failed to delete user")
            }
        } else {
            throw Exception("Failed to deactivate employee")
        }
    }
}