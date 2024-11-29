package com.kovi.kovinewinterface.domain.login.mapper;


import com.kovi.kovinewinterface.domain.login.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface LoginMapper {
    MemberDto findMemberById(String memberId);

    int saveLoginInfoById(String memberId);

    int saveUserChk(MemberDto memberDto);
    LocalDateTime demoCheck(String memberId);
}
