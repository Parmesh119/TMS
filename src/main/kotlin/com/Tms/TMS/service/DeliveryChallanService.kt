package com.Tms.TMS.service

import com.Tms.TMS.model.DeliveryChallan
import com.Tms.TMS.repository.DeliveryChallanRepository
import com.Tms.TMS.repository.DeliveryOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import java.util.UUID

@Service
class DeliveryChallanService(private val deliveryChallanRepository: DeliveryChallanRepository,
                             private val deliveryOrderRepository: DeliveryOrderRepository) {

    fun createDeliveryChallan(do_id: String): DeliveryChallan {
        val deliveryOrder = deliveryOrderRepository.findById(do_id)

        if (deliveryOrder == null) {
            throw Exception("Delivery Order not found")
        }
        return try {
            val deliveryChallan = DeliveryChallan(
                id = UUID.randomUUID().toString(),
                deliveryOrderId = do_id,
                status = "in-progress",
                created_at = Instant.now().epochSecond,
                updated_at = Instant.now().epochSecond,
                dateOfChallan = Instant.now().epochSecond
            )
            return deliveryChallanRepository.create(deliveryChallan)
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }

    fun getDeliveryChallanById(id: String): DeliveryChallan {
        return deliveryChallanRepository.findById(id) ?: throw Exception("Delivery Challan not found")
    }

//    fun updateDeliveryChallan(deliveryChallan: DeliveryChallan): DeliveryChallan? {
//        return try {
//            if (deliveryChallan.id == null) {
//                throw Exception("Delivery Challan ID is required")
//            }
//
//            // Step 1: Fetch deliveryOrderId associated with the given deliveryChallan.id
//            val deliveryOrderId = deliveryChallanRepository.findDeliveryOrderIdByChallanId(deliveryChallan.id!!)
//                ?: throw Exception("No associated Delivery Order found for this Delivery Challan")
//
//            // Step 2: Fetch all deliveryOrderItem IDs associated with the deliveryOrderId
//            val deliveryOrderItemIds = deliveryChallanRepository.findIdsByDeliveryOrderId(deliveryOrderId)
////            println("Valid Delivery Order Item IDs: $deliveryOrderItemIds")
//
//            // Create a set for quick lookup of valid deliveryOrderItem IDs
//            val validItemIds = deliveryOrderItemIds.toSet()
//
//            // Step 3: Fetch the existing delivery challan from the database
//            val existingDeliveryChallan = deliveryChallanRepository.findById(deliveryChallan.id!!)
//                ?: throw Exception("Delivery Challan not found")
//
//            // Step 4: Validate and map deliveryChallanItems
//            val updatedItems = deliveryChallan.deliveryChallanItems.map { newItem ->
//                if (newItem.deliveryorderItemId == null) {
//                    // Try to find a valid deliveryOrderItemId
//                    val validId = validItemIds.firstOrNull()
//                    if (validId == null) {
//                        throw Exception("No valid deliveryOrderItemId available for item: $newItem")
//                    }
//
//                    // Assign a valid deliveryOrderItemId to the item
//                    newItem.copy(
//                        id = newItem.id ?: UUID.randomUUID().toString(),
//                        deliveryorderItemId = validId
//                    )
//                } else {
//                    // Ensure the existing deliveryOrderItemId is valid
//                    if (!validItemIds.contains(newItem.deliveryorderItemId)) {
//                        throw Exception("Invalid deliveryOrderItemId: ${newItem.deliveryorderItemId}")
//                    }
//
//                    // Update the item without changing the ID
//                    newItem.copy(
//                        deliveringQuantity = newItem.deliveringQuantity,
//                        deliveryorderItemId = newItem.deliveryorderItemId
//                    )
//                }
//            }
//
//            // Step 5: Prepare the updated delivery challan
//            val updatedChallan = existingDeliveryChallan.copy(
//                deliveryChallanItems = updatedItems,
//                dateOfChallan = Instant.now().epochSecond,
//                updated_at = Instant.now().epochSecond
//            )
//
//            // Debugging: Log the updated delivery challan
////            println("Updated Delivery Challan: $updatedChallan")
//
//            // Step 6: Save the updated delivery challan
//            val updatedRows = deliveryChallanRepository.updateWithItem(updatedChallan)
//            if (updatedRows > 0) {
//                return updatedChallan
//            }
//            throw Exception("Failed to update Delivery Challan")
//        } catch (ex: Exception) {
////            println("Error updating Delivery Challan: ${ex.message}")
//            throw ex
//        }
//    }

    @Transactional
    fun updateDeliveryChallan(deliveryChallan: DeliveryChallan): DeliveryChallan {
        return try {
            if (deliveryChallan.id == null) {
                throw Exception("Delivery Challan ID is required")
            }
            val existingDeliveryChallan = deliveryChallanRepository.findById(deliveryChallan.id!!)
                ?: throw Exception("Delivery Challan not found")

            val updatedChallan = existingDeliveryChallan.copy(
                deliveryChallanItems = deliveryChallan.deliveryChallanItems.map {
                    it.takeUnless { it.id == null } ?: it.copy(id = UUID.randomUUID().toString())
                },
                status = deliveryChallan.status,
                partyName = deliveryChallan.partyName,
                totalDeliveringQuantity = deliveryChallan.totalDeliveringQuantity,
                updated_at = Instant.now().epochSecond
            )
            return deliveryChallanRepository.update(updatedChallan)
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }



    fun listDeliveryChallans(page: Int, size: Int, sortField: String, sortOrder: String): List<DeliveryChallan> {
        val offset = (page - 1) * size
//        logger.info("Fetching records with page=$page, size=$size, offset=$offset")
        val results = deliveryChallanRepository.findAll(size, offset, sortField, sortOrder)
//        logger.info("Found ${results.size} records")
        return results
    }
}