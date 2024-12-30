package com.Tms.TMS.service

import com.Tms.TMS.model.Employee
import com.Tms.TMS.repository.EmployeeRepository
import org.springframework.stereotype.Service

@Service
class EmployeeService(private val employeeRepository: EmployeeRepository) {

    fun getAllEmployee(): List<Employee> {
        return employeeRepository.getAllEmployee()
    }

    fun getEmployeeById(id: String): Employee {
        return employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
    }

    fun createEmployee(employee: Employee): Employee {
        return employeeRepository.createEmployee(employee)
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