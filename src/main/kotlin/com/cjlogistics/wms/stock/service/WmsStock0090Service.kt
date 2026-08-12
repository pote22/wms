package com.cjlogistics.wms.stock.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.stock.mapper.WmsStock0090Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsStock0090Service (
    private val wmsStock0090Mapper : WmsStock0090Mapper
) {

    private val LOG = LoggerFactory.getLogger(WmsStock0090Service::class.java)

    /** 트랜잭션 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val list = wmsStock0090Mapper.selectItrnList(paramMap);

        return Response (
              resultCode      = "0000",
              resultMessage   = "정상적으로 처리되었습니다.",
              accessToken     = "",
              expireDate      = null,
              data            = list
        )
    }
}
