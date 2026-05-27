package com.cjlogistics.wms.receipt.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsReceipt0010Mapper {
    fun selectRcptHdrList(map : Map<String, Any>)       : List<Map<String, Any>>
    fun selectRcptDtlList(map : Map<String, Any>)       : List<Map<String, Any>>
    fun selectRcptKeyInfo(map : Map<String, Any>)       : List<Map<String, Any>>
    fun selectRcptStatusInfo(map : Map<String, Any>)    : String?
    fun insertRcptHdrInfo(map : Map<String, Any>)       : Int
    fun insertRcptDtlInfo(map : Map<String, Any>)       : Int
}