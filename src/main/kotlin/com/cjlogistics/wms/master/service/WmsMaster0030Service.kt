package com.cjlogistics.wms.master.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.mapper.WmsMaster0030Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsMaster0030Service (private val wmsMaster0030Mapper : WmsMaster0030Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0030Service::class.java)

    /** 품목목록 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val list = wmsMaster0030Mapper.selectProdList(paramMap)
        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    /** 품목정보 저장 */
    fun saveProdInfo(paramMap : Map<String, Any>) : Response {
        val prodList = paramMap["prodList"] as? List<Map<String, Any>> ?: throw IllegalArgumentException("저장할 데이터가 없습니다.")

        if (prodList.isEmpty()) {
            throw IllegalArgumentException("저장할 데이터가 없습니다.")
        }

        // 저장수행
        prodList.forEach {
            prodInfo -> wmsMaster0030Mapper.mergeProdInfo(prodInfo)
        }

        return Response (
            resultCode      = "0000",
            resultMessage   = "저장되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = null
        )
    }

    /** 품목삭제 */
    fun removeProdInfo(paramMap : Map<String, Any>) : Response {
        var prodList = paramMap["prodList"] as? List<Map<String, Any>> ?: throw IllegalArgumentException("삭제할 항목을 선택해주세요")

        if (prodList.isEmpty()) {
            throw IllegalArgumentException("삭제할 항목을 선택해주세요")
        }

        // 삭제
        wmsMaster0030Mapper.deleteProdInfo(paramMap)

        return Response (
            resultCode      = "0000",
            resultMessage   = "삭제되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = null
        )
    }

    /** 엑셀업로드 : 유효성 체크 */
    fun getCheckList(paramMap : Map<String, Any>) : Response {
        val list = paramMap["prodList"] as? List<Map<String, Any>> ?: emptyList()
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
        val intRegx   = Regex("^[0-9]+$")               // 0 ~ 9 정수체크 (음수체크)
        val floatRegx = Regex("^[0-9]*\\.?[0-9]*$")     // 0 ~ 9 실수체크 (음수체크)
        val nullRegx  = Regex("\\u0000")                // 널값체크

        return list.mapIndexed { idx, prod ->
            val errors     = mutableListOf<String>()
            val prodCd     = prod["prodCd"]?.toString()?.replace(nullRegx, "")?.trim()      ?: ""
            val price      = prod["price"]?.toString()?.replace(nullRegx, "")?.trim()       ?: ""
            val innerpack  = prod["innerpack"]?.toString()?.replace(nullRegx, "")?.trim()   ?: ""
            val weight     = prod["weight"]?.toString()?.replace(nullRegx, "")?.trim()      ?: ""
            val realWeight = prod["realWeight"]?.toString()?.replace(nullRegx, "")?.trim()  ?: ""

            if (prodCd.isEmpty()) {
                errors.add("품목번호 ")
            }
                
            if (price.isNotEmpty() && !price.matches(intRegx)) {
                errors.add("단가 ")
            }

            if (innerpack.isNotEmpty() && !innerpack.matches(intRegx)) {
                errors.add("용기:수량 ")
            }
            
            if (weight.isNotEmpty() && !weight.matches(floatRegx)) {
                errors.add("중량 ")
            }     

            if (realWeight.isNotEmpty() && !realWeight.matches(floatRegx)) {
                errors.add("실중량 ")
            }

            mapOf("rowIndex" to idx, "isValid" to errors.isEmpty(), "errors" to errors)
        }
    }
}