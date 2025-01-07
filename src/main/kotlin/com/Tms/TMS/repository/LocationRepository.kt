package com.Tms.TMS.repository

import com.Tms.TMS.model.Location
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.data.crossstore.ChangeSetPersister

@Component
@Repository
class LocationRepository(private val jdbcTemplate: JdbcTemplate) {
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
        search: String = "",
        districts: List<String> = emptyList(),
        talukas: List<String> = emptyList(),
        statuses: List<String> = emptyList(),
        getAll: Boolean = false,
        page: Int = 1,
        size: Int = 10,
    ): List<Location> {
        return try {
            val sqlBuilder = StringBuilder("SELECT * FROM location WHERE 1=1")
            val params = mutableListOf<Any>()

            if (search.isNotBlank()) {
                sqlBuilder.append(" AND name ILIKE ?")
                params.add("%$search%")
            }

            if (districts.isNotEmpty()) {
                sqlBuilder.append(" AND district IN (${districts.joinToString(",") { "?" }})")
                params.addAll(districts)
            }

            if (talukas.isNotEmpty()) {
                sqlBuilder.append(" AND taluka IN (${talukas.joinToString(",") { "?" }})")
                params.addAll(talukas)
            }

            if (statuses.isNotEmpty()) {
                sqlBuilder.append(" AND status IN (${statuses.joinToString(",") { "?" }})")
                params.addAll(statuses)
            }

            sqlBuilder.append(" ORDER BY created_at DESC")

            if (!getAll) {
                sqlBuilder.append(" LIMIT ? OFFSET ?")
                params.add(size)
                params.add((page - 1) * size)
            }

            val sql = sqlBuilder.toString()

            val locations = jdbcTemplate.query(sql, params.toTypedArray()) { rs, _ -> Location(
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
            ) }

            return locations
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