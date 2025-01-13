package com.Tms.TMS.repository

import com.Tms.TMS.model.DeliveryChallan
import com.Tms.TMS.model.DeliveryChallanItems
import com.Tms.TMS.model.deliveryOrderItems
import com.Tms.TMS.model.deliveryorder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
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
            driverId = rs.getString("driverId"),
            deliveryChallanItems = emptyList()
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

    @Transactional
    fun update(deliveryChallan: DeliveryChallan): DeliveryChallan {
        try {
            // 1. Update main challan
            val mainUpdateSql = """
            UPDATE deliverychallan
            SET 
                status = ?,
                dateofchallan = ?,
                totaldeliveringquantity = ?,
                updated_at = ?,
                transportationcompanyid = ?,
                vehicleid = ?,
                driverid = ?
            WHERE dc_number = ?
        """

            jdbcTemplate.update(
                mainUpdateSql,
                deliveryChallan.status,
                deliveryChallan.dateOfChallan,
                deliveryChallan.totalDeliveringQuantity,
                Instant.now().toEpochMilli(),
                deliveryChallan.transportationCompanyId,
                deliveryChallan.vehicleId,
                deliveryChallan.driverId,
                deliveryChallan.id
            )

            // 2. Get existing items
            val existingItems = getChallanItemById(deliveryChallan.id)
            val existingItemIds = existingItems.mapNotNull { it.id }.toSet()
            val newItemIds = deliveryChallan.deliveryChallanItems.mapNotNull { it.id }.toSet()

            // 3. Delete items that are no longer present
            val itemsToDelete = existingItems.filter { it.id != null && !newItemIds.contains(it.id) }
            if (itemsToDelete.isNotEmpty()) {
                val deleteSql = "DELETE FROM deliverychallanitem WHERE id = ? AND dc_number = ?"
                itemsToDelete.forEach { item ->
                    jdbcTemplate.update(deleteSql, item.id, deliveryChallan.id)
                }
            }

            // 4. Update or Insert items
            deliveryChallan.deliveryChallanItems.forEach { item ->
                if (item.id != null && existingItemIds.contains(item.id)) {
                    // Update existing item
                    val updateItemSql = """
                    UPDATE deliverychallanitem
                        SET deliveringquantity = ?,
                        deliveryorderitemid = ?
                    WHERE id = ? AND dc_number = ?
                """
                    jdbcTemplate.update(
                        updateItemSql,
                        item.deliveringQuantity,
                        item.deliveryOrderItemId,
                        item.id,
                        deliveryChallan.id
                    )
                } else {
                    // Insert new item
                    val insertItemSql = """
                    INSERT INTO deliverychallanitem 
                    (id, dc_number, deliveryorderitemid, deliveringquantity)
                    VALUES (?, ?, ?, ?)
                """
                    val newItemId = item.id ?: UUID.randomUUID().toString()
                    jdbcTemplate.update(
                        insertItemSql,
                        newItemId,
                        deliveryChallan.id,
                        item.deliveryOrderItemId,
                        item.deliveringQuantity
                    )
                }
            }

            // 5. Validate all items exist in delivery order
            val validateSql = """
            SELECT COUNT(*) 
            FROM deliverychallanitem dci
            JOIN deliveryorderitem doi ON dci.deliveryorderitemid = doi.id
            WHERE dci.dc_number = ?
        """
            val itemCount = jdbcTemplate.queryForObject(validateSql, Int::class.java, deliveryChallan.id)
            if (itemCount != deliveryChallan.deliveryChallanItems.size) {
                throw Exception("Some delivery order items not found")
            }

//             update status based on delivery order
            var grandtotal = deliveryChallan.totalDeliveringQuantity
            var total: Double = 0.0
            for (item in deliveryChallan.deliveryChallanItems) {
                total += item.quantity
            }

            if(grandtotal == total) {
                updateStatus(deliveryChallan.deliveryOrderId!!, "delivered")
            } else {
                updateStatus(deliveryChallan.deliveryOrderId!!, "pending")
            }

            // 6. Return updated challan
            return findById(deliveryChallan.id)
                ?: throw Exception("Failed to retrieve updated delivery challan")

        } catch (e: Exception) {
            println("Error updating delivery challan: ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to update delivery challan: ${e.message}")
        }
    }

    fun updateStatus(id: String, status: String): String {
        try {
            val sql = "UPDATE deliveryorder SET status = ? WHERE do_number = ?"
            val res = jdbcTemplate.update(sql, status, id) > 0
            return if (res) {
                "Delivery order status updated successfully"
            } else {
                "Failed to update delivery order status"
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // Helper function to validate quantities
    private fun validateDeliveryOrderItemQuantities(deliveryChallan: DeliveryChallan) {
        val validateQuantitySql = """
        SELECT doi.quantity, 
               (
                   SELECT COALESCE(SUM(dci.deliveringquantity), 0)
                   FROM deliverychallanitem dci
                   JOIN deliverychallan dc ON dci.dc_number = dc.dc_number
                   WHERE dci.deliveryorderitemid = doi.id
                   AND dc.status != 'cancelled'
                   AND dc.dc_number != ?
               ) as total_delivering
        FROM deliveryorderitem doi
        WHERE doi.id = ?
    """

        deliveryChallan.deliveryChallanItems.forEach { item ->
            jdbcTemplate.queryForObject(validateQuantitySql,
                { rs, _ ->
                    val doiQuantity = rs.getDouble("quantity")
                    val totalDelivering = rs.getDouble("total_delivering")
                    val availableQuantity = doiQuantity - totalDelivering

                    if (item.deliveringQuantity > availableQuantity) {
                        throw Exception("Delivering quantity exceeds available quantity for item ${item.deliveryOrderItemId}")
                    }
                },
                deliveryChallan.id,
                item.deliveryOrderItemId
            )
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
        END), 0) AS deliveredQuantity
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
                    deliveringQuantity = rs.getDouble("deliveringquantity"),
                    deliveredQuantity = rs.getDouble("deliveredquantity")
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
                    totalDeliveringQuantity = rs.getDouble("totalDeliveringQuantity"),
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
}