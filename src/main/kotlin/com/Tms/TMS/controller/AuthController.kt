    package com.Tms.TMS.controller

    import com.Tms.TMS.model.AuthResponse
    import com.Tms.TMS.model.LoginRequest
    import com.Tms.TMS.model.RefreshRequest
    import com.Tms.TMS.model.User
    import com.Tms.TMS.service.AuthService
    import org.springframework.http.ResponseEntity
    import org.springframework.web.bind.annotation.*


    @CrossOrigin
    @RestController
    @RequestMapping("/api/v1/auth")
    class AuthController(private val authService: AuthService) {

        @PostMapping("/login")
        fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
            return ResponseEntity.ok(authService.login(loginRequest.email, loginRequest.password))
        }

        @PostMapping("/refresh")
        fun refresh(@RequestBody refreshRequest: RefreshRequest): ResponseEntity<Any> {
            return ResponseEntity.ok(authService.refresh(refreshRequest.refreshToken))
        }
    }