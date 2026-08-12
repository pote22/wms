package com.cjlogistics.wms.stock.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsStock0090Mapper {
    fun selectItrnList(map : Map<String, Any>) : List<Map<String, Any>>
}