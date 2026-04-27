-- =============================================
-- Function Name : fn_get_srvc_nm
-- Description   : 고객사명 조회
-- Parameter     : p_srvc_cd - 고객사 코드
-- Return        : 고객사명 (VARCHAR)
-- Create Date   : 2026-04-20
-- auther		 : 허민
-- =============================================
CREATE OR REPLACE FUNCTION wms.fn_get_srvc_nm(p_srvc_cd varchar)
RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE 
	v_srvc_nm varchar;
BEGIN
	--고객사 코드로 고객사명 조회
	SELECT SRVC_NM
	  INTO v_srvc_nm
	  FROM WMS.TB_SRVC
	 WHERE SRVC_CD = p_srvc_cd;
	 -- 조회된 고객사명 반환
	RETURN v_srvc_nm; 

EXCEPTION
	-- 데이터가 없을 경우 null 반환
	WHEN NO_DATA_FOUND THEN
  RETURN NULL;
	-- 기타예외 발생시 NULL 반환
    WHEN OTHERS THEN
  RETURN NULL;
END;
$$;

-- =============================================
-- Function Name : fn_get_wh_nm
-- Description   : 고객사 센터명 조회
-- Parameter     : p_srvc_cd, p_wh_cd - 고객사 코드, 센터 코드
-- Return        : 센터명 (VARCHAR)
-- Create Date   : 2026-04-20
-- auther		 : 허민
-- =============================================

CREATE OR REPLACE FUNCTION wms.fn_get_wh_nm(p_srvc_cd varchar, p_wh_cd varchar)
RETURNS varchar
LANGUAGE plpgsql
AS $$
DECLARE
	v_wh_nm	varchar;
BEGIN
	-- 고객사 센터명 조회
	SELECT WH_NM
	  INTO v_wh_nm
	  FROM TB_WH
	 WHERE 1 = 1
	   AND SRVC_CD = p_srvc_cd
	   AND WH_CD = p_wh_cd;
	RETURN v_wh_nm;

EXCEPTION 
	-- 데이터가 없을 경우, NULL 반환
	WHEN NO_DATA_FOUND THEN
		RETURN NULL;
	-- 기타 예외 발생시, NULL 반환
	WHEN OTHERS THEN
		RETURN NULL;
END;
$$;

-- =============================================
-- Function Name : fn_get_user_nm
-- Description   : 사용자 이름 조회
-- Parameter     : p_user_id - 사용자 ID
-- Return        : 사용자명 (VARCHAR)
-- Create Date   : 2026-04-27
-- auther		 : 허민
-- =============================================
CREATE OR REPLACE FUNCTION WMS.FN_GET_USER_NM(P_USER_ID VARCHAR)
RETURNS VARCHAR
LANGUAGE PLPGSQL
AS $$
DECLARE
	V_USER_NM VARCHAR;
BEGIN
	-- 사용자 이름 조회
	SELECT USER_NM
	  INTO V_USER_NM
	  FROM TB_USER
	 WHERE 1 = 1
	   AND USER_ID = P_USER_ID;
	RETURN V_USER_NM;

EXCEPTION
	-- 데이터가 없는 경우, NULL 반환
	WHEN NO_DATA_FOUND THEN
		RETURN NULL;
	-- 기타 예외 발생시, NULL 반환
	WHEN OTHERS THEN
		RETURN NULL;
END
$$
;