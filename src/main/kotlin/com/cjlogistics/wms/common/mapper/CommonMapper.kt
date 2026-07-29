package com.cjlogistics.wms.common.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommonMapper {
    fun insertUserLog(map: Map<String, Any>): Int
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
