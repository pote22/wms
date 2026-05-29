package com.cjlogistics.wms.receipt.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsReceipt0020Mapper {
    fun selectReceiptList(map : Map<String, Any>)           : List<Map<String, Any>>
    fun updateReceiptRmkInfo(map : Map<String, Any>)        : Int
    fun updateReceiptConfirmInfo(map : Map<String, Any>)    : Int
}