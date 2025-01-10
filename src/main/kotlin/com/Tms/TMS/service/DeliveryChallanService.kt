package com.Tms.TMS.service

import com.Tms.TMS.model.*
import com.Tms.TMS.repository.DeliveryChallanRepository
import com.Tms.TMS.repository.DeliveryOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import java.util.UUID

@Service
class DeliveryChallanService(private val deliveryChallanRepository: DeliveryChallanRepository,
                             private val deliveryOrderRepository: DeliveryOrderRepository) {

    fun generateDeliveryChallanId(): String {
        val rowCount = deliveryChallanRepository.getDeliveryChallanCount()
        val nextId = rowCount?.plus(1)
        return String.format("DC_%04d", nextId)
    }

    fun createDeliveryChallan(do_id: String): DeliveryChallan {
        val deliveryOrder = deliveryOrderRepository.findById(do_id)

        if (deliveryOrder == null) {
            throw Exception("Delivery Order not found")
        }
        return try {
            val deliveryChallan = DeliveryChallan(
                id = generateDeliveryChallanId(),
                deliveryOrderId = do_id,
                status = "delivered",
                created_at = Instant.now().epochSecond,
                updated_at = Instant.now().epochSecond,
                dateOfChallan = Instant.now().epochSecond,
                totalDeliveringQuantity = 0.0,
                partyName = deliveryOrder.partyName,
                transportationCompanyId = null,
                transportationCompanyName = null,
                vehicleId = null,
                driverId = null
            )
            return deliveryChallanRepository.create(deliveryChallan)
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }

    fun getDeliveryChallanById(id: String): DeliveryChallan {
        return deliveryChallanRepository.findById(id) ?: throw Exception("Delivery Challan not found")
    }

    @Transactional
    fun updateDeliveryChallan(deliveryChallan: DeliveryChallan): DeliveryChallan {
        return try {
            if (deliveryChallan.id == null) {
                throw Exception("Delivery Challan ID is required")
            }

            val existingDeliveryChallan = deliveryChallanRepository.findById(deliveryChallan.id)
            if (existingDeliveryChallan == null) {
                throw Exception("Delivery Challan not found")
            }

            val updatedChallan = existingDeliveryChallan.copy(
                deliveryChallanItems = deliveryChallan.deliveryChallanItems.map { item ->
                    if (item.id == null) {
                        item.copy(
                            id = UUID.randomUUID().toString(),
                            deliveryChallanId = deliveryChallan.id
                        )
                    } else {
                        item
                    }
                }.map { item ->
                    val deliveryOrderItem = deliveryOrderRepository.findById(deliveryChallan.deliveryOrderId!!)?.deliveryOrderSections?.flatMap { it.deliveryOrderItems }
                        ?.find { it.id == item.deliveryOrderItemId }
                        ?: throw Exception("Delivery Order Item not found")

                    val updatedDeliveringQuantity = if(item.deliveringQuantity > deliveryOrderItem.quantity) {
                        deliveryOrderItem.quantity
                    } else {
                        item.deliveringQuantity
                    }
                    item.copy(deliveringQuantity = updatedDeliveringQuantity)
                },
                status = deliveryChallan.status,
                partyName = deliveryChallan.partyName,
                totalDeliveringQuantity = deliveryChallan.deliveryChallanItems.sumOf { it.deliveringQuantity },
                updated_at = Instant.now().epochSecond,
                transportationCompanyId = deliveryChallan.transportationCompanyId,
                vehicleId = deliveryChallan.vehicleId,
                driverId = deliveryChallan.driverId
            )

            // Update delivery challan using repository
            return deliveryChallanRepository.update(updatedChallan)
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }


    fun listDeliveryChallans(
        search: String? = null,
        page: Int,
        size: Int,
        deliveryOrderIds: List<String>? = emptyList(),
        fromDate: Long?,
        toDate: Long?,
        status: List<String>? = emptyList(),
        partyIds: List<String>? = emptyList(),
        transportationCompanyIds: List<String>? = emptyList(),
        getAll: Boolean,
        sortField: String,
        sortOrder: String
    ): List<DeliveryChallanOutputRecord> {
        return deliveryChallanRepository.findAll(
            search,
            page,
            size,
            deliveryOrderIds,
            fromDate,
            toDate,
            status,
            partyIds,
            transportationCompanyIds,
            getAll,
            sortField,
            sortOrder
        ).map { challan ->
            val deliveryOrder = deliveryOrderRepository.findById(challan.deliveryOrderId!!)
                ?: throw  Exception("Delivery Order not Found")
            val totalDeliveredQuantity = challan.deliveryChallanItems.sumOf { it.deliveringQuantity }
            val deliveryOrderItem = deliveryOrder.deliveryOrderSections?.flatMap { it.deliveryOrderItems }
                ?.find { challan.deliveryChallanItems.any { item -> item.deliveryOrderItemId == it.id } }
            val totalQuantity = deliveryOrderItem?.quantity ?: 0.0


            val updatedStatus = when {
                totalDeliveredQuantity >= totalQuantity -> "delivered"
                totalDeliveredQuantity > 0 -> "partially-delivered"
                else -> "pending"
            }
            DeliveryChallanOutputRecord(
                id = challan.id,
                deliveryOrderId = challan.deliveryOrderId,
                dateOfChallan = challan.dateOfChallan,
                status = updatedStatus,
                partyName = challan.partyName,
                transportationCompanyName = challan.transportationCompanyName,
                driverName = null, // You might need additional logic to fetch driver name
                totalDeliveringQuantity = challan.totalDeliveringQuantity,
            )
        }
    }
}