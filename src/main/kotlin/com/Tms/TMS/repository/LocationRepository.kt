package com.Tms.TMS.repository

import com.Tms.TMS.model.Location
import com.Tms.TMS.model.Party
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
@Repository
class LocationRepository(private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(LocationRepository::class.java)

    private val rowMapper = RowMapper { rs, _ ->
        Location(
            id = rs.getString("id"),
            name = rs.getString("name"),
            pointOfContact = rs.getString("pointOfContact"),
            contactNumber = rs.getString("contactNumber"),
            email = rs.getString("email"),
            addressLine1 = rs.getString("addressLine1"),
            addressLine2 = rs.getString("addressLine2"),
            state = rs.getString("state"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            city = rs.getString("city"),
            pincode = rs.getString("pincode"),
            status = rs.getString("status")
        )
    }

    fun getAlllocation(
        search: String?,
        district: List<String>,
        taluka: List<String>,
        statuses: List<String>,
        getAll: Boolean,
        page: Int,
        size: Int
    ): List<Location> {
        return try {
            val sqlBuilder = StringBuilder("SELECT * FROM location WHERE 1 = 1")

            if (district.isNotEmpty()) {
                sqlBuilder.append(" AND location.district IN (${district.joinToString(",") { "'$it'" }})")
            }

            if (taluka.isNotEmpty()) {
                sqlBuilder.append(" AND location.taluka IN (${taluka.joinToString(",") { "'$it'" }})")
            }

            if (statuses.isNotEmpty()) {
                sqlBuilder.append(" AND status IN (${statuses.joinToString(",") { "'$it'" }})")
            }

            if (!search.isNullOrBlank()) {
                sqlBuilder.append(" AND name ILIKE ?")
            }

            if (!getAll) {
                sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?")
            } else {
                sqlBuilder.append(" ORDER BY created_at DESC")
            }

            val sql = sqlBuilder.toString()
            val offset = (page - 1) * size
            val queryParams = mutableListOf<Any>()

            if (!search.isNullOrBlank()) {
                queryParams.add("%$search%")
            }
            queryParams.add(size)
            queryParams.add(offset)

            return jdbcTemplate.query(sql, rowMapper, *queryParams.toTypedArray())
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun getLocationById(id: String): Location? {
        return jdbcTemplate.queryForObject("SELECT * FROM location WHERE id = ?", rowMapper, id)
    }

    fun createLocation(location: Location): Location {
        val sql =
            "INSERT INTO location (id, name,pointOfContact, contactNumber, email, addressLine1, addressLine2, state, district, taluka, city, pincode) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            location.id,
            location.name,
            location.pointOfContact,
            location.contactNumber,
            location.email,
            location.addressLine1,
            location.addressLine2,
            location.state,
            location.district,
            location.taluka,
            location.city,
            location.pincode
        ) > 0
        val result = getLocationById(location.id!!)
        return result ?: throw Exception("Failed to retrieve created location")
    }

    fun updateLocation(id: String, location: Location): Int {
        return try {
            val sql = """
            UPDATE location
            SET name = ?,pointOfContact = ?, contactNumber = ?, email = ?, addressLine1 = ?, addressLine2 = ?, 
                state = ?, district = ?, taluka = ?, city = ?, pincode = ?
            WHERE id = ?
        """.trimIndent()
            return jdbcTemplate.update(
                sql,
                location.name,
                location.pointOfContact,
                location.contactNumber.toString(),
                location.email,
                location.addressLine1,
                location.addressLine2,
                location.state,
                location.district,
                location.taluka,
                location.city,
                location.pincode.toString(),
                id // This is where the ID should be passed to match the "WHERE id = ?"
            )
        } catch (ex: Exception) {
            throw Exception("Location not found")
        }
    }


    fun deleteLocation(id: String): Boolean {
        val sql = "DELETE FROM location WHERE id = ?"
        val answer = jdbcTemplate.update(sql, id) > 0
        if (answer) {
            return true
        }
        return false
    }

    fun deactivateLocation(id: String): Location {
        logger.info("Deactivating location with id: $id")
        try {

            val updateCount = jdbcTemplate.update(
                "UPDATE location SET status = 'inactive' WHERE id = ?",
                id
            )

            if (updateCount == 0) {
                throw ChangeSetPersister.NotFoundException()
            }

            return getLocationById(id) ?: throw Exception("Failed to retrieve updated employee")
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun activateLocation(id: String): Location {
        logger.info("Activating location with id: $id")
        try {
            val updateCount = jdbcTemplate.update(
                "UPDATE location SET status = 'active' WHERE id = ?",
                id
            )

            if (updateCount == 0) {
                throw ChangeSetPersister.NotFoundException()
            }

            return getLocationById(id) ?: throw Exception("Failed to retrieve updated employee")
        } catch (ex: Exception) {
            throw ex
        }
    }
}