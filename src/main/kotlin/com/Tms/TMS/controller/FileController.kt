package com.Tms.TMS.controller

import com.Tms.TMS.model.CloudinaryFile
import com.Tms.TMS.service.FileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@CrossOrigin
@RestController
@RequestMapping("/api/v1/files")
class FileController(private val fileService: FileService) {

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<CloudinaryFile> {
        val uploadResult: MutableMap<*, *> = fileService.uploadFile(file)
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file")

        val fileExtension = uploadResult["format"] as? String ?:
        uploadResult["public_id"] as String?

        val cloudinaryFile = CloudinaryFile(
            publicId = uploadResult["public_id"] as String,
            secureUrl = uploadResult["secure_url"] as? String,
            resourceType = uploadResult["resource_type"] as? String,
            format = uploadResult["format"] as? String,
            version = uploadResult["version"] as? Long,
            createdAt = uploadResult["created_at"] as? String,
            originalFilename = uploadResult["original_filename"] as? String,
            width = uploadResult["width"] as? Int,
            height = uploadResult["height"] as? Int,
            bytes = uploadResult["bytes"] as? Int,
            fileExtension = fileExtension
        )

        return ResponseEntity(cloudinaryFile, HttpStatus.OK)
    }

    @GetMapping("/download/{publicId}")
    fun downloadFile(@PathVariable publicId: String): ResponseEntity<ByteArray> {
        val fileBytes = fileService.downloadFile(publicId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND,"File not found")
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(fileBytes)
    }


    @GetMapping("/uploadDetails/{publicId}")
    fun getUploadDetails(@PathVariable publicId: String): ResponseEntity<CloudinaryFile> {

        val uploadResult = fileService.getUploadDetails(publicId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")

        val fileExtension = uploadResult["format"] as? String ?:
        uploadResult["public_id"] as String?
        val cloudinaryFile = CloudinaryFile(
            publicId = uploadResult["public_id"] as String,
            secureUrl = uploadResult["secure_url"] as? String,
            resourceType = uploadResult["resource_type"] as? String,
            format = uploadResult["format"] as? String,
            version = uploadResult["version"] as? Long,
            createdAt = uploadResult["created_at"] as? String,
            originalFilename = uploadResult["original_filename"] as? String,
            width = uploadResult["width"] as? Int,
            height = uploadResult["height"] as? Int,
            bytes = uploadResult["bytes"] as? Int,
            fileExtension = fileExtension
        )
        return ResponseEntity(cloudinaryFile, HttpStatus.OK)
    }
}