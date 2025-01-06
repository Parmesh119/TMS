package com.Tms.TMS.controller

import com.Tms.TMS.model.DeliveryChallan
import com.Tms.TMS.model.DeliveryChallanListRequest
import com.Tms.TMS.service.DeliveryChallanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/api/v1/delivery-challans")
class DeliveryChallanController(private val deliveryChallanService: DeliveryChallanService) {

    // Delivery Challan Controller operations
    // Create a new delivery challan
    @GetMapping("/create/from-delivery-order/{deliveryOrderId}")
    fun createDeliveryChallan(@PathVariable deliveryOrderId: String): ResponseEntity<DeliveryChallan> {
        // Implement logic to create a new delivery challan
        return ResponseEntity.ok(deliveryChallanService.createDeliveryChallan(deliveryOrderId))
    }

    @GetMapping("/get/{id}")
    fun getDeliveryChallanById(@PathVariable id: String): ResponseEntity<DeliveryChallan> {
        // Implement logic to get delivery challan by id
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallanById(id))
    }

    @PostMapping("/update")
    fun updateDeliveryChallan(@RequestBody deliveryChallan: DeliveryChallan): ResponseEntity<DeliveryChallan> {
        // Implement logic to update an existing delivery challan
        return ResponseEntity.ok(deliveryChallanService.updateDeliveryChallan(deliveryChallan))
    }

    @PostMapping("/list")
    fun listDeliveryChallans(
        @RequestBody request: DeliveryChallanListRequest
    ): ResponseEntity<List<DeliveryChallan>> {
        return ResponseEntity.ok(
            deliveryChallanService.listDeliveryChallans(
                request.page ?: 1,
                request.size ?: 10,
                request.sortField ?: "created_at",
                request.sortOrder ?: "desc"
            )
        )
    }
}