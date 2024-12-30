package com.Tms.TMS.repository

import com.Tms.TMS.model.Material
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
@Repository
class MaterialRepository(private val jdbcTemplate: JdbcTemplate) {
    private val rowMapper = RowMapper { rs, _ ->
        Material(
            id = rs.getString("id"),
            name = rs.getString("name"),
        )
    }

        fun listAllMaterial(): List<Material> {
            return jdbcTemplate.query("SELECT * FROM material", rowMapper)
        }

        fun getMaterialById(id: String): Material? {
            return jdbcTemplate.queryForObject("SELECT * FROM material WHERE id =?", rowMapper, id)
        }

        fun createMaterial(material: Material): Material {
            val sql =
                "INSERT INTO material (id, name) " +
                        "VALUES (?, ?)"

            val answer = jdbcTemplate.update(
                sql,
                material.id,
                material.name
            ) > 0
            val result = getMaterialById(material.id!!)
            return result ?: throw Exception("Failed to retrieve created material")
        }

        fun updateMaterial(id: String, material: Material): Int {
            return try {
                val sql = """
                UPDATE material
                SET name = ?
                WHERE id = ?
            """.trimIndent()
                jdbcTemplate.update(sql, material.name, id)
            } catch (ex: Exception) {
                throw Exception("Material not found")
            }
        }
    }