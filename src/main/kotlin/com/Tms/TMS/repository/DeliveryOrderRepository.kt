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
            id = rs.getString("do_number"),
            contractId = rs.getString("contractId"),
            partyId = rs.getString("partyId"),
            partyName = rs.getString("partyName"),
            dateOfContract = rs.getLong("dateOfContract").takeIf { !rs.wasNull() },
            status = rs.getString("status"),
            created_at = rs.getLong("created_at")?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            },
            deliveryOrderSections = getdeliveryorderSections(rs.getString("do_number"))
        )
    }

    private val deliveryOrderItemRowMapper = RowMapper { rs: ResultSet, _: Int ->
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
            dueDate = rs.getLong("dueDate"),
            deliveredQuantity = 0.0
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
                d.do_number, 
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
                    id = rs.getString("do_number"),
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
                WHERE d.do_number = ?
            """

            val deliveryOrder = jdbcTemplate.queryForObject(deliveryOrderSql,
                RowMapper { rs, _ ->
                    deliveryorder(
                        id = rs.getString("do_number"),
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

            val deliveryOrderItemsSql = "SELECT * FROM DeliveryOrderItem WHERE do_number = ?"
            val deliveryOrderItems = jdbcTemplate.query(deliveryOrderItemsSql, deliveryOrderItemRowMapper, id)

            // Fetch Delivery Challan Items related to Delivery Order Items
            val deliveryChallanItemsSql = """
               SELECT dci.deliveryOrderItemId, dc.dc_number AS deliveryChallanId, dci.deliveringQuantity
                FROM deliveryChallanItem dci
                JOIN deliverychallan dc ON dci.dc_number = dc.dc_number
                WHERE dc.do_number = ?
            """
            val deliveryChallanItems = jdbcTemplate.query(deliveryChallanItemsSql,
                RowMapper{rs, _ ->
                    AssociatedDeliverChallanItemMetadata(
                        id = rs.getString("deliveryOrderItemId"),
                        deliveringQuantity = rs.getDouble("deliveringQuantity"),
                        deliveryChallanId = rs.getString("deliveryChallanId")
                    )
                }, id
            )

            // Group deliveryChallanItems by deliveryOrderItemId
            val deliveryChallanItemsGroupedByOrderItem = deliveryChallanItems.groupBy { it.id }


            val updatedDeliveryOrderItems = deliveryOrderItems.map { item ->
                val associatedDCs = deliveryChallanItemsGroupedByOrderItem[item.id] ?: emptyList()
                item.copy(associatedDeliveryChallanItems = associatedDCs)
            }

            val sections = updatedDeliveryOrderItems.groupBy { it.district ?: "null_district" }.map { (district, items) ->
                val actualDistrict = if (district == "null_district") null else district
                deliveryOrderSections(
                    district = actualDistrict,
                    totalQuantity = items.sumOf { it.quantity },
                    totalDeliveredQuantity = items.sumOf { it.deliveredQuantity },
//                    status = items.firstOrNull()?.status ?: "",
                    deliveryOrderItems = items
                )
            }

            val grandTotalQuantity = updatedDeliveryOrderItems.sumOf { it.quantity }
            val grandTotalDeliveredQuantity = updatedDeliveryOrderItems.sumOf { item ->
                item.associatedDeliveryChallanItems.sumOf { it.deliveringQuantity }
            }


            return deliveryOrder.copy(
                deliveryOrderSections = sections,
                grandTotalQuantity = grandTotalQuantity,
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
            INSERT INTO deliveryorder (do_number, contractId, partyId, dateOfContract, status, created_at)
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
             WHERE do_number = ?
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
                "DELETE FROM deliveryorder WHERE do_number = ?",
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
        deliverychallan dc ON dci.dc_number = dc.dc_number
    WHERE 
        doi.do_number = ?
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
    fun getLastDoNumber(): String? {
        val sql = "SELECT MAX(do_number) FROM deliveryorder"
        return jdbcTemplate.queryForObject(sql, String::class.java)
    }

    fun getDeliveryOrderWithDetails(doNumber: String): DeliveryOrderExportData? {
        val headerSql = """
            SELECT 
                d_order.do_number,
                d_order.dateofcontract,
                p.contactnumber as client_contact_number,
                p.name as party_name,
                (SELECT SUM(quantity) FROM deliveryorderitem WHERE do_number = ?) as total_quantity,
                (
                    SELECT COALESCE(SUM(dci.deliveringquantity), 0)
                    FROM deliverychallan dc
                    JOIN deliverychallanitem dci ON dc.dc_number = dci.dc_number
                    WHERE dc.do_number = ? AND dc.status = 'delivered'
                ) as total_delivered
            FROM    
                deliveryorder d_order
                LEFT JOIN party_location p ON d_order.partyid = p.id
            WHERE 
                d_order.do_number = ?
        """.trimIndent()

        val deliveryOrder = jdbcTemplate.queryForObject(headerSql, { rs, _ ->
            DeliveryOrderExportData(
                do_number = rs.getString("do_number"),
                totalQuantity = rs.getDouble("total_quantity"),
                totalDelivered = rs.getDouble("total_delivered"),
                clientContactNumber = rs.getString("client_contact_number"),
                partyName = rs.getString("party_name"),
                dateOfContract = rs.getLong("dateofcontract"),
                items = mutableListOf(),
                challans = mutableListOf()
            )
        }, doNumber, doNumber, doNumber) ?: return null

        // Get delivery order items
        val itemsSql = """
            SELECT 
                doi.id,
                doi.district,
                doi.taluka,
                l.name as location_name,
                m.name as material_name,
                doi.quantity,
                doi.rate,
                doi.duedate,
                COALESCE(
                    (
                        SELECT SUM(dci.deliveringquantity)
                        FROM deliverychallanitem dci
                        JOIN deliverychallan dc ON dci.dc_number = dc.dc_number
                        WHERE dci.deliveryorderitemid = doi.id AND dc.status = 'delivered'
                    ), 0
                ) as delivered_quantity
            FROM 
                deliveryorderitem doi
                LEFT JOIN location l ON doi.locationid = l.id
                LEFT JOIN material m ON doi.materialid = m.id
            WHERE 
                doi.do_number = ?
            ORDER BY 
                doi.district, doi.id
        """.trimIndent()

        val items = jdbcTemplate.query(itemsSql, { rs, _ ->
            DeliveryOrderItemExportData(
                district = rs.getString("district"),
                taluka = rs.getString("taluka"),
                locationName = rs.getString("location_name"),
                materialName = rs.getString("material_name"),
                quantity = rs.getDouble("quantity"),
                deliveredQuantity = rs.getDouble("delivered_quantity"),
                rate = rs.getDouble("rate"),
                dueDate = rs.getLong("duedate"),
                status = rs.getString("status")
            )
        }, doNumber)

        // Get challans
        val challansSql = """
            SELECT 
                dc.dc_number,
                dc.dateofchallan,
                dc.totaldeliveringquantity
            FROM 
                deliverychallan dc
            WHERE 
                dc.do_number = ?
            ORDER BY 
                dc.dateofchallan
        """.trimIndent()

        val challans = jdbcTemplate.query(challansSql, { rs, _ ->
            DeliveryChallanExportData(
                dc_number = rs.getString("dc_number"),
                dateOfChallan = rs.getLong("dateofchallan"),
                quantity = rs.getDouble("totaldeliveringquantity")
            )
        }, doNumber)

        return deliveryOrder.copy(
            items = items,
            challans = challans
        )
    }
}