package com.kovi.kovinewinterface.service.libUpdate;

import com.kovi.house.dao.libUpdate.LibUpdateDAO;
import com.kovi.house.model.entity.libUpdate.UpdateInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("LibUpdateTransactionService")
@Transactional
public class LibUpdateTransactionService {

    @Resource(name = "LibUpdateDAO")
    LibUpdateDAO libUpdateDAO;

    public int insertHipUpdateData(UpdateInfoVO searchVO, Map<String, Object> paramMap, List<Map<String, Object>> fileDatas) {
        int check = (int) libUpdateDAO.insertHipDateInfoAdd(searchVO);
        if (check == 0) {
            return -1;
        }

        int check2 = insertHipDateFileAdd(paramMap, fileDatas);
        if (check2 == 0) {
            return -2;
        }

        return 1;
    }

    public int insertHipDateFileAdd(Map<String, Object> paramMap, List<Map<String, Object>> fileDatas) {
        String updNo = String.valueOf(paramMap.get("updNo")).replace("null", "");
        String adminId = String.valueOf(paramMap.get("adminId")).replace("null", "");

        if ("".equals(updNo)) {
            return -2; // 잘못된 입력값
        }

        int updSnCnt = libUpdateDAO.getMaxUpdSn(updNo);
        int insertedCount = 0;

        for (Map<String, Object> fileData : fileDatas) {
            Map<String, Object> insertParams = new HashMap<>();
            insertParams.put("updNo", updNo);
            insertParams.put("updSn", ++updSnCnt);
            insertParams.put("filePath", String.valueOf(fileData.get("filePath")).replace("null", ""));
            String fileName = String.valueOf(fileData.get("fileName")).replace("null", "");
            insertParams.put("fileName", fileName);
            insertParams.put("adminId", adminId);
            libUpdateDAO.insertHipDateFileAdd(insertParams);
            insertedCount++;
        }

        return insertedCount > 0 ? 1 : -2;
    }
}
