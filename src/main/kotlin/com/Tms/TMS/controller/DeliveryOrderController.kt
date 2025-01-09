package com.Tms.TMS.controller

import com.Tms.TMS.model.DeliverOrderItemMetadata
import com.Tms.TMS.model.ListDeliveryOrderItem
import com.Tms.TMS.model.deliveryOrderInput
import com.Tms.TMS.model.deliveryorder
import com.Tms.TMS.service.DeliveryOrderService
import org.springframework.http.HttpHeaders
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
        @RequestBody listInput: deliveryOrderInput
    ): ResponseEntity<List<ListDeliveryOrderItem>> {
        return try {
            ResponseEntity.ok(
                deliveryOrderService.listAllDeliveryOrder(
                    search = listInput.search,
                    page = listInput.page,
                    size = listInput.size,
                    status = listInput.statuses,
                    partyId = listInput.partyIds,
                    fromDate = listInput.fromDate,
                    toDate = listInput.toDate
                )
            )
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
           throw ex
        }
    }

    // create delivery order
    @PostMapping("/create")
    fun createDeliveryOrder(@RequestBody orderRequest: deliveryorder): ResponseEntity<deliveryorder> {
        val deliveryOrderSections = orderRequest.deliveryOrderSections ?: emptyList()
        return try {
            val createdOrder = deliveryOrderService.createDeliveryOrder(orderRequest, deliveryOrderSections)
            ResponseEntity.ok(createdOrder)
        } catch (ex: Exception) {
            throw ex
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

    @GetMapping("/list/delivery-order-items/{deliveryOrderId}")
    fun listDeliveryOrderItems(@PathVariable deliveryOrderId: String): ResponseEntity<List<DeliverOrderItemMetadata>> {
        return ResponseEntity.ok(deliveryOrderService.listDeliveryOrderItemsForDeliveryOrderId(deliveryOrderId))
    }

    @GetMapping("/download-csv/{do_number}")
    fun exportDeliveryOrder(@PathVariable do_number: String): ResponseEntity<ByteArray> {
        val csvData = deliveryOrderService.generateCsvFile(do_number)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=delivery_order_$do_number.csv")
            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
            .body(csvData)
    }
}