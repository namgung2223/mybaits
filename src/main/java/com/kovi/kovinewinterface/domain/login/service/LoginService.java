package com.kovi.kovinewinterface.domain.login.service;

import com.kovi.kovinewinterface.domain.login.dto.*;
import com.kovi.kovinewinterface.domain.login.mapper.LoginMapper;
import com.kovi.kovinewinterface.domain.login.utils.LoginUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {


    private final LoginMapper loginMapper;
    private final LoginUtils loginUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public boolean matchingPassword(MemberDto memberDto, String password){
        if(memberDto == null) throw new NoSuchElementException("로그인 정보를 확인해주세요.");
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

    public boolean demoCheck(MemberDto memberDto) {
        //데모 사용중인지 체크
        if(memberDto.getEventJoin() != null){
            LocalDateTime endDate = loginMapper.demoCheck(memberDto.getMemberId());
            if (!LoginUtils.isValidDemoPeriod(endDate)) {
                throw new IllegalStateException("사용기간이 종료되었습니다. 문의사항은 1644-4932로 연락주세요.");
            }
        }
        return true;
    }

    public void refreshHashMap(HashMapDto hashMapDto){
        //신천러 로그인 기록 insert
        loginMapper.saveLauncherHashMap(hashMapDto);
        //신런처 1분전 로그인 기록 삭제
        loginMapper.deleteLauncher();
    }



    public void createXmlFile(HttpServletRequest request, MemberDto memberDto) {
        List<ProgramXmlInfoDto> programXmlInfoDtos = new ArrayList<>();
        List<ProductUseDto> usingProductsById = new ArrayList<>();

        //아카데미인 경우 아키3만 사용
        if("academy".equals(memberDto.getChange()) && request.getRemoteAddr().contains("210.125.195.") || request.getRemoteAddr().contains("210.125.193.204")){
            ProductUseDto productUseDto = new ProductUseDto("R");
            usingProductsById.add(productUseDto);
        }else{
        //사용중 프로그램 xml정보 조회
            usingProductsById = loginMapper.findUsingProductsById(memberDto.getMemberId());
        }

        //정지일이 현재일보다 이전일 경우 사용 정지 상태이므로 사용 리스트에서 제거
        usingProductsById.removeIf(product -> "Z".equals(product.getPriceType()) && product.getPauseDt().isBefore(LocalDate.now()));

        //Kitchen 프로그램 사용중일 시 키친2.5도 포함
        boolean haveProgramKitchen = usingProductsById.stream().anyMatch(x -> "K".equals(x.getPrdtCd()));
        if(haveProgramKitchen){
            ProductUseDto productUseDto = new ProductUseDto("I");
            usingProductsById.add(productUseDto);
        }

        //KHR 프로그램 사용여부
        ProductKhrInfo xmlInfoByPrdtCd = loginMapper.findKHRById(memberDto.getMemberId());
        if(xmlInfoByPrdtCd != null){
            if("A".equals(xmlInfoByPrdtCd.getProdStat()) || "y".equals(xmlInfoByPrdtCd.getTempDateStatus())){
                ProductUseDto productUseDto = new ProductUseDto("KHR");
                usingProductsById.add(productUseDto);
            }
        }
        
        //사용 가능 프로그램 이 없을 경우 에러발생
        if(usingProductsById.isEmpty()){
            throw new IllegalStateException("해당 계정은 현재 프로그램 사용이 제한되어 있습니다. 홈페이지 마이페이지에서 확인하시거나 고객지원센터(1644-4932)로 문의 바랍니다.");
        }

        //product_info, product_demo , product_use 사용중인 프로그램 조회
        programXmlInfoDtos = loginMapper.findXmlInfoByPrdtCd(usingProductsById);
        //fullpackuser set
        loginUtils.mapServiceTypeToPrograms(programXmlInfoDtos,usingProductsById);
        //xml파일생성
        loginUtils.createXmlFile(request,memberDto,programXmlInfoDtos);
    }

}


