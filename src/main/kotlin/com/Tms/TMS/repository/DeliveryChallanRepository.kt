package com.Tms.TMS.repository

import com.Tms.TMS.model.DeliveryChallan
import com.Tms.TMS.model.DeliveryChallanItems
import com.Tms.TMS.model.deliveryOrderItems
import com.Tms.TMS.model.deliveryorder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Repository
class DeliveryChallanRepository(private val jdbcTemplate: JdbcTemplate) {

    private fun deliveryChallanRowMapper(rs: ResultSet): DeliveryChallan {
        return DeliveryChallan(
            id = rs.getString("id"),
            deliveryOrderId = rs.getString("deliveryOrderId"),
            status = rs.getString("status"),
            created_at = rs.getLong("created_at"),
            updated_at = rs.getLong("updated_at"),
            dateOfChallan = rs.getLong("dateOfChallan"),
            totalDeliveringQuantity = 0.0,
            partyName = rs.getString("partyName"),
            transportationCompanyId = rs.getString("transportationCompanyId"),
            vehicleId = rs.getString("vehicleId"),
            driverId = rs.getString("driverId")
        )
    }

    fun create(deliveryChallan: DeliveryChallan): DeliveryChallan {
        try {
            val sql = """
        INSERT INTO deliverychallan(id, deliveryOrderId, status, created_at, updated_at, dateofchallan)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

            jdbcTemplate.update(
                sql,
                deliveryChallan.id,
                deliveryChallan.deliveryOrderId,
                deliveryChallan.status,
                deliveryChallan.created_at,
                deliveryChallan.updated_at,
                deliveryChallan.dateOfChallan
            )

            return deliveryChallan
        } catch (ex: Exception) {
            throw Exception("Failed to create delivery challan: ${ex.message}")
        }
    }

    fun update(deliveryChallan: DeliveryChallan): DeliveryChallan {
        try {
            val sql = """
        UPDATE deliverychallan
        SET
            dateofchallan = ?,
            status = ?,
            totaldeliveringquantity = ?,
            updated_at = ?
        WHERE id = ?
        """

            val currentTime = Instant.now().toEpochMilli()

            jdbcTemplate.update(
                sql,
                deliveryChallan.dateOfChallan,
                deliveryChallan.status,
                deliveryChallan.totalDeliveringQuantity,
                currentTime,
                deliveryChallan.id
            )

            val existingItems = getChallanItemById(deliveryChallan.id!!)
            val newItems = deliveryChallan.deliveryChallanItems

            val existingItemIds = existingItems.mapNotNull { it.id }.toSet()
            val newItemIds = newItems.mapNotNull { it.id }.toSet()

            val itemsToCreate = newItems.filter { it.id == null || !existingItemIds.contains(it.id) }
            val itemsToUpdate = newItems.filter { it.id != null && existingItemIds.contains(it.id) }
            val itemsToDelete = existingItems.filter { it.id != null && !newItemIds.contains(it.id) }

            // Delete removed items
            if (itemsToDelete.isNotEmpty()) {
                val deleteSql = "DELETE FROM deliverychallanitem WHERE id = ?"
                itemsToDelete.forEach { item ->
                    jdbcTemplate.update(deleteSql, item.id)
                }
            }

            // Update existing items
            val updateItemSql = """
            UPDATE deliverychallanitem
            SET
                deliveringquantity = ?,
                deliveryorderitemid = ?
            WHERE id = ?
        """

            itemsToUpdate.forEach { item ->
                jdbcTemplate.update(
                    updateItemSql,
                    item.deliveringQuantity,
                    item.deliveryOrderItemId,  // Make sure this is included
                    item.id
                )
            }

            // Create new items
            val createItemSql = """
            INSERT INTO deliverychallanitem (
                id,
                deliverychallanid,
                deliveryorderitemid,
                deliveringquantity
            ) VALUES (?, ?, ?, ?)
        """

            itemsToCreate.forEach { item ->
                jdbcTemplate.update(
                    createItemSql,
                    item.id ?: UUID.randomUUID().toString(),
                    deliveryChallan.id,
                    item.deliveryOrderItemId,  // Explicitly include deliveryorderItemId
                    item.deliveringQuantity
                )
            }
            return findById(deliveryChallan.id) ?: throw Exception("Failed to retrieve updated delivery challan")
        } catch (e: Exception) {
            throw e
        }
    }

    fun createItem(item: DeliveryChallanItems): DeliveryChallanItems {
        try {
            val sql = """
            INSERT INTO deliverychallanitem(
                id, 
                deliverychallanid, 
                deliveryorderitemid, 
                deliveringquantity
            )
            VALUES (?, ?, ?, ?)
        """.trimIndent()

            jdbcTemplate.update(
                sql,
                item.id ?: UUID.randomUUID().toString(),
                item.deliveryChallanId,
                item.deliveryOrderItemId,  // Ensure this is included
                item.deliveringQuantity
            )

            return item
        } catch (ex: Exception) {
            throw Exception("Failed to create delivery challan item: ${ex.message}")
        }
    }

    fun findById(id: String): DeliveryChallan? {
        return try {
            val sql = """
                SELECT 
                    dc.*, 
                    p.name AS partyName  -- Match model field
                FROM 
                    deliverychallan AS dc
                LEFT JOIN 
                    deliveryorder AS d_orders ON dc.deliveryorderid = d_orders.id
                LEFT JOIN 
                    party_location AS p ON d_orders.partyid = p.id
                WHERE 
                    dc.id = ?;

            """.trimIndent()
            val deliveryChallan = jdbcTemplate.queryForObject(sql, { rs, _ -> deliveryChallanRowMapper(rs) }, id) ?: return null
            val items = getChallanItemById(id)
            deliveryChallan.copy(
                deliveryChallanItems = items
            )

        } catch (ex: Exception) {
            throw ex
        }
    }

//

    fun getChallanItemById(id: String): List<DeliveryChallanItems> {
        return try {
            val sql = """
            SELECT 
        dci.*,
        doi.district,
        doi.taluka,
        loc.name AS location_name,
        mat.name AS material_name,
        doi.quantity,
        doi.rate, 
        doi.duedate, 
        COALESCE(SUM(CASE 
            WHEN dc.status = 'delivered' THEN dci_sub.deliveringquantity 
            ELSE 0 
        END), 0) AS delivered_quantity
    FROM 
        deliverychallanitem dci
    JOIN 
        deliveryorderitem doi ON dci.deliveryorderitemid = doi.id
    JOIN 
        location loc ON doi.locationid = loc.id
    JOIN 
        material mat ON doi.materialid = mat.id
    LEFT JOIN 
        deliverychallanitem dci_sub ON dci_sub.deliveryorderitemid = doi.id
    LEFT JOIN 
        deliverychallan dc ON dci_sub.deliverychallanid = dc.id
    WHERE 
        dci.deliverychallanid = ?
    GROUP BY 
        dci.id, doi.id, doi.district, doi.taluka, loc.name, mat.name, 
        doi.quantity, doi.rate, doi.duedate
""".trimIndent()

            jdbcTemplate.query(sql, { rs, _ ->
                DeliveryChallanItems(
                    id = rs.getString("id"),
                    deliveryChallanId = rs.getString("deliverychallanid"),
                    deliveryOrderItemId = rs.getString("deliveryorderitemid"),
                    district = rs.getString("district"),
                    taluka = rs.getString("taluka"),
                    locationName = rs.getString("location_name"),
                    materialName = rs.getString("material_name"),
                    quantity = rs.getDouble("quantity"),
                    rate = rs.getDouble("rate"),
                    dueDate = rs.getLong("duedate"),
                    deliveringQuantity = rs.getDouble("deliveringquantity")
                )
            }, id)
        } catch (ex: Exception) {
            println("Error in getChallanItemById: ${ex.message}")
            ex.printStackTrace()
            throw ex
        }
    }



    fun deleteItem(id: String): Int {
        val sql = "DELETE FROM deliverychallanitem WHERE id = ?"
        return jdbcTemplate.update(sql, id)
    }


    fun findAll(limit: Int, offset: Int, sortField: String, sortOrder: String): List<DeliveryChallan> {
        return try {
            val sql = """
            SELECT 
                dc.*,
                pl.name AS partyName
            FROM deliverychallan AS dc
            LEFT JOIN deliveryorder AS dord ON dc.deliveryOrderId = dord.id
            LEFT JOIN party_location AS pl ON dord.partyId = pl.id
            ORDER BY dc.${sortField} ${sortOrder}
            LIMIT ? OFFSET ?
        """.trimIndent()

            jdbcTemplate.query(sql, { rs, _ -> DeliveryChallan(
                id = rs.getString("id"),
                deliveryOrderId = rs.getString("deliveryOrderId"),
                status = rs.getString("status"),
                created_at = rs.getLong("created_at"),
                updated_at = rs.getLong("updated_at"),
                dateOfChallan = rs.getLong("dateOfChallan"),
                totalDeliveringQuantity = 0.0,
                partyName = rs.getString("partyName"),
                transportationCompanyId = rs.getString("transportationCompanyId"),
                vehicleId = rs.getString("vehicleId"),
                driverId = rs.getString("driverId")
            )}, limit, offset)
        } catch (ex: Exception) {
            if (!isValidSortField(sortField) || !isValidSortOrder(sortOrder)) {
                throw IllegalArgumentException("Invalid sort parameters")
            }
            throw ex
        }
    }

    // Add validation helpers
    private fun isValidSortField(field: String): Boolean {
        val validFields = setOf("created_at", "updated_at", "dateOfChallan", "status")
        return validFields.contains(field)
    }

    private fun isValidSortOrder(order: String): Boolean {
        return order.uppercase() in setOf("ASC", "DESC")
    }

//    fun update(deliveryChallan: DeliveryChallan): Int? {
//        return try {
//            val sql = """
//        UPDATE deliverychallan SET dateOfChallan = ?, status = ?, updated_at = ?
//        WHERE id = ?
//        """.trimIndent()
//
//            val updated_challan = jdbcTemplate.update(sql, deliveryChallan.dateOfChallan, deliveryChallan.status, deliveryChallan.updated_at, deliveryChallan.id)
//            if (updated_challan == 0) {
//                throw Exception("Delivery Challan not found")
//            }
//            updated_challan
//        } catch (ex: Exception) {
//            throw ex
//        }
//
//    }

//    fun updateWithItem(deliveryChallan: DeliveryChallan): Int {
//        return try {
//            val challanUpdate = update(deliveryChallan)
//            if (challanUpdate == 0) {
//                return challanUpdate
//            } else {
//                val current_challan = getChallanItemById(deliveryChallan.id!!)
//                val updated_challan = deliveryChallan.deliveryChallanItems ?: emptyList()
//
//                val itemsToDelete = current_challan.filter { current -> updated_challan.none { updated -> updated.id == current.id } }
//
//                itemsToDelete.forEach {
//                    deleteItem(it.id!!)
//                }
//
//                updated_challan.forEach {
//                    if(current_challan.any {
//                                current -> current.id == it.id
//                        }) {
//                        updateItem(it)
//                    } else {
//                        createItem(it)
//                    }
//                }
//                challanUpdate!!
//            }
//        } catch (ex: Exception) {
//            throw ex
//        }
//    }

    //    fun updateItem(updateItem: DeliveryChallanItems): Int {
//        return try {
//            if (updateItem.deliveryorderItemId == null) {
//                throw Exception("deliveryorderItemId cannot be null for update operation")
//            }
//
//            val sql = """
//        UPDATE deliverychallanitem
//        SET
//            deliverychallanid = ?,
//            deliveryorderitemid = ?,
//            deliveringQuantity = ?
//        WHERE id = ?
//    """.trimIndent()
//
//            jdbcTemplate.update(
//                sql,
//                updateItem.deliveryChallanId,
//                updateItem.deliveryorderItemId,
//                updateItem.deliveringQuantity,
//                updateItem.id
//            )
//        } catch (ex: Exception) {
//            throw ex
//        }
//    }

}