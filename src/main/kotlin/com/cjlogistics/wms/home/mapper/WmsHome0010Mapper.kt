package com.cjlogistics.wms.home.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface WmsHome0010Mapper {
    fun selectList(paramMap: Map<String, Any>): List<Map<String, Any>>
    fun getNextBoardId(): Int
    fun saveNotice(paramMap: Map<String, Any>): Int
    fun deleteNotice(paramMap: Map<String, Any>): Int
    fun selectFileList(paramMap: Map<String, Any>): List<Map<String, Any>>
    fun selectFileInfo(paramMap: Map<String, Any>): Map<String, Any>?
    fun insertFile(paramMap: Map<String, Any>): Int
    fun deleteFile(paramMap: Map<String, Any>): Int
}
