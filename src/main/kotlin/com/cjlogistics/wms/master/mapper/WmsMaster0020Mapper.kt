package com.cjlogistics.wms.master.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsMaster0020Mapper {
    fun selectClientList(paramMap : Map<String, Any>)   : List<Map<String, Any>>
    fun mergeClientInfo(paramMap : Map<String, Any>)    : Int
    fun deleteClientInfo(paramMap : Map<String, Any>)   : Int
}