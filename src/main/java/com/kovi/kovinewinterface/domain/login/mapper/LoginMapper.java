package com.kovi.kovinewinterface.domain.login.mapper;


import com.kovi.kovinewinterface.domain.login.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoginMapper {
    MemberDto findMemberById(String memberId);

    int saveLoginInfoById(String memberId);

    int saveUserChk(MemberDto memberDto);
    LocalDateTime demoCheck(String memberId);

    List<ProductUseDto> findUsingProductsById(String memberId);
    List<ProgramXmlInfoDto> findXmlInfoByPrdtCd(List<ProductUseDto> dtos);
    ProductKhrInfo findKHRById(String memberId);

    void saveLauncherHashMap(HashMapDto hashMapDto);
    void deleteLauncher();
}
