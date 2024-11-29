package com.kovi.kovinewinterface.domain.login.service;

import com.kovi.kovinewinterface.domain.login.utils.LoginUtils;
import com.kovi.kovinewinterface.domain.login.dto.MemberDto;
import com.kovi.kovinewinterface.domain.login.mapper.LoginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginMapper loginMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public boolean matchingPassword(MemberDto memberDto, String password){
        if(memberDto == null) throw new NoSuchElementException("회원 NULL");
        //패스워드 매칭
        if(bCryptPasswordEncoder.matches(password, memberDto.getPwd())) return true;
        else throw new NoSuchElementException("로그인 정보를 확인해주세요.");
    }

    public MemberDto findMemberById(String memberId){
        return loginMapper.findMemberById(memberId);
    }

    public void saveMemberLog(MemberDto memberDto,String ip){
        memberDto.setIp(ip);
        //접속자 데이터
        loginMapper.saveLoginInfoById(memberDto.getMemberId());
        //로그 데이터
        loginMapper.saveUserChk(memberDto);
    }

    public boolean demoCheck(String memberId) {
        LocalDateTime endDate = loginMapper.demoCheck("rimdy");
        if (endDate == null || !LoginUtils.isValidDemoPeriod(endDate)) {
            throw new IllegalStateException("이벤트 기간(7일)이 종료되었습니다. 문의사항은 1644-4932로 연락주세요.");
        }
        return true;
    }


}


