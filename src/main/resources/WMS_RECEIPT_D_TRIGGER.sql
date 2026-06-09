/********************************************************
 * 1. 프로그램 ID : WMS.FN_TB_RECEIPT_D_CONFIRM 
 * 2. 유형       : 트리거 함수(FUNCTION)
 * 3. 사용언어    : PL/SQL (PROGRESQL)
 * 4. 기능정의		: 입고상세(WMS.TB_RECEIPT_D) 트리거 함수
 * 5. 입력변수		: 
 * 6. 리턴값		:
 * 7. 변경이력		: N/A
 * 8. 버전관리
 * ------------------------------------------------------
 *  VERSION		작성자	일자			내용
 * ------------------------------------------------------		
 *  1.0			허민		26.06.08	 입고상세(WMS.TB_RECEIPT_D) 트리거 함수 작성
 * 
 ********************************************************/

CREATE OR REPLACE FUNCTION WMS.FN_TB_RECEIPT_D_CONFIRM ()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
DECLARE
	V_CHKCNT			NUMERIC								:= NULL;								-- 프로세스 수행 개수
	
	V_VENDOR_CD			WMS.TB_RECEIPT_H.VENDOR_CD%TYPE  	:= NULL;
	V_VENDOR_NM			WMS.TB_RECEIPT_H.VENDOR_NM%TYPE  	:= NULL;
	
	V_PROC_NAME			VARCHAR(100)						:= NULL;								-- 프로시저명
	V_PROC_PARAM		VARCHAR(1000)						:= NULL;								-- 파라메터명
	V_RTRN_MSG			VARCHAR(1000)						:= NULL;								-- 처리메세지
	
	V_CHKRECEIVED_QTY	WMS.TB_RECEIPT_D.RECEIVED_QTY%TYPE 	:= 0;									-- 입고디테일 총 확정량
	V_LOT_YN			VARCHAR(1)							:= 'N';									-- 입고일자 관리여부
	V_RMK				WMS.TB_RECEIPT_D.RMK%TYPE			:= NULL;								-- 입고디테일 비고 사용
	V_LOT_NO			WMS.TB_RECEIPT_D.LOT_NO%TYPE		:= NULL;								-- LOT번호(입고일자)
	
BEGIN
	V_PROC_NAME		:= 'WMS.FN_TB_RECEIPT_D_CONFIRM';
	V_PROC_PARAM	:= '';
	V_RTRN_MSG     	:= '';
	
	-- 재고 입고일자 관리여부 확인
	-- 1021 : 석기전자
	IF NEW.SRVC_CD IN ('1201') THEN
		V_LOT_YN 	:= 'Y';
		V_LOT_NO	:= NEW.LOT_NO;
	END IF;
	
	-- 입고 완료시 처리
	IF OLD.STATUS != '09' AND NEW.STATUS = '09' THEN
		-- 입고헤더 수량 업데이트
		BEGIN
			 UPDATE WMS.TB_RECEIPT_H
		        SET OPEN_QTY = COALESCE(OPEN_QTY, 0) + COALESCE(NEW.OPEN_QTY, 0)
		          , RECEIVED_QTY = COALESCE(RECEIVED_QTY, 0) + COALESCE(NEW.RECEIVED_QTY, 0)
		          , UPD_ID = NEW.UPD_ID
		          , UPD_DATE = NOW()
		      WHERE SRVC_CD = NEW.SRVC_CD
		        AND WH_CD = NEW.WH_CD
		        AND IN_NO = NEW.IN_NO;
		
		EXCEPTION
		   	 WHEN OTHERS THEN
		        V_RTRN_MSG := 'UPDATE TB_RECEIPT_H(수량업데이트) : ' || SQLERRM;
		        RAISE EXCEPTION '%', V_RTRN_MSG;
		END;

		-- 입고헤더 상태 업데이트
		BEGIN
			UPDATE WMS.TB_RECEIPT_H
			   SET STATUS = CASE WHEN EXPECTED_QTY = 0 OR EXPECTED_QTY = RECEIVED_QTY THEN '09'		-- 입고완료(정상)
							     WHEN EXPECTED_QTY < 0 OR EXPECTED_QTY = RECEIVED_QTY THEN '09'		-- 입고완료(-재고처리)
								 WHEN RECEIVED_QTY > 0 AND EXPECTED_QTY <> RECEIVED_QTY THEN '01' 	-- 부분입고
								 WHEN RECEIVED_QTY = 0 AND EXPECTED_QTY > 0 THEN '00'				-- 입고예정
								 ELSE '99'															-- 기타
						     END
                 , RECEIPT_DATE = NOW()
                 , REG_ID = NEW.REG_ID
				 , REG_DATE = NOW()
			 WHERE SRVC_CD = NEW.SRVC_CD
               AND WH_CD = NEW.WH_CD
               AND IN_NO = NEW.IN_NO;			   
			   
		EXCEPTION
		     WHEN OTHERS THEN
				V_RTRN_MSG := 'UPDATE TB_RECEIPT_H(상태업데이트) : ' || SQLERRM;
				RAISE EXCEPTION '%', V_RTRN_MSG;
		END;
		
		BEGIN
			-- 재고 증차감 (확정수량이 있을때만)
			IF NEW.RECEIVED_QTY > 0 THEN
				CALL WMS.PR_STOCK_QTY_MGNT(
					NEW.SRVC_CD
				  , NEW.WH_CD
				  , NEW.IN_ZONE_CD
				  , NEW.IN_LOC_CD
				  , NEW.PROD_CD
				  , COALESCE(V_LOT_NO, TO_CHAR(NOW(), 'YYYYMMDD'))
				  , 'DP'
				  , COALESCE(NEW.RECEIVED_QTY, 0) 
				  , NEW.PLT_ID
				  , 'N'
				  , NEW.RMK
				  , COALESCE(NEW.PLT_QTY, 0)
				  , COALESCE(NEW.CARTON_QTY, 0)
				  , NEW.REG_ID
				);
			END IF;
			
			EXCEPTION
			     WHEN OTHERS THEN
					V_RTRN_MSG := 'WMS.PR_STOCK_QTY_MGNT(재고증차감) : ' || SQLERRM;
					RAISE EXCEPTION '%', V_RTRN_MSG;
		END;
		
		BEGIN
			-- 고객사 조회
			SELECT VENDOR_CD
			     , VENDOR_NM
			  INTO V_VENDOR_CD
			     , V_VENDOR_NM
		      FROM WMS.TB_RECEIPT_H
			 WHERE SRVC_CD	= NEW.SRVC_CD
               AND WH_CD	= NEW.WH_CD
               AND IN_NO	= NEW.IN_NO;
			
			CALL WMS.PR_ITRN(
				NEW.SRVC_CD
			  , NEW.WH_CD
			  , 'DP'
			  , NEW.PROD_CD
			  , COALESCE(V_LOT_NO, TO_CHAR(NOW(), 'YYYYMMDD'))
			  , ''
			  , ''
			  , ''
			  , NEW.IN_ZONE_CD
			  , NEW.IN_LOC_CD
			  , 'X' || TO_CHAR(NOW(), 'YYYYMMDD') || NEW.IN_EXPECTED_SEQ::TEXT
			  , NEW.IN_NO
			  , COALESCE(NEW.RECEIVED_QTY, 0)
			  , TO_CHAR(NOW(), 'YYYYMMDD')
			  , COALESCE(V_VENDOR_CD, '')
			  , COALESCE(V_VENDOR_NM, '')
			  , NEW.RMK
			  , NEW.UPD_ID
			);
		
		EXCEPTION
		     WHEN OTHERS THEN
				 V_RTRN_MSG := 'PR_ITRN : ' || SQLERRM;
				 RAISE EXCEPTION '%', V_RTRN_MSG;
		END;
	END IF;
	
	RETURN NEW;					-- 정상시 리턴
	
	EXCEPTION
		 WHEN OTHERS THEN
			V_RTRN_MSG := V_PROC_NAME || ' 오류 : ' || V_RTRN_MSG;
			
			-- 콘솔출력
			RAISE NOTICE '% 오류 : %', V_PROC_NAME, V_RTRN_MSG;
			
			-- 처리 실패시 로그 DB에 저장
			INSERT INTO WMS.TB_PROC_LOG 
			( PROC_NM, EXEC_START_DATE, EXEC_PARAM, RESULT_MSG, EXEC_USER ) 
			VALUES 
			( V_PROC_NAME, NOW(), V_PROC_PARAM, V_RTRN_MSG, NEW.UPD_ID);
			
			RETURN NULL;		-- 에러시 전체 롤백
END;
$$;

