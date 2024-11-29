package com.kovi.kovinewinterface.common;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpConnectUtil {

    public static String getBody(String apiUrl, Map<String, String> requestHeaders, String method, boolean inOutPut, String requestBody){
        HttpURLConnection con = connect(apiUrl);
        String returnString = null;
        try {
            con.setRequestMethod(method);
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            con.setDoOutput(inOutPut);

            if (inOutPut) {
                OutputStream os = con.getOutputStream();
                os.write(requestBody.getBytes());
                os.flush();

                Charset charset = StandardCharsets.UTF_8;
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출ㅌ4
                returnString =  readBody(con.getInputStream());
            } else { // 오류 발생
                returnString = readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }

        return returnString;
    }


    public static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }


    public static String readBody(InputStream body) throws UnsupportedEncodingException {
        System.out.println(body);
        InputStreamReader streamReader = new InputStreamReader(body,"UTF-8");

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

}
