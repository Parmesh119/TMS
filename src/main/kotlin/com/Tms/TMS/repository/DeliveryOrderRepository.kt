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
class DeliveryOrderRepository(private val jdbcTemplate: JdbcTemplate) {
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
            rate = rs.getFloat("rate"),
            unit = rs.getString("unit"),
            dueDate = rs.getLong("dueDate"),
            status = rs.getString("status")
        )
    }

    fun getdeliveryorderSections(deliveryOrderId: String): List<deliveryOrderSections> {
        return emptyList()
    }

    fun findAll(limit: Int, offset: Int, sortField: String, sortOrder: String): List<deliveryorder> {
        val sql = """
    SELECT 
        d.id AS id,
        d.contractId,
        d.partyId,
        p.name AS partyName,
        d.status,
        d.created_at,
        d.dateofcontract
    FROM DeliveryOrder d
    LEFT JOIN party_location p ON d.partyId = p.id
    ORDER BY created_at ${sortOrder}
    LIMIT ? OFFSET ?
"""

        return jdbcTemplate.query(sql, { rs, _ ->
            deliveryorder(
                id = rs.getString("id"),
                contractId = rs.getString("contractId"),
                partyId = rs.getString("partyId"),
                partyName = rs.getString("partyName"),  // Add this line
                status = rs.getString("status"),
                dateOfContract = rs.getLong("dateOfContract"),
                created_at = rs.getLong("created_at")?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                },
                deliveryOrderSections = mutableListOf()
            )
        }, limit, offset)
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
                    totalInProgressQuantity = items.sumOf { it.inProgressQuantity ?: 0 },
                    totalDeliveredQuantity = items.sumOf { it.deliveredQuantity ?: 0 },
                    status = items.firstOrNull()?.status ?: "",
                    deliveryOrderItems = items
                )
            }

            // Calculate grand totals
            val grandTotalQuantity = deliveryOrderItems.sumOf { it.quantity }
            val grandTotalPendingQuantity = deliveryOrderItems.sumOf { it.pendingQuantity ?: 0 }
            val grandTotalInProgressQuantity = deliveryOrderItems.sumOf { it.inProgressQuantity ?: 0 }
            val grandTotalDeliveredQuantity = deliveryOrderItems.sumOf { it.deliveredQuantity ?: 0 }

            // Return the complete deliveryorder with sections and totals
            return deliveryOrder.copy(
                deliveryOrderSections = sections,
                grandTotalQuantity = grandTotalQuantity,
                grandTotalPendingQuantity = grandTotalPendingQuantity,
                grandTotalInProgressQuantity = grandTotalInProgressQuantity,
                grandTotalDeliveredQuantity = grandTotalDeliveredQuantity
            )
        } catch (ex: Exception) {
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
            status = rs.getString("status"),
            rate = rs.getDouble("rate"),
            dueDate = rs.getLong("duedate"),
            deliveredQuantity = 0.0,
            inProgressQuantity = 0.0
        )
    }

    fun getDeliveryOrderItemById(deliveryOrderId: String): List<DeliverOrderItemMetadata> {
        return try {
            val sql = """
            select 
                doi.id,
                doi.district,
                doi.taluka,
                doi.quantity,
                doi.status,
                doi.rate,
                doi.duedate,
                m.name as materialName,
                lo.name as locationName
            from deliveryorderitem as doi
            join location as lo on lo.id = doi.locationid
            join material as m on m.id = doi.materialid
            where doi.deliveryorderid = ?
        """.trimIndent()

            val items = jdbcTemplate.query(sql, deliveryOrderItemMetaDataMapper, deliveryOrderId)

            // Fetch related DeliveryChallanItems and calculate delivered and inProgress quantities
            items.forEach { item ->
                calculateDeliveredAndInProgressQuantities(item)
            }

            return items
        } catch (e: Exception) {
            throw e;
        }
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
        item.inProgressQuantity = totalInProgressQuantity
    }
}