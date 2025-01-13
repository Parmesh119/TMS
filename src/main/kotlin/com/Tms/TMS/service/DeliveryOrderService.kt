package com.Tms.TMS.service

import com.Tms.TMS.model.*
import com.Tms.TMS.repository.DeliveryOrderItemRepository
import com.Tms.TMS.repository.DeliveryOrderRepository
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
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
        deliveryOrderRepository.update(orderRequest)
        val itemsToSave = sections.flatMap { section ->
            section.deliveryOrderItems?.map { item ->
                item.copy(deliveryOrderId = orderRequest.id) // Associate with the deliveryOrderId
            } ?: emptyList()
        }

        deliveryOrderItemRepository.syncItems(itemsToSave, orderRequest.id!!)

        val updatedDeliveryOrder = deliveryOrderRepository.findById(orderRequest.id)
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

    fun generateDoXlsxFile(doNumber: String): ByteArray {
        val deliveryOrderData = deliveryOrderRepository.getDeliveryOrderWithDetails(doNumber)
            ?: throw RuntimeException("Delivery order not found")

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Delivery Order")

        var rowNum = 0

        // Create cell styles
        val headerStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
            })
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Header section
        createRow(sheet, rowNum++, listOf("DO Number", deliveryOrderData.do_number))
        createRow(sheet, rowNum++, listOf("Total Quantity", deliveryOrderData.totalQuantity.toString()))
        createRow(sheet, rowNum++, listOf("Total Delivered", deliveryOrderData.totalDelivered.toString()))
        createRow(sheet, rowNum++, listOf("Client Contact Number", deliveryOrderData.clientContactNumber))
        createRow(sheet, rowNum++, listOf("Party", deliveryOrderData.partyName))
        createRow(sheet, rowNum++, listOf("Date Of Contract", formatDate(deliveryOrderData.dateOfContract)))
        rowNum++

        // Delivery Order Items section
        createRow(sheet, rowNum++, listOf("Delivery Order Items"), headerStyle)
        val itemHeaders = listOf("Sr No", "District", "Taluka", "Location", "Material",
            "Quantity", "Delivered Quantity", "Rate", "Due Date", "Status")
        createRow(sheet, rowNum++, itemHeaders, headerStyle)

        var srNo = 1
        var currentDistrict: String? = null
        var districtQuantity = 0.0
        var districtDeliveredQuantity = 0.0

        deliveryOrderData.items.forEach { item ->
            if (currentDistrict != item.district) {
                if (currentDistrict != null) {
                    createRow(sheet, rowNum++, listOf(
                        "Total For District: $currentDistrict",
                        "",
                        "",
                        "",
                        "",
                        districtQuantity.toString(),
                        districtDeliveredQuantity.toString()
                    ))
                    rowNum++
                }
                currentDistrict = item.district
                districtQuantity = 0.0
                districtDeliveredQuantity = 0.0
                srNo = 1
            }

            createRow(sheet, rowNum++, listOf(
                srNo.toString(),
                item.district,
                item.taluka,
                item.locationName,
                item.materialName,
                item.quantity.toString(),
                item.deliveredQuantity.toString(),
                item.rate.toString(),
                formatDate(item.dueDate),
                item.status
            ))

            districtQuantity += item.quantity
            districtDeliveredQuantity += item.deliveredQuantity
            srNo++
        }

        // Last district total
        currentDistrict?.let {
            createRow(sheet, rowNum++, listOf(
                "Total For District: $it",
                "",
                "",
                "",
                "",
                districtQuantity.toString(),
                districtDeliveredQuantity.toString()
            ))
        }
        rowNum++

        // Delivery Challan section
        createRow(sheet, rowNum++, listOf("Delivery Challan"), headerStyle)
        createRow(sheet, rowNum++, listOf("Sr No", "Id", "Date", "Quantity"), headerStyle)

        deliveryOrderData.challans.forEachIndexed { index, challan ->
            createRow(sheet, rowNum++, listOf(
                (index + 1).toString(),
                challan.dc_number,
                formatDate(challan.dateOfChallan),
                challan.quantity.toString()
            ))
        }

        // Auto-size columns
        (0..9).forEach { sheet.autoSizeColumn(it) }

        return ByteArrayOutputStream().use { out ->
            workbook.write(out)
            out.toByteArray()
        }
    }

    private fun createRow(sheet: Sheet, rowNum: Int, values: List<String?>, style: CellStyle? = null): Row {
        val row = sheet.createRow(rowNum)
        values.forEachIndexed { colNum, value ->
            val cell = row.createCell(colNum)
            cell.setCellValue(value)
            style?.let { cell.cellStyle = it }
        }
        return row
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""

        return try {
            val instant = Instant.ofEpochMilli(timestamp)
            val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (e: Exception) {
            ""
        }
    }

    fun generateAllExcelFiles(doNumber: String): Map<String, ByteArray> {
        val files = mutableMapOf<String, ByteArray>()

        // Generate main DO file

        files["delivery_order - $doNumber.xlsx"] = generateDoXlsxFile(doNumber)

        // Get all DCs for this DO
        val deliveryOrderData = deliveryOrderRepository.getDeliveryOrderWithDetails(doNumber)


        // Generate individual DC files
        deliveryOrderData?.let{
            it.challans.forEach { challan ->
                files["delivery_challan - ${challan.dc_number}.xlsx"] = generateDcXlsxFile(challan.dc_number)
            }
        }


        return files
    }

    private fun generateDcXlsxFile(dcNumber: String): ByteArray {
        val challanData = deliveryOrderRepository.getDeliveryChallanDetails(dcNumber)
            ?: throw RuntimeException("Delivery challan not found")

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Delivery Challan")

        var rowNum = 0

        val headerStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Header section
        createRow(sheet, rowNum++, listOf("DC Number", challanData.dc_number))
        createRow(sheet, rowNum++, listOf("DO Number", challanData.do_number))
        createRow(sheet, rowNum++, listOf("Date of Challan", formatDate(challanData.dateOfChallan)))
        createRow(sheet, rowNum++, listOf("Total Quantity", challanData.totalDeliveringQuantity.toString()))
        createRow(sheet, rowNum++, listOf("Transportation Company", challanData.transportationCompanyName ?: ""))

        // Added Driver, Vehicle Details
        createRow(sheet, rowNum++, listOf("Driver Name", challanData.driverName ?: ""))
        createRow(sheet, rowNum++, listOf("Vehicle Type", challanData.vehicleType ?: ""))
        createRow(sheet, rowNum++, listOf("Vehicle Number", challanData.vehicleNumber ?: ""))

        createRow(sheet, rowNum++, listOf("Status", challanData.status ?: ""))

        rowNum++

        // Items section
        createRow(sheet, rowNum++, listOf("Delivery Challan Items"), headerStyle)
        val itemHeaders = listOf("Sr No", "District", "Taluka", "Location", "Material",
            "Delivering Quantity", "Rate")
        createRow(sheet, rowNum++, itemHeaders, headerStyle)

        challanData.items.forEachIndexed { index, item ->
            createRow(sheet, rowNum++, listOf(
                (index + 1).toString(),
                item.district,
                item.taluka,
                item.locationName ?: "",
                item.materialName ?: "",
                item.deliveringQuantity.toString(),
                item.rate.toString()
            ))
        }

        // Auto-size columns
        (0..6).forEach { sheet.autoSizeColumn(it) }

        return ByteArrayOutputStream().use { out ->
            workbook.write(out)
            out.toByteArray()
        }
    }
}