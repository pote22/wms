package com.cjlogistics.wms.storage.service

import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun fileSave(file: MultipartFile, boardId: Int): String
    fun fileLoad(filePath: String): ByteArray
    fun fileDelete(filePath: String)
}
