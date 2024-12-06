package com.kovi.kovinewinterface.domain.login.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class HashMapDto {
    private String hashMap;
    private String memberId;

    public HashMapDto(String hashMap, String memberId){
        this.hashMap = hashMap;
        this.memberId = memberId;
    }
}
