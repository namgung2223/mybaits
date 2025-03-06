/**
 * 
 */
package com.kovi.kovinewinterface.common;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author koviWeb
 *
 */
public class ConstDef {

	public enum ResultCode {
		SUCCESS,
		FAIL
	}

	/**
	 *  @desc    : 브라우저 유형
	 */
	public static final class BrowserType {
		public static final String	MSIE	= "MSIE";
		public static final String	CHROME	= "Chrome";
		public static final String	FIREFOX	= "Firefox";
		public static final String	OPERA	= "Opera";
		public static final String	SAFARI	= "Safari";
	}

	/**
	 *  @desc    : OS 유형
	 */
	public static final class OSType {
		public static final String	WINDOWS	= "Windows";
		public static final String	MAC		= "Mac";
		public static final String	UNIX	= "Unix";
		public static final String	SOLARIS	= "Solaris";
		public static final String	UNKNOWN	= "Unknown";
	}
	
	/**
	 *	@desc    : 서버 타입
	 */
	public static final class Profile {
		public static final String PRODUCTION 	= "prd";
		public static final String DEVELOPEMENT = "dev";
		public static final String LOCAL 		= "local";
	}
	 
	/**
	 * @desc	: content type
	 */
	public static final class ContentType {
		public static final String DLL		= "application/x-msdownload";
		public static final String ZIP 		= "application/zip";
		public static final String GIF 		= "image/gif";
		public static final String JPG 		= "image/jpeg";
		public static final String DEFAULT 	= "application/octer-stream";
	}
	
	

	public static final Integer LOGIN_MAX_ERROR_CNT = 5;
	
	public static final String FILE_SEPARATOR = File.separator;
	
	public static final String EXCEL_TEMPLETE = "templeteFile";
	public static final String EXCEL_DOWNLOAD = "filename";
	public static final String EXCEL_CELL_MERGE = "mergeCell";
	
	public static final String	AUTH_USER_SESSION	= "authUserSession";
	public static final String	EXCEL_DOWNLOAD_SESSION	= "excelDownloadSession";
	public static final String	RSA_PRIVATE_KEY_SESSION	= "rsaPrivateKey";
	
	public static final String	AUTH_USER_MENU = "authUserMenu";
	public static final String	LOGIN_DATE_TIME	= "loginDateTime";
	
	public static final List<String> WHITE_LIST = Arrays.asList("pdf","zip");
	
	public static final String excelTempletePath = "/WEB-INF/templete";

}
