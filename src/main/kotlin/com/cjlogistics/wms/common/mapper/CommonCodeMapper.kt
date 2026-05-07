package com.cjlogistics.wms.common.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommonCodeMapper {
    fun selectVehicleTonList(): List<Map<String, Any>>
    fun selectTonClsCdCheck(tonClsCd: String): String?
    fun selectProdSearchList(map: Map<String, Any>): List<Map<String, Any>>
}
