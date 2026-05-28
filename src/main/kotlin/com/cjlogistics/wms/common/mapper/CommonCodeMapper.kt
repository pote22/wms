package com.cjlogistics.wms.common.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommonCodeMapper {
    fun selectCommonCodeList(map : Map<String, Any>)    : List<Map<String, Any>>
    fun selectCommonCodeCheck(map : Map<String, Any>)   : String?
    fun selectProdSearchList(map : Map<String, Any>)    : List<Map<String, Any>>
    fun selectClientSearchList(map : Map<String, Any>)  : List<Map<String, Any>>
    fun selectZoneSearchList(map : Map<String, Any>)    : List<Map<String, Any>>
    fun selectVehicleSearchList(map : Map<String, Any>) : List<Map<String, Any>>
    fun selectLocSearchList(map : Map<String, Any>)     : List<Map<String, Any>>
    // 엑셀 업로드 유효성 검증
    fun selectProdCheck(map : Map<String, Any>)         : String?
    fun selectZoneCheck(map : Map<String, Any>)         : String?
    fun selectLocCheck(map : Map<String, Any>)          : String?
}
