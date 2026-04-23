package com.cjlogistics.wms.master.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsMaster0010Mapper {
    fun selectVehicleList(map: Map<String, Any>): List<Map<String, Any>>
    fun mergeVehicleInfo(map: Map<String, Any>): Int
    fun deleteVehicleInfo(map: Map<String, Any>): Int
}
