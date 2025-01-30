package com.Tms.TMS.controller

import com.Tms.TMS.model.*
import com.Tms.TMS.service.EmailService
import com.Tms.TMS.service.EmployeeService
import com.Tms.TMS.service.GenerateAccessToken
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@CrossOrigin
@RequestMapping("/api/v1/employees")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val GenerateAccessToken: GenerateAccessToken
) {


    // List of all employee
    @PostMapping("/list")
    fun getEmployee(@RequestBody employeeListRequest: EmployeeListRequest): ResponseEntity<List<Employee>> {
        return ResponseEntity.ok(employeeService.getAllEmployee(employeeListRequest.search, employeeListRequest.roles, employeeListRequest.statuses, employeeListRequest.page, employeeListRequest.size))
    }

    // Get employee by Id
    @GetMapping("/get/{id}")
    fun getEmployeeById(@PathVariable id: String): ResponseEntity<Employee> {
        val accessToken = GenerateAccessToken.getAccessTokenFromOpenID()
        val headers = GenerateAccessToken.createHeaders(accessToken)
        return ResponseEntity.ok(employeeService.getEmployeeById(id, headers))
    }

    // Create new employee
    @PostMapping("/create")
    fun createEmployee(@RequestBody employee: Employee): ResponseEntity<Any> {
        return try {
            val keycloakUserDto = Keycloak_User_DTO(
                username = employee.email,
                email = employee.email,
                enabled = true,
                emailVerified = true
            )
            val accessToken = GenerateAccessToken.getAccessTokenFromOpenID()
            val headers = GenerateAccessToken.createHeaders(accessToken)
            val createdEmployee = employeeService.createEmployee(employee, headers, keycloakUserDto)
            ResponseEntity.ok(createdEmployee)
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to (e.message ?: "Unauthorized access")))
        }
    }

    @PostMapping("/update")
    fun updateEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        return try {
            val id = employee.id!!

            val keycloakUpdate = UserUpdateDTO (
                username = employee.email,
                email = employee.email,
                firstName = employee.name,
                enabled = true,
            )
            val accessToken = GenerateAccessToken.getAccessTokenFromOpenID()
            val headers = GenerateAccessToken.createHeaders(accessToken)
            ResponseEntity.ok(employeeService.updateEmployee(id, employee, keycloakUpdate, headers))
            ResponseEntity.ok(employeeService.getEmployeeById(id, headers))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }

    // delete employee
    @DeleteMapping("/delete/{id}")
    fun deleteEmployee(@PathVariable id: String): Boolean {
        val token = GenerateAccessToken.getAccessTokenFromOpenID()
        val headers = GenerateAccessToken.createHeaders(token)
        return employeeService.deleteEmployee(id, headers)
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Employee> {
        val token = GenerateAccessToken.getAccessTokenFromOpenID()
        val headers = GenerateAccessToken.createHeaders(token)

        return ResponseEntity.ok(employeeService.deactivateEmployee(id, headers))
    }

    @PostMapping("/forgot-password/send-mail")
    fun sendResetPasswordEmail(@RequestBody emailRequest: EmailRequest): ResponseEntity<String> {
        return try {
            val accessToken = GenerateAccessToken.getAccessTokenFromOpenID()
            val headers = GenerateAccessToken.createHeaders(accessToken)
            val response = employeeService.sendResetPasswordEmail(emailRequest.email, headers)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access")
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody passwordResetRequest: PasswordResetRequest): ResponseEntity<String> {
        return try {
            if(passwordResetRequest.password != passwordResetRequest.confirmPassword) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Passwords do not match")
            }
            val accessToken = GenerateAccessToken.getAccessTokenFromOpenID()
            val headers = GenerateAccessToken.createHeaders(accessToken)
            val response = passwordResetRequest.password?.let { passwordResetRequest.confirmPassword?.let { it1 ->
                passwordResetRequest.temporary?.let { it2 ->
                    employeeService.resetPassword(it,
                        it1, it2, passwordResetRequest.email, headers)
                }
            } }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access")
        }
    }
}