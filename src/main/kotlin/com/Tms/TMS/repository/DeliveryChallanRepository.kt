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

    fun getDeliveryChallanCount(): Int? {
        val sql = "SELECT COUNT(*) FROM deliverychallan"
        return jdbcTemplate.queryForObject(sql, Int::class.java)
    }

    private fun deliveryChallanRowMapper(rs: java.sql.ResultSet): DeliveryChallan {
        return DeliveryChallan(
            id = rs.getString("dc_number"),  // Changed to dc_number
            deliveryOrderId = rs.getString("do_number"),  // Changed to do_number
            status = rs.getString("status"),
            created_at = rs.getLong("created_at"),
            updated_at = rs.getLong("updated_at"),
            dateOfChallan = rs.getLong("dateOfChallan"),
            totalDeliveringQuantity = rs.getDouble("totalDeliveringQuantity"),
            partyName = rs.getString("partyName"),
            transportationCompanyId = rs.getString("transportationCompanyId"),
            transportationCompanyName = null,
            vehicleId = rs.getString("vehicleId"),
            driverId = rs.getString("driverId")
        )
    }

    fun create(deliveryChallan: DeliveryChallan): DeliveryChallan {
        try {
            val sql = """
        INSERT INTO deliverychallan(dc_number, do_number, status, created_at, updated_at, dateofchallan)
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
            // Update main challan
            val sql = """
            UPDATE deliverychallan
            SET dateofchallan = ?, status = ?, totaldeliveringquantity = ?,
                updated_at = ?, transportationcompanyid = ?, vehicleid = ?, driverid = ?
            WHERE dc_number = ?
        """

            jdbcTemplate.update(sql,
                deliveryChallan.dateOfChallan,
                deliveryChallan.status,
                deliveryChallan.totalDeliveringQuantity,
                Instant.now().toEpochMilli(),
                deliveryChallan.transportationCompanyId,
                deliveryChallan.vehicleId,
                deliveryChallan.driverId,
                deliveryChallan.id
            )

            val existingItems = getChallanItemById(deliveryChallan.id)
            val newItems = deliveryChallan.deliveryChallanItems

            // First verify all deliveryOrderItemIds exist
            newItems.forEach { item ->
                val itemExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM deliveryorderitem WHERE id = ?",
                    Int::class.java,
                    item.deliveryOrderItemId
                ) ?: 0

                if (itemExists == 0) {
                    throw Exception("DeliveryOrderItem with id ${item.deliveryOrderItemId} not found")
                }
            }

            // Delete removed items
            val existingItemIds = existingItems.mapNotNull { it.id }.toSet()
            val newItemIds = newItems.mapNotNull { it.id }.toSet()

            existingItems.filter { it.id != null && !newItemIds.contains(it.id) }.forEach { item ->
                jdbcTemplate.update("DELETE FROM deliverychallanitem WHERE id = ?", item.id)
            }

            // Update and insert items
            newItems.forEach { item ->
                if (item.id != null && existingItemIds.contains(item.id)) {
                    jdbcTemplate.update("""
                    UPDATE deliverychallanitem
                    SET deliveringquantity = ?,
                        deliveryorderitemid = ?
                    WHERE id = ? AND dc_number = ?
                """,
                        item.deliveringQuantity,
                        item.deliveryOrderItemId,
                        item.id,
                        deliveryChallan.id
                    )
                } else {
                    jdbcTemplate.update("""
                    INSERT INTO deliverychallanitem (id, dc_number, deliveryorderitemid, deliveringquantity)
                    VALUES (?, ?, ?, ?)
                """,
                        item.id ?: UUID.randomUUID().toString(),
                        deliveryChallan.id,
                        item.deliveryOrderItemId,
                        item.deliveringQuantity
                    )
                }
            }

            return findById(deliveryChallan.id) ?: throw Exception("Failed to retrieve updated delivery challan")
        } catch (e: Exception) {
            println("Error updating delivery challan: ${e.message}")
            throw e
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
                    deliveryorder AS d_orders ON dc.do_number = d_orders.do_number
                LEFT JOIN 
                    party_location AS p ON d_orders.partyid = p.id
                WHERE 
                    dc.dc_number = ?;

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
        deliverychallan dc ON dci_sub.dc_number = dc.dc_number
    WHERE 
        dci.dc_number = ?
    GROUP BY 
        dci.id, doi.id, doi.district, doi.taluka, loc.name, mat.name, 
        doi.quantity, doi.rate, doi.duedate
""".trimIndent()

            jdbcTemplate.query(sql, { rs, _ ->
                DeliveryChallanItems(
                    id = rs.getString("id"),
                    deliveryChallanId = rs.getString("dc_number"),
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


    fun findAll(
        search: String? = null,
        page: Int,
        size: Int,
        deliveryOrderIds: List<String>? = emptyList(),
        fromDate: Long?,
        toDate: Long?,
        statuses: List<String>? = emptyList(),
        partyIds: List<String>? = emptyList(),
        transportationCompanyIds: List<String>? = emptyList(),
        getAll: Boolean,
        sortField: String,
        sortOrder: String
    ): List<DeliveryChallan> {
        return try {
            // Validate sort parameters
            if (!isValidSortField(sortField) || !isValidSortOrder(sortOrder)) {
                throw IllegalArgumentException("Invalid sort parameters")
            }

            // Start building the SQL query
            val sql = StringBuilder(
                """
            SELECT 
                dc.*, 
                pl.name AS partyName, 
                tc.company_name AS transportationCompanyName
            FROM deliverychallan AS dc
            LEFT JOIN deliveryorder AS dord ON dc.do_number = dord.do_number
            LEFT JOIN party_location AS pl ON dord.partyId = pl.id
            LEFT JOIN transportationcompany AS tc ON dc.transportationCompanyId = tc.id
            """.trimIndent()
            )

            // Collect conditions for filtering
            val conditions = mutableListOf<String>()

            if (!deliveryOrderIds.isNullOrEmpty()) {
                conditions.add("dc.do_number IN (${deliveryOrderIds.joinToString { "'${it}'" }})")
            }

            if (fromDate != null) {
                conditions.add("dc.dateOfChallan >= $fromDate")
            }

            if (toDate != null) {
                conditions.add("dc.dateOfChallan <= $toDate")
            }

            if (!statuses.isNullOrEmpty()) {
                conditions.add("dc.status IN (${statuses.joinToString { "'${it}'" }})")
            }

            if (!partyIds.isNullOrEmpty()) {
                conditions.add("dord.partyId IN (${partyIds.joinToString { "'${it}'" }})")
            }

            if (!transportationCompanyIds.isNullOrEmpty()) {
                conditions.add("dc.transportationCompanyId IN (${transportationCompanyIds.joinToString { "'${it}'" }})")
            }

            if (!search.isNullOrEmpty()) {
                conditions.add("(dc.dc_number LIKE '%$search%' OR pl.name LIKE '%$search%' OR tc.company_name LIKE '%$search%')")
            }

            // Add conditions to the WHERE clause
            if (conditions.isNotEmpty()) {
                sql.append(" WHERE ").append(conditions.joinToString(" AND "))
            }

            // Append sorting and pagination
            sql.append(" ORDER BY dc.$sortField $sortOrder LIMIT ? OFFSET ?")

            val limit = size
            val offset = (page - 1) * size

            // Query the database
            return jdbcTemplate.query(sql.toString(), { rs, _ ->
                DeliveryChallan(
                    id = rs.getString("dc_number"),
                    deliveryOrderId = rs.getString("do_number"),
                    status = rs.getString("status"),
                    created_at = rs.getLong("created_at"),
                    updated_at = rs.getLong("updated_at"),
                    dateOfChallan = rs.getLong("dateOfChallan"),
                    totalDeliveringQuantity = 0.0,
                    partyName = rs.getString("partyName"),
                    transportationCompanyId = rs.getString("transportationcompanyId"),
                    transportationCompanyName = rs.getString("transportationCompanyName"),
                    vehicleId = rs.getString("vehicleId"),
                    driverId = rs.getString("driverId")
                )
            }, limit, offset)
        } catch (ex: Exception) {
            throw ex
        }
    }

    private fun isValidSortField(field: String): Boolean {
        val validFields = listOf("dc_number", "do_number", "status", "dateOfChallan", "created_at", "updated_at")
        return field in validFields
    }

    private fun isValidSortOrder(order: String): Boolean {
        return order.equals("ASC", ignoreCase = true) || order.equals("DESC", ignoreCase = true)
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
//    fun createItem(item: DeliveryChallanItems): DeliveryChallanItems {
//        try {
//            val sql = """
//            INSERT INTO deliverychallanitem(
//                id,
//                deliverychallanid,
//                deliveryorderitemid,
//                deliveringquantity
//            )
//            VALUES (?, ?, ?, ?)
//        """.trimIndent()
//
//            jdbcTemplate.update(
//                sql,
//                item.id ?: UUID.randomUUID().toString(),
//                item.deliveryChallanId,
//                item.deliveryOrderItemId,  // Ensure this is included
//                item.deliveringQuantity
//            )
//
//            return item
//        } catch (ex: Exception) {
//            throw Exception("Failed to create delivery challan item: ${ex.message}")
//        }
//    }
}