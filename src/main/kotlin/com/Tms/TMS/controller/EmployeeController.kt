package com.Tms.TMS.controller

import com.Tms.TMS.model.*
import com.Tms.TMS.service.EmployeeService
import com.Tms.TMS.service.GenerateAccessToken
import com.Tms.TMS.util.MetricsUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.TimeUnit

@RestController
@CrossOrigin
@RequestMapping("/api/v1/employees")
class EmployeeController {

    @Autowired
    private lateinit var employeeService: EmployeeService
    @Autowired
    private lateinit var generateAccessToken: GenerateAccessToken
    @Autowired
    private lateinit var metricsUtil: MetricsUtil

    private val baseUrl = "/api/v1/employees"
    private val employeeListEndpoint = "$baseUrl/list"
    private val getEmployeeByIdEndpoint = "$baseUrl/get/{id}"
    private val createEmployeeEndpoint = "$baseUrl/create"
    private val updateEmployeeEndpoint = "$baseUrl/update"
    private val deleteEmployeeEndpoint = "$baseUrl/delete/{id}"
    private val deactivateEmployeeEndpoint = "$baseUrl/deactivate/{id}"
    private val sendResetPasswordEmailEndpoint = "$baseUrl/forgot-password/send-mail"
    private val resetPasswordEndpoint = "$baseUrl/reset-password"

    @PostConstruct
    fun init() {
        metricsUtil.registerEndpoint(employeeListEndpoint)
        metricsUtil.registerEndpoint(getEmployeeByIdEndpoint)
        metricsUtil.registerEndpoint(createEmployeeEndpoint)
        metricsUtil.registerEndpoint(updateEmployeeEndpoint)
        metricsUtil.registerEndpoint(deleteEmployeeEndpoint)
        metricsUtil.registerEndpoint(deactivateEmployeeEndpoint)
        metricsUtil.registerEndpoint(sendResetPasswordEmailEndpoint)
        metricsUtil.registerEndpoint(resetPasswordEndpoint)
    }

    // List of all employee
    @PostMapping("/list")
    fun getEmployee(@RequestBody employeeListRequest: EmployeeListRequest): ResponseEntity<List<Employee>> {
        Thread.sleep(10000)
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = employeeListEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)

        return try {
            val employeeList = employeeService.getAllEmployee(employeeListRequest.search, employeeListRequest.roles, employeeListRequest.statuses, employeeListRequest.page, employeeListRequest.size)
            ResponseEntity.ok(employeeList)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // Get employee by Id
    @GetMapping("/get/{id}")
    fun getEmployeeById(@PathVariable id: String): ResponseEntity<Employee> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = getEmployeeByIdEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val accessToken = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(accessToken)
            val employee = employeeService.getEmployeeById(id, headers)
            ResponseEntity.ok(employee)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // Create new employee
    @PostMapping("/create")
    fun createEmployee(@RequestBody employee: Employee): ResponseEntity<Any> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = createEmployeeEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val keycloakUserDto = Keycloak_User_DTO(
                username = employee.email,
                email = employee.email,
                enabled = true,
                emailVerified = true
            )
            val accessToken = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(accessToken)
            val createdEmployee = employeeService.createEmployee(employee, headers, keycloakUserDto)
            ResponseEntity.ok(createdEmployee)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to (e.message ?: "Unauthorized access")))
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @PostMapping("/update")
    fun updateEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = updateEmployeeEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)

        return try {
            val id = employee.id!!

            val keycloakUpdate = UserUpdateDTO (
                username = employee.email,
                email = employee.email,
                firstName = employee.name,
                enabled = true,
            )
            val accessToken = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(accessToken)
            employeeService.updateEmployee(id, employee, keycloakUpdate, headers)
            ResponseEntity.ok(employeeService.getEmployeeById(id, headers))
        } catch (ex: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    // delete employee
    @DeleteMapping("/delete/{id}")
    fun deleteEmployee(@PathVariable id: String): ResponseEntity<Boolean> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = deleteEmployeeEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)
        return try {
            val token = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(token)
            val deleted = employeeService.deleteEmployee(id, headers)
            ResponseEntity.ok(deleted)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @GetMapping("/deactivate/{id}")
    fun deactivateEmployee(@PathVariable id: String): ResponseEntity<Employee> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = deactivateEmployeeEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)

        return try {
            val token = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(token)

            val employee = employeeService.deactivateEmployee(id, headers)
            ResponseEntity.ok(employee)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @PostMapping("/forgot-password/send-mail")
    fun sendResetPasswordEmail(@RequestBody emailRequest: EmailRequest): ResponseEntity<String> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = sendResetPasswordEmailEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)

        return try {
            val accessToken = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(accessToken)
            val response = employeeService.sendResetPasswordEmail(emailRequest.email, headers)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access")
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody passwordResetRequest: PasswordResetRequest): ResponseEntity<String> {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = resetPasswordEndpoint
        metricsUtil.trackActiveRequests(endpoint, true)

        return try {
            if(passwordResetRequest.password != passwordResetRequest.confirmPassword) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Passwords do not match")
            }
            val accessToken = generateAccessToken.getAccessTokenFromOpenID()
            val headers = generateAccessToken.createHeaders(accessToken)
            val response = passwordResetRequest.password?.let { passwordResetRequest.confirmPassword?.let { it1 ->
                passwordResetRequest.temporary?.let { it2 ->
                    employeeService.resetPassword(it,
                        it1, it2, passwordResetRequest.email, headers)
                }
            } }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            status = "error"
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access")
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }


    //For the global and per endpoints metrics
    @GetMapping("/metrics")
    fun getMetrics(): ResponseEntity<Map<String, Any>> {
        val detailedMetrics = metricsUtil.getEndpointMetrics()
        val allMetrics = metricsUtil.getGlobalMetrics().toMutableMap()

        allMetrics["endpoints"] = detailedMetrics

        return ResponseEntity.ok(allMetrics.toMap())
    }
}