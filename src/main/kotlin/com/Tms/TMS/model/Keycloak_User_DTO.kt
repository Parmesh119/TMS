package com.Tms.TMS.model

data class Keycloak_User_DTO(
    val username: String? = null,
    val email: String? = null, // Made nullable with default value
    val firstName: String? = null, // Made nullable with default value
    val lastName: String? = null, // Made nullable with default value
    val enabled: Boolean? = true,
    val emailVerified: Boolean? = true,
    val requiredActions: List<String>? = null,
    val serviceAccountClientId: String? = null,
    val credentials: List<CredentialDTO>? = null,
    val groups: List<String>? = null,
    val clientRoles: Map<String, List<String>>? = null,
    val attributes: List<AttributeDTO>? = null,
)


data class AttributeDTO(
    val required: RequiredRolesDTO?
)

data class RequiredRolesDTO(
    val roles: List<String>?
)

data class CredentialDTO(
    val type: String = "1234",
    val value: String,
    val temporary: Boolean = true
)

data class UserUpdateDTO(
    val id: String? = null,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String? = null,
    val enabled: Boolean? = true,
    val serviceAccountClientId: String? = null,
    val credentials: List<CredentialDTO>? = null,
    val groups: List<String>? = null,
    val clientRoles: Map<String, List<String>>? = null,
    val attributes: List<AttributeDTO>? = null,
)

data class deactivationDTO(
    val id: String,
    val enabled: Boolean? = true
)

data class EmailRequest (
    val email: String
)

data class ResetPasswordDTO(
    val redirectUri: String,
    val clientId: String
)

data class PasswordResetRequest (
    val email: String,
    val password: String? = "1234",
    val confirmPassword: String? = "1234",
    val temporary: Boolean? = false
)

data class RefreshRequest(
    val refresh_token: String
)