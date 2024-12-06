package com.kovi.kovinewinterface.application.controller;

import com.kovi.kovinewinterface.common.FileUtil;
import com.kovi.kovinewinterface.domain.login.dto.HashMapDto;
import com.kovi.kovinewinterface.domain.login.dto.MemberDto;
import com.kovi.kovinewinterface.domain.login.service.LoginService;
import com.kovi.kovinewinterface.domain.login.utils.LoginUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {


    private final LoginService loginService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping({"/oldLauncherLogin","/newLauncherLogin"})
    public String test(HttpServletRequest request, HttpServletResponse response, @RequestParam String id, @RequestParam String password) {
        String message =String.format("http://www.koviarchi.co.kr/service/xml44test/%s.xml",id);
        String randomNumber = LoginUtils.randomNumber();
        String result = "TRUE";

        //신런처 요청일 경우 아이디를 암호화하고 테이블 insert delete 실행 -> result는 암호화된 아이디
        if("/newLauncherLogin".equals(request.getRequestURI())){
            result = bCryptPasswordEncoder.encode(FileUtil.getToday("yyyy-MM-dd HH-mm") + id);
            HashMapDto hashMapDto = new HashMapDto(result,id);
            loginService.refreshHashMap(hashMapDto);
        }

        try {
            MemberDto memberDto = loginService.findMemberById(id);
            boolean b = loginService.demoCheck(memberDto);
            if(loginService.matchingPassword(memberDto, password) && b){
                loginService.saveMemberLog(memberDto,request.getRemoteAddr());
                loginService.createXmlFile(request,memberDto);
//                LoginUtils.setCookies(response,memberDto.getMemberId(),memberDto.getName());
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            e.toString();
            return String.format("FALSE:%s:%s:??????",e.getMessage(),randomNumber);
        } catch (Exception e) {
            e.toString();
            throw new RuntimeException(e);
        }
        return String.format("%s:%s:%s:??????",result , message, randomNumber);
    }

}
