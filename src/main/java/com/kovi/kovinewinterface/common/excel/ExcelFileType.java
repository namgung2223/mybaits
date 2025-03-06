package com.kovi.kovinewinterface.common.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class ExcelFileType {

    /**
     * 엑셀파일을 읽어서 Workbook 객체에 리턴한다.
     * XLS와 XLSX 확장자를 비교한다.
     *
     * @param inputStream 엑셀 파일 InputStream
     * @param fileName 엑셀 파일명 (확장자 포함)
     * @return Workbook 객체 (HSSFWorkbook 또는 XSSFWorkbook)
     */
    public static Workbook getWorkbook(InputStream inputStream, String fileName) {
        HSSFWorkbook wb = null;
        XSSFWorkbook xwb = null;

        // 파일 확장자 체크
        String extNm = fileName.substring(fileName.lastIndexOf("."));

        try {
            // 파일 확장자에 따라 읽을 수 있는 워크북 객체 생성
            if (".XLS".equals(extNm) || ".xls".equals(extNm)) {
                wb = new HSSFWorkbook(inputStream);  // .xls 파일 처리
            } else if (".XLSX".equals(extNm) || ".xlsx".equals(extNm)) {
                xwb = new XSSFWorkbook(inputStream); // .xlsx 파일 처리
            }
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일을 읽는 중 오류 발생: " + e.getMessage(), e);
        }

        // 확장자에 맞는 Workbook 객체 반환
        if (wb != null) {
            return wb;
        } else if (xwb != null) {
            return xwb;
        } else {
            throw new IllegalArgumentException("지원되지 않는 엑셀 파일 형식입니다: " + extNm);
        }
    }
}
