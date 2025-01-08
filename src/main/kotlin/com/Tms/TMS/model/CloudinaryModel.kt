package com.Tms.TMS.model

data class CloudinaryFile(
    val publicId: String,
    val secureUrl: String? = null,
    val resourceType: String? = null,
    val format: String? = null,
    val version: Long? = null,
    val createdAt: String? = null,
    val originalFilename: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val bytes: Int? = null,
    val fileExtension : String? = null
)