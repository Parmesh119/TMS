package com.Tms.TMS.service

import com.Tms.TMS.model.Transpotation
import com.Tms.TMS.repository.TransportationCompanyRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class TransportationCompanyService(
    private val transportationCompanyRepository: TransportationCompanyRepository // inject the repository here
) {
    // Define user endpoints here

    // Get transportation company by id
    fun getTransportationCompanyById(id: String): Transpotation {
        return transportationCompanyRepository.getTransportationCompanyById(id) ?: throw Exception("Transportation company not found")
    }

    // Create transportation company
    fun createTransportationCompany(transpotation: Transpotation): Transpotation {
        val transportationCompany = transpotation.copy(
            created_at = Instant.now().epochSecond,
            updated_at = Instant.now().epochSecond,
            vehicles = transpotation.vehicles,
            drivers = transpotation.drivers
        )
        return transportationCompanyRepository.createTransportationCompany(transportationCompany)
    }

    // Update transportation company
    fun updateTransportationCompany(transpotation: Transpotation): Transpotation {
        if (transpotation.id == null) {
            throw IllegalArgumentException("Transportation company ID is required")
        }
        val transportCompanyUpdate = transpotation.copy(
            updated_at = Instant.now().epochSecond
        )
        return transportationCompanyRepository.updateTransportationCompany(transportCompanyUpdate)
    }

    // List transportation companies
    fun listTransportationCompanies(
        search: String,
        statuses: List<String>,
        page: Int,
        size: Int,
        getAll: Boolean
    ): List<Transpotation> {
        return transportationCompanyRepository.listTransportationCompanies(search = search, status = statuses, page = page, size = size, getAll = getAll)
    }
}