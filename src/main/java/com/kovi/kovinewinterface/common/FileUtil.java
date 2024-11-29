package com.kovi.kovinewinterface.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

	protected static final Logger logger = LoggerFactory.getLogger(FileUtil.class);


	/**
	 * 행안부 보안취약점 점검 조치 방안.
	 *
	 * @param value
	 * @return
	 */
	public static String filePathReplaceAll(String value) {
		String returnValue = value;
		if (returnValue == null || returnValue.trim().equals("")) {
			return "";
		}

		returnValue = returnValue.replaceAll("\\.\\.", ""); // ..
		returnValue = returnValue.replaceAll("&", "");

		return returnValue;
	}

	public static String fileNameReplaceAll(String value) {
		String returnValue = value;
		if (returnValue == null || returnValue.trim().equals("")) {
			return "";
		}

		returnValue = returnValue.replaceAll("/", "");
		returnValue = returnValue.replaceAll("&", "");
		returnValue = returnValue.replaceAll("\\\\", ""); // ..
		returnValue = returnValue.replaceAll("\\.\\.", "");

		return returnValue;
	}

//	public static boolean checkImgMimeType(InputStream inputStream) throws IOException {
//		String mimeType = new Tika().detect(inputStream);
//		return mimeType.startsWith("image");
//	}


	/**
	 * 스크립트 확장자 체크 ( .jsp, .asp, .php, .inc)
	 *
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
					if (!multipartFile.isEmpty()) {
						fileName = multipartFile.getOriginalFilename();
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
			if (logger.isDebugEnabled()) {
				logger.error("IllegalState Exception => : {}", e.getMessage());
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.toString());
			}
		}
		return check;

	}



	/**
	 * 파일 다운로드
	 * eee
	 * @param fileFullPath
	 * @param response
	 */
	public static void fileDownload(String fileFullPath, HttpServletResponse response) {
		String fileName = String.valueOf(fileFullPath);

		File file = new File(filePathReplaceAll(fileName));

		try(FileInputStream fis = new FileInputStream(file);
			OutputStream out = response.getOutputStream();){

			response.setContentType("application/octer-stream");

			response.setHeader("Set-Cookie", "fileDownload=true; path=/;SameSite=None;secure;" );
			response.setHeader("Content-Disposition",
					"attachment;filename=" + URLEncoder.encode(fileNameReplaceAll(file.getName()), "UTF-8") + "");
			response.setHeader("Content-Transper-Encoding", "binary");
			response.setContentLength((int) file.length());
			FileCopyUtils.copy(fis, out);
			out.flush();
		} catch (FileNotFoundException e) {
			if (logger.isErrorEnabled()) {
				logger.error("[FileNotFoundException ==> : {}]", e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("[IOException ==> : {}]", e.getMessage());
			}
		}
	}

	/**
	 * 파일 업로드(단일)
	 *
	 * @param fileFullPath
	 * @param response
	 * @param request
	 */
	public static Map<String,String> fileUpload(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		Map<String,String> returnMap = new HashMap<>();
		String fileName = "";
		String ext = "";
		String svrFileName = "";
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();
		MultipartFile multipartFile = null;
		while (iterator.hasNext()) {
			multipartFile = req.getFile(iterator.next());
			if (!multipartFile.isEmpty()) {
				fileName = multipartFile.getOriginalFilename();
				ext		= fileName.substring(fileName.lastIndexOf(".")); //확장자
				svrFileName	= UUID.randomUUID().toString().replaceAll("-", "") + ext;
				returnMap.put("uploadPath",fileFullPath+svrFileName);
				returnMap.put("originNm",fileName);
				returnMap.put("svrNm",svrFileName);
				returnMap.put("ext",ext);
			}
		}

		/* 파일 만들기 */
		File targetDir = new File(filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}

		String fullPath = Paths.get(fileFullPath, svrFileName).toString();

		File file = new File(filePathReplaceAll(fullPath));

		if (multipartFile != null) {
			try {
				multipartFile.transferTo(file);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}

	/**
	 * 파일 업로드(다중)
	 *
	 * @param fileFullPath
	 * @param response
	 * @param request
	 */
	public static List<String> multiFileUpload(String fileFullPath, HttpServletResponse response, HttpServletRequest request) {
		String fileName;

		/* 폴더 만들기 */
		File targetDir = new File(filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();
		while (iterator.hasNext()) {
			List<MultipartFile> mf = req.getFiles(iterator.next());

			for (MultipartFile multipartFile : mf) {
				if (!multipartFile.isEmpty()) {
					fileName = multipartFile.getOriginalFilename();
					if (fileName != null && !"".equals(fileName)) {

						fileFullPath = Paths.get(fileFullPath, fileName).toString();

						try {
							multipartFile.transferTo(new File(fileFullPath));
							logger.info("성공");

							if (logger.isDebugEnabled()) {
								logger.debug(("filePath = " + fileFullPath));
							}
						} catch (IllegalStateException e) {
							if (logger.isDebugEnabled()) {
								logger.error("IllegalState Exception =>" + e.getMessage());
							}
						} catch (IOException e) {
							if (logger.isDebugEnabled()) {
								logger.error("IO Exception =>" + e.getMessage());
							}
						} catch (Exception e) {
							if (logger.isErrorEnabled()) {
								logger.error(" err : " + e.toString());
							}
						}
					}
				}
			}
		}
		return null;

	}
	/**
	 * 파일 업로드(단일)
	 * 유저 프사 업로드
	 * @param fileFullPath
	 * @param response
	 * @param request
	 */
	public static String multiFileProfileUpload(String fileFullPath, HttpServletResponse response, HttpServletRequest request, String mem_id) {
		String fileName = "";
		String ext 		= "";
		String realFilePath = "";
		/* 폴더 만들기 */
		File targetDir = new File(filePathReplaceAll(Paths.get(fileFullPath).toString()));
		/* 디렉토리가 없을 경우 생성 */
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = req.getFileNames();
		while (iterator.hasNext()) {
			List<MultipartFile> mf = req.getFiles(iterator.next());

			for (MultipartFile multipartFile : mf) {
				if (!multipartFile.isEmpty()) {
					fileName = multipartFile.getOriginalFilename();
					ext		 = fileName.substring(fileName.lastIndexOf(".") + 1);
					if (!"".equals(fileName)) {

						realFilePath = mem_id+ "." + ext;
						fileFullPath = Paths.get(fileFullPath, mem_id + "." + ext).toString();
						try {
							multipartFile.transferTo(new File(fileFullPath));
							logger.info("성공");

							if (logger.isDebugEnabled()) {
								logger.debug(("filePath = " + fileFullPath));
							}
						} catch (IllegalStateException e) {
							if (logger.isDebugEnabled()) {
								logger.error("IllegalState Exception => : {}", e.getMessage());
							}
						} catch (IOException e) {
							if (logger.isDebugEnabled()) {
								logger.error("IO Exception => : {}", e.getMessage());
							}
						} catch (Exception e) {
							if (logger.isErrorEnabled()) {
								logger.error(" err : {}", e.toString());
							}
						}
					}
				}
			}
		}
		return realFilePath;
	}

	/**
	 * 폴더 삭제(하위 폴더 파일 전체 삭제를 위한 재귀함수)
	 *
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path) {
		File deleteFolder = new File(path);

		if (deleteFolder.exists()) {
			File[] deleteFolderList = deleteFolder.listFiles();

			for (File file : deleteFolderList) {
				if (file.isFile()) {
					logger.info("file Delete: {}", file.getPath() + file.getName());
					file.delete();
				} else {
					deleteFile(file.getPath());
				}
				file.delete();
			}
			deleteFolder.delete();
		}
		return true;
	}

	/**
	 * zip파일 압축해제
	 *
	 * @param path - 업로드할 파일 위치
	 * @param file - zip파일
	 * @return
	 */
	public static List<Map<String, Object>> unZip(String root, String path, File file) {
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
			zis = new ZipInputStream(fis);

			// entry가 없을때까지 뽑기
			while ((zipentry = zis.getNextEntry()) != null) {
				map = new HashMap<String, Object>();
				String filename = zipentry.getName();

				try {
					File unZipfile = new File(root + path + "/" + filename);

					fos = new FileOutputStream(unZipfile);
					int length = 0;
					while ((length = zis.read()) != -1) {
						fos.write(length);
					}

					fos.flush();
					fos.close();
					zis.closeEntry();
					// 파일 데이터 추출

					// 2020-08-26 파일
					if (filename.contains("/")) {
						map.put("fileName", filename.substring(filename.lastIndexOf("/") + 1));
						map.put("filePath", path + "/" + filename.substring(0, filename.lastIndexOf("/") + 1));
					} else {
						map.put("fileName", filename);
						map.put("filePath", path + "/");
					}

					list.add(map);
				} catch (IllegalStateException e) {
					if (logger.isDebugEnabled()) {
						logger.error("IllegalState Exception =>" + e.getMessage());
					}
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger.error("IO Exception =>" + e.getMessage());
					}
				} catch (Exception e) {
					if (logger.isErrorEnabled()) {
						logger.error(e.getMessage());
					}
				}
			}
			zis.close();
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.error("IOException Exception =>" + e.getMessage());
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
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (logger.isDebugEnabled()) {
					logger.error("IOException Exception =>" + e.getMessage());
				}
			}

		}

		return list;

	}


	/**
	 * 파일 업로드(다중)
	 *
	 */
//	public static boolean checkFilesAreImgFile(HttpServletRequest request) {
//
//		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
//		Iterator<String> iterator = req.getFileNames();
//
//		while (iterator.hasNext()) {
//			List<MultipartFile> mf = req.getFiles(iterator.next());
//
//			for (MultipartFile multipartFile : mf) {
//				if (!multipartFile.isEmpty() && (!FileUtil.checkImgMimeType(multipartFile))) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}


	public static void getHttpDownloadFile(HttpServletResponse response, String subPath, String fileName) throws IOException {

		String encodedPath = Arrays.stream(subPath.split("/"))
				.map(s -> URLEncoder.encode(s).replaceAll("[+]", "%20"))
				.collect(Collectors.joining("/"));

		String testUrl = "https://app.kovihouse.com:444/APT/IMG/" + encodedPath + "/" + URLEncoder.encode(fileName).replaceAll("[+]", "%20");
		URL site = new URL(testUrl.trim());
		URLConnection url = site.openConnection();


		try(BufferedInputStream in 	 = new BufferedInputStream(url.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader(HttpHeaders.SET_COOKIE, "fileDownload=true; path=/;SameSite=None;secure;");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + URLEncoder.encode(fileName, Charsets.UTF_8));
			response.setHeader("Content-Transfer-Encoding", "binary");

			FileCopyUtils.copy(in, out);

			out.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("삭제된 파일입니다");
		} catch (IOException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("네트워크 상태가 좋지 않아서 실패했습니다.");
		}
	}
}// Finish this class