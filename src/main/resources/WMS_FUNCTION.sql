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
	  FROM WMS.TB_WH
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