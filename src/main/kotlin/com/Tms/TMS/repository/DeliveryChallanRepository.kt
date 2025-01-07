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
            partyName = rs.getString("partyName")
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
                val deleteSql = """
                    DELETE FROM deliverychallanitem
                    WHERE id = ?
                """.trimIndent()
                itemsToDelete.forEach { item ->
                    jdbcTemplate.update(deleteSql, item.id)
                }
            }

            // Update existing items
            val updateItemSql = """
                UPDATE deliverychallanitem
                SET
                    deliveringquantity = ?
                WHERE id = ?
            """.trimIndent()

            itemsToUpdate.forEach { item ->
                jdbcTemplate.update(
                    updateItemSql,
                    item.deliveringQuantity,
                    item.id
                )
            }

            // Create new items
            itemsToCreate.forEach { item ->
                createItem(item)
            }

            return deliveryChallan
        }catch (e: Exception){
            throw e;
        }
    }

    fun createItem(item: DeliveryChallanItems): DeliveryChallanItems {
        try {
            val sql = """
        INSERT INTO deliverychallanitem(id, deliverychallanid, deliveryorderitemid, deliveringQuantity)
        VALUES (?, ?, ?, ?)
        """.trimIndent()

            jdbcTemplate.update(
                sql,
                item.id,
                item.deliveryChallanId,
                item.deliveryorderItemId,
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
                pl.name AS partyName
                FROM deliverychallan as dc
            LEFT JOIN deliveryorder as dord ON dc.deliveryOrderId = dord.id
            LEFT JOIN party_location as pl ON dord.partyId = pl.id
            WHERE dc.id = ?
            """.trimIndent()
            val deliveryChallan = jdbcTemplate.queryForObject(sql, { rs, _ -> deliveryChallanRowMapper(rs) }, id) ?: return null
            val items = getChallanItemById(id)
            print(items)
            deliveryChallan.copy(
                deliveryChallanItems = items
            )

        } catch (ex: Exception) {
            throw ex
        }
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

    fun getChallanItemById(id: String): List<DeliveryChallanItems> {
        return try {
            val sql = """
        SELECT 
        dci.*,
        doi.district,
        doi.taluka,
        loc.name AS locationName,
        mat.name AS materialName,
        doi.quantity,
        doi.rate, 
        doi.duedate, 
        doi.status,
        COALESCE(SUM(CASE 
            WHEN dc.status = 'delivered' THEN dci_sub.deliveringquantity 
            ELSE 0 
        END), 0) AS delivered_quantity,
        COALESCE(SUM(CASE 
            WHEN dc.status = 'in-progress' THEN dci_sub.deliveringquantity 
            ELSE 0 
        END), 0) AS in_progress_quantity
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
        doi.quantity, doi.rate, doi.duedate, doi.status
""".trimIndent()

            return jdbcTemplate.query(sql, { rs, _ ->
                DeliveryChallanItems(
                    id = rs.getString("id"),
                    deliveryChallanId = rs.getString("deliveryChallanId"),         // Changed to match DB column
                    deliveryorderItemId = rs.getString("deliveryorderItemId"),     // Changed to match DB column
                    district = rs.getString("district"),
                    taluka = rs.getString("taluka"),
                    locationName = rs.getString("locationName"),                  // Changed to match SQL alias
                    materialName = rs.getString("materialName"),                  // Changed to match SQL alias
                    quantity = rs.getDouble("quantity"),
                    rate = rs.getDouble("rate"),
                    dueDate = rs.getLong("dueDate"),                              // Changed to match DB column
                    deliveringQuantity = rs.getDouble("deliveringQuantity")       // Changed to match DB column
                )
            }, id)
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun findDeliveryOrderIdByChallanId(challanId: String): String? {
        val sql = """
        SELECT deliveryOrderId
        FROM deliveryChallan
        WHERE id = ?
    """
        return jdbcTemplate.queryForObject(sql, String::class.java, challanId)
    }

    fun findIdsByDeliveryOrderId(deliveryOrderId: String): List<String> {
        val sql = """
        SELECT id
        FROM deliveryOrderItem
        WHERE deliveryOrderId = ?
    """
        return jdbcTemplate.queryForList(sql, String::class.java, deliveryOrderId)
    }



    fun deleteItem(id: String): Int {
        val sql = "DELETE FROM deliverychallanitem WHERE id = ?"
        return jdbcTemplate.update(sql, id)
    }

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
                partyName = rs.getString("partyName")
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

}