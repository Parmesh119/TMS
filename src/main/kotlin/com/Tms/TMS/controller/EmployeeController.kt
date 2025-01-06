package com.Tms.TMS.controller

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.EmployeeListRequest
import com.Tms.TMS.service.EmployeeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/v1/employees")
class EmployeeController(private val employeeService: EmployeeService) {

    // List of all employee
    @PostMapping("/list")
    fun getEmployee(@RequestBody employeeListRequest: EmployeeListRequest): ResponseEntity<List<Employee>> {
        return ResponseEntity.ok(employeeService.getAllEmployee(employeeListRequest.search, employeeListRequest.roles, employeeListRequest.statuses, employeeListRequest.page, employeeListRequest.size))
    }

    // Get employee by Id
    @GetMapping("/get/{id}")
    fun getEmployeeById(@PathVariable id: String): ResponseEntity<Employee> {
        return ResponseEntity.ok(employeeService.getEmployeeById(id))
    }

    // Create new employee
    @PostMapping("/create")
    fun createEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        return ResponseEntity.ok(employeeService.createEmployee(employee))
    }

    @PostMapping("/update")
    fun updateEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        return try {
            val id = employee.id!!
            ResponseEntity.ok(employeeService.updateEmployee(id, employee))
            ResponseEntity.ok(employeeService.getEmployeeById(id))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }

    // delete employee
    @DeleteMapping("/delete/{id}")
    fun deleteEmployee(@PathVariable id: String): Boolean {
        return employeeService.deleteEmployee(id)
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Employee> {
        return ResponseEntity.ok(employeeService.deactivateEmployee(id))
    }
}