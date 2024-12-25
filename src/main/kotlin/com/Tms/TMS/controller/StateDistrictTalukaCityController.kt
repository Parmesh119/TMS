package com.Tms.TMS.controller

import com.Tms.TMS.Model.StateDistrictTalukaCities
import com.Tms.TMS.Service.StateDTC
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
@CrossOrigin
class StateDistrictTalukaCityController(
    private val stateDistrictTalukaCityService: StateDTC
) {
    @GetMapping("/states/list")
    fun listStates(): List<String> {
        return stateDistrictTalukaCityService.listStates()
    }

    @PostMapping("/districts/list")
    fun listDistricts(@RequestBody stateRequest: StateDistrictTalukaCities.StateRequest): List<String> {
        return stateDistrictTalukaCityService.listDistricts(stateRequest)
    }

    @PostMapping("/talukas/list")
    fun listTalukas(@RequestBody districtRequest: StateDistrictTalukaCities.DistrictRequest): List<String> {
        return stateDistrictTalukaCityService.listTalukas(districtRequest)
    }

    @PostMapping("/cities/list")
    fun listCities(@RequestBody cityRequest: StateDistrictTalukaCities.CityRequest): List<String> {
        return stateDistrictTalukaCityService.listCities(cityRequest)
    }
}