package com.Tms.TMS.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RequestMapping("/api/v1/units")
@RestController
class UnitController {

    @GetMapping("/list")
    @Operation(
        summary = "Get user by ID",
        description = "Returns a user based on the ID"
    )
    @ApiResponses(value = [
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user"
        ),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    ])
    fun listUnits(): ResponseEntity<List<String>> {
        val units = listOf("MT", "Kg", "Units", "Ltr")
        return ResponseEntity.ok(units)
    }
}