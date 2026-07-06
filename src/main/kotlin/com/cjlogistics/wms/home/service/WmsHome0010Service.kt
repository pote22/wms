package com.cjlogistics.wms.home.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.home.mapper.WmsHome0010Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsHome0010Service(
        private val wmsHome0010Mapper : WmsHome0010Mapper,
        private val wmsHomeFileService : WmsHome0010FileService
) {

    private val LOG = LoggerFactory.getLogger(WmsHome0010Service::class.java)

    fun getList(paramMap: Map<String, Any>): Response {
        LOG.info("---> GET_LIST Service 호출")
        
        // 공지사항 조회
        var list = wmsHome0010Mapper.selectList(paramMap)
        
        return Response(
                resultCode      = "0000",
                resultMessage   = "조회완료",
                accessToken     = "",
                expireDate      = null,
                data            = list
        )
    }

    fun saveList(paramMap: Map<String, Any>): Response {
        LOG.info("---> SAVE_LIST Service 호출")
        val boardId     = (paramMap["boardId"] as? Number)?.toInt()?.takeIf { it > 0 } ?: wmsHome0010Mapper.getNextBoardId()
        val newParamMap = paramMap.toMutableMap().apply { put("boardId", boardId) }
       
        // 공지사항 저장
        wmsHome0010Mapper.saveNotice(newParamMap)
        
        return Response(
                resultCode      = "0000",
                resultMessage   = "저장완료",
                accessToken     = "",
                expireDate      = null,
                data            = listOf(mapOf("board_id" to boardId))
        )
    }

    fun deleteList(paramMap: Map<String, Any>): Response {
        LOG.info("---> DELETE_LIST Service 호출")

        // 1-1. 삭제할 boardId 목록 가져오기
        val boardIds = paramMap["boardIds"] as? List<*> ?: throw IllegalArgumentException("삭제할 항목을 선택해주세요.")

        // 1-2. boardId가 없는경우
        if (boardIds.isEmpty()) {
            throw IllegalArgumentException("삭제할 항목을 선택해주세요.")
        }

        /*
        // 2-1. 첨부파일 삭제
        val files = wmsHome0010Mapper.selectFileListByBoardIds(paramMap)

        // 2-2. 첨부파일이 존재하는 경우
        if (files.length > 0) {
            files.forEach { file ->
                // 2-2-1. 파일 삭제
                wmsHomeFileService.deleteFile(mapOf("fileId" to file.fileId!!))
            }
        }
        */
        
        // 공지사항 삭제
        wmsHome0010Mapper.deleteNotice(paramMap)

        return Response(
                resultCode      = "0000",
                resultMessage   = "삭제완료",
                accessToken     = "",
                expireDate      = null,
                data            = null
        )
    }
}
