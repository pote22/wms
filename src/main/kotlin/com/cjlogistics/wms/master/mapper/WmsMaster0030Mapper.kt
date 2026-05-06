package com.cjlogistics.wms.master.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsMaster0030Mapper {
    fun selectProdList(map : Map<String, Any>)  : List<Map<String, Any>> 
    fun mergeProdInfo(map : Map<String, Any>)   : Int
    fun deleteProdInfo(map : Map<String, Any>)  : Int
} 