package com.cjlogistics.wms.storage.service

import java.io.File
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@ConditionalOnProperty(name = ["file.storage"], havingValue = "local", matchIfMissing = true)
class LocalFileStorageService(@Value("\${file.upload-path}") private val uploadPath: String) :
        FileStorageService {

    // 파일 저장
    override fun fileSave(file: MultipartFile, boardId: Int): String {
        // 저장 경로 설정
        val dir = File("$uploadPath/board/$boardId")
        // 디렉토리 생성
        if (!dir.exists()) dir.mkdirs()
        // 파일명 생성
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        // 파일 저장
        val dest = File(dir, fileName)
        // 파일 전송
        file.transferTo(dest)
        // 파일 경로 반환
        return dest.absolutePath
    }

    // 파일 불러오기
    override fun fileLoad(filePath: String): ByteArray {
        return File(filePath).readBytes()
    }

    // 파일 삭제
    override fun fileDelete(filePath: String) {
        File(filePath).delete()
    }
}
