package com.Tms.TMS.service

import com.Tms.TMS.model.deliveryorder
import com.Tms.TMS.model.deliveryOrderSections
import com.Tms.TMS.repository.DeliveryOrderItemRepository
import com.Tms.TMS.repository.DeliveryOrderRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DeliveryOrderService(private val deliveryOrderRepository: DeliveryOrderRepository,
    private val deliveryOrderItemRepository: DeliveryOrderItemRepository
) {
    // Delivery Order Service operations

    // List all delivery orders
    fun listAllDeliveryOrder(page: Int, size: Int, sortField: String, sortOrder: String): List<deliveryorder> {
        // Implement logic to list all delivery orders
        val offset = (page - 1) * size
        return deliveryOrderRepository.findAll(size, offset, sortField, sortOrder)
    }

    // get by id
    fun getDeliveryOrderById(id: String): deliveryorder? {
        // Implement logic to get delivery order by id
        return deliveryOrderRepository.findById(id)
    }

    // create delivery order
    fun createDeliveryOrder(orderRequest: deliveryorder, deliveryOrderSections: List<deliveryOrderSections>): ResponseEntity<deliveryorder> {
        // Implement logic to create a new delivery order
        deliveryOrderRepository.create(orderRequest)
        val itemsToSave = deliveryOrderSections.flatMap { section ->
            section.deliveryOrderItems?.map { item ->
                item.copy(deliveryOrderId = orderRequest.id)
            } ?: emptyList()
        }
        deliveryOrderItemRepository.saveAll(orderRequest.id!!, itemsToSave)
        return ResponseEntity.ok(orderRequest)
    }

    // update delivery order
    fun updateDeliveryOrder(orderRequest: deliveryorder, sections: List<deliveryOrderSections>): deliveryorder {
        // Implement logic to update an existing delivery order
        deliveryOrderRepository.update(orderRequest)
        val itemsToSave = sections.flatMap { section ->
            section.deliveryOrderItems?.map { item ->
                item.copy(deliveryOrderId = orderRequest.id) // Associate with the deliveryOrderId
            } ?: emptyList()
        }

        deliveryOrderItemRepository.syncItems(itemsToSave, orderRequest.id!!)
        return orderRequest;
    }

    // delete delivery order
    fun deleteOrder(id: String): Int {
        // Implement logic to delete an existing delivery order
        return deliveryOrderRepository.deleteById(id)
    }
}