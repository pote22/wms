package com.cjlogistics.wms.stock.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.stock.mapper.WmsStock0010Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsStock0010Service (
    private val wmsStock0010Mapper : WmsStock0010Mapper
) {

    private val LOG = LoggerFactory.getLogger(WmsStock0010Service::class.java)

    /** 재고현황 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val list = wmsStock0010Mapper.selectStockList(paramMap);

        return Response (
              resultCode      = "0000",
              resultMessage   = "정상적으로 처리되었습니다.",
              accessToken     = "",
              expireDate      = null,
              data            = list
        )
    }
}