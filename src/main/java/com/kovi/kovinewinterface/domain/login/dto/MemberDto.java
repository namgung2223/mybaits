package com.kovi.kovinewinterface.domain.login.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class MemberDto {
    @JsonProperty("PRDT_CD")
    private String prdtCd;
    private String ip;

    private String memberId;
    private String pwd;
    @JsonProperty("SHA_CHK")
    private String shaChk;
    @JsonProperty("com_name")
    private String comName;
    private String name;
    @JsonProperty("event_join")
    private String eventJoin;
    private String change;
}
