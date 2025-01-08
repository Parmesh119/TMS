package com.Tms.TMS.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Service
class FileService(private val cloudinary: Cloudinary) {

    fun uploadFile(file: MultipartFile): MutableMap<*, *>? {
        return try {
            val uploadParams = ObjectUtils.asMap(
                "resource_type", "auto"
            )
            val res = cloudinary.uploader().upload(file.bytes, uploadParams)
            res
        } catch (e: IOException) {
            throw RuntimeException("Error while uploading image : " + e.message)
        }
    }

    fun downloadFile(publicId: String): ByteArray? {
        try {
            val response = cloudinary.api().resource(publicId, ObjectUtils.emptyMap())
            val secureUrl = response["secure_url"] as String?
            if (secureUrl != null) {
                val url = URL(secureUrl)
                return url.openStream().readAllBytes()
            }
            return null
        } catch (e: Exception) {
            throw RuntimeException("Error while getting image: ${e.message}")
        }
    }

    fun getUploadDetails(publicId: String) : MutableMap<*, *>? {
        return  try{
            val response = cloudinary.api().resource(publicId, ObjectUtils.emptyMap())
            response
        }catch (e: Exception) {
            throw RuntimeException("Error while getting image: ${e.message}")
        }
    }
}
