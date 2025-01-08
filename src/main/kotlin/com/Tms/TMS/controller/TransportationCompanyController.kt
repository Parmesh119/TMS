package com.Tms.TMS.controller

import com.Tms.TMS.model.ListTransportationCompaniesInput
import com.Tms.TMS.model.Transpotation
import com.Tms.TMS.service.TransportationCompanyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transportation-companies")
class TransportationCompanyController(
    private val transportationCompanyService: TransportationCompanyService // inject the service here
) {

    // Define user endpoints here

    // Get transportation company by id
    @GetMapping("/get/{id}")
    fun getTransportationCompanyById(@PathVariable id: String): ResponseEntity<Transpotation> {
        return ResponseEntity.ok(transportationCompanyService.getTransportationCompanyById(id))
    }

    // Create transportation company
    @PostMapping("/create")
    fun createTransportationCompany(@RequestBody transpotation: Transpotation): ResponseEntity<Transpotation> {
        return ResponseEntity.ok(transportationCompanyService.createTransportationCompany(transpotation))
    }

    // Update transportation company
    @PostMapping("/update")
    fun updateTransportationCompany(@RequestBody transpotation: Transpotation): ResponseEntity<Transpotation> {
        return ResponseEntity.ok(transportationCompanyService.updateTransportationCompany(transpotation))
    }

    @PostMapping("/list")
    fun listTransportationCompanies(@RequestBody listRequest: ListTransportationCompaniesInput): ResponseEntity<List<Transpotation>> {
        return ResponseEntity.ok(transportationCompanyService.listTransportationCompanies(listRequest.search, listRequest.statuses, listRequest.page, listRequest.size, listRequest.getAll))
    }
}