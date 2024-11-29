package com.kovi.kovinewinterface.application.controller;

import com.kovi.kovinewinterface.domain.login.utils.LoginUtils;
import com.kovi.kovinewinterface.domain.login.dto.MemberDto;
import com.kovi.kovinewinterface.domain.login.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {


    private final LoginService loginService;

    @GetMapping("/oldLauncherLogin")
    public String test(HttpServletRequest request, HttpServletResponse response, @RequestParam String id, @RequestParam String password) {
        String message ="로그인 정보를 확인해주세요.";
        String randomNumber = LoginUtils.randomNumber();
        try {
            MemberDto memberDto = loginService.findMemberById(id);
            if(loginService.matchingPassword(memberDto, password) && loginService.demoCheck(id)){
                LoginUtils.setCookies(response,memberDto.getMemberId(),memberDto.getName());
                loginService.saveMemberLog(memberDto,request.getRemoteAddr());
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            return String.format("FALSE:%s:%s:??????",e.getMessage(),randomNumber);
        } catch (UnsupportedEncodingException e) {
            return String.format("FALSE:%s:%s:??????",message,randomNumber);
        }
        return String.format("TRUE:%s:%s:??????",message,randomNumber);
    }

}
