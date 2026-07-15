package com.cjlogistics.wms.master.service

import com.cjlogistics.wms.common.mapper.CommonCodeMapper
import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.mapper.WmsMaster0020Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsMaster0020Service (private val wmsMaster0020Mapper : WmsMaster0020Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0020Service::class.java)

    /** 거래처 목록 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val list = wmsMaster0020Mapper.selectClientList(paramMap)

        return Response (
            resultCode = "0000",
            resultMessage = "정상적으로 처리되었습니다.",
            accessToken = "",
            expireDate = null,
            data = list
        )
    }

    /** 거래처 정보 등록 & 업데이트 */
    fun saveClientInfo(paramMap : Map<String, Any>) : Response {
        val list = paramMap["clientList"] as? List<Map<String, Any>> 
        ?: throw IllegalArgumentException("저장할 데이터가 없습니다.")

        if (list.isEmpty()) {
            throw IllegalArgumentException("저장할 데이터가 없습니다.")
        }

        list.forEach {
            info -> wmsMaster0020Mapper.mergeClientInfo(info)
        }
        
        return Response (
            resultCode = "0000",
            resultMessage = "저장되었습니다.",
            accessToken = "",
            expireDate = null,
            data = null
        )
    }

    /** 거래처 정보 삭제 */
    fun removeClientInfo(paramMap : Map<String, Any>) : Response {
        val list = paramMap["clientCdList"] as? List<*> ?: throw IllegalArgumentException("삭제할 항목을 선택해주세요.")

        if (list.isEmpty()) {
            throw IllegalArgumentException("삭제할 항목을 선택해주세요.")
        }

        wmsMaster0020Mapper.deleteClientInfo(paramMap)

        return Response (
            resultCode = "0000",
            resultMessage = "삭제되었습니다.",
            accessToken = "",
            expireDate = null,
            data = null
        )
    }

    /** 엑셀업로드 : 유효성 체크 */
    fun getCheckList(paramMap : Map<String, Any>) : Response {
        val list = paramMap["clientList"] as? List<Map<String, Any>> ?: emptyList()

        return Response(
            resultCode      = "0000",
            resultMessage   = "검증완료",
            accessToken     = "",
            expireDate      = null,
            data            = setCheckRows(list)
        )
    }

    /** 파라메터 값 유효 체크 */
    fun setCheckRows(list : List<Map<String, Any>>) : List<Map<String, Any>> {
        val nullRegx  = Regex("\\u0000")                                            // 널값정규식체크
        val hpNoRegx  = Regex("^0\\d{1,2}\\d{3,4}\\d{4}$")                        // 휴대전화정규식체크
        val emailRegx = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$") // 이메일정규식체크

        return list.mapIndexed { idx, v ->
            val errors      = mutableListOf<String>()
            val clientCd        = v["clientCd"]?.toString()?.replace(nullRegx, "")?.trim() ?: ""
            val businessNo      = v["businessNo"]?.toString()?.replace(nullRegx, "")?.trim() ?: ""
            val presidentHp     = v["presidentHp"]?.toString()?.replace(nullRegx, "")?.replace("-", "")?.trim() ?: ""
            var presidentEmail  = v["presidentEmail"]?.toString()?.replace(nullRegx, "")?.trim() ?: ""

            if (clientCd.isEmpty()) {
                errors.add("거래처코드 ")
            }

            if (businessNo.isNotEmpty() && businessNo.length < 10) {
                errors.add("사업자번호 ")
            }

            if (presidentHp.isNotEmpty() && !hpNoRegx.matches(presidentHp)) {
                errors.add("연락처 ")
            }

            if (presidentEmail.isNotEmpty() && !emailRegx.matches(presidentEmail)) {
                errors.add("이메일 ")
            }
            
            mapOf("rowIndex" to idx, "isValid" to errors.isEmpty(), "errors" to errors)
        }
    }
}
