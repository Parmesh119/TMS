package com.Tms.TMS.repository

import com.Tms.TMS.model.deliveryOrderItems
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class DeliveryOrderItemRepository(private val jdbcTemplate: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        deliveryOrderItems(
            id = rs.getString("id"),
            deliveryOrderId = rs.getString("do_number"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            locationId = rs.getString("locationId"),
            materialId = rs.getString("materialId"),
            quantity = rs.getDouble("quantity"),
            rate = rs.getDouble("rate"),
            unit = rs.getString("unit"),
            dueDate = rs.getLong("dueDate").takeIf { !rs.wasNull() },
        )
    }


    // Implement Delivery Order Item Repository operations
    fun saveAll(doNumber: String, items: List<deliveryOrderItems>) {
        val sql = """
            INSERT INTO deliveryorderitem (
                id, do_number, district, taluka, locationId, materialId, quantity,
               rate, unit, dueDate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """

        try {
            items.forEach { item ->
                jdbcTemplate.update(
                    sql,
                    item.id,
                    item.deliveryOrderId,
                    item.district,
                    item.taluka,
                    item.locationId,
                    item.materialId,
                    item.quantity,
                    item.rate ?: 0.0,
                    item.unit,
                    item.dueDate ?: 0,
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }
    fun syncItems(items: List<deliveryOrderItems>, doNumber: String) {
        val existingItems = getExistingItems(doNumber)

        val itemsToUpdate = mutableListOf<deliveryOrderItems>()
        val itemsToDelete = mutableListOf<deliveryOrderItems>()
        val itemsToInsert = mutableListOf<deliveryOrderItems>()

        items.forEach { item ->
            val existingItem = existingItems.find { it.id == item.id }

            if (existingItem != null) {
                val mergedItem = item.copy(
                    district =  item.district ?: existingItem.district,
                    taluka =  item.taluka ?: existingItem.taluka,
                    locationId = item.locationId ?: existingItem.locationId,
                    materialId = item.materialId ?: existingItem.materialId,
                    unit = item.unit ?: existingItem.unit,
                    dueDate = item.dueDate ?: existingItem.dueDate,
                    deliveryOrderId = existingItem.deliveryOrderId
                )
                itemsToUpdate.add(mergedItem)
            } else {
                itemsToInsert.add(item.copy(deliveryOrderId = doNumber))
            }
        }

        existingItems.forEach { existingItem ->
            if (!items.any { it.id == existingItem.id }) {
                itemsToDelete.add(existingItem)
            }
        }
        updateItems(itemsToUpdate)
        insertItems(itemsToInsert)
        deleteItems(itemsToDelete)
    }

    fun getExistingItems(doNumber: String): List<deliveryOrderItems> {
        val sql = "SELECT * FROM DeliveryOrderItem WHERE do_number = ?"
        return jdbcTemplate.query(sql, rowMapper, doNumber)
    }

    private fun updateItems(items: List<deliveryOrderItems>) {
        if (items.isEmpty()) return
        val sql = """
        UPDATE DeliveryOrderItem 
        SET 
            district = ?, 
            taluka = ?, 
            locationId = ?, 
            materialId = ?, 
            quantity = ?, 
            rate = ?, 
            unit = ?, 
            dueDate = ?
        WHERE id = ? AND do_number = ?
    """

        try {
            items.forEach { item ->
                jdbcTemplate.update(
                    sql,
                    item.district,
                    item.taluka,
                    item.locationId,
                    item.materialId,
                    item.quantity,
                    item.rate ?: 0,
                    item.unit,
                    item.dueDate ?: 0,
                    item.id,
                    item.deliveryOrderId
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteItems(items: List<deliveryOrderItems>) {
        if (items.isEmpty()) return
        val sql = "DELETE FROM DeliveryOrderItem WHERE id = ? AND do_number = ?"
        try {
            items.forEach { item ->
                jdbcTemplate.update(sql, item.id, item.deliveryOrderId)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun insertItems(items: List<deliveryOrderItems>) {
        if (items.isEmpty()) return
        val sql = """
            INSERT INTO deliveryorderitem (
                id, do_number, district, taluka, locationId, materialId, quantity,
               rate, unit, dueDate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        try {
            items.forEach { item ->
                jdbcTemplate.update(
                    sql,
                    item.id,
                    item.deliveryOrderId,
                    item.district,
                    item.taluka,
                    item.locationId,
                    item.materialId,
                    item.quantity,
                    item.rate ?: 0.0,
                    item.unit,
                    item.dueDate ?: 0
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }
}