package com.kovi.kovinewinterface.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	protected static Logger loggerError = LoggerFactory.getLogger("exceptionLogger");

	/**
	 * zip파일 확장자 체크
	 * @return
	 */
	public static boolean zipFileExtCheck(File zipFile) {
		FileInputStream fis2 = null;
		ZipInputStream zis2 = null;
		ZipEntry zipentry2 = null;
		boolean check = true;
		// 파일 스트림
		try {
			fis2 = new FileInputStream(zipFile);
			// Zip 파일 스트림
			zis2 = new ZipInputStream(fis2, Charset.forName("EUC-KR"));

			while ((zipentry2 = zis2.getNextEntry()) != null) {
				String zipFilename = zipentry2.getName();
				String zipExtNm = FilenameUtils.getExtension(zipFilename); // 확장자명 처리
				if ("jsp".equals(zipExtNm) || "asp".equals(zipExtNm) || "php".equals(zipExtNm)
						|| "inc".equals(zipExtNm)) {
					check = false;
				}
				zis2.closeEntry();
			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				loggerError.error("IOException Exception =>" + e.getMessage());
			}
		}catch (Exception e) {
			if (loggerError.isErrorEnabled()) {
				loggerError.error("IOException Exception =>" + e.getMessage());
			}
		}finally {
			try {
				if (zis2 != null) {
					zis2.closeEntry();
					zis2.close();
				}
				if (fis2 != null)
					fis2.close();
			} catch (IOException e) {
				if (loggerError.isDebugEnabled()) {
					loggerError.error("IOException Exception =>" + e);
				}
			}

		}
		return check;
	}
	
	/**
	 * 스크립트 확장자  체크 ( .jsp, .asp, .php, .inc)
	 * @param response
	 * @param request
	 * @return
	 */
	public static boolean fileExtCheck(HttpServletResponse response, HttpServletRequest request) {
		String fileName = "";
		boolean check = true;
		try {

			MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
			Iterator<String> iterator = req.getFileNames();

			while (iterator.hasNext()) {
				List<MultipartFile> mf = req.getFiles(iterator.next());

				for (MultipartFile multipartFile : mf) {
					if (multipartFile.isEmpty() == false) {
						fileName = StringUtils.isEmpty(multipartFile.getOriginalFilename()) ? ""
								: multipartFile.getOriginalFilename();
						if (fileName != null && !"".equals(fileName)) {
							String extNm = FilenameUtils.getExtension(fileName); // 확장자명 처리

							// 확장자 체크
							if ("jsp".equals(extNm) || "asp".equals(extNm) || "php".equals(extNm)
									|| "inc".equals(extNm)) {
								check = false;
							}
						}
					}
				}
			}

		} catch (IllegalStateException e) {
			if (loggerError.isDebugEnabled()) {
				loggerError.error("IllegalState Exception =>" + e.getMessage());
			}
		} catch (Exception e) {
			if (loggerError.isErrorEnabled()) {
				loggerError.error(e.toString());
			}
		}
		return check;

	}
	
	/**
	 * 파일 다운로드
	 * @param fileFullPath
	 * @param response
	 */
	public static void fileDownload(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		String fileName = String.valueOf(fileFullPath).replace("null", "");
		
//		File file = new File(EgovWebUtil.filePathReplaceAll(fileName));
		File file = new File(fileName);
		OutputStream out = null;
		FileInputStream fis = null;
		try {
//			fileName = FileBrowserEncode(fileName, request);
			
			fis = new FileInputStream(file);
			out = response.getOutputStream();
			
			response.setContentType(ConstDef.ContentType.DEFAULT);

			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(EgovWebUtil.fileNameReplaceAll(file.getName()), "UTF-8")+"");
//			response.setHeader("Content-Disposition", "attachment;filename="+FileBrowserEncode(file.getName(), request)+"");
			response.setHeader("Content-Transper-Encoding", "binary");
			response.setContentLength((int)file.length());
			
			byte[] buf = new byte[8192];
			int bytesread = 0, bytesBuffered = 0;
		
			while((bytesread = fis.read(buf)) > -1) {
				out.write(buf, 0, bytesread);
				bytesBuffered += bytesread;
				
				if(bytesBuffered > 1024 * 1024) {
					bytesBuffered = 0;
					out.flush();
				}
			}
			
//			FileCopyUtils.copy(fis, out);
//			out.flush();
		} catch (FileNotFoundException e) {
			if (loggerError.isErrorEnabled()) {
				loggerError.error("[FileNotFoundException ==> {}]", e.getMessage());
				loggerError.error("[FileName ==> {}]", file.getName());
			}
		} catch (IOException e){
			if (loggerError.isErrorEnabled()) {
				loggerError.error("[IOException ==> {}]", e.getMessage());
				loggerError.error("[FileName ==> {}]", file.getName());
			}
		}catch (Exception e){
			if (loggerError.isErrorEnabled()) {
				loggerError.error("[Exception ==> {}]", e.getMessage());
				loggerError.error("[FileName ==> {}]", file.getName());
			}
		}finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(out);
		}

	}
	

	/**
	 * 파일 다운로드(server 파일명, real 파일명 구분)
	 * @param fileFullPath
	 * @param realFileNm
	 * @param response
	 */
	public static void fileDownload2(String fileFullPath,String realFileNm, HttpServletResponse response) {
		String fileName = StringUtils.isEmpty(fileFullPath) ? "" : fileFullPath;

		File file = new File(EgovWebUtil.filePathReplaceAll(fileName));
		OutputStream out = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			out = response.getOutputStream();
			
			response.setContentType(ConstDef.ContentType.DEFAULT);
			
			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(EgovWebUtil.fileNameReplaceAll(realFileNm), "UTF-8")+"");
			response.setHeader("Content-Transper-Encoding", "binary");
			response.setContentLength((int)file.length());
			
			FileCopyUtils.copy(fis, out);
			out.flush();
		} catch (FileNotFoundException e) {
			if (loggerError.isErrorEnabled()) {
				loggerError.error("[FileNotFoundException ==> {}]", e.getMessage());
			}
		} catch (IOException e){
			if (loggerError.isErrorEnabled()) {
				loggerError.error("[IOException ==> {}]", e.getMessage());
			}
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(out);
		}
		
	}
	
	
	
	/**
	 * 파일 업로드(단일)
	 * @param fileFullPath
	 * @param response
	 * @param request
	 */
	public static void fileUpload(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		String fileName = "";

		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();
		MultipartFile multipartFile = null;
		while (iterator.hasNext()) {
			multipartFile = req.getFile(iterator.next());
			if (multipartFile.isEmpty() == false) {
				fileName = multipartFile.getOriginalFilename();
			}

		}

		/* 파일 만들기 */
		File targetDir = new File(EgovWebUtil.filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}

		String fullPath = Paths.get(fileFullPath, fileName).toString();

		File file = new File(EgovWebUtil.filePathReplaceAll(fullPath));

		if (multipartFile != null) {
			try {
				multipartFile.transferTo(file);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	/**
	 * 파일 업로드 단일 + 서버 저장 이름 분류
	 * @param fileFullPath
	 * @param response
	 * @param request
	 * @return 
	 */
	public static Map<String, Object> fileUpload2(String fileFullPath, HttpServletResponse response,
			HttpServletRequest request) {
		
		Map<String, Object> fileData = new HashMap<String, Object>();
		
		String fileName = "";
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();
		MultipartFile multipartFile = null;
		while (iterator.hasNext()) {
			multipartFile = req.getFile(iterator.next());
			if (multipartFile.isEmpty() == false) {
				fileName = multipartFile.getOriginalFilename();
			}

		}

		/* 폴더 만들기 */
		File targetDir = new File(EgovWebUtil.filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}

		
		try {

			if (multipartFile.isEmpty() == false) {

				if (fileName != null && !"".equals(fileName)) {

					/* 확장자 처리 */
					String extNm = FilenameUtils.getExtension(fileName); // 확장자명 처리

					// 원본명, 서버파일명 리스트
			
					UUID uuid = UUID.randomUUID();
					String newFileName = uuid.toString() + "_" + getToday("yyMMddHHmm") + "." + extNm;

					fileData.put("oriFileNm", fileName);
					fileData.put("svrFileNm", newFileName);

					String fullPath = Paths.get(fileFullPath + newFileName).toString();

					File file = new File(EgovWebUtil.filePathReplaceAll(fullPath));

					multipartFile.transferTo(file);

				}
			}
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return fileData;
	}
	
	/**
	 * 파일 업로드(다중)
	 * @param fileFullPath
	 * @param response
	 * @param request
	 */
	public static void multiFileUpload(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		String fileName = "";

		/* 폴더 만들기 */
		File targetDir = new File(EgovWebUtil.filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();

		while (iterator.hasNext()) {
			List<MultipartFile> mf = req.getFiles(iterator.next());

			for (MultipartFile multipartFile : mf) {
				if (multipartFile.isEmpty() == false) {
					fileName = StringUtils.isEmpty(multipartFile.getOriginalFilename()) ? "" : multipartFile.getOriginalFilename();
					if (fileName != null && !"".equals(fileName)) {

						fileFullPath = Paths.get(fileFullPath, fileName).toString();

						try {
							if(multipartFile != null){
								multipartFile.transferTo(new File(fileFullPath));
							}
							
							if (logger.isDebugEnabled()) {
								logger.debug(("filePath = " + fileFullPath));
							}
						} catch (IllegalStateException e) {
							if (loggerError.isDebugEnabled()) {
								loggerError.error("IllegalState Exception =>" + e.getMessage());
							}
						} catch (IOException e) {
							if (loggerError.isDebugEnabled()) {
								loggerError.error("IO Exception =>" + e.getMessage());
							}
						} catch (Exception e) {
							if (loggerError.isErrorEnabled()) {
								loggerError.error(" err : " + e.toString());
							}
						}
					}
				}
			}
		}

	}
	
	/**
	 * 파일 업로드 다중 + 서버 저장 이름 분류
	 * @param fileFullPath
	 * @param response
	 * @param request
	 * @return 
	 */
	public static List<Map<String, Object>> multiFileUpload2(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		String fileName = "";
		Map<String, Object> fileData;
		List<Map<String, Object>> fileList = new ArrayList<Map<String,Object>>();
		/* 폴더 만들기 */
		File targetDir = new File(EgovWebUtil.filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		
		MultipartHttpServletRequest req = null;
		Iterator<String> iterator = null;
		try {
			req = (MultipartHttpServletRequest) request;
			iterator = req.getFileNames();			
		}catch (ClassCastException e) {
			throw e;
		}
		

		while (iterator.hasNext()) {
			List<MultipartFile> mf = req.getFiles(iterator.next());
			
			for (MultipartFile multipartFile : mf) {
				if (multipartFile.isEmpty() == false) {
					fileName = StringUtils.isEmpty(multipartFile.getOriginalFilename()) ? "" : multipartFile.getOriginalFilename();

					if (fileName != null && !"".equals(fileName)) {


						try {
							/* 확장자 처리 */
							String extNm = FilenameUtils.getExtension(fileName); //확장자명 처리
							String fullPath = "";
							
							//원본명, 서버파일명 리스트
							fileData = new HashMap<String, Object>();

							
							UUID uuid = UUID.randomUUID();
							String newFileName = uuid.toString()+"_"+ getToday("yyyyMMddHHmmss")+"."+extNm;
							
							fileData.put("oriFileNm", fileName);
							fileData.put("svrFileNm", newFileName);

							fullPath = fileFullPath + newFileName;	
							
							/* 파일생성 */
							File file = new File(fullPath);
							
							multipartFile.transferTo(file);
							
							fileList.add(fileData);
		
						if (logger.isDebugEnabled()) {
								logger.debug(("filePath = " + fileFullPath));
						}
				} catch (IllegalStateException e) {
							if (loggerError.isDebugEnabled()) {
								loggerError.error("IllegalState Exception =>" + e.getMessage());
							}
						} catch (IOException e) {
							if (loggerError.isDebugEnabled()) {
								loggerError.error("IO Exception =>" + e.getMessage());
							}
						} catch (Exception e) {
							if (loggerError.isErrorEnabled()) {
								loggerError.error(" err : " + e.toString());
							}
						}
					}
				}
			}
		}
		return fileList;
	}
	
	
	/**
	 * ZIP파일 생성
	 * @param list
	 * @param response
	 */
	public static String zipFileDownload(List<Map<String, Object>> list, String tmmpPath, String svrPath, String zipFileName,
			HttpServletResponse response) {

		String fileFullPath = null;

		JSONArray array = JSONArray.fromObject(list);

		if (array.size() > 0) {

			FileInputStream fileStream = null;
			FileOutputStream fileOutputStream = null;
			BufferedInputStream in = null;
			ZipOutputStream out = null;

//					+ ".zip";
			String realFolder = tmmpPath + getToday("yyyyMMdd") + ConstDef.FILE_SEPARATOR;
			fileFullPath = realFolder + zipFileName;

			try {
				File tempFile = new File(EgovWebUtil.filePathReplaceAll(realFolder));
				// 폴더 생성
				if (!tempFile.exists()) {
					tempFile.mkdirs();
				}

				fileOutputStream = new FileOutputStream(EgovWebUtil.filePathReplaceAll(fileFullPath));
				out = new ZipOutputStream(fileOutputStream);

				for (int i = 0; i < array.size(); i++) {

					JSONObject obj = (JSONObject) array.get(i);

					String fileName = StringUtils.isEmpty(String.valueOf(obj.get("fileName"))) ? "" : String.valueOf(obj.get("fileName"));
					String filePath = StringUtils.isEmpty(String.valueOf(obj.get("filePath"))) ? "" : String.valueOf(obj.get("filePath"));
					String svrFilePath = "";
					String zipPath = "";

					if (!StringUtils.isEmpty(fileName)) {
						svrFilePath = svrPath + filePath + EgovWebUtil.fileNameReplaceAll(fileName);

						File f = new File(svrFilePath);
						if (f.exists()) {
							fileStream = new FileInputStream(svrFilePath);
							in = new BufferedInputStream(fileStream, 1024 * 128 * 100);

							zipPath = filePath + EgovWebUtil.fileNameReplaceAll(fileName);
							// zip파일에 파일(+디렉토리) 추가
							out.putNextEntry(new ZipEntry(zipPath));

							if (logger.isDebugEnabled()) {
								logger.debug(EgovWebUtil.removeCRLF("File : " + zipPath));
							}

							byte[] buffer = new byte[1024 * 128 * 100];
							int length = -1;
							if (in != null) {
								while ((length = in.read(buffer, 0, 1024 * 128 * 100)) != -1) {
									out.write(buffer, 0, length);
								}
							}

							try {
								out.closeEntry();
							} catch (IOException e) {
								if (loggerError.isErrorEnabled()) {
									loggerError.error("[IOException ==> {}]", EgovWebUtil.removeCRLF(e.getMessage()));
								}
							} finally {
								IOUtils.closeQuietly(in);
							}

							if (logger.isDebugEnabled()) {
								logger.debug(EgovWebUtil.removeCRLF("zip file : {}, Entry {}, file Make Success!!!"),
										EgovWebUtil.removeCRLF(zipFileName));
							}
						}else {
							if (logger.isDebugEnabled()) {
								logger.debug(EgovWebUtil.removeCRLF("Not File : " + svrFilePath));
							}
						}
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug(EgovWebUtil.removeCRLF("Make Zip File Success!!!"));
				}

			} catch (FileNotFoundException e) {
				if (loggerError.isErrorEnabled()) {
					loggerError.error(EgovWebUtil.removeCRLF("[FileNotFoundException ==> {}]"),
							EgovWebUtil.removeCRLF(e.getMessage()));
				}
			} catch (IOException e) {
				if (loggerError.isErrorEnabled()) {
					loggerError.error(EgovWebUtil.removeCRLF("[IOException ==> {}]"),
							EgovWebUtil.removeCRLF(e.getMessage()));
				}
			} finally {
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(fileStream);
				IOUtils.closeQuietly(fileOutputStream);
			}

		}
//		}

		return fileFullPath;
	}
	
	/**
	 * 폴더 삭제(하위 폴더 파일 전체 삭제를 위한 재귀함수)
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path) {
		File deleteFolder = new File(path);

		if(deleteFolder.exists()){
			File[] deleteFolderList = deleteFolder.listFiles();
			for (int i = 0; i < deleteFolderList.length; i++) {
				if(deleteFolderList[i].isFile()) {
					logger.info("[Delete to file ==> {}]", deleteFolderList[i].getPath());
					deleteFolderList[i].delete();
				}else {
					deleteFile(deleteFolderList[i].getPath());
				}
				logger.info("[Delete to file ==> {}]", deleteFolderList[i].getPath());
				deleteFolderList[i].delete(); 
			}
			logger.info("[Delete to file ==> {}]", deleteFolder.delete());
			deleteFolder.delete();
		}
		return true;
	}

	/**
	 * zip파일 압축해제
	 * @param path - 업로드할 파일 위치
	 * @param file - zip파일
	 * @return 
	 */
	public static List<Map<String, Object>> unZip2(String root,String path, File file) {
		// TODO Auto-generated method stub
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry zipentry = null;
		FileOutputStream fos = null;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		try {
			// 파일 스트림
			fis = new FileInputStream(file);
			// Zip 파일 스트림
			zis = new ZipInputStream(fis, Charset.forName("EUC-KR"));

			// entry가 없을때까지 뽑기
			while ((zipentry = zis.getNextEntry()) != null) {
				map = new HashMap<String, Object>();
				String filename = zipentry.getName();
				
				try {
					//폴더와 파일 구분
					if("/".equals(filename.substring(filename.length()-1))) {
						File Folder = new File(root + path+ "/" + filename);
						if (!Folder.exists()) {
							Folder.mkdir(); //폴더 생성합니다.
						}
					}else {
						File unZipfile = new File(root + path+ "/" + filename);
						
						fos = new FileOutputStream(unZipfile);
						int length = 0;
						while ((length = zis.read()) != -1) {
							fos.write(length);
						}
						
						fos.flush();
						fos.close();
						zis.closeEntry();
						// 파일 데이터 추출
						
						//2020-08-26 파일
						if(filename.contains("/")) {
							map.put("fileName", filename.substring(filename.lastIndexOf("/")+1));
							map.put("filePath", path + "/" +  filename.substring(0, filename.lastIndexOf("/")+1));
						}else {
							map.put("fileName", filename);
							map.put("filePath", path + "/");
						}
						list.add(map);
					}
					
				} catch (IllegalStateException e) {
					if (loggerError.isDebugEnabled()) {
						loggerError.error("IllegalState Exception =>" + e.getMessage());
					}
				} 
//				catch (IOException e) {
//					if (loggerError.isDebugEnabled()) {
//						loggerError.error("IO Exception =>" + e.getMessage());
//					}
//				}
				catch (Exception e) {
					if (loggerError.isErrorEnabled()) {
						loggerError.error(e.getMessage());
					}
				}
			}
			if (zis != null) zis.close();
		} catch (IOException e) {
			if (loggerError.isDebugEnabled()) {
				loggerError.error("IOException Exception =>" + e.getMessage());
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (zis != null) {
					zis.closeEntry();
					zis.close();
				}
				if(fis != null)fis.close();
			} catch (IOException e) {
				if (loggerError.isDebugEnabled()) {
					loggerError.error("IOException Exception =>" + e.getMessage());
				}
			}

		}

		return list;

	}
	
	/**
	 * zip 파일 압축해제
	 * @param root
	 * @param path
	 * @param file
	 * @return
	 */
	public static List<Map<String, Object>> unZip(String root, String path, File file) {
		List<Map<String, Object>> list = new ArrayList<>();

		// ZipFile을 생성할 때 UTF-8과 EUC-KR 중 적절한 인코딩을 사용해야 함
		Charset zipEncoding = Charset.forName("EUC-KR");

		try (ZipFile zipFile = new ZipFile(file, zipEncoding)) { // 파일 인코딩 유지
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				Map<String, Object> map = new HashMap<>();

				String filename = zipEntry.getName();

				Path newPath = zipSlipProtect(zipEntry, Paths.get(root + path));

				if (zipEntry.isDirectory()) {
					Files.createDirectories(newPath);
				} else {
					if (newPath.getParent() != null && Files.notExists(newPath.getParent())) {
						Files.createDirectories(newPath.getParent());
					}
					try (InputStream zis = zipFile.getInputStream(zipEntry)) {
						Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
					}

					if (filename.contains("/")) {
						map.put("fileName", filename.substring(filename.lastIndexOf("/") + 1));
						map.put("filePath", path + "/" + filename.substring(0, filename.lastIndexOf("/") + 1));
					} else {
						map.put("fileName", filename);
						map.put("filePath", path + "/");
					}
					list.add(map);
				}
			}
		} catch (IOException e) {
			loggerError.error("IOException Exception => " + e.getMessage(), e);
		} catch (Exception e) {
			loggerError.error("Exception => " + e.getMessage(), e);
		}

		return list;
	}


	public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
    }

	/**
	 * 이미지 파일 출력 - 2020-05-12 익스플로러에서 출력된지 않는 이슈발생 아래의 ResponseEntity 사용방법으로 변경
	 * @param realPath
	 * @param request
	 * @param response
	 */
	public static void getImgOutPut(String realPath, HttpServletRequest request, HttpServletResponse response) {
		
		File file = null;
		OutputStream outStream = null;
		FileInputStream fileStream = null;
		BufferedInputStream bis=null;
		BufferedOutputStream bos=null;
		

		int fileSize = 0;
		try {
			if (StringUtils.isEmpty(realPath))
				throw new IOException("연결 예외 발생");
			
			/* path */
			//realPath = realFolder + filePath + EgovWebUtil.fileNameReplaceAll(sFileNm);	
			/* 파일 다운로드 처리 */
			if(!StringUtils.isEmpty(realPath)) {
				file = new File(EgovWebUtil.filePathReplaceAll(realPath));
			}
			
			if(file != null){
				fileStream = new FileInputStream(file); //이쪽에서 에러
				fileSize = (int)file.length();
			}
			
			bis = new BufferedInputStream(fileStream);
			outStream = response.getOutputStream();
			bos = new BufferedOutputStream(outStream);
			
			response.reset();
			response.setContentType("application/x-msdownload");
			//response.setContentType("application/octer-stream");
			response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(EgovWebUtil.fileNameReplaceAll(file.getName()), "UTF-8")+"");
			response.setHeader("Content-Transper-Encoding", "binary");
			response.setContentLength(fileSize);
			
			byte[] buffer = new byte[1024*128*10];
			int length=0;
			while ((length = bis.read(buffer, 0, 1024*128*10)) != -1) {
				bos.write(buffer, 0, length);
			}

			bos.flush();
		} catch (FileNotFoundException e) {
			if (loggerError.isErrorEnabled()) {
				loggerError.error( EgovWebUtil.removeCRLF("[FileNotFoundException ==> {}]"), EgovWebUtil.removeCRLF(e.getMessage()));
			}
			
		} catch (IOException e){
			if (loggerError.isErrorEnabled()) {
				loggerError.error( EgovWebUtil.removeCRLF("[IOException ==> {}]"), EgovWebUtil.removeCRLF(e.getMessage()));
				e.printStackTrace();
			}
		} finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(bis);
		}
	}
	
	
	/**
	 * 이미지 파일 출력 ResponseEntity 사용
	 * @param realPath
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public static ResponseEntity<byte[]> getEntiyImgOutPut(String realPath, String fileNm) throws IOException {
		
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		File file = null;
		try {
			String formatName = fileNm.substring(fileNm.lastIndexOf(".") + 1);
			formatName = formatName.toUpperCase();
			MediaType mType = null;

			if ("JPG".equals(formatName)) {
				mType = MediaType.IMAGE_JPEG;
			} else if ("GIF".equals(formatName)) {
				mType = MediaType.IMAGE_GIF;
			} else if ("PNG".equals(formatName)) {
				mType = MediaType.IMAGE_PNG;
			}

			HttpHeaders headers = new HttpHeaders();

			/* 파일 다운로드 처리 */
			if (!StringUtils.isEmpty(realPath)) {
				file = new File(EgovWebUtil.filePathReplaceAll(realPath));
			} else {
				throw new IOException("연결 예외 발생");
			}
			in = new FileInputStream(file);

			if (mType != null) {
				// image file
				headers.setContentType(mType);
			} else {
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				headers.add("Content-Disposition", "attachment;filename="
						+ URLEncoder.encode(EgovWebUtil.fileNameReplaceAll(file.getName()), "UTF-8") + "");
			}

			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
		} catch (IOException e) {
			// TODO: handle exception
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			throw e;
		}

		return entity;
	}

	/**
	 * 특정확장자 리스트 출력
	 * @param dirPath
	 * @param extStr
	 * @return
	 */
	public static List<String> getDirectoryInFiles(String dirPath, String extStr) {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();

		File dir = new File(dirPath);
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(extStr);
			}
		};
		File files[] = dir.listFiles(filter);

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			list.add(file.getName());
		}

		return list;
	}
	
	/**
	 * 파일 삭제 처리
	 * @param filePath
	 * @return
	 */
	public static boolean DeleteFile2(String filePath) {
		// TODO Auto-generated method stub
		boolean fileDeleted = false;
		File file = new File(EgovWebUtil.filePathReplaceAll(filePath));
		if(file.exists()) {
			String n =  file.getPath();
			// 파일 삭제
			fileDeleted = file.delete();	
			logger.info("[Delete to file ==> {}]", n);
			
		}
		
		return fileDeleted;
	}

	public static String getToday(String format) {
		if (StringUtils.isEmpty(format))
			return null;

		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
		return sdf.format(new Date());
	}

	public static void makeExcelFile(String excelFileTitle, List<LinkedHashMap<String, Object>> rowList, String[] colList, HttpServletResponse response){

		try {
			//Excel down 시작
			Workbook workbook = new HSSFWorkbook();

			//시트생성
			Sheet sheet = workbook.createSheet("Member");

			Row row = null;
			Cell cell = null;
			int rowNo = 0;
			//테이블헤더 스타일
			CellStyle headStyle = workbook.createCellStyle();
			headStyle.setBorderTop(BorderStyle.THIN);
			headStyle.setBorderBottom(BorderStyle.THIN);
			headStyle.setBorderLeft(BorderStyle.THIN);
			headStyle.setBorderRight(BorderStyle.THIN);

			//배경색은 노랑
			headStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
			headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			//데이터용 경계 스타일 테두리만지정
			CellStyle bodyStyle = workbook.createCellStyle();
			bodyStyle.setBorderTop(BorderStyle.THIN);
			bodyStyle.setBorderBottom(BorderStyle.THIN);
			bodyStyle.setBorderLeft(BorderStyle.THIN);
			bodyStyle.setBorderRight(BorderStyle.THIN);

			//헤더 작성
			row = sheet.createRow(rowNo++);
			for(int i = 0; i < colList.length; i++) {
				cell = row.createCell(i);
				cell.setCellStyle(headStyle);
				cell.setCellValue(colList[i]);
			}

			for(Map<String, Object> excelRow : rowList) {
				row = sheet.createRow(rowNo++);
				for(int i = 0; i < colList.length; i++) {
					cell = row.createCell(i);
					cell.setCellStyle(bodyStyle);

					Object value = excelRow.get(colList[i]);
					if(value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue(" "); // 값이 없으면 공백 처리
					}
				}
			}

			//컨텐츠 타입과 파일명 지정
			response.setContentType("ms-vnd/excel");
			response.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode(LocalDate.now()+excelFileTitle+".xls","UTF-8"));

			//엑셀출력
			workbook.write(response.getOutputStream());
			workbook.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	
}//Finish this class