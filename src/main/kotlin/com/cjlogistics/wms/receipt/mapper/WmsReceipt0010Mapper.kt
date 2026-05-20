package com.cjlogistics.wms.receipt.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsReceipt0010Mapper {
    fun selectReceiptHeaderList(map : Map<String, Any>) : List<Map<String, Any>>
    fun selectReceiptDetailList(map : Map<String, Any>) : List<Map<String, Any>>
    fun selectReceptKeyInfo(map : Map<String, Any>)     : List<Map<String, Any>>
    fun mergeReceiptHeaderInfo(map : Map<String, Any>)  : Int
    fun mergeReceiptDetailInfo(map : Map<String, Any>)  : Int
}