package com.Tms.TMS.service

import com.Tms.TMS.model.*
import com.Tms.TMS.repository.AuthRepository
import com.Tms.TMS.repository.EmployeeRepository
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val authService: AuthService,
    private val authRepository: AuthRepository,
    private val restTemplate: RestTemplate,
    private val emailService: EmailService,
) {

    private val realm = "TMS"
//    private val adminBaseUrl = "http://localhost:8080/admin/realms/TMS"

    @Value("\${keycloak.auth-server-url}")
    private val authServerUrl: String? = null

    @Value("\${keycloak.resource}")
    private val clientId: String? = null

    @Value("\${keycloak.credentials.secret}")
    private val clientSecret: String? = null


    fun getAllEmployee(
        search: String,
        roles: List<String>,
        status: List<String>,
        page: Int,
        size: Int
    ): List<Employee> {
        return employeeRepository.getAllEmployee(search, roles, status, page, size)
    }

    fun getEmployeeById(id: String, headers: HttpHeaders): Employee {
        return employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
    }

    fun getKeycloakUserById (usermame: String, headers: HttpHeaders): UserRepresentation {
        val response = restTemplate.exchange(
            "${authServerUrl}/admin/realms/TMS/users?username=$usermame",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            object : ParameterizedTypeReference<List<UserRepresentation>>() {}
        )
        return response.body?.firstOrNull() ?: throw Exception("User not found")
    }

    @Transactional
    fun createEmployee(employee: Employee, headers: HttpHeaders, keycloakUserDto: Keycloak_User_DTO): Employee {
        return try {
            val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val password: String = (1..6)
                .map { characters[Random.nextInt(characters.length)] }
                .joinToString("")

        val createUser = RegisterRequest(
            username = employee.email,
            email = employee.email,
            role = employee.role,
            password = password
        )

        val user = UserRepresentation().apply {
            username = employee.email
            email = createUser.email
            firstName = employee.name?: "user"
            credentials = listOf(
                CredentialRepresentation().apply {
                    type = "password"
                    value = createUser.password
                    isTemporary = true
                }
            )
            isEnabled = keycloakUserDto.enabled?: true
            requiredActions = listOf()

        }

        val request = HttpEntity(user, headers)
        val response = restTemplate.postForEntity(
            "$authServerUrl/admin/realms/TMS/users",
            request,
            String::class.java
        )
        if(response.statusCode.isError) {
            throw RuntimeException("Failed to create user")
        } else {
            val userId = extractUserId(response)

            val clientid = getClientByClientId(headers, "Employee")?.id?:throw Exception("Not found")

            val res = employee.role.let {
                val roleResponse = restTemplate.exchange(
                    "$authServerUrl/admin/realms/TMS/clients/$clientid/roles/$it",
                    HttpMethod.GET,
                    HttpEntity<Void>(headers),
                    RoleRepresentation::class.java
                )
                roleResponse.body ?: throw RuntimeException("Role not found")
            }

            val roleRequest = HttpEntity(listOf(res), headers)
            val response_role = restTemplate.postForEntity(
                "$authServerUrl/admin/realms/TMS/users/$userId/role-mappings/clients/$clientid",
                roleRequest,
                Void::class.java
            )
             if(response_role.statusCode.is2xxSuccessful) {
                authService.register(createUser)
                employeeRepository.createEmployee(employee)
                val loginLink = "http://example.com/login"
                 val emailBody = """
    Welcome to [Your Company Name]!

    Here are your login details:
    Email: ${employee.email}
    Password: ${password}

    Please log in to your account using the link below within the next 12 hours:
    $loginLink

    For security reasons, we recommend changing your password after your first login.

    If you have any questions or need assistance, please contact our support team at support@example.com.

    Best regards,
    The [Your Company Name] Team
""".trimIndent()
                emailService.sendEmail(employee.email, "Welcome to TMS! Your New Account Created", emailBody)
                return employee
            } else {
                throw RuntimeException("Failed to create user")
             }
        }
        } catch (ex: Exception) {
            print(ex.message)
            throw Exception("Failed to create user")
        }
    }

    fun getClientByClientId(headers: HttpHeaders, clientId: String): ClientRepresentation? {
        val restTemplate = RestTemplate()

        val responseEntity = restTemplate.exchange(
            "$authServerUrl/admin/realms/TMS/clients?clientId=$clientId",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            Array<ClientRepresentation>::class.java
        )

        return responseEntity.body?.firstOrNull()
    }

    fun extractUserId(response: ResponseEntity<String>): String {
        // Logic to extract userId from the response, e.g., parsing the URL or response body
        return response.headers.location?.path?.split("/")?.last()
            ?: throw RuntimeException("User ID extraction failed")
    }

    fun getIdByUsername(username: String, headers: HttpHeaders): String? {
        val restTemplate = RestTemplate()

        val url = "$authServerUrl/admin/realms/TMS/users?username=$username"

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            object : ParameterizedTypeReference<List<Map<String, Any>>>() {}
        )

        return response.body?.firstOrNull()?.get("id") as? String
    }


    fun updateEmployee(id: String, employee: Employee, updateDTO: UserUpdateDTO, headers: HttpHeaders): Employee {
        return try {
            val keycloak_id = updateDTO.username.let { getKeycloakUserById(it, headers) }
            val existingUser = getEmployeeById(id, headers) ?: throw Exception("Employee not found")

            val user = UserRepresentation().apply {
                email = updateDTO.email
                firstName = updateDTO.firstName
                isEnabled = updateDTO.enabled?: true

                if(updateDTO.credentials != null) {
                    credentials = updateDTO.credentials.map { credentialDTO ->
                        CredentialRepresentation().apply {
                            type = credentialDTO.type
                            value = credentialDTO.value
                            isTemporary = credentialDTO.temporary?: true
                        }
                    }
                }

                if (updateDTO.attributes != null) {
                    for (attribute in updateDTO.attributes) {
                        val requiredRoles = attribute.required?.roles
                        if (requiredRoles != null && requiredRoles.isNotEmpty()) {
                            println("Required roles: $requiredRoles")
                        }
                    }
                }
            }
            val keycloakId = keycloak_id.id
            val request = org.springframework.http.HttpEntity(user, headers)
            val response = restTemplate.exchange(
                "$authServerUrl/admin/realms/TMS/users/$keycloakId",
                org.springframework.http.HttpMethod.PUT,
                request,
                object : ParameterizedTypeReference<List<UserRepresentation>>() {}
            )

            val clientid = getClientByClientId(headers, updateDTO.serviceAccountClientId ?: "Employee")?.id
                ?: throw RuntimeException("Client not found")

            val res = listOf(employee.role).let { roles -> // Convert role to a List<String>
                val roleResponse = restTemplate.exchange(
                    "$authServerUrl/admin/realms/TMS//users/$keycloakId/role-mappings/clients/$clientid",
                    HttpMethod.GET,
                    HttpEntity<Void>(headers),
                    object : ParameterizedTypeReference<List<RoleRepresentation>>() {}  // Expecting a list
                )

                val rolesToRemove = roleResponse.body?.filter { role -> role.name !in roles }

                if (!rolesToRemove.isNullOrEmpty()) {
                    val removeRoleRequest = HttpEntity(rolesToRemove, headers)
                    restTemplate.exchange(
                        "$authServerUrl/admin/realms/TMS/users/$keycloakId/role-mappings/clients/$clientid",
                        HttpMethod.DELETE,
                        removeRoleRequest,
                        Void::class.java
                    )
                }

                val roleMappings = roles.map { roleName ->
                    val roleResponse = restTemplate.exchange(
                        "$authServerUrl/admin/realms/TMS/clients/$clientid/roles/$roleName",
                        HttpMethod.GET,
                        HttpEntity<Void>(headers),
                        RoleRepresentation::class.java
                    )
                    roleResponse.body ?: throw RuntimeException("Role $roleName not found")
                }

                val roleRequest = HttpEntity(roleMappings, headers)
                restTemplate.postForEntity(
                    "$authServerUrl/admin/realms/TMS/users/$keycloakId/role-mappings/clients/$clientid",
                    roleRequest,
                    String::class.java
                )
            }


            if(res.statusCode.is2xxSuccessful) {
                val updatedRows = id.let { employeeRepository.updateEmployee(it, employee) } ?: throw Exception("Employee not found")
                if(updatedRows > 0) {
                    employeeRepository.getEmployeeById(id)?: throw Exception("Employee not found")
                } else {
                    throw Exception("Employee not found")
                }
            } else {
                throw Exception("Failed to update employee")
            }

        } catch (ex: Exception) {
            throw Exception("Employee not found")
        }
    }

    fun deleteEmployee(id: String, headers: HttpHeaders): Boolean {
        headers.contentType = MediaType.APPLICATION_JSON

        val username = employeeRepository.getEmployeeById(id)
        val keycloak_id = username?.let { getIdByUsername(it.email, headers) }
        if(keycloak_id == null) {
            throw Exception("Employee not found")
        }
        val response = restTemplate.exchange(
            "$authServerUrl/admin/realms/TMS/users/$keycloak_id",
            HttpMethod.DELETE,
            HttpEntity<Void>(headers),
            String::class.java
        )

        if(response.statusCode.is2xxSuccessful) {
            return employeeRepository.deleteEmployee(id)
        }
        throw Exception("Failed to delete employee")
    }

    fun deactivateEmployee(id: String, headers: HttpHeaders): Employee {
        val employee = employeeRepository.getEmployeeById(id) ?: throw ChangeSetPersister.NotFoundException()

        if (employee.status == "inactive") {
            throw IllegalStateException("Employee is already inactive")
        }

        headers.contentType = MediaType.APPLICATION_JSON
        val username = employeeRepository.getEmployeeById(id)?.email
        val keycloak_id = username?.let { getIdByUsername(it, headers) }

        val keycloakUpdate = keycloak_id?.let {
            deactivationDTO (
                id = it,
                enabled = false,
            )
        }

        val response = restTemplate.exchange(
            "$authServerUrl/admin/realms/TMS/users/$keycloak_id",
            HttpMethod.PUT,
            HttpEntity(keycloakUpdate, headers),
            String::class.java
        )

        if(response.statusCode.is2xxSuccessful) {
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
        throw Exception("Failed to deactivate employee")
    }

    fun sendResetPasswordEmail(email: String, headers: HttpHeaders): String {
        val keycloak_id = getKeycloakUserById(email, headers).id
        val actions = listOf("UPDATE_PASSWORD")

        val request = HttpEntity(actions, headers)
        val response = restTemplate.exchange(
            "$authServerUrl/admin/realms/TMS/users/$keycloak_id/execute-actions-email",
            HttpMethod.PUT,
            request,
            String::class.java
        )

        if(response.statusCode.is2xxSuccessful) {
            return "Email Sent"
        }
        throw Exception("Failed to send reset password email")
    }

    fun resetPassword(password: String, confirmPassword: String, temporary: Boolean, email: String, headers: HttpHeaders): String {
        val keycloakId = getKeycloakUserById(email, headers).id
        val credentialRepresentation = CredentialRepresentation().apply {
            type = "password"
            value = password
            isTemporary = temporary
        }
        val request = HttpEntity(credentialRepresentation, headers)  // Remove the listOf()

        val response = restTemplate.exchange(
            "$authServerUrl/admin/realms/TMS/users/$keycloakId/reset-password",
            HttpMethod.PUT,
            request,
            String::class.java
        )
        return if (response.statusCode.is2xxSuccessful) {
            authRepository.updateUserPassword(password, email)
        } else {
            throw Exception("Failed to reset password")
        }
    }
}