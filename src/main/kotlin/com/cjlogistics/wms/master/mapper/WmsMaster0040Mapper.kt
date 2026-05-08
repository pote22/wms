package com.cjlogistics.wms.master.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsMaster0040Mapper {
    fun selectZoneList(map : Map<String, Any>)  : List<Map<String, Any>>
    fun selectLocList(map : Map<String, Any>)   : List<Map<String, Any>>
    fun mergeZoneInfo(map : Map<String, Any>)   : Int
    fun mergeLocInfo(map : Map<String, Any>)    : Int
}