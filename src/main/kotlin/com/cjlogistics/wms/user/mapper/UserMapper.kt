package com.cjlogistics.wms.user.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper {
    fun findByLogin(request: Map<String, Any>): Map<String, Any>?
}
