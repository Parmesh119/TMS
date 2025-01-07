package com.Tms.TMS.repository

import com.Tms.TMS.model.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.Instant
import java.time.ZoneId

@Repository
class DeliveryOrderRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val deliveryChallanRepository: DeliveryChallanRepository
) {
    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        deliveryorder(
            id = rs.getString("id"),
            contractId = rs.getString("contractId"),
            partyId = rs.getString("partyId"),
            partyName = rs.getString("partyName"),
            dateOfContract = rs.getLong("dateOfContract").takeIf { !rs.wasNull() },
            status = rs.getString("status"),
            created_at = rs.getLong("created_at")?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            },
            deliveryOrderSections = getdeliveryorderSections(rs.getString("id"))
        )
    }

    private val deliveryOrderItemRowMapper = RowMapper { rs: ResultSet, _: Int ->
        deliveryOrderItems(
            id = rs.getString("id"),
            deliveryOrderId = rs.getString("deliveryOrderId"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            locationId = rs.getString("locationId"),
            materialId = rs.getString("materialId"),
            quantity = rs.getInt("quantity"),
            status = rs.getString("status"),
            rate = rs.getFloat("rate"),
            unit = rs.getString("unit"),
            dueDate = rs.getLong("dueDate"),

        )
    }

    fun getdeliveryorderSections(deliveryOrderId: String): List<deliveryOrderSections> {
        return emptyList()
    }

    fun findAll(
        search: String,
        page: Int,
        size: Int,
        status: List<String>,
        partyId: List<String>,
        fromDate: Long?,
        toDate: Long?
    ): List<ListDeliveryOrderItem> {
        try {
            val offset = (page - 1) * size

            val sqlBuilder = StringBuilder("""
            SELECT 
                d.id, 
                d.contractId, 
                d.status, 
                d.dateOfContract, 
                p.name AS partyName
            FROM deliveryorder d
            LEFT JOIN party_location p ON d.partyId = p.id
            WHERE 1 = 1
        """.trimIndent())

            val params = mutableListOf<Any?>()

            // Add search filter if provided
            if (search.isNotEmpty()) {
                sqlBuilder.append(" AND (d.contractId ILIKE ? OR d.partyId ILIKE ?)")
                params.add("%$search%")
                params.add("%$search%")
            }

            // Add status filter if provided
            if (status.isNotEmpty()) {
                sqlBuilder.append(" AND d.status = ANY (?)")
                params.add(status.toTypedArray())
            }

            // Add partyId filter if provided
            if (partyId.isNotEmpty()) {
                sqlBuilder.append(" AND d.partyId = ANY (?)")
                params.add(partyId.toTypedArray())
            }

            // Add fromDate filter if provided
            if (fromDate != null) {
                sqlBuilder.append(" AND d.dateOfContract >= ?")
                params.add(fromDate)
            }

            // Add toDate filter if provided
            if (toDate != null) {
                sqlBuilder.append(" AND d.dateOfContract <= ?")
                params.add(toDate)
            }

            sqlBuilder.append(" ORDER BY d.created_at DESC LIMIT ? OFFSET ?")
            params.add(size)
            params.add(offset)

            val sql = sqlBuilder.toString()

            return jdbcTemplate.query(sql, { rs, _ ->
                ListDeliveryOrderItem(
                    id = rs.getString("id"),
                    contractId = rs.getString("contractId"),
                    status = rs.getString("status"),
                    partyName = rs.getString("partyName"),
                    dateOfContract = rs.getLong("dateOfContract"),
                )
            }, *params.toTypedArray())
        } catch (e: Exception) {
            throw e
        }
    }





    fun findById(id: String): deliveryorder? {
        return try {
            val deliveryOrderSql = """
            SELECT 
                d.*,
                p.name AS partyName
            FROM deliveryorder d
            LEFT JOIN party_location p ON d.partyId = p.id
            WHERE d.id = ?
        """

            val deliveryOrder = jdbcTemplate.queryForObject(deliveryOrderSql,
                RowMapper { rs, _ ->
                    deliveryorder(
                        id = rs.getString("id"),
                        contractId = rs.getString("contractId"),
                        partyId = rs.getString("partyId"),
                        partyName = rs.getString("partyName"),
                        dateOfContract = rs.getLong("dateOfContract").takeIf { !rs.wasNull() },
                        status = rs.getString("status"),
                        created_at = rs.getLong("created_at")?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        },
                        deliveryOrderSections = emptyList()  // Initialize with empty list
                    )
                },
                id
            ) ?: return null

            // Fetch DeliveryOrderItems
            val deliveryOrderItemsSql = "SELECT * FROM DeliveryOrderItem WHERE deliveryOrderId = ?"
            val deliveryOrderItems = jdbcTemplate.query(deliveryOrderItemsSql, deliveryOrderItemRowMapper, id)

            // Group into DeliveryOrderSections
            val sections = deliveryOrderItems.groupBy { it.district ?: "null_district" }.map { (district, items) ->
                val actualDistrict = if (district == "null_district") null else district
                deliveryOrderSections(
                    district = actualDistrict,
                    totalQuantity = items.sumOf { it.quantity },
                    totalPendingQuantity = items.sumOf { it.pendingQuantity ?: 0 },
                    totalDeliveredQuantity = items.sumOf { it.deliveredQuantity ?: 0 },
                    status = items.firstOrNull()?.status ?: "",
                    deliveryOrderItems = items
                )
            }

            // Calculate grand totals
            val grandTotalQuantity = deliveryOrderItems.sumOf { it.quantity }
            val grandTotalPendingQuantity = deliveryOrderItems.sumOf { it.pendingQuantity ?: 0 }
            val grandTotalDeliveredQuantity = deliveryOrderItems.sumOf { it.deliveredQuantity ?: 0 }

            // Return the complete deliveryorder with sections and totals
            return deliveryOrder.copy(
                deliveryOrderSections = sections,
                grandTotalQuantity = grandTotalQuantity,
                grandTotalPendingQuantity = grandTotalPendingQuantity,
                grandTotalDeliveredQuantity = grandTotalDeliveredQuantity
            )
        } catch (ex: Exception) {
            println(ex.message)
            throw ex
        }
    }

    fun create(order: deliveryorder): deliveryorder {
        try {
            val sql = """
            INSERT INTO deliveryorder (id, contractId, partyId, dateOfContract, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """
            val created_at = Instant.now().toEpochMilli()
            jdbcTemplate.update(
                sql,
                order.id,
                order.contractId,
                order.partyId,
                order.dateOfContract,
                order.status,
                created_at
            )
            return order.copy(created_at = Instant.ofEpochMilli(created_at).atZone(ZoneId.systemDefault()).toLocalDateTime())
        } catch (e: Exception) {
            throw e
        }
    }

    @Transactional
    fun update(order: deliveryorder): deliveryorder {
        try {
            val sql = """
            UPDATE deliveryorder SET
                contractId = ?,
                partyId = ?,
                dateOfContract = ?,
                status = ?
            WHERE id = ?
        """
            return if (jdbcTemplate.update(
                    sql,
                    order.contractId,
                    order.partyId,
                    order.dateOfContract,
                    order.status,
                    order.id
                ) > 0
            ) {
                order
            } else {
                throw Exception("Failed to update delivery order")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteById(id: String): Int {
        try {
            return jdbcTemplate.update(
                "DELETE FROM deliveryorder WHERE id = ?",
                id
            )
        } catch (e: Exception) {
            throw e
        }
    }

    private val deliveryOrderItemMetaDataMapper = RowMapper { rs: ResultSet, _: Int ->
        DeliverOrderItemMetadata(
            id = rs.getString("id"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            locationName = rs.getString("locationName"),
            materialName = rs.getString("materialName"),
            quantity = rs.getDouble("quantity"),
            rate = rs.getDouble("rate"),
            dueDate = rs.getLong("duedate"),
            deliveredQuantity = 0.0,
        )
    }

    fun listDeliverOrderItemMetadata(deliveryOrderId: String): List<DeliverOrderItemMetadata> {
        val sql = """
    SELECT 
        doi.id,
        doi.district,
        doi.taluka,
        loc.name AS locationName,
        mat.name AS materialName,
        doi.quantity,
        doi.rate,
        doi.duedate,
        COALESCE(SUM(CASE 
            WHEN dc.status = 'delivered' THEN dci.deliveringquantity
            ELSE 0 
        END), 0) AS deliveredquantity
    FROM 
        deliveryorderitem doi
    JOIN 
        location loc ON doi.locationid = loc.id
    JOIN 
        material mat ON doi.materialid = mat.id
    LEFT JOIN 
        deliverychallanitem dci ON doi.id = dci.deliveryorderitemid
    LEFT JOIN 
        deliverychallan dc ON dci.deliverychallanid = dc.id
    WHERE 
        doi.deliveryorderid = ?
    GROUP BY 
        doi.id, doi.district, doi.taluka, loc.name, mat.name, 
        doi.quantity, doi.rate, doi.duedate
""".trimIndent()

        val res = jdbcTemplate.query(sql, { rs, _ ->
            DeliverOrderItemMetadata(
                id = rs.getString("id"),
                district = rs.getString("district"),
                taluka = rs.getString("taluka"),
                locationName = rs.getString("locationName"),
                materialName = rs.getString("materialName"),
                quantity = rs.getDouble("quantity"),
                rate = rs.getDouble("rate"),
                dueDate = rs.getLong("dueDate"),
                deliveredQuantity = rs.getDouble("deliveredQuantity")
            )
        }, deliveryOrderId)
        return res
    }

    private fun calculateDeliveredAndInProgressQuantities(item: DeliverOrderItemMetadata) {
//        println("Calculating quantities for DeliveryOrderItem ID: ${item.id}")

        val challanItemsSql = """
            SELECT
                dc.status,
                dci.deliveringquantity
            FROM deliverychallanitem as dci
            JOIN deliverychallan as dc ON dci.deliverychallanid = dc.id
            WHERE dci.deliveryorderitemid = ?
        """.trimIndent()

        val deliveryChallanItems = jdbcTemplate.query(
            challanItemsSql,
            { rs, _ ->
                object {
                    val status = rs.getString("status")
                    val deliveringQuantity = rs.getDouble("deliveringquantity")
                }
            }, item.id
        )
//        println("Number of deliveryChallanItems found: ${deliveryChallanItems.size}")


        var totalDeliveredQuantity = 0.0
        var totalInProgressQuantity = 0.0

        deliveryChallanItems.forEach {
//            println("ChallanItem status: ${it.status}, deliveringQuantity: ${it.deliveringQuantity}")
            when (it.status) {
                "DELIVERED" -> totalDeliveredQuantity += it.deliveringQuantity
                "IN_PROGRESS" -> totalInProgressQuantity += it.deliveringQuantity
                "pending" -> totalInProgressQuantity += it.deliveringQuantity  // Treat "pending" as in-progress
            }
        }

//        println("Total Delivered Quantity : ${totalDeliveredQuantity}")
//        println("Total InProgress Quantity : ${totalInProgressQuantity}")


        item.deliveredQuantity = totalDeliveredQuantity
    }
}