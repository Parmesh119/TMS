package com.Tms.TMS.service

import com.Tms.TMS.model.DeliverOrderItemMetadata
import com.Tms.TMS.model.ListDeliveryOrderItem
import com.Tms.TMS.model.deliveryorder
import com.Tms.TMS.model.deliveryOrderSections
import com.Tms.TMS.repository.DeliveryOrderItemRepository
import com.Tms.TMS.repository.DeliveryOrderRepository
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class DeliveryOrderService(
    private val deliveryOrderRepository: DeliveryOrderRepository,
    private val deliveryOrderItemRepository: DeliveryOrderItemRepository
) {
    // Delivery Order Service operations

    // List all delivery orders
    fun listAllDeliveryOrder(
        search: String,
        page: Int,
        size: Int,
        status: List<String>,
        partyId: List<String>,
        fromDate: Long?,
        toDate: Long?
    ): List<ListDeliveryOrderItem> {
        return deliveryOrderRepository.findAll(
            search = search,
            page = page,
            size = size,
            status = status,
            partyId = partyId,
            fromDate = fromDate,
            toDate = toDate
        ).map { item ->
            val deliveryOrder = deliveryOrderRepository.findById(item.id)
                ?: throw RuntimeException("Delivery order not found")
            item.copy(
                grandTotalDeliveredQuantity = deliveryOrder.grandTotalDeliveredQuantity,
                grandTotalQuantity = deliveryOrder.grandTotalQuantity
            )
        }
    }

    // get by id
    fun getDeliveryOrderById(id: String): deliveryorder? {
        return deliveryOrderRepository.findById(id)
    }

    // create delivery order
    fun createDeliveryOrder(orderRequest: deliveryorder, deliveryOrderSections: List<deliveryOrderSections>): deliveryorder {
        val nextDoNumber = generateNextDoNumber()
        val deliveryOrderWithDoNumber = orderRequest.copy(id = nextDoNumber)

        val createdOrder = deliveryOrderRepository.create(deliveryOrderWithDoNumber)
        val itemsToSave = deliveryOrderSections.flatMap { section ->
            section.deliveryOrderItems?.map { item ->
                item.copy(deliveryOrderId = nextDoNumber)
            } ?: emptyList()
        }
        deliveryOrderItemRepository.saveAll(nextDoNumber, itemsToSave)
        return createdOrder
    }

    // update delivery order
    fun updateDeliveryOrder(orderRequest: deliveryorder, sections: List<deliveryOrderSections>): deliveryorder {
        val updatedOrder = deliveryOrderRepository.update(orderRequest)
        val itemsToSave = sections.flatMap { section ->
            section.deliveryOrderItems?.map { item ->
                item.copy(deliveryOrderId = orderRequest.id) // Associate with the deliveryOrderId
            } ?: emptyList()
        }

        deliveryOrderItemRepository.syncItems(itemsToSave, orderRequest.id!!)

        val updatedDeliveryOrder = deliveryOrderRepository.findById(orderRequest.id!!)
            ?: throw RuntimeException("Delivery order not found")

        return updatedDeliveryOrder.copy(
            grandTotalQuantity = updatedDeliveryOrder.grandTotalQuantity,
            grandTotalDeliveredQuantity = updatedDeliveryOrder.grandTotalDeliveredQuantity
        )
    }

    fun deleteOrder(id: String): Int {
        return deliveryOrderRepository.deleteById(id)
    }

    fun listDeliveryOrderItemsForDeliveryOrderId(deliveryOrderId: String): List<DeliverOrderItemMetadata> {
        return deliveryOrderRepository.listDeliverOrderItemMetadata(deliveryOrderId)
    }

    fun generateNextDoNumber(): String {
        val lastDoNumber = deliveryOrderRepository.getLastDoNumber()
        val nextNumber = if (lastDoNumber == null) {
            1
        } else {
            val lastNumber = lastDoNumber.substring(3).toIntOrNull() ?: 0
            lastNumber + 1
        }
        return "DO_" + String.format("%04d", nextNumber)
    }

    fun generateCsvFile(doNumber: String): ByteArray {
        val deliveryOrderData = deliveryOrderRepository.getDeliveryOrderWithDetails(doNumber)
            ?: throw RuntimeException("Delivery order not found")

        val csvBuilder = StringBuilder()

        // Header section
        csvBuilder.appendLine("DO Number,${deliveryOrderData.do_number}")
        csvBuilder.appendLine("Total Quantity,${deliveryOrderData.totalQuantity}")
        csvBuilder.appendLine("Total Delivered,${deliveryOrderData.totalDelivered}")
        csvBuilder.appendLine("Client Contact Number,${deliveryOrderData.clientContactNumber}")
        csvBuilder.appendLine("Party,${deliveryOrderData.partyName}")
        csvBuilder.appendLine("Date Of Contract,${formatDate(deliveryOrderData.dateOfContract)}")
        csvBuilder.appendLine()

        // Delivery Order Items section
        csvBuilder.appendLine("Delivery Order Items")
        csvBuilder.appendLine("Sr No,Taluka,Location,Material,Quantity,Delivered Quantity,Rate,Due Date,Status")

        var srNo = 1
        var currentDistrict: String? = null
        var districtQuantity: Double = 0.0
        var districtDeliveredQuantity: Double = 0.0

        deliveryOrderData.items.forEach { item ->
            if (currentDistrict != item.district) {
                if (currentDistrict != null) {
                    // Print district total
                    csvBuilder.appendLine("Total For District: $currentDistrict,$districtQuantity,$districtDeliveredQuantity")
                    csvBuilder.appendLine()
                }
                currentDistrict = item.district
                districtQuantity = item.quantity
                districtDeliveredQuantity = item.deliveredQuantity
                srNo = 1
            }

            csvBuilder.appendLine(
                "${srNo},${item.taluka},${item.locationName},${item.materialName}," +
                        "${item.quantity},${item.deliveredQuantity},${item.rate}," +
                        "${formatDate(item.dueDate)},${item.status}"
            )
            districtQuantity += item.quantity
            districtDeliveredQuantity += item.deliveredQuantity
            srNo++
        }

        // Print last district total
        currentDistrict?.let {
            csvBuilder.appendLine("Total For District: $it,$districtQuantity,$districtDeliveredQuantity")
            csvBuilder.appendLine()
        }

        // Delivery Challan section
        csvBuilder.appendLine("Delivery Challan")
        csvBuilder.appendLine("Sr No,Id,Date,Quantity")
        deliveryOrderData.challans.forEachIndexed { index, challan ->
            csvBuilder.appendLine(
                "${index + 1},${challan.dc_number},${formatDate(challan.dateOfChallan)}," +
                        "${challan.quantity}"
            )
        }

        val csvData = csvBuilder.toString().toByteArray(Charsets.UTF_8)

        // Create exports directory if it doesn't exist
        val exportDir = File("exports")
        if (!exportDir.exists()) {
            exportDir.mkdir()
        }

        // Save the file in the exports directory
        val fileName = "delivery_order_${doNumber}.csv"
        val file = File(exportDir, fileName)
        file.writeBytes(csvData)

        return csvData
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""

        return try {
            val instant = Instant.ofEpochMilli(timestamp)
            val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            date.format(DateTimeFormatter.ofPattern("dd/MMM/yyyy"))
        } catch (e: Exception) {
            ""
        }
    }
}