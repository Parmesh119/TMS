package com.Tms.TMS.model

import java.time.LocalDateTime
import java.util.*

data class DeliveryChallan (
    val id: String,
    val deliveryOrderId: String?,
    val status: String?,
    val created_at: Long?,
    val updated_at: Long?,
    val dateOfChallan: Long?,
    val totalDeliveringQuantity: Double = 0.0,
    val partyName: String?,
    val deliveryChallanItems: List<DeliveryChallanItems> = emptyList(),
    val transportationCompanyId: String?,
    val transportationCompanyName: String? = null,
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
    val deliveredQuantity: Double = 0.0,
    val rate: Double = 0.0,
    val dueDate: Long?,
    val deliveringQuantity: Double = 0.0,
)

data class DeliveryChallanListRequest(
    val search: String? = null,
    val page: Int?,
    val size: Int?,
    val deliveryOrderIds: List<String> = emptyList(),
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val statuses: List<String> = emptyList(),
    val partyIds: List<String> = emptyList(),
    val transportationCompanyIds: List<String> = emptyList(),
    val getAll: Boolean = false,
)

data class DeliveryChallanOutputRecord(
    val id: String,
    val deliveryOrderId: String,
    val dateOfChallan: Long?,
    val status: String?,
    val partyName: String?,
    val transportationCompanyName: String?,
    val driverName: String?,
    val totalDeliveringQuantity: Double = 0.0,
)
