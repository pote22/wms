package com.cjlogistics.wms.common.mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface CommonCodeMapper {
    fun getTonClsCdCheck(tonClsCd: String): String?
}
