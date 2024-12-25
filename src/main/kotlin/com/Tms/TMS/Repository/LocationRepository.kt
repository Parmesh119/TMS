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
            point_of_contact = rs.getString("point_of_contact"),
            phone = rs.getInt("phone"),
            email = rs.getString("email"),
            address1 = rs.getString("address1"),
            address2 = rs.getString("address2"),
            state = rs.getString("state"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            city = rs.getString("city"),
            zipcode = rs.getInt("zipcode"),
        )
    }

    fun getAlllocation(): List<Location> {
        val sql = "SELECT * FROM location"
        return jdbcTemplate.query(sql, rowMapper)
    }

    fun getLocationById(id: String): Location? {
        return jdbcTemplate.queryForObject("SELECT * FROM location WHERE id = ?", rowMapper, id)
    }

    fun createLocation(location: Location): Boolean {
        val sql =
            "INSERT INTO location (id, name, point_of_contact, phone, email, address1, address2, state, district, taluka, city, zipcode) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            location.id,
            location.name,
            location.point_of_contact,
            location.phone,
            location.email,
            location.address1,
            location.address2,
            location.state,
            location.district,
            location.taluka,
            location.city,
            location.zipcode
        ) > 0
        if (answer) {
            return true
        }
        return false
    }

    fun updateLocation(id: String): Location? {
        val sql = "UPDATE location SET name = ?, point_of_contact = ?, phone = ?, email = ?, address1 = ?, address2 = ?, state = ?, district = ?, taluka = ?, city = ?, zipcode = ? WHERE id = ?"
        return jdbcTemplate.queryForObject(sql, rowMapper, id)
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