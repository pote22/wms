package com.cjlogistics.wms.stock.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsStock0010Mapper {
    fun selectStockList(map : Map<String, Any>) : List<Map<String, Any>>
}