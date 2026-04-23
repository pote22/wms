package com.cjlogistics.wms.master.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.mapper.WmsMaster0010Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsMaster0010Service(private val wmsMaster0010Mapper: WmsMaster0010Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0010Service::class.java)

    /** 차량목록 조회 */
    fun getList(paramMap: Map<String, Any>): Response {
        var list = wmsMaster0010Mapper.selectVehicleList(paramMap)

        return Response(
                resultCode = "0000",
                resultMessage = "정상적으로 처리되었습니다.",
                accessToken = "",
                expireDate = null,
                data = list
        )
    }

    /** 차량목록 저장 */
    fun saveVehicle(paramMap: Map<String, Any>): Response {
        var vehicles =
                paramMap["vehicles"] as? List<Map<String, Any>>
                        ?: throw IllegalArgumentException("저장할 데이터가 없습니다.")

        if (vehicles.isEmpty()) {
            throw IllegalArgumentException("저장할 데이터가 없습니다.")
        }

        vehicles.forEach { vehicle -> wmsMaster0010Mapper.mergeVehicleInfo(vehicle) }

        return Response(
                resultCode = "0000",
                resultMessage = "저장되었습니다.",
                accessToken = "",
                expireDate = null,
                data = null
        )
    }

    /** 차량목록 삭제 */
    fun removeVehicle(paramMap: Map<String, Any>): Response {
        val vehicleNos =
                paramMap["vehicleNos"] as? List<*>
                        ?: throw IllegalArgumentException("삭제할 항목을 선택해주세요.")

        if (vehicleNos.isEmpty()) {
            throw IllegalArgumentException("삭제할 항목을 선택해주세요.")
        }

        wmsMaster0010Mapper.deleteVehicleInfo(paramMap)

        return Response(
                resultCode = "0000",
                resultMessage = "삭제되었습니다.",
                accessToken = "",
                expireDate = null,
                data = null
        )
    }
}
