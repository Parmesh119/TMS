package com.Tms.TMS.controller

import com.Tms.TMS.model.DeliverOrderItemMetadata
import com.Tms.TMS.model.ListDeliveryOrderItem
import com.Tms.TMS.model.deliveryOrderInput
import com.Tms.TMS.model.deliveryorder
import com.Tms.TMS.service.DeliveryOrderService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    @GetMapping("/download-all-excel/{do_number}")
    fun exportAllFiles(@PathVariable do_number: String): ResponseEntity<ByteArray> {
        val files = deliveryOrderService.generateAllExcelFiles(do_number)

        // Create DO-specific directory
        val doDir = File("exports/$do_number")
        if (!doDir.exists()) {
            doDir.mkdirs()
        }

        // Create ZIP file containing all Excel files
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)

        files.forEach { (filename, data) ->
            // Save individual file
            val file = File(doDir, filename)
            file.writeBytes(data)

            // Add to ZIP
            zos.putNextEntry(ZipEntry(filename))
            zos.write(data)
            zos.closeEntry()
        }

        zos.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=DO_${do_number}_all_files.zip")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(baos.toByteArray())
    }
}