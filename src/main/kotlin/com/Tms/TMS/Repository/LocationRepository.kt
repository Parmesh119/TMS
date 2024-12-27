package com.Tms.TMS.Repository

import com.Tms.TMS.Model.Location
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
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
        )
    }

    fun getAlllocation(): List<Location> {
        val sql = "SELECT * FROM location"
        return jdbcTemplate.query(sql, rowMapper)
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
}