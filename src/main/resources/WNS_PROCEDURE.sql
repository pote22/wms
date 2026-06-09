/********************************************************
 * 1. 프로그램 ID : WMS.PR_STOCK_QTY_MGNT 
 * 2. 유형       : 프로시저(PROCEDURE)
 * 3. 사용언어    : PL/SQL (PROGRESQL)
 * 4. 기능정의		: 재고 증/차감 프로시저
 * 5. 입력변수		: 
 * 6. 리턴값		: 
 * 7. 변경이력		: N/A
 * 8. 버전관리
 * ------------------------------------------------------
 *  VERSION		작성자	일자			내용
 * ------------------------------------------------------		
 *  1.0			허민		26.06.04	재고 증/차감 프로시저 작성
 * 
 ********************************************************/

CREATE OR REPLACE PROCEDURE WMS.PR_STOCK_QTY_MGNT (
	I_SRVC_CD		IN VARCHAR		-- 고객사
  , I_WH_CD			IN VARCHAR		-- 센터
  , I_ZONE_CD		IN VARCHAR		-- 존
  , I_LOC_CD		IN VARCHAR		-- 로케이션
  , I_PROD_CD		IN VARCHAR		-- 품목
  , I_LOT_NO		IN VARCHAR		-- 로트번호
  , I_MOVE_TYPE		IN VARCHAR		-- 증/차감 구분(DP : 증감(+) / WD : 차감(-))
  , I_QTY			IN NUMERIC		-- 수량
  , I_PLT_ID		IN VARCHAR		-- 파렛트ID
  , I_PLT_YN		IN VARCHAR		-- 파렛트여부(Y/N)
  , I_RMK			IN VARCHAR		-- 비고
  , I_PLT_QTY		IN NUMERIC		-- 파렛트수량
  , I_CARTON_QTY	IN NUMERIC		-- CARTON 수량
  , I_USER_ID		IN VARCHAR
)
LANGUAGE PLPGSQL
AS $$
DECLARE
	V_QTY			WMS.TB_STOCK_D.STOCK_QTY%TYPE;
	
	V_PROC_NAME		VARCHAR(100)	:= NULL;	-- 프로시저명
	V_PROC_PARAM	VARCHAR(1000)	:= NULL;	-- 파라메터명
	V_RTRN_MSG		VARCHAR(1000)	:= NULL;	-- 처리메세지
	
BEGIN
	V_PROC_NAME		:= 'PR_STOCK_QTY_MGNT';

	V_PROC_PARAM	:= 'I_SRVC_CD : ' 	|| I_SRVC_CD 	|| ' / ' ||
					   'I_WH_CD : ' 	|| I_WH_CD 		|| ' / ' ||
					   'I_ZONE_CD : ' 	|| I_ZONE_CD 	|| ' / ' || 
					   'I_LOC_CD : ' 	|| I_LOC_CD 	|| ' / ' ||
					   'I_PROD_CD : ' 	|| I_PROD_CD 	|| ' / ' ||
					   'I_LOT_NO : ' 	|| I_LOT_NO 	|| ' / ' ||
					   'I_MOVE_TYPE : ' || I_MOVE_TYPE 	|| ' / ' ||
					   'I_QTY : ' 		|| I_QTY 		|| ' / ' ||
					   'I_PLT_ID : ' 	|| I_PLT_ID 	|| ' / ' ||
					   'I_USER_ID : ' 	|| I_USER_ID;
					   
	V_RTRN_MSG     	:= '';
	
	-- 증/차감 구분에 따라 수량 기호를 변경
	IF I_MOVE_TYPE = 'DP' THEN
		V_QTY := COALESCE(I_QTY, 0); 
	ELSIF I_MOVE_TYPE = 'WD' THEN
		V_QTY := COALESCE(I_QTY, 0) * -1;
	ELSE
		V_RTRN_MSG := '증/차감 구분값이 올바르지 않습니다';
		RETURN;
	END IF;
	
	BEGIN
		-- 헤더 테이블 재고 증/차감
		INSERT INTO WMS.TB_STOCK_H (
			SRVC_CD
		  , WH_CD
		  , ZONE_CD
		  , LOC_CD
		  , PROD_CD
		  , STOCK_QTY
		  , REG_ID
		  , REG_DATE	  
		) VALUES (
			I_SRVC_CD
		  , I_WH_CD
		  , I_ZONE_CD
		  , I_LOC_CD
		  , I_PROD_CD
		  , V_QTY
		  , I_USER_ID
		  , NOW()
		)
		
		ON CONFLICT (SRVC_CD, WH_CD, ZONE_CD, LOC_CD, PROD_CD)
		DO UPDATE
		   SET STOCK_QTY 	= COALESCE(TB_STOCK_H.STOCK_QTY, 0) 	+ V_QTY
			 , UPD_ID		= I_USER_ID
			 , UPD_DATE 	= NOW();
	
	EXCEPTION 
		 WHEN OTHERS THEN
		 V_RTRN_MSG := 'UPSERT TB_STOCK_H : ' || SQLERRM;
		 RAISE EXCEPTION '%', V_RTRN_MSG;
		
    END;
	
	BEGIN

		-- 디테일 테이블 재고 증/차감
		INSERT INTO WMS.TB_STOCK_D (
			SRVC_CD
		  , WH_CD
		  , ZONE_CD
		  , LOC_CD
		  , PROD_CD
		  , LOT_NO
		  , STOCK_QTY
		  , PLT_ID
		  , PLT_YN
		  , RMK
		  , PLT_QTY
		  , CARTON_QTY
		  , REG_ID
		  , REG_DATE
		) VALUES (
			I_SRVC_CD
		  , I_WH_CD
		  , I_ZONE_CD
		  , I_LOC_CD
		  , I_PROD_CD
		  , I_LOT_NO
		  , V_QTY
		  , I_PLT_ID
		  , I_PLT_YN
		  , I_RMK
		  , COALESCE(I_PLT_QTY, 0)
		  , COALESCE(I_CARTON_QTY, 0)
		  , I_USER_ID
		  , NOW()
		)
		
		ON CONFLICT (SRVC_CD, WH_CD, ZONE_CD, LOC_CD, PROD_CD)
		DO UPDATE 
		   SET STOCK_QTY 	= COALESCE(TB_STOCK_D.STOCK_QTY, 	0) 	+ V_QTY
			 , PLT_QTY 	 	= COALESCE(TB_STOCK_D.PLT_QTY, 		0) 	+ COALESCE(I_PLT_QTY, 0)
			 , CARTON_QTY 	= COALESCE(TB_STOCK_D.CARTON_QTY, 	0) 	+ COALESCE(I_CARTON_QTY, 0)
			 , UPD_ID 		= I_USER_ID
			 , UPD_DATE 	= NOW();

	EXCEPTION 
		 WHEN OTHERS THEN
		 V_RTRN_MSG := 'UPSERT TB_STOCK_D : ' || SQLERRM;
		 RAISE EXCEPTION '%', V_RTRN_MSG; 

    END;
		 
-- 에외처리
EXCEPTION 
	WHEN OTHERS THEN
	-- 콘솔 출력
	RAISE NOTICE '% 오류 : %', V_PROC_NAME, V_RTRN_MSG;  
	-- 처리 실패시 로그 DB에 저장
	INSERT INTO WMS.TB_PROC_LOG 
	( PROC_NM, EXEC_START_DATE, EXEC_PARAM, RESULT_MSG, EXEC_USER ) 
	VALUES 
	( V_PROC_NAME, NOW(), V_PROC_PARAM, V_RTRN_MSG, I_USER_ID );

END;
$$;

/********************************************************
 * 1. 프로그램 ID : WMS.PR_ITRN 
 * 2. 유형       : 프로시저(PROCEDURE)
 * 3. 사용언어    : PL/SQL (PROGRESQL)
 * 4. 기능정의		: 트랜잭션(ITRN) 프로시저
 * 5. 입력변수		: 
 * 6. 리턴값		:
 * 7. 변경이력		: N/A
 * 8. 버전관리
 * ------------------------------------------------------
 *  VERSION		작성자	일자			내용
 * ------------------------------------------------------		
 *  1.0			허민		26.06.04	 트랜잭션(ITRN) 프로시저 작성
 * 
 ********************************************************/

CREATE OR REPLACE PROCEDURE WMS.PR_ITRN (
	I_SRVC_CD			IN VARCHAR		-- 고객사
  , I_WH_CD				IN VARCHAR		-- 센터
  , I_TRAN_TYPE			IN VARCHAR		-- 트랜잭션 타입 (DP : 입고, WD : 출고, MV :재고이동, AJ : 재고조정, TR : 물류이동)
  , I_PROD_CD			IN VARCHAR		-- 품목코드
  , I_LOT_NO			IN VARCHAR		-- 로트번호(YYYYMMDD)				
  , I_FROM_ZONE_CD		IN VARCHAR		-- FROM 존ID 
  , I_FROM_LOC_CD		IN VARCHAR		-- FROM 로케이션ID
  , I_FROM_ID			IN VARCHAR		-- FROM 시리얼 ID(BOX/PLT)
  , I_TO_ZONE_CD		IN VARCHAR		-- TO 존ID
  , I_TO_LOC_CD			IN VARCHAR		-- TO 로케이션ID
  , I_TO_ID				IN VARCHAR		-- TO 시리얼 ID (BOX/PLT)
  , I_SOURCE_KEY		IN VARCHAR		-- 지시번호
  , I_QTY				IN NUMERIC		-- 작업수량
  ,	I_EFFECTIVE_DATE	IN VARCHAR		-- 작업일자
  , I_CLIENT_CD			IN VARCHAR		-- 거래처코드
  , I_CLIENT_NM			IN VARCHAR		-- 거래처명
  , I_RMK				IN VARCHAR		-- 비고
  , I_USER_ID			IN VARCHAR		-- 사용자ID
)
LANGUAGE PLPGSQL
AS $$
DECLARE
	V_ITRN_KEY	 	WMS.TB_ITRN.ITRN_KEY%TYPE;	-- 트랜잭션 KEY
	V_QTY			WMS.TB_ITRN.QTY%TYPE;
	
	V_PROC_NAME		VARCHAR(100)	:= NULL;	-- 프로시저명
	V_PROC_PARAM	VARCHAR(1000)	:= NULL;	-- 파라메터명
	V_RTRN_MSG		VARCHAR(1000)	:= NULL;	-- 처리메세지
BEGIN
	
	V_PROC_NAME		:= 'WMS.PR_ITRN';
	V_PROC_PARAM	:= 'I_SRVC_CD : ' 	|| I_SRVC_CD 	|| ' / ' ||
					   'I_WH_CD : ' 	|| I_WH_CD 		|| ' / ' ||
					   'I_TRAN_TYPE : ' || I_TRAN_TYPE 	|| ' / ' || 
					   'I_PROD_CD : ' 	|| I_PROD_CD 	|| ' / ' ||
					   'I_LOT_NO : ' 	|| I_LOT_NO 	|| ' / ' ||
					   'I_SOURCE_KEY : '|| I_SOURCE_KEY || ' / ' ||
					   'I_QTY : ' 		|| I_QTY 		|| ' / ' ||
					   'I_USER_ID : ' 	|| I_USER_ID;
					   
	V_RTRN_MSG     	:= '';
	
	-- 입고
	IF I_TRAN_TYPE = 'DP' THEN
		V_QTY		:= COALESCE(I_QTY, 0);
		
	-- 출고
	ELSIF  I_TRAN_TYPE = 'WD' THEN
		V_QTY		:= COALESCE(I_QTY, 0);
		
	-- 물류이동
	ELSIF  I_TRAN_TYPE = 'MV' THEN
		V_QTY		:= COALESCE(I_QTY, 0);
		
	-- 재고조정
	ELSIF  I_TRAN_TYPE = 'AJ' THEN
		V_QTY		:= COALESCE(I_QTY, 0);
	
	-- 물류이동
	ELSIF  I_TRAN_TYPE = 'TR' THEN
		V_QTY		:= COALESCE(I_QTY, 0);
		
	-- 아무것도 아닌 상황일경우
	ELSE
		V_RTRN_MSG	:= '트랜잭션 수행할 내용이 해당되지 않습니다.';
		RETURN;
	END IF;
	
	-- ITRNKEY 생성하기
	SELECT NEXTVAL('WMS.SEQ_TB_ITRN_KEY')::TEXT INTO V_ITRN_KEY; 
	
	-- 트랜잭션 생성
	BEGIN
		INSERT INTO WMS.TB_ITRN (
			SRVC_CD
		  , WH_CD
		  , ITRN_KEY
		  , TRAN_TYPE
		  , PROD_CD
		  , LOT_NO
		  , FROM_ZONE_CD
		  , FROM_LOC_CD
		  , FROM_ID
		  , TO_ZONE_CD
		  , TO_LOC_CD
		  , TO_ID
		  , SOURCE_KEY
		  , QTY
		  , EFFECTIVE_DATE
		  , VENDOR_CD
		  , VENDOR_NM
		  , RMK
		  , REG_ID
		  , REG_DATE
		  , UPD_ID
		  , UPD_DATE
		) VALUES (
			I_SRVC_CD
		  , I_WH_CD
		  , V_ITRN_KEY
		  , I_TRAN_TYPE
		  , I_PROD_CD
		  , I_LOT_NO
		  , I_FROM_ZONE_CD
		  , I_FROM_LOC_CD
		  , I_FROM_ID
		  , I_TO_ZONE_CD
		  , I_TO_LOC_CD
		  , I_TO_ID
		  , I_SOURCE_KEY
		  , V_QTY
		  , I_EFFECTIVE_DATE
		  , I_CLIENT_CD
		  , I_CLIENT_NM
		  , I_RMK
		  , I_USER_ID
		  , NOW()
		  , I_USER_ID
		  , NOW()
		);
		
	EXCEPTION
	     WHEN OTHERS THEN
			V_RTRN_MSG := 'INSERT INTO TB_ITRN : ' || SQLERRM;
		RAISE EXCEPTION '%', V_RTRN_MSG; 
	
	END;

-- 에외처리
EXCEPTION 
	WHEN OTHERS THEN

	-- 콘솔 출력
	RAISE NOTICE '% 오류 : %', V_PROC_NAME, V_RTRN_MSG;  
	
	-- 처리 실패시 로그 DB에 저장
	INSERT INTO WMS.TB_PROC_LOG 
	( PROC_NM, EXEC_START_DATE, EXEC_PARAM, RESULT_MSG, EXEC_USER ) 
	VALUES 
	( V_PROC_NAME, NOW(), V_PROC_PARAM, V_RTRN_MSG, I_USER_ID );
	
END;
$$;
