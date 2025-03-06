package com.kovi.kovinewinterface.service.libUpdate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface LibUpdateService {

    Map<String, Object> setSolutionFileUpload(HttpServletRequest request, HttpServletResponse response, UpdateInfoVO searchVO);


    boolean setHipUpdateInfoSave(Map<String, Object> paramMap);
    boolean setHipUpdateInfoRemove(Map<String, Object> paramMap);

    void ajaxGetUpdateFileDownload(String fileDownArr, HttpServletRequest request, HttpServletResponse response);

    List<LinkedHashMap<String, Object>> getTableExcelData(String tableNm);

    Map<String, Object> setExcelDataUpload(HttpServletRequest request, HttpServletResponse response);
}
