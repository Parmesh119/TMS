package com.Tms.TMS.service

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.RegisterRequest
import com.Tms.TMS.repository.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val authService: AuthService
) {

    fun getAllEmployee(): List<Employee> {
        return employeeRepository.getAllEmployee()
    }

    fun getEmployeeById(id: String): Employee {
        return employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
    }

    @Transactional
    fun createEmployee(employee: Employee): Employee {
//        val password = authService.generateRandomPassword(6)
        val password = "abcd"
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
}