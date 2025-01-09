package com.Tms.TMS.model

import java.time.LocalDateTime
import java.util.*

data class DeliveryChallan (
    val id: String? = UUID.randomUUID().toString(),
    val deliveryOrderId: String?,
    val status: String?,
    val created_at: Long?,
    val updated_at: Long?,
    val dateOfChallan: Long?,
    val totalDeliveringQuantity: Double = 0.0,
    val partyName: String?,
    val deliveryChallanItems: List<DeliveryChallanItems> = emptyList(),
    val transportationCompanyId: String?,
    val vehicleId: String?,
    val driverId: String?,
)

data class DeliveryChallanItems (
    val id: String? = UUID.randomUUID().toString(),
    val deliveryChallanId: String?,
    val deliveryOrderItemId: String?,
    val district: String,
    val taluka: String,
    val locationName: String?,
    val materialName: String?,
    val quantity: Double = 0.0,
    val rate: Double = 0.0,
    val dueDate: Long?,
    val deliveringQuantity: Double = 0.0,
)

data class DeliveryChallanListRequest(
    val page: Int?,
    val size: Int?,
    val sortField: String?,
    val sortOrder: String?,
    val deliveryOrderIds: List<String> = emptyList(),
)