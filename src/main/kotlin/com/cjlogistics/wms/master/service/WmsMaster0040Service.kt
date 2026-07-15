package com.cjlogistics.wms.master.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.mapper.WmsMaster0040Mapper
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlin.collections.emptyList

@Service
class WmsMaster0040Service(private val wmsMaster0040Mapper : WmsMaster0040Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0040Service::class.java)

    /** 센터별 존 목록 조회 */
    fun getZoneList(paramMap : Map<String, Any>) : Response {
        val list = wmsMaster0040Mapper.selectZoneList(paramMap);

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    /** 존별 로케이션 조회 */
    fun getLocList(paramMap : Map<String, Any>) : Response {
        val list = wmsMaster0040Mapper.selectLocList(paramMap);

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    /** 존&로케이션 정보 저장&수정 */
    fun saveInfo(paramMap : Map<String, Any>) : Response {
        var zoneList    = paramMap["zoneList"]  as? List<Map<String, Any>> ?: emptyList();
        var locList     = paramMap["locList"]   as? List<Map<String, Any>> ?: emptyList();
        
        fun extractMessage(throwable: Throwable, defaultMessage: String): String {
            var current: Throwable? = throwable
            while (current != null) {
                val message = current.message?.trim()
                if (!message.isNullOrEmpty()) {
                    return message
                }
                current = current.cause
            }
            return defaultMessage
        }
        
        if (zoneList.isEmpty() && locList.isEmpty()) {
            throw IllegalArgumentException("저장할 데이터가 없습니다.")
        }

        // 존&로케이션 정보 저장
        zoneList.forEach { v ->
            try {
                wmsMaster0040Mapper.mergeZoneInfo(v)
            } catch (e: Exception) {
                throw IllegalArgumentException(extractMessage(e, "존 정보 저장 중 오류가 발생했습니다."))
            }
        }

        locList.forEach { v ->
            try {
                wmsMaster0040Mapper.mergeLocInfo(v)
            } catch (e: Exception) {
                throw IllegalArgumentException(extractMessage(e, "로케이션 정보 저장 중 오류가 발생했습니다."))
            }
        }
        
        return Response (
            resultCode      = "0000",
            resultMessage   = "저장되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = null
        )
    }

    /** 엑셀업로드 : 유효성 체크 */
    fun getCheckList(paramMap : Map<String, Any>) : Response {
        val list = paramMap["zoneList"] as? List<Map<String, Any>> ?: emptyList();

        return Response(
            resultCode      = "0000",
            resultMessage   = "검증완료",
            accessToken     = "",
            expireDate      = null,
            data            = setCheckRows(list)
        ) 
    }

    /** 엑셀업로드 : 유효성 검증 */
    private fun setCheckRows(list : List<Map<String, Any>>) : List<Map<String, Any>> {
        val nullRegx  = Regex("\\u0000")                // 널값체크

        return list.mapIndexed { idx, v ->
            val errors      = mutableListOf<String>()
            val zoneCd      = v["zoneCd"]?.toString()?.replace(nullRegx, "")?.trim() ?: "";

            if (zoneCd.isEmpty()) {
                errors.add("존코드 ")
            }

            mapOf("rowIndex" to idx, "isValid" to errors.isEmpty(), "errors" to errors)
        }
    }
}
