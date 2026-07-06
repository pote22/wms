package com.cjlogistics.wms.common.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommonMapper {
    fun insertUserLog(map: Map<String, Any>): Int
}
