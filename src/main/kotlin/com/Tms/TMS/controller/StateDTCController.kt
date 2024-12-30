package com.Tms.TMS.controller

import com.Tms.TMS.model.DTO.StateDTCRequest
import com.Tms.TMS.service.StateDTCService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
class StateDTCController(private val stateDTCService: StateDTCService) {

    @GetMapping("/stateDTC/state/list")
    fun listStates()
    : ResponseEntity<List<String>>
    {
        return ResponseEntity.ok(stateDTCService.listStates())
    }

    @PostMapping("/stateDTC/district/list")
    fun listDistricts(@RequestBody districtFilterRequest: StateDTCRequest.DistrictFilterRequest): ResponseEntity<List<String>> {
        return ResponseEntity.ok(stateDTCService.listDistricts(districtFilterRequest))
    }


    @PostMapping("/stateDTC/taluka/list")
    fun listTalukas(@RequestBody talukaFilterRequest: StateDTCRequest.TalukaFilterRequest): ResponseEntity<List<String>> {
        return ResponseEntity.ok(stateDTCService.listTalukas(talukaFilterRequest))
    }

    @PostMapping("/stateDTC/city/list")
    fun listCities(@RequestBody CityFilterRequest: StateDTCRequest.CityFilterRequest): ResponseEntity<List<String>> {
        return ResponseEntity.ok(stateDTCService.listCities(CityFilterRequest))
    }
}