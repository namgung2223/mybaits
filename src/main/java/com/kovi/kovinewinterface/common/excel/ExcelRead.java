package com.kovi.kovinewinterface.common.excel;

import com.kovi.house.common.CommonUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ExcelRead {
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelRead.class);
	
	public static List<Map<String, String>> read(ExcelReadOption excelReadOption, String fileName) {
        // 엑셀 파일을 읽기 위한 InputStream 사용
        InputStream inputStream = excelReadOption.getInputStream();

        // 엑셀 파일을 Workbook으로 읽기 (파일 경로 대신 InputStream 사용)
        Workbook wb = null;
        try {
            wb = ExcelFileType.getWorkbook(inputStream, fileName);
        } catch (Exception e) {
            logger.error("엑셀 파일을 읽는 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("엑셀 파일을 읽는 중 오류 발생", e);
        }

        // 첫 번째 시트 가져오기
        Sheet sheet = wb.getSheetAt(0);

        logger.info("Sheet 이름: " + wb.getSheetName(0));
        logger.info("데이터가 있는 Sheet의 수: " + wb.getNumberOfSheets());

        // 유효한 행의 개수
        int numOfRows = sheet.getPhysicalNumberOfRows();
        int numOfCells = 0;

        Row row = null;
        Cell cell = null;

        String cellName = "";
        Map<String, String> map = null;
        List<Map<String, String>> result = new ArrayList<>();
        List<String> keyName = new ArrayList<>();

        // 각 Row를 반복
        for (int rowIndex = excelReadOption.getStartRow(); rowIndex < numOfRows; rowIndex++) {
            if (rowIndex == 0) { // 첫 번째 행: 컬럼 이름 처리
                row = sheet.getRow(rowIndex);

                if (row != null) {
                    numOfCells = row.getPhysicalNumberOfCells();

                    // 헤더 컬럼 이름을 keyName 리스트에 저장
                    for (int cellIndex = 0; cellIndex < numOfCells; cellIndex++) {
                        cell = row.getCell(cellIndex);
                        String headerName = CommonUtil.camelCase(String.valueOf(cell).replace("null", ""));
                        keyName.add(headerName);

                        // OutputColumns에 헤더 컬럼 이름 설정
                        excelReadOption.setOutputColumns(headerName);
                    }
                }
            } else { // 데이터 행
                row = sheet.getRow(rowIndex);

                if (row != null) {
                    numOfCells = keyName.size();
                    map = new HashMap<>();
                    for (int cellIndex = 0; cellIndex < numOfCells; cellIndex++) {
                        cell = row.getCell(cellIndex);
                        cellName = keyName.get(cellIndex);

                        // outputColumns에 포함된 컬럼만 처리
                        if (!excelReadOption.getOutputColumns().contains(cellName)) {
                            continue;
                        }

                        map.put(cellName, ExcelCellRef.getValue(cell));
                    }
                    result.add(map);
                }
            }
        }

        return result;
    }
}
