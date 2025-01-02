package com.Tms.TMS.controller

import com.Tms.TMS.model.deliveryorder
import com.Tms.TMS.service.DeliveryOrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/api/v1/delivery-orders")
class DeliveryOrderController(private val deliveryOrderService: DeliveryOrderService) {
    // Delivery Order Controller operations

    // List all delivery orders
    @PostMapping("/list")
    fun listAllDeliveryOrders(
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("size", defaultValue = "2") size: Int,
        @RequestParam("sortField", defaultValue = "created_at") sortField: String,
        @RequestParam("sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<List<deliveryorder>> {
        // Implement logic to list all delivery orders
        return try {
            ResponseEntity.ok(deliveryOrderService.listAllDeliveryOrder(page, size, sortField, sortOrder))
        } catch (ex: Exception) {
            throw ex
        }
    }

    // get by id
    @GetMapping("/get/{id}")
    fun getDeliveryOrderById(@PathVariable id: String): ResponseEntity<deliveryorder> {
        // Implement logic to get delivery order by id
        return try {
            ResponseEntity.ok(deliveryOrderService.getDeliveryOrderById(id))
        } catch (ex: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    // create delivery order
    @PostMapping("/create")
    fun createDeliveryOrder(@RequestBody orderRequest: deliveryorder): ResponseEntity<deliveryorder> {
        // Implement logic to create a new delivery order
        val deliveryOrderSections = orderRequest.deliveryOrderSections ?: emptyList();
        return try {
            ResponseEntity.ok(deliveryOrderService.createDeliveryOrder(orderRequest, deliveryOrderSections).body)
        } catch (ex: Exception) {
            ResponseEntity.internalServerError().body(orderRequest)
        }
    }

    // update delivery order
    @PostMapping("/update")
    fun updateDeliveryOrder(@RequestBody orderRequest: deliveryorder): ResponseEntity<deliveryorder> {
        // Implement logic to update an existing delivery order
        val deliveryOrderSections = orderRequest.deliveryOrderSections ?: emptyList();
        return try {
            val updatedRows = deliveryOrderService.updateDeliveryOrder(orderRequest, deliveryOrderSections)
            if (updatedRows != null) {
                ResponseEntity.ok(orderRequest)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (ex: Exception) {
            ResponseEntity.internalServerError().body(orderRequest)
        }
    }

    // delete delivery order by id
    @DeleteMapping("/delete/{id}")
    fun deleteDeliveryOrderById(@PathVariable id: String): ResponseEntity<String> {
        // Implement logic to delete delivery order by id
        return try {
            val deliveryOrder = deliveryOrderService.deleteOrder(id)
            if (deliveryOrder < 0) {
                return ResponseEntity.notFound().build()
            } else {
                ResponseEntity.ok("Delivery order deleted successfully")
            }
        } catch (ex: Exception) {
            ResponseEntity.internalServerError().body("An error occurred: ${ex.message}")
        }
    }
}