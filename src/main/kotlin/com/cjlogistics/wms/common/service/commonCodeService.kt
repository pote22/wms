package com.cjlogistics.wms.common.service

import com.cjlogistics.wms.common.mapper.CommonCodeMapper
import com.cjlogistics.wms.dto.Response
import org.springframework.stereotype.Service

@Service
class CommonCodeService(private val commonCodeMapper: CommonCodeMapper) {
    fun getTonList() : Response {
        val list = commonCodeMapper.selectVehicleTonList()

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }
}