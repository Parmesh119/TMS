package com.Tms.TMS.model

import java.time.LocalDateTime
import java.util.*

data class deliveryorder (
    val id: String? , // will contain do_number
    val contractId: String?,
    val partyId: String?,
    val partyName: String?,
    val dateOfContract: Long?,
    val status: String,
    val created_at: LocalDateTime?,
    val grandTotalQuantity: Int? = 0,
    val grandTotalPendingQuantity: Int? = 0,
    val grandTotalDeliveredQuantity: Int? = 0,
    val deliveryOrderSections: List<deliveryOrderSections>?,
)

data class deliveryOrderSections (
    val district: String?,
    val totalQuantity: Int = 0,
    val totalPendingQuantity: Int = 0,
    val totalDeliveredQuantity: Int = 0,
    val status: String = "pending",
    val deliveryOrderItems: List<deliveryOrderItems> = emptyList()
)

data class deliveryOrderItems (
    val id: String? = UUID.randomUUID().toString(),
    val deliveryOrderId: String?,
    val district: String?,
    val taluka: String?,
    val locationId: String?,
    val materialId: String?,
    val quantity: Int,
    val pendingQuantity: Int? = 0,
    val deliveredQuantity: Int? = 0,
    val status: String = "pending",
    val rate: Float?,
    val unit: String?,
    val dueDate: Long?,
    val associatedDeliveryChallanItems: List<AssociatedDeliverChallanItemMetadata> = emptyList()
)

data class DeliverOrderItemMetadata(
    val id: String,
    val district: String,
    val taluka: String,
    val locationName: String,
    val materialName: String,
    val quantity: Double,
    val rate: Double?,
    val dueDate: Long?,
    var deliveredQuantity: Double = 0.0,
)

data class deliveryOrderInput (
    val page: Int = 1,
    val partyIds: List<String> = emptyList(),
    val search: String = "",
    val size: Int = 10,
    val statuses: List<String> = emptyList(),
    val fromDate: Long? = null,
    val toDate: Long? = null
)

data class ListDeliveryOrderItem(
    val id: String,
    val contractId: String?,
    val partyName: String?,
    val status: String?,
    val dateOfContract: Long?
)

data class DeliveryOrderExportData(
    val do_number: String,
    val totalQuantity: Int,
    val totalDelivered: Int,
    val clientContactNumber: String?,
    val partyName: String?,
    val dateOfContract: Long?,  // Updated to be nullable
    val items: List<DeliveryOrderItemExportData>,
    val challans: List<DeliveryChallanExportData>
)

data class DeliveryOrderItemExportData(
    val district: String,
    val taluka: String,
    val locationName: String?,
    val materialName: String?,
    val quantity: Int,
    val deliveredQuantity: Int,
    val rate: Double,
    val dueDate: Long?,  // Updated to be nullable
    val status: String
)

data class DeliveryChallanExportData(
    val dc_number: String,
    val dateOfChallan: Long?,  // Updated to be nullable
    val quantity: Double
)

data class AssociatedDeliverChallanItemMetadata(
    val id: String,
    val deliveringQuantity: Double = 0.0,
    val deliveryChallanId: String
)