package com.kovi.kovinewinterface.domain.login.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ProgramXmlInfoDto {

    private String ProgramNm;
    private String type;
    private String prdtCd;
    private String exeName;
    private String installUrl;
    private String majorVer;
    private String fullPackUser;
    private String arUser;
    private LocalDateTime regDate;


}
