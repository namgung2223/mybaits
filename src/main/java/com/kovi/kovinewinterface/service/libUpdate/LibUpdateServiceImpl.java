package com.kovi.kovinewinterface.service.libUpdate;


import com.kovi.kovinewinterface.common.EgovWebUtil;
import com.kovi.kovinewinterface.common.FileUtil;
import com.kovi.kovinewinterface.common.excel.ExcelRead;
import com.kovi.kovinewinterface.common.excel.ExcelReadOption;
import com.kovi.kovinewinterface.vo.libUpdate.UpdateInfoVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service("LibUpdateService")
@Transactional
public class LibUpdateServiceImpl implements LibUpdateService {

    @Value("${Lib.Update.Root}")
    String rootPath;

    @Value("${Lib.Update.Temp}")
    String tempPath;


    @Resource(name = "LibUpdateTransactionService")
    private LibUpdateTransactionService libUpdateTransactionService;

    private final Logger logger = LoggerFactory.getLogger(LibUpdateServiceImpl.class);

    @Override
    public Map<String, Object> setSolutionFileUpload(HttpServletRequest request, HttpServletResponse response, UpdateInfoVO searchVO) {
        List<Map<String, Object>> fileDatas = new ArrayList<>();
        List<Map<String, Object>> failFileDatas = new ArrayList<>();

        String path = Optional.ofNullable(request.getParameter("path")).orElse("");
        String reUploadCheck = Optional.ofNullable(request.getParameter("reUploadCheck")).orElse("");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("updNo", searchVO.getUpdNo());
        paramMap.put("adminId", searchVO.getAdminId());

        // 중복 체크
        if (isDuplicateUpdateNo(searchVO, reUploadCheck)) {
            return createResult(false, "중복된 업데이트 번호 입니다.");
        }

        // 파일 확장자 체크
        if (!FileUtil.fileExtCheck(response, request)) {
            return createResult(false, "Script가 가능한 파일들은 업로드 할 수 없습니다.(.jsp, .php, .asp, .inc)");
        }

        // 파일 업로드 수행
        processFileUpload(request, path, fileDatas, failFileDatas);

        // 파일 등록 및 DB 처리
        return handleDatabaseUpdate(searchVO, paramMap, reUploadCheck, fileDatas, failFileDatas);
    }


    private boolean isDuplicateUpdateNo(UpdateInfoVO searchVO, String reUploadCheck) {
        return updNoCheck != null && !"check".equals(reUploadCheck);
    }

    private void processFileUpload(HttpServletRequest request, String path, List<Map<String, Object>> fileDatas, List<Map<String, Object>> failFileDatas) {
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
        Iterator<String> iterator = req.getFileNames();

        while (iterator.hasNext()) {
            List<MultipartFile> files = req.getFiles(iterator.next());

            for (MultipartFile multipartFile : files) {
                if (!multipartFile.isEmpty()) {
                    handleSingleFileUpload(multipartFile, path, fileDatas, failFileDatas);
                }
            }
        }
    }
    private void handleSingleFileUpload(MultipartFile multipartFile, String path, List<Map<String, Object>> fileDatas, List<Map<String, Object>> failFileDatas) {
        String fileName = multipartFile.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) return;

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", fileName);
        fileData.put("filePath", path + "/");

        try {
            // 확장자 처리
            String extNm = FilenameUtils.getExtension(fileName);
            String fullPath = "";

            // 압축 파일 처리
            if (isCompressedFile(extNm)) {
                fullPath = prepareTempDirectory(fileName);
                File file = new File(fullPath);
                multipartFile.transferTo(file);

                if (!FileUtil.zipFileExtCheck(file)) {
                    throw new IOException("허용되지 않은 파일 확장자.");
                }

                List<Map<String, Object>> zipFileDatas = FileUtil.unZip(rootPath, path, file);
                fileDatas.addAll(zipFileDatas);
            } else {
                fullPath = Paths.get(rootPath + path, fileName).toString();
                File file = new File(fullPath);
                multipartFile.transferTo(file);
                fileDatas.add(fileData);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("filePath = " + fullPath);
            }

        } catch (Exception e) {
            failFileDatas.add(fileData);
            logger.error("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    private boolean isCompressedFile(String extNm) {
        return Arrays.asList("zip", "apk", "rar", "7z", "tar").contains(extNm);
    }

    private String prepareTempDirectory(String fileName) {
        String fullPath = EgovWebUtil.filePathReplaceAll(tempPath + FileUtil.getToday("yyyyMMdd"));
        File tempFile = new File(fullPath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return Paths.get(fullPath, fileName).toString();
    }


    private Map<String, Object> handleDatabaseUpdate(UpdateInfoVO searchVO, Map<String, Object> paramMap, String reUploadCheck, List<Map<String, Object>> fileDatas, List<Map<String, Object>> failFileDatas) {
        if (fileDatas.isEmpty()) {
            return createResult(false, "파일이 업로드되지 않았습니다.");
        }
        int check = libUpdateTransactionService.insertHipUpdateData(searchVO, paramMap, fileDatas);
        if (check < 0) {
            return createResult(false, "파일 내역 등록 중 문제가 발생했습니다.");
        }

        boolean result = failFileDatas.isEmpty();
        String msg = result ? "등록 완료" : "일부 파일 업로드가 실패 했습니다.";

        return createFinalResult(result, msg, fileDatas, failFileDatas);
    }


    private Map<String, Object> createResult(boolean result, String msg) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        resultMap.put("msg", msg);
        return resultMap;
    }

    private Map<String, Object> createFinalResult(boolean result, String msg, List<Map<String, Object>> fileDatas, List<Map<String, Object>> failFileDatas) {
        Map<String, Object> resultMap = createResult(result, msg);
        resultMap.put("fileDatas", fileDatas);
        if (!failFileDatas.isEmpty()) {
            resultMap.put("failFileDatas", failFileDatas);
        }
        return resultMap;
    }



    @Override
    public boolean setHipUpdateInfoSave(Map<String, Object> paramMap) {
        boolean result = false;
        int check1 = 0;

        try {
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }finally {
            if(check1 > 0) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean setHipUpdateInfoRemove(Map<String, Object> paramMap) {
        boolean result = false;
        int check1 = 0;
        try {
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }finally {
            if(check1 > 0) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void ajaxGetUpdateFileDownload(String fileDownArr, HttpServletRequest request, HttpServletResponse response) {
        String fileData = StringUtils.isEmpty(fileDownArr) ? "null" : fileDownArr.replace("&quot;", "\"");
        if ("null".equals(fileData)) {
            return;
        }

        JSONArray fileArray = JSONArray.fromObject(fileData);
        if (fileArray.isEmpty()) {
            return;
        }

        String downloadFileName = String.format("업데이트파일내역_%s.zip", FileUtil.getToday("yyyyMMddHHmmss"));
        String realFolder = tempPath + FileUtil.getToday("yyyyMMdd") + File.separator;
        String fileFullPath = realFolder + downloadFileName;

        createDirectory(realFolder);
        createZipFile(fileArray, fileFullPath);

        FileUtil.fileDownload(fileFullPath, response, request);
    }

    /**
     * 지정된 경로의 디렉토리를 생성
     */
    private void createDirectory(String path) {
        File directory = new File(EgovWebUtil.filePathReplaceAll(path));
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * JSON 배열을 기반으로 ZIP 파일을 생성
     */
    private void createZipFile(JSONArray fileArray, String zipFilePath) {
        try (FileOutputStream fos = new FileOutputStream(EgovWebUtil.filePathReplaceAll(zipFilePath));
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (Object obj : fileArray) {
                JSONObject jsonObj = (JSONObject) obj;
                addFileToZip(jsonObj, zos);
            }

            logger.debug(EgovWebUtil.removeCRLF("Make Zip File Success!!!"));
        } catch (IOException e) {
            logger.error("[IOException ==> {}]", EgovWebUtil.removeCRLF(e.getMessage()));
        }
    }

    /**
     * 개별 파일을 ZIP 파일에 추가
     */
    private void addFileToZip(JSONObject fileObj, ZipOutputStream zos) {
        String fileName = fileObj.optString("upFile", "");
        String filePath = fileObj.optString("filePath", "");
        if (StringUtils.isEmpty(fileName)) {
            return;
        }

        String serverFilePath = Paths.get(rootPath + filePath, EgovWebUtil.fileNameReplaceAll(fileName)).toString();
        String zipEntryPath = filePath + EgovWebUtil.fileNameReplaceAll(fileName);

        try (FileInputStream fis = new FileInputStream(serverFilePath);
             BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 128)) {

            zos.putNextEntry(new ZipEntry(zipEntryPath));
            byte[] buffer = new byte[1024 * 128];
            int length;
            while ((length = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();

            logger.debug(EgovWebUtil.removeCRLF("zip file: {}, Entry {}, file Make Success!!!"),
                    EgovWebUtil.removeCRLF(zipEntryPath));
        } catch (IOException e) {
            logger.error("[IOException ==> {}]", EgovWebUtil.removeCRLF(e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LinkedHashMap<String, Object>> getTableExcelData(String tableNm) {
        List<LinkedHashMap<String, Object>> tableExcelData;
        switch (tableNm) {
            case "noticeBoard":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataNoticeBoard();
                break;
            case "mDefProperty":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataMDefProperty();
                break;
            case "brandCode":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataBrandCode();
                break;
            case "brandModel":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataBrandModel();
                break;
            case "classBrand":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataClassBrand();
                break;
            case "item":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataITEM();
                break;
            case "majorCode":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataMajorCode();
                break;
            case "systemItem":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataSystemItem();
                break;
            case "menuItem":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataMenuItem();
                break;
            case "libMenuLCLS":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataLibMenuLCLS();
                break;
            case "libMenuMCLS":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataLibMenuMCLS();
                break;
            case "libMenuSCLS":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataLibMenuSCLS();
                break;
            case "modelHouseItem":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataModelHouseItem();
                break;
            case "modelHouseTemplate":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataModelHouseTemplate();
                break;
            case "mDefPropertyGroup":
                tableExcelData = (List<LinkedHashMap<String, Object>>) libUpdateDAO.getTableExcelDataMDefPropertyGroup();
                break;
            default:
                tableExcelData = new ArrayList<>();
                break;
        }

        return tableExcelData;
    }

    @Override
    public Map<String, Object> setExcelDataUpload(HttpServletRequest request, HttpServletResponse response) {

        String msg = "";
        boolean result = false;
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String tableNm = request.getParameter("tableNm").isEmpty() ? "" : request.getParameter("tableNm");
        int tableDegree = (int) libUpdateDAO.getTableDegree(getTableNm(tableNm));


        if ("".equals(tableNm)) {
            msg = "등록도중 문제가 발생했습니다.";
            resultMap.put("msg", msg);
            resultMap.put("result", result);
            return resultMap;
        }

        MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
        Iterator<String> iterator = req.getFileNames();
        MultipartFile multipartFile = null;

        // 엑셀 파일을 메모리에서 바로 읽기
        while (iterator.hasNext()) {
            multipartFile = req.getFile(iterator.next());
            if (multipartFile != null && !multipartFile.isEmpty()) {
                try {
                    ExcelReadOption excelReadOption = new ExcelReadOption();
                    // MultipartFile의 InputStream을 사용하여 엑셀 파일 처리
                    InputStream inputStream = multipartFile.getInputStream();
                    // 엑셀 파일을 읽는 로직 (디스크에 저장하지 않고 메모리에서 바로 처리)
                    excelReadOption.setInputStream(inputStream);
                    excelReadOption.setStartRow(0); // 필요한 경우 시작 행 번호 조정

                    // 엑셀 읽기
                    List<Map<String, String>> excelContent = ExcelRead.read(excelReadOption, multipartFile.getOriginalFilename());

                    if(excelReadOption.getOutputColumns().size() != tableDegree) {
                        msg = "엑셀 파일 컬럼형식이 잘못 되었습니다.";
                        resultMap.put("msg", msg);
                        resultMap.put("result", result);
                        return resultMap;
                    }

                    int check = 0;
                    switch (tableNm) {
                        case "brandCode":
                            check = (int) libUpdateDAO.updateExcelDataBrandCode(excelContent);
                            break;
                        case "brandModel":
                            check = (int) libUpdateDAO.updateExcelDataBrandModel(excelContent);
                            break;
                        case "classBrand":
                            check = (int) libUpdateDAO.updateExcelDataClassBrand(excelContent);
                            break;
                        case "item":
                            check = (int) libUpdateDAO.updateExcelDataITEM(excelContent);
                            break;
                        case "majorCode":
                            check = (int) libUpdateDAO.updateExcelDataMajorCode(excelContent);
                            break;
                        case "systemItem":
                            check = (int) libUpdateDAO.updateExcelDataSystemItem(excelContent);
                            break;
                        case "menuItem":
                            check = (int) libUpdateDAO.updateExcelDataMenuItem(excelContent);
                            break;
                        case "libMenuLCLS":
                            check = (int) libUpdateDAO.updateExcelDataLibMenuLCLS(excelContent);
                            break;
                        case "libMenuMCLS":
                            check = (int) libUpdateDAO.updateExcelDataLibMenuMCLS(excelContent);
                            break;
                        case "libMenuSCLS":
                            check = (int) libUpdateDAO.updateExcelDataLibMenuSCLS(excelContent);
                            break;
                        case "modelHouseItem":
                            check = (int) libUpdateDAO.updateExcelDataModelHouseItem(excelContent);
                            break;
                        case "modelHouseTemplate":
                            check = (int) libUpdateDAO.updateExcelDataModelHouseTemplate(excelContent);
                            break;
                        case "mDefProperty":
                            check = (int) libUpdateDAO.updateExcelDataMDefProperty(excelContent);
                            break;
                        case "mDefPropertyGroup":
                            check = (int) libUpdateDAO.updateExcelDataMDefPropertyGroup(excelContent);
                            break;
                        case "noticeBoard":
                            check = (int) libUpdateDAO.updateExcelDataNoticeBoard(excelContent);
                            break;
                        default:
                            check = -1; // 또는 오류 처리
                            break;
                    }


                    if (check > 0) {
                        msg = "등록 완료";
                        result = true;
                    } else {
                        msg = "등록도중 문제가 발생했습니다.";
                    }
                } catch (IOException e) {
                    logger.error("IOException =>" + e.getMessage());
                    msg = "엑셀 파일 처리 중 오류가 발생했습니다.";
                } catch (Exception e) {
                    logger.error("Exception =>" + e.getMessage());
                    msg = "등록도중 문제가 발생했습니다.";
                }
            }
        }
        resultMap.put("msg", msg);
        resultMap.put("result", result);
        return resultMap;
    }


    private String getTableNm(String tableNm){
        String result = "";

        switch (tableNm) {
            case "brandCode":
                result = "m_Lib_Brand_code";
                break;
            case "brandModel":
                result = "m_Lib_Brand_model";
                break;
            case "classBrand":
                result = "m_Lib_ClassBrand";
                break;
            case "item":
                result = "m_Lib_ITEM";
                break;
            case "majorCode":
                result = "m_Lib_major_code";
                break;
            case "systemItem":
                result = "m_Lib_System_item";
                break;
            case "menuItem":
                result = "m_LibMenu_item";
                break;
            case "libMenuLCLS":
                result = "m_LibMenu_LCLS";
                break;
            case "libMenuMCLS":
                result = "m_LibMenu_MCLS";
                break;
            case "libMenuSCLS":
                result = "m_LibMenu_SCLS";
                break;
            case "modelHouseItem":
                result = "m_ModelHouse_Item";
                break;
            case "modelHouseTemplate":
                result = "m_ModelHouse_Template";
                break;
            case "mDefProperty":
                result = "m_Def_Property";
                break;
            case "mDefPropertyGroup":
                result = "m_Def_PropertyGroup";
                break;
            case "noticeBoard":
                result = "m_Notice_Board";
                break;
            default:
                result = "Unknown Table";
                break;
        }

        return result;
    }

}
