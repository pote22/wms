-- ============================================================
-- WMS 트리거 정의 파일
-- 스터디 목적: PL/pgSQL 트리거/함수/시퀀스 학습용
-- ============================================================

SET search_path TO wms;

-- ------------------------------------------------------------
-- 1. ITRN_KEY 채번 시퀀스
--    형식: YYYYMMDD(8) + LPAD(seq, 9, '0')  → 총 17자리
-- ------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS wms.seq_itrn_key
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;


-- ------------------------------------------------------------
-- 2. 트리거 함수: fn_receipt_confirm_trigger
--    TB_RECEIPT_D AFTER UPDATE 시 호출
--    처리: 재고 반영(TB_STOCK_H/D), 이력 적재(TB_ITRN), 헤더 상태 갱신(TB_RECEIPT_H)
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION wms.fn_receipt_confirm_trigger()
RETURNS TRIGGER AS $$
DECLARE
    v_itrn_key  VARCHAR(20);
    v_exist_cnt INT;
BEGIN
    -- 입고예정수량이 변경되고 상태가 '09'(입고완료) 인 행만 처리
    IF OLD.EXPECTED_QTY IS DISTINCT FROM NEW.EXPECTED_QTY AND NEW.STATUS = '09' THEN

        -- ① ITRN_KEY 채번: YYYYMMDD + 9자리 시퀀스
        v_itrn_key := TO_CHAR(NOW(), 'YYYYMMDD')
                   || LPAD(nextval('wms.seq_itrn_key')::TEXT, 9, '0');

        -- --------------------------------------------------------
        -- Step 1. TB_STOCK_H UPSERT (로케이션 재고 헤더)
        --   PK: (LOC_ID, ZONE_CD, WH_CD, SRVC_CD)
        --   이미 존재하면 STOCK_QTY 증가, 없으면 신규 INSERT
        -- --------------------------------------------------------
        SELECT COUNT(*) INTO v_exist_cnt
          FROM wms.TB_STOCK_H
         WHERE LOC_ID  = NEW.IN_LOC_CD
           AND ZONE_CD = NEW.IN_ZONE_CD
           AND WH_CD   = NEW.WH_CD
           AND SRVC_CD = NEW.SRVC_CD;

        IF v_exist_cnt > 0 THEN
            UPDATE wms.TB_STOCK_H
               SET STOCK_QTY = COALESCE(STOCK_QTY, 0) + COALESCE(NEW.RECEIVED_QTY, 0)
                 , UPD_ID    = NEW.UPD_ID
                 , UPD_DATE  = NOW()
             WHERE LOC_ID    = NEW.IN_LOC_CD
               AND ZONE_CD   = NEW.IN_ZONE_CD
               AND WH_CD     = NEW.WH_CD
               AND SRVC_CD   = NEW.SRVC_CD;
        ELSE
            INSERT INTO wms.TB_STOCK_H
                (LOC_ID, ZONE_CD, WH_CD, SRVC_CD, PART_NO,
                 STOCK_QTY, ALLOC_QTY, PICK_QTY,
                 REG_ID, REG_DATE, UPD_ID, UPD_DATE)
            VALUES
                (NEW.IN_LOC_CD, NEW.IN_ZONE_CD, NEW.WH_CD, NEW.SRVC_CD, NEW.PROD_CD,
                 COALESCE(NEW.RECEIVED_QTY, 0), 0, 0,
                 NEW.UPD_ID, NOW(), NEW.UPD_ID, NOW());
        END IF;

        -- --------------------------------------------------------
        -- Step 2. TB_STOCK_D UPSERT (LOT별 재고 상세)
        --   PK: (ZONE_CD, LOC_ID, SRVC_CD, WH_CD)
        --   이미 존재하면 STOCK_QTY 증가 + LOT_NO 갱신, 없으면 신규 INSERT
        -- --------------------------------------------------------
        SELECT COUNT(*) INTO v_exist_cnt
          FROM wms.TB_STOCK_D
         WHERE LOC_ID  = NEW.IN_LOC_CD
           AND ZONE_CD = NEW.IN_ZONE_CD
           AND WH_CD   = NEW.WH_CD
           AND SRVC_CD = NEW.SRVC_CD;

        IF v_exist_cnt > 0 THEN
            UPDATE wms.TB_STOCK_D
               SET STOCK_QTY = COALESCE(STOCK_QTY, 0) + COALESCE(NEW.RECEIVED_QTY, 0)
                 , LOT_NO    = COALESCE(NEW.LOT_NO, TO_CHAR(NOW(), 'YYYYMMDD'))
                 , UPD_ID    = NEW.UPD_ID
                 , UPD_DATE  = NOW()
             WHERE LOC_ID    = NEW.IN_LOC_CD
               AND ZONE_CD   = NEW.IN_ZONE_CD
               AND WH_CD     = NEW.WH_CD
               AND SRVC_CD   = NEW.SRVC_CD;
        ELSE
            INSERT INTO wms.TB_STOCK_D
                (LOC_ID, ZONE_CD, WH_CD, SRVC_CD, PART_NO, LOT_NO,
                 STOCK_QTY, ALLOC_QTY, PICK_QTY,
                 PLT_YN, PLT_QTY, CARTON_QTY,
                 REG_ID, REG_DATE, UPD_ID, UPD_DATE)
            VALUES
                (NEW.IN_LOC_CD, NEW.IN_ZONE_CD, NEW.WH_CD, NEW.SRVC_CD,
                 NEW.PROD_CD, COALESCE(NEW.LOT_NO, TO_CHAR(NOW(), 'YYYYMMDD')),
                 COALESCE(NEW.RECEIVED_QTY, 0), 0, 0,
                 'N', 0, 0,
                 NEW.UPD_ID, NOW(), NEW.UPD_ID, NOW());
        END IF;

        -- --------------------------------------------------------
        -- Step 3. TB_ITRN INSERT (입고 트랜잭션 이력)
        --   TRAN_TYPE = 'DP' (Deposit/입고)
        --   SOURCE_KEY = 입고지시번호(IN_NO)
        -- --------------------------------------------------------
        INSERT INTO wms.TB_ITRN
            (ITRN_KEY, WH_CD, SRVC_CD,
             TRAN_TYPE, PART_NO, LOT_NO,
             TO_ZONE_CD, TO_LOC_CD,
             SOURCE_KEY, QTY,
             EFFECTIVE_DATE, VENDOR_CD, VENDOR_NM,
             REG_ID, REG_DATE, UPD_ID, UPD_DATE)
        VALUES
            (v_itrn_key, NEW.WH_CD, NEW.SRVC_CD,
             'DP', NEW.PROD_CD, COALESCE(NEW.LOT_NO, TO_CHAR(NOW(), 'YYYYMMDD')),
             NEW.IN_ZONE_CD, NEW.IN_LOC_CD,
             NEW.IN_NO, COALESCE(NEW.RECEIVED_QTY, 0),
             TO_CHAR(NOW(), 'YYYYMMDD'), NEW.VENDOR_CD, NEW.VENDOR_NM,
             NEW.UPD_ID, NOW(), NEW.UPD_ID, NOW());

        -- --------------------------------------------------------
        -- Step 4. TB_RECEIPT_H STATUS 갱신
        --   해당 IN_NO의 모든 상세가 '09' → 헤더 '09'(입고완료)
        --   일부만  '09' → 헤더 '01'(부분입고)
        --   RECEIVED_QTY 는 상세 합산으로 갱신
        -- --------------------------------------------------------
        IF NOT EXISTS (
            SELECT 1
              FROM wms.TB_RECEIPT_D
             WHERE IN_NO   = NEW.IN_NO
               AND WH_CD   = NEW.WH_CD
               AND SRVC_CD = NEW.SRVC_CD
               AND STATUS  != '09'
        ) THEN
            -- 전체 완료
            UPDATE wms.TB_RECEIPT_H
               SET STATUS       = '09'
                 , RECEIVED_QTY = (
                       SELECT SUM(COALESCE(RECEIVED_QTY, 0))
                         FROM wms.TB_RECEIPT_D
                        WHERE IN_NO   = NEW.IN_NO
                          AND WH_CD   = NEW.WH_CD
                          AND SRVC_CD = NEW.SRVC_CD
                   )
                 , RECEIPT_DATE = NOW()::DATE
                 , UPD_ID       = NEW.UPD_ID
                 , UPD_DATE     = NOW()
             WHERE IN_NO        = NEW.IN_NO
               AND WH_CD        = NEW.WH_CD
               AND SRVC_CD      = NEW.SRVC_CD;
        ELSE
            -- 부분 완료
            UPDATE wms.TB_RECEIPT_H
               SET STATUS       = '01'
                 , RECEIVED_QTY = (
                       SELECT SUM(COALESCE(RECEIVED_QTY, 0))
                         FROM wms.TB_RECEIPT_D
                        WHERE IN_NO   = NEW.IN_NO
                          AND WH_CD   = NEW.WH_CD
                          AND SRVC_CD = NEW.SRVC_CD
                   )
                 , UPD_ID       = NEW.UPD_ID
                 , UPD_DATE     = NOW()
             WHERE IN_NO        = NEW.IN_NO
               AND WH_CD        = NEW.WH_CD
               AND SRVC_CD      = NEW.SRVC_CD;
        END IF;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ------------------------------------------------------------
-- 3. 트리거 등록
--    기존 동명 트리거가 있으면 먼저 삭제 후 재생성
-- ------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_receipt_confirm ON wms.TB_RECEIPT_D;

CREATE TRIGGER trg_receipt_confirm
    AFTER UPDATE
    ON wms.TB_RECEIPT_D
    FOR EACH ROW
    EXECUTE FUNCTION wms.fn_receipt_confirm_trigger();
