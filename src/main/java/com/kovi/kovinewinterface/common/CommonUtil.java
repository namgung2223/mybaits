package com.kovi.kovinewinterface.common;

import com.kovi.kovinewinterface.vo.libUpdate.DefaultVO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

	@Autowired
	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	/**
	 * @author	childmild
	 * @category 파일 업로드 공통함수
	 * @param	request 서블릿 리퀘스트 
	 * @param	path 저장되어질 위치의 파일경로
	 * @param	uuid 파일명을 암호화 해야할 경우에 사용
	 * @return 
	 * @throws Exception
	 */
	public Object fileUpload(ServletRequest request, String path, Boolean uuid) throws Exception{
		
		//파라미터 셋팅
    	String subPath= request.getParameter("subPath");
    	String fileName= request.getParameter("fileName");
    	File newFile = null;
    	
    	path = path+File.separator+subPath;
    	
    	System.out.println("fileUpload path : "+path + " / fileName : "+ fileName);		
    	
		// 10Mbyte 제한
	    int maxSize  = 1024*1024*30; 
	    
    	//MultipartHttpServletRequest 생성
    	MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
    	
    	Iterator<String> iter =  mhsr.getFileNames();
    	
    	MultipartFile mfile = null;
    	String fieldName = "";
    	Object result= null;
    	
    	//디렉토리가 없다면 생성 
    	File dir = new File(path);
    	
    	if (!dir.isDirectory()){
    		dir.mkdirs();
    	}

    	try {		
    		
    		while (iter.hasNext()) {
        		fieldName	= iter.next(); //내용을 가져와서 
        		mfile		= mhsr.getFile(fieldName);
        		
        		String origName;
        		origName	= new String(mfile.getOriginalFilename().getBytes("8859_1"), "UTF-8"); //한글깨짐 방지
        		
        		//파일명이 없다면
        		if ("".equals(origName)) {
    				continue;
    			}
        		
        		String saveFileName = null;
        		File serverFile = null;
        		String ext = null;
        		if (uuid == true) {
        		
        			//파일 명 변경(uuid로 암호)
        			ext		= origName.substring(origName.lastIndexOf(".")); //확장자
        			saveFileName	= getUuid() + ext;
        			origName = saveFileName;
        			serverFile		= new File(path + File.separator + saveFileName); //설정한 path에 파일저장 
        			mfile.transferTo(serverFile);
        			
    			}else{
    				//파일 명 변경(uuid로 암호)
        			serverFile		= new File(path + File.separator + origName); //설정한 path에 파일저장 	
        			mfile.transferTo(serverFile);
        			File reNameFile = new File(path + File.separator + fileName); 
        			if(serverFile.renameTo(reNameFile)){
        				result = 1;
        			}else{
        				result = -2;
        			}

    			}  		
    		}	
    		
		} catch (Exception e) {
			result = -3;
			e.printStackTrace();
		}
		return result;	
   
	}//finish fileUpload()
	
public List<Object> MultifileUpload(ServletRequest request, String path, Boolean uuid) throws Exception{
		
		//파라미터 셋팅
    	String subPath= request.getParameter("subPath");
    	String fileName= request.getParameter("fileName");
    	
    	File newFile = null;
    	List<Object> resultList = new ArrayList<Object>();
    	
    	path = path+File.separator+subPath;
    	System.out.println("subPath::::::::::"+ subPath);
    	System.out.println("path:::::::"+ path);

		// 10Mbyte 제한
	    int maxSize  = 1024*1024*30; 
	    
    	//MultipartHttpServletRequest 생성
    	MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
    	
    	Iterator<String> iter =  mhsr.getFileNames();
    	System.out.println("파일::::::"+mhsr.getFileNames());
    	MultipartFile mfile = null;
    	String fieldName = "";
    	Object result= null;
    	
    	//디렉토리가 없다면 생성 
    	File dir = new File(path);
    	
    	if (!dir.isDirectory()){
    		dir.mkdirs();
    	}

    	try {		
    		
    		while (iter.hasNext()) {
        		fieldName	= iter.next(); //내용을 가져와서 
        		System.out.println("fieldName:::::::::::::"+fieldName);
        		
        		mfile		= mhsr.getFile(fieldName);
        		
        		String origName;
        		origName	= new String(mfile.getOriginalFilename().getBytes("8859_1"), "UTF-8"); //한글깨짐 방지
        		
        		//파일명이 없다면
        		if ("".equals(origName)) {
    				continue;
    			}
        		
        		String saveFileName = null;
        		File serverFile = null;
        		String ext = null;
        		if (uuid == true) {
        		
        			//파일 명 변경(uuid로 암호)
        			ext		= origName.substring(origName.lastIndexOf(".")); //확장자
        			saveFileName	= getUuid() + ext;
        			origName = saveFileName;
        			serverFile		= new File(path + File.separator + saveFileName); //설정한 path에 파일저장 
        			mfile.transferTo(serverFile);
        			
    			}else{
    				//파일 명 변경(uuid로 암호)
        			serverFile		= new File(path + File.separator + origName); //설정한 path에 파일저장 	
        			mfile.transferTo(serverFile);

    			}
        		Map<Object, Object> file = new HashMap<Object, Object>();
	    		file.put("fieldname", fieldName);
	    		file.put("origName", origName);
	    		file.put("sfile", saveFileName);
	    		resultList.add(file);
    		}	
    		
		} catch (Exception e) {
			result = -3;
			e.printStackTrace();
		}
		return resultList;	
   
	}//finish fileUpload()
		
	public List<Object> ListfileUpload(ServletRequest request, String path, Boolean uuid) throws Exception{
		
    	MultipartFile mfile = null;
    	String fieldName = "";
    	List<Object> resultList = new ArrayList<Object>();
		
		try{
			//MultipartHttpServletRequest 생성
	    	MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
	    	
	    	Iterator<String> iter =  mhsr.getFileNames();
	    	System.out.println("iter : "+ iter.hasNext());
	    	//디렉토리가 없다면 생성 
	    	File dir = new File(path);
	    	
	    	if (!dir.isDirectory()){
	    		dir.mkdirs();
	    	}
	    	
	    	//값이 나올때까지
	    	while (iter.hasNext()) {
	    		fieldName	= iter.next(); //내용을 가져와서 
	    		
	    		if(!fieldName.equals("vr_file_1") ){
	        		mfile		= mhsr.getFile(fieldName);    	
		    		String origName;
		//    		origName	= new String(mfile.getOriginalFilename().getBytes("8859_1"), "UTF-8"); //한글깨짐 방지
		    		origName	= new String(mfile.getOriginalFilename()); //한글깨짐 방지
		    		
		    		
		    		//파일명이 없다면
		    		if ("".equals(origName)) {
						continue;
					}
		    		
		    		String saveFileName = null;
		    		File serverFile = null;
		    		String ext = null;
		    		if (uuid == true) {
		    			//파일 명 변경(uuid로 암호)
		    			ext		= origName.substring(origName.lastIndexOf(".")); //확장자
		    			saveFileName	= getUuid() + ext;
		    			origName = saveFileName;
		    			serverFile		= new File(path + File.separator + saveFileName); //설정한 path에 파일저장 
		    			mfile.transferTo(serverFile);
					}else{
						//파일 명 변경(uuid로 암호)
		    			serverFile		= new File(path + File.separator + origName); //설정한 path에 파일저장 
		    			mfile.transferTo(serverFile);
						
					}
		    		
		    		Map<Object, Object> file = new HashMap<Object, Object>();
		    		file.put("fieldname", fieldName);
		    		file.put("origName", origName);
		    		file.put("sfile", saveFileName);
		    		resultList.add(file);
	    		}
	
	    		
			}//finish while 
		}catch(Exception e){
			e.printStackTrace();
		}
			
    	//DB에 기입해야할 original name, uuid 통해서 바뀐 name , 저장되어있는 폴더위치 맵에 담아서 리턴 
    	return resultList;
	    	
	}//finish fileUpload()
	
	
public List<Object> coverImgupload(ServletRequest request, String path, Boolean uuid) throws Exception{
		
    	MultipartFile mfile = null;
    	String fieldName = "";
    	List<Object> resultList = new ArrayList<Object>();
		
		try{
			//MultipartHttpServletRequest 생성
	    	MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
	    	
	    	Iterator<String> iter =  mhsr.getFileNames();
	    	System.out.println("iter : "+ iter.hasNext());
	    	//디렉토리가 없다면 생성 
	    	File dir = new File(path);
	    	
	    	if (!dir.isDirectory()){
	    		dir.mkdirs();
	    	}
	    	
	    	//값이 나올때까지
	    	while (iter.hasNext()) {
	    		fieldName	= iter.next(); //내용을 가져와서 
	    		
	        		mfile		= mhsr.getFile(fieldName);    	
		    		String origName;
		//    		origName	= new String(mfile.getOriginalFilename().getBytes("8859_1"), "UTF-8"); //한글깨짐 방지
		    		origName	= new String(mfile.getOriginalFilename()); //한글깨짐 방지
		    		
		    		
		    		//파일명이 없다면
		    		if ("".equals(origName)) {
						continue;
					}
		    		
		    		String saveFileName = null;
		    		File serverFile = null;
		    		String ext = null;
		    		if (uuid == true) {
		    			//파일 명 변경(uuid로 암호)
		    			ext		= origName.substring(origName.lastIndexOf(".")); //확장자
		    			saveFileName	= getUuid() + ext;
		    			origName = saveFileName;
		    			serverFile		= new File(path + File.separator + saveFileName); //설정한 path에 파일저장 
		    			mfile.transferTo(serverFile);
					}else{
						//파일 명 변경(uuid로 암호)
		    			serverFile		= new File(path + File.separator + origName); //설정한 path에 파일저장 
		    			mfile.transferTo(serverFile);
						
					}
		    		
		    		Map<Object, Object> file = new HashMap<Object, Object>();
		    		file.put("fieldname", "img_file");
		    		file.put("origName", origName);
		    		file.put("sfile", saveFileName);
		    		resultList.add(file);
	
	    		
			}//finish while 
		}catch(Exception e){
			e.printStackTrace();
		}
			
    	//DB에 기입해야할 original name, uuid 통해서 바뀐 name , 저장되어있는 폴더위치 맵에 담아서 리턴 
    	return resultList;
	    	
	}//finish fileUpload()

public List<Object> ListfileUpload2(ServletRequest request, String path, Boolean uuid) throws Exception{
	
	MultipartFile mfile = null;
	String fieldName = "";
	List<Object> resultList = new ArrayList<Object>();
	
	try{
		//MultipartHttpServletRequest 생성
    	MultipartHttpServletRequest mhsr = (MultipartHttpServletRequest) request;
    	
    	Iterator<String> iter =  mhsr.getFileNames();
    	System.out.println("iter : "+ iter.hasNext());
    	
    	//값이 나올때까지
    	while (iter.hasNext()) {
    		fieldName	= iter.next(); //내용을 가져와서 
    		//현재 년도, 월, 일
    		Calendar cal = Calendar.getInstance();
    		int year = cal.get ( cal.YEAR );
    		int month = cal.get ( cal.MONTH ) + 1 ;
    		int date = cal.get ( cal.DATE ) ;
    		System.out.println("fieldName: "+ fieldName);
        	String realpath = "";
    		if("cover_img_file".equals(fieldName) ){
    			realpath = path + "\\_Bg\\"+year+"\\"+month+"\\"+date;
    		}else if("banner_img_file".equals(fieldName)) {
    			realpath = path + "\\_Member\\_Banner";
    		}else  {
    			realpath = path + "\\_Member\\_thumbnail\\memberBest";
    		}
        	
        	//디렉토리가 없다면 생성 
        	File dir = new File(realpath);
        	
        	if (!dir.isDirectory()){
        		dir.mkdirs();
        	}
    		
    			mfile		= mhsr.getFile(fieldName);    	
	    		String origName;
	//    		origName	= new String(mfile.getOriginalFilename().getBytes("8859_1"), "UTF-8"); //한글깨짐 방지
	    		origName	= new String(mfile.getOriginalFilename()); //한글깨짐 방지
	    		
	    		
	    		//파일명이 없다면
	    		if ("".equals(origName)) {
					continue;
				}
	    		
	    		String saveFileName = null;
	    		File serverFile = null;
	    		String ext = null;
	    		if (uuid == true) {
	    			//파일 명 변경(uuid로 암호)
	    			ext		= origName.substring(origName.lastIndexOf(".")); //확장자
	    			saveFileName	= getUuid() + ext;
	    			origName = saveFileName;
	    			serverFile		= new File(realpath + File.separator + saveFileName); //설정한 path에 파일저장 
	    			mfile.transferTo(serverFile);
				}else{
					//파일 명 변경(uuid로 암호)
	    			serverFile		= new File(realpath + File.separator + origName); //설정한 path에 파일저장 
	    			mfile.transferTo(serverFile);
					
				}
	    		
	    		Map<Object, Object> file = new HashMap<Object, Object>();
	    		file.put("fieldname", fieldName);
	    		file.put("origName", origName);
	    		file.put("sfile", saveFileName);
	    		resultList.add(file);
    		

    		
		}//finish while 
	}catch(Exception e){
		e.printStackTrace();
	}
		
	//DB에 기입해야할 original name, uuid 통해서 바뀐 name , 저장되어있는 폴더위치 맵에 담아서 리턴 
	return resultList;
    	
}//finish fileUpload()


	public String copyinto(MultipartFile upload, String path){
		
		//TODO : properties로 뺄것
		//String path = "D:/Service_Web/app.kovihouse.com/_UpFile/_Board/_XML_File";  //CHECK : 서버구동 전 확인(path)    
		//String path="C:/test/disA/_XML_File";
		//String reFileName = null;
		String fileName = null;
		
		try {
			byte bytes[]=upload.getBytes();
			File newFile=new File(path);
			if(!newFile.exists()){
				newFile.mkdirs();
			}
			
			fileName = upload.getOriginalFilename();
/*			fileName = upload.getOriginalFilename().substring(0, upload.getOriginalFilename().lastIndexOf(".")); //xml 소문자
			reFileName = fileName+".xml";*/
			
			newFile=new File(path+"\\"+fileName);
			FileOutputStream fos=new FileOutputStream(newFile);
			fos.write(bytes);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileName;
	}// Finish copyinto

	
	/**
	 * 
	 * @author		childmild
	 * @category	파일 기록시 같은 이름으로 저장되지 안해게 해주는 효과와 DB를 통해서만 뭐하는 파일이닞 알수있게해주는 효과
	 * @return
	 * 
	 */
	public static String getUuid() {
		
    	return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
    	
    }//finish getUuid()
	
	/**
	 * 
	 * @param path 오픈될 파일의 경로
	 * @author childmild
	 * @category 파일 오픈 함수 해당 파일을 던져서 운영체제에 해당 프로그램이 설치되어있으면 실행 
	 * 
	 */
	public static void appRun(String path){
		
		try {

			path = "http://60.196.157.196:8088" + path; //서버 주소 부분 받는거 확인하기
//			path = "D://kepcofile" + path.replace("/rootPath", ""); //서버 주소 부분 받는거 확인하기
			
			
			File file = new File(path);
			
			Process process = Runtime.getRuntime().exec( "rundll32 url.dll, FileProtocolHandler " + file ); //윈도우 
//			Process process = Runtime.getRuntime().exec( "rundll32 url.dll, FileProtocolHandler " + file.getAbsolutePath() ); //윈도우 
//			Process process = Runtime.getRuntime().exec( "open " + file); //mac
			
			file.getAbsolutePath();
			
			process.waitFor();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}//Finish appRun()
	
	 /**
     * Java에서 JavaScript문법을 수행한다.
     * @param jsEngine
     * @throws ScriptException
     */
    public static void invokeHelloScript (ScriptEngine jsEngine) throws ScriptException 
    {
            jsEngine.eval("println('Hello from JavaScript')");
    }
    
    
    /**
     * Java에서 JavaScript함수를 정의한다.  
     * @param engine
     * @throws ScriptException
     */
    public static void defineScriptFunction(ScriptEngine engine, String id, String cert_code, String time_stamp) throws ScriptException 
    {
            // Define a function in the script engine
            engine.eval(
                            "function sayHello() {" +
                            "    location.href ='kovihouse://act_cert?mem_id="+id+"&cert_code="+cert_code+ "&time_stamp="+time_stamp + 
                            "}"
            );
    }
    
    
    /**
     * Java에서 정의한 JavaScript함수를 호출한다.
     * @param engine
     * @throws ScriptException
     */
    public static void invokeScriptFunctionFromEngine(ScriptEngine engine) throws ScriptException 
    {
            engine.eval("sayHello()");
    }
    
  //asp 암호화 코드 --> 코비온라인에서 복사해옴 190528 by hyun
  	public String get_hoya_en(String key, String value) throws Exception {
  		
  		String result = null;
  		StringBuffer sbuf = new StringBuffer();
      	String param = key+"=" + value;
      	
          // URL 객체 생성
          URL url = new URL("http://www.koviarchi.co.kr/hoya_en_interface.asp" + "?" + param);
           
          // URLConnection 생성
          URLConnection urlConn = url.openConnection();
           
          InputStream is = urlConn.getInputStream();
          InputStreamReader isr = new InputStreamReader(is, "UTF-8");
          BufferedReader br = new BufferedReader(isr);
           
          String str;
          while((str=br.readLine()) != null){
              sbuf.append(str + "\r\n");
          }
          
          // 콘솔에 출력하기
          JSONParser parser = new JSONParser();
          String str_sbuf = sbuf.toString().trim();
          Object object = parser.parse(str_sbuf);
          JSONObject jsonObject = (JSONObject)object;
          
          result = jsonObject.get(key).toString();
              
  		return result;
  	}

  	
  //asp 암호화 코드 --> 코비온라인에서 복사해옴 190528 by hyun
  /*230321 암호화 코드 exception 처리 */
  @SuppressWarnings("finally")
  public String get_hoya_de(String key, String value)  {

	  String result = null;
	  StringBuffer sbuf = new StringBuffer();
	  String param = key+"=" + value;
	  try {
		  // URL 객체 생성
		  URL url = new URL("http://www.koviarchi.co.kr/hoya_de_interface.asp" + "?" + param);

		  // URLConnection 생성
		  URLConnection urlConn = url.openConnection();

		  InputStream is = urlConn.getInputStream();
		  InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		  BufferedReader br = new BufferedReader(isr);

		  String str;
		  while((str=br.readLine()) != null){
			  sbuf.append(str + "\r\n");
		  }

		  // 콘솔에 출력하기
		  JSONParser parser = new JSONParser();
		  String str_sbuf = sbuf.toString().trim();
		  Object object = parser.parse(str_sbuf);
		  JSONObject jsonObject = (JSONObject)object;

		  result = jsonObject.get(key).toString();

	  }catch (Exception e) {
		  // TODO: handle exception
		  e.getLocalizedMessage();
		  result = value;
	  }finally {
		  return result;
	  }
  }
    
	
  	
	public String getEncryptPwdValue(String inputPwd, Map<String,Object> SHA_CHK) {
		String BcryptPwd = null;
		String SHA_YN = (String) (SHA_CHK.get("SHA_CHK") == null ?  "" :SHA_CHK.get("SHA_CHK")); //SHA_CHK 암호화 적용 여부
		String mem_pw = (String) (SHA_CHK.get("mem_pw") == null ?  "" :SHA_CHK.get("mem_pw")); //SHA_CHK 암호화 적용 여부
		String chk_yn = (String) (SHA_CHK.get("chk_yn") == null ?  "" :SHA_CHK.get("chk_yn")) ; //기존 호야 암호화 여부
		try {
			CommonUtil commonUtil = new CommonUtil();
			//SHA-256 Bcrypt로 암호화
  		if("O".equals(SHA_YN)) {
  	  		boolean result =passwordEncoder.matches(inputPwd, mem_pw);
  	  		
  	  		//matches 결과 
  	  		if(result) {
  	  			BcryptPwd = (String)SHA_CHK.get("mem_pw");  // TRUE이면 DB에 pwd 값 리턴
  	  		}else {
  	  			BcryptPwd = null; 						  // FALSE이면 password null 값 리턴
  	  		}
  		}else{
  			//hoya로 암호화
//  			if("y".equals(chk_yn)) {
  				BcryptPwd = commonUtil.get_hoya_en("pwd", inputPwd); // 호야 인코딩 값 리턴
//  			}else{
//  				BcryptPwd = inputPwd; 					   // 평문 리턴 (사용자에게 입력받은 값 그대로)
//  				System.out.println("no hoyapwd" + BcryptPwd);
//  			}
  			
  		}
  		
		} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		return BcryptPwd;
	}

	/**
	 * dataTable OrderColumn, OrderDir 처리
	 * sql Injection 처리
	 * @param orderColumn
	 * @return
	 */
	public static void DataTableOrderCheck(HttpServletRequest request, DefaultVO searchVO)  {
		String orderColumnNum = String.valueOf(request.getParameter("order[0][column]")).replace("null", "0");
		String orderColumn = String.valueOf(request.getParameter("columns[" + orderColumnNum + "][data]"));

		String orderDir = String.valueOf(request.getParameter("order[0][dir]")).replace("null", "asc").toLowerCase();


		//특수문자 포함 체크 포함일시 "NULL" 처리
		String pattern = ".*[*~`!^-_=+<>@\\#$%&\\[\\]\\{\\}/?,.;:|\\(\\)].*";  //특수문자 체크
		Matcher match;
		match = Pattern.compile(pattern).matcher(orderColumn);
		if(match.find()) {
			orderColumn = "NULL";
		}

		orderColumn = CommonUtil.potholeCase(orderColumn);

		if(!"asc".equals(orderDir) && !"desc".equals(orderDir)) {
			orderDir = "asc";
		}

		searchVO.setOrderColumn(orderColumn);
		searchVO.setOrderDir(orderDir);
	}

	/**
	 * camel -> Pothole Case 변환 클래스
	 * @param cpCase
	 * @return
	 */
	public static String potholeCase(String str) {

		String regex = "([a-z0-9])([A-Z])";

		String replacement = "$1_$2";

		String value = "";

		if(str != null && !"".equals(str)) {
			value = str.replaceAll(regex, replacement).toUpperCase();
		}

		return value;
	}


	public static String camelCase(String underScore) {

		// '_' 가 나타나지 않으면 이미 camel case 로 가정함.
		// 단 첫째문자가 대문자이면 camel case 변환 (전체를 소문자로) 처리가
		// 필요하다고 가정함. --> 아래 로직을 수행하면 바뀜
		if (underScore.indexOf('_') < 0 && Character.isLowerCase(underScore.charAt(0))) {
			return underScore;
		}
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		int len = underScore.length();

		for (int i = 0; i < len; i++) {
			char currentChar = underScore.charAt(i);
			if (currentChar == '_') {
				nextUpper = true;
			} else {
				if (nextUpper) {
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				} else {
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}
		return result.toString();
	}


}//Finish this class