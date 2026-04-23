package com.cjlogistics.wms.home.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.home.mapper.WmsHome0010Mapper
import com.cjlogistics.wms.storage.service.FileStorageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Service
class WmsHome0010FileService(
        private val wmsHome0010Mapper: WmsHome0010Mapper,
        private val fileStorageService: FileStorageService
) {
    private val LOG = LoggerFactory.getLogger(WmsHome0010FileService::class.java)

    /** 첨부파일 조회 */
    fun getFileList(paramMap: Map<String, Any>): Response {
        LOG.info("---> GET_FILE_LIST Service 호출")
        var list = wmsHome0010Mapper.selectFileList(paramMap)
        return Response(
                resultCode = "0000",
                resultMessage = "조회완료",
                accessToken = "",
                expireDate = null,
                data = list
        )
    }

    /** 첨부파일 업로드 */
    fun uploadFile(boardId: Int, userId: String, files: List<MultipartFile>): Response {
        LOG.info("---> UPLOAD_FILE Service 호출")
        files.forEach { file ->
            // 파일 저장
            var filePath = fileStorageService.fileSave(file, boardId)
            // 파일 정보 저장
            wmsHome0010Mapper.insertFile(
                    mapOf(
                            "boardId" to boardId,
                            "userId" to userId,
                            "fileNm" to (file.originalFilename ?: "unknown"),
                            "fileSize" to String.format("%.1f MB", file.size / (1024.0 * 1024.0)),
                            "filePath" to filePath,
                    )
            )
        }

        return Response(
                resultCode = "0000",
                resultMessage = "저장완료",
                accessToken = "",
                expireDate = null,
                data = null
        )
    }

    /** 첨부파일 다운로드 */
    fun downloadFile(fileId: Int): ResponseEntity<ByteArray> {
        LOG.info("---> DOWNLOAD_FILE Service 호출")
        var fileMeta = wmsHome0010Mapper.selectFileInfo(mapOf("fileId" to fileId)) ?: throw IllegalArgumentException("파일을 찾을 수 없습니다.")
        var bytes = fileStorageService.fileLoad(fileMeta["file_path"].toString())

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileMeta["file_nm"]}\"")
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .body(bytes)
        
    }

    /** 파일 삭제 */
    fun deleteFile(paramMap: Map<String, Any>): Response {
          LOG.info("---> DELETE_FILE Service 호출")
          val fileId = (paramMap["fileId"] as Number).toInt()
          val fileMeta = wmsHome0010Mapper.selectFileInfo(mapOf("fileId" to fileId))
              ?: throw IllegalArgumentException("파일을 찾을 수 없습니다.")
          fileStorageService.fileDelete(fileMeta["file_path"].toString())
          wmsHome0010Mapper.deleteFile(mapOf("fileId" to fileId))

          return Response(
                resultCode = "0000",
                resultMessage = "삭제완료",
                accessToken = "",
                expireDate = null,
                data = null
        )
      }
}
