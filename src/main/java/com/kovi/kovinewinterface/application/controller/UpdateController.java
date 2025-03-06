package com.kovi.kovinewinterface.application.controller;

import com.kovi.house.common.CommonUtil;
import com.kovi.house.common.FileUtil;
import com.kovi.house.model.entity.libUpdate.UpdateInfoVO;
import com.kovi.house.service.libUpdate.LibUpdateService;
import com.kovi.kovinewinterface.common.FileUtil;
import com.kovi.kovinewinterface.service.libUpdate.LibUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.sf.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;


@Controller
@RequiredArgsConstructor
public class UpdateController {

    private final Logger logger = LoggerFactory.getLogger(UpdateController.class);

    @Value("${Lib.Update.Folder}")
    String isDir;
    @Value("${Lib.Update.Root}")
    String rootDir;

    private final LibUpdateService libUpdateService;


    @GetMapping("/fileUpdate")
    public String fileUpdate(){
        return "fileUpdate/fileUpdateMgt";
    }

    /**
     * 데이터 업데이트 팝업
     */
    @RequestMapping(value = "/updateMgt/dataUpdatePopup.do", method = RequestMethod.GET)
    public String dataUpdatePopupPage(Model model) {
        model.addAttribute("type", "add");
        return "fileUpdate/dataUpdatePopup";
    }




    /**
     * 업데이트 목록 조회
     * @param model
     * @param request
     * @param searchVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxGetUpdateInfoList.do", method = RequestMethod.POST)
    public View ajaxGetUpdateInfoList(ModelMap model, HttpServletRequest request, UpdateInfoVO searchVO) throws Exception {
        model.clear();

        CommonUtil.DataTableOrderCheck(request,searchVO);

        if(searchVO.getFirstDate1() != null)searchVO.setFirstDate1(searchVO.getFirstDate1().replace(".", ""));
        if(searchVO.getFirstDate2() != null)searchVO.setFirstDate2(searchVO.getFirstDate2().replace(".", ""));


        List<Map<String, Object>> list = libUpdateService.getUpdateInfoList(searchVO);

        if(list.size() > 0) {
            model.addAttribute("recordsTotal", list.get(0).get("totalCount"));
            model.addAttribute("recordsFiltered", list.get(0).get("totalCount"));
        }else {
            model.addAttribute("recordsTotal", 0);
            model.addAttribute("recordsFiltered", 0);
        }

        model.addAttribute("data", list);
        return new MappingJackson2JsonView();
    }

    /**
     * 업데이트 파일 목록
     * @param model
     * @param request
     * @param searchVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxGetUpdateFileList.do", method = RequestMethod.POST)
    public View ajaxGetUpdateFileList(ModelMap model, HttpServletRequest request, UpdateInfoVO searchVO) throws Exception {
        model.clear();

        CommonUtil.DataTableOrderCheck(request,searchVO);

        List<Map<String, Object>> list = libUpdateService.getUpdateFileList(searchVO);

        if(list.size() > 0) {
            model.addAttribute("recordsTotal", list.get(0).get("totalCount"));
            model.addAttribute("recordsFiltered", list.get(0).get("totalCount"));
        }else {
            model.addAttribute("recordsTotal", 0);
            model.addAttribute("recordsFiltered", 0);
        }

        model.addAttribute("data", list);
        return new MappingJackson2JsonView();
    }


    /**
     * 업데이트 목록 저장
     * @param model
     * @param rowArr
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxSetHipUpdateInfoSave.do", method = RequestMethod.POST)
    public View ajaxSetHipUpdateInfoSave(ModelMap model,@RequestParam String rowArr,@RequestParam String adminId) {
        model.clear();

        String msg = "";
        String updateData = StringUtils.isEmpty(String.valueOf(rowArr.replace("&quot;", "\""))) ? "null":rowArr;
        boolean result = false;

        if (!"null".equals(updateData)) {

            JSONArray updateArray = JSONArray.fromObject(updateData);

            if (updateArray.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("updateDatas", updateArray);
                paramMap.put("adminId", adminId);
                result = libUpdateService.setHipUpdateInfoSave(paramMap);
            }
        }
        if(result) {
            msg = "저장 완료";
        }else{
            msg = "저장 도중 문제가 발생했습니다.";
        }
        model.addAttribute("msg", msg);
        model.addAttribute("result", result);
        return new MappingJackson2JsonView();
    }

    /**
     * 업데이트 목록 삭제
     * @param model
     * @param rowArr
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxSetHipUpdateInfoRemove.do", method = RequestMethod.POST)
    public View ajaxSetHipUpdateInfoRemove(ModelMap model,@RequestParam String rowArr,@RequestParam String adminId) {
        model.clear();

        String msg = "";
        String updateData = StringUtils.isEmpty(String.valueOf(rowArr.replace("&quot;", "\""))) ? "null":rowArr;
        boolean result = false;

        if (!"null".equals(updateData)) {

            JSONArray updateArray = JSONArray.fromObject(updateData);

            if (updateArray.size() > 0) {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("updateDatas", updateArray);
                paramMap.put("adminId", adminId);
                result = libUpdateService.setHipUpdateInfoRemove(paramMap);
            }
        }


        if(result) {
            msg = "삭제 완료";
        }else{
            msg = "삭제 도중 문제가 발생했습니다.";
        }


        model.addAttribute("msg", msg);
        model.addAttribute("result", result);
        return new MappingJackson2JsonView();
    }


    /**
     * 업데이트 디렉토리 구조(트리)
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/updateMgt/ajaxGetServerDirectoryTree.do", method = RequestMethod.POST)
    public View ajaxGetServerDirectoryTree(ModelMap model) {
        List<Map<String, Object>> treeList = new ArrayList<>();
        List<String> allowedFolders = Arrays.asList("_ArchiLib", "_LibFile", "_ModelHouse_Template", "_ModelHouse");

        for (String folder : allowedFolders) {
            File baseFolder = new File(rootDir, folder);
            if (baseFolder.exists() && baseFolder.isDirectory()) {
                Map<String, Object> tree = new HashMap<>();
                tree.put("id", folder);
                tree.put("text", folder);
                tree.put("children", true); // 하위 폴더 존재 표시
                treeList.add(tree);
            }
        }

        model.addAttribute("treeList", treeList);
        return new MappingJackson2JsonView();
    }


    @RequestMapping(value = "/updateMgt/ajaxGetFolderChildren.do", method = RequestMethod.POST)
    public View ajaxGetFolderChildren(@RequestParam("parentPath") String parentPath, ModelMap model) {
        List<Map<String, Object>> childList = new ArrayList<>();
        File parentDir = new File(rootDir, parentPath);

        File[] subDirs = parentDir.listFiles(File::isDirectory);

        if (subDirs != null) {
            for (File dir : subDirs) {
                File[] subSubDirs = dir.listFiles(File::isDirectory);
                Map<String, Object> child = new HashMap<>();
                child.put("id", parentPath + "/" + dir.getName());
                child.put("text", dir.getName());
                child.put("children", subSubDirs != null && subSubDirs.length > 0);
                childList.add(child);
            }
        }

        model.addAttribute("treeList", childList);
        return new MappingJackson2JsonView();
    }


    /**
     * 폴더 생성
     *
     * @param model
     * @param searchVO
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxSetFolderCreate.do", method = RequestMethod.POST)
    public View ajaxSetFolderCreate(ModelMap model, HttpServletRequest request) throws Exception {

        String fullPath = StringUtils.isEmpty(String.valueOf(request.getParameter("fullPath"))) ? "":String.valueOf(request.getParameter("fullPath"));
        String msg = "";
        boolean result = false;

        File file = new File(isDir + fullPath);
        // 폴더 생성
        try {
            if (!file.exists()) {
                file.mkdirs();
                msg = "폴더 생성 완료";
                result = true;
            }else {
                msg = "중복되는 폴더명 입니다.";
            }
        } catch (Exception e) {
            e.getStackTrace();
            msg = "폴더 생성 중 에러가 발생했습니다";
        }

        model.addAttribute("msg", msg);
        model.addAttribute("result", result);
        return new MappingJackson2JsonView();
    }


    /**
     * 폴더 삭제
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxSetFolderDelete.do", method = RequestMethod.POST)
    public View ajaxSetFolderDelete(ModelMap model, HttpServletRequest request) throws Exception {
        String path = StringUtils.isEmpty(String.valueOf(request.getParameter("path"))) ? "":String.valueOf(request.getParameter("path"));
        String msg = "";
        boolean result = false;

        try {
            FileUtils.deleteDirectory(new File(isDir + path));
            result = true;
        } catch (Exception e) {
            e.getStackTrace();
        }

        if(result) {
            msg = "폴더 삭제 완료";
        }else {
            msg = "폴더 삭제 중 오류가 발생했습니다.";
        }

        model.addAttribute("msg", msg);
        model.addAttribute("result", result);
        return new MappingJackson2JsonView();
    }

    /**
     * 폴더 디렉토리 추가 팝업
     */
    @RequestMapping(value = "/updateMgt/folderAddPopup.do", method = RequestMethod.POST)
    public String getFolderAddPopup(ModelMap model, HttpServletRequest request) {
        model.addAttribute("path", request.getParameter("path"));
        return "fileUpdate/folderAddPopup";
    }


    /**
     * 업데이트 등록(+파일 업로드)
     * @param model
     * @param request
     * @param response
     * @param searchVO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxSetSolutionFileUpload.do", method = RequestMethod.POST)
    public View ajaxSetSolutionFileUpload(ModelMap model, HttpServletRequest request, HttpServletResponse response, UpdateInfoVO searchVO) throws Exception {
        model.clear();

        Map<String, Object> resultMap = libUpdateService.setSolutionFileUpload(request, response, searchVO);
        model.addAttribute("failFileDatas", resultMap.get("failFileDatas"));
        model.addAttribute("fileDatas", resultMap.get("fileDatas"));
        model.addAttribute("msg", resultMap.get("msg"));
        model.addAttribute("result", resultMap.get("result"));
        return new MappingJackson2JsonView();
    }

    /**
     * 업데이트 파일 다운로드(zip형식)
     * @param fileDownArr
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/ajaxGetUpdateFileDownload.do", method = RequestMethod.POST)
    public void ajaxGetUpdateFileDownload(@RequestParam String fileDownArr, HttpServletRequest request, HttpServletResponse response) {
        libUpdateService.ajaxGetUpdateFileDownload(fileDownArr, request, response);
    }

    /**
     * 엑셀 업로드
     * @param model
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateMgt/setExcelFileUpload.do", method = RequestMethod.POST)
    public View setProudtExcelFileUpload(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        model.clear();

        Map<String, Object> resultMap = libUpdateService.setExcelDataUpload(request, response);
        model.addAttribute("msg", resultMap.get("msg"));
        model.addAttribute("result", resultMap.get("result"));
        return new MappingJackson2JsonView();
    }

    /**
     * 엑셀다운로드
     * @return
     */
    @RequestMapping(value = "/updateMgt/tableExelDown.do", method = RequestMethod.POST)
    public View tableExelDown(ModelMap model, @RequestParam String tableNm, HttpServletRequest request, HttpServletResponse response) {
        model.clear();

        List<LinkedHashMap<String, Object>> excelData = libUpdateService.getTableExcelData(tableNm);

        if(!"".equals(tableNm)) {
            String[] headArray = new String[0];

            if (!excelData.isEmpty()) {
                LinkedHashSet<String> headerSet = new LinkedHashSet<>();
                for (LinkedHashMap<String, Object> row : excelData) {
                    headerSet.addAll(row.keySet());
                }
                headArray = headerSet.toArray(new String[0]);
            }
            FileUtil.makeExcelFile(String.format("_%s 목록",tableNm), excelData, headArray, response);
        }
        return new MappingJackson2JsonView();
    }


    private static void addDirectoryToTree(File dir, String rootDir, List<Map<String, Object>> treeList) {
        for (File info : FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (info.isDirectory()) {
                String parent = info.getParent().replace(Paths.get(rootDir).toString(), "#");

                if (parent.equals("#" + File.separator)) {
                    parent = "#"; // 루트 폴더는 "#"으로 설정
                }

                Map<String, Object> tree = new HashMap<>();
                tree.put("id", parent + "\\" + info.getName());
                tree.put("text", info.getName());
                tree.put("parent", parent);

                treeList.add(tree);
            }
        }
    }




}
