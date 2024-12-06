package com.kovi.kovinewinterface.domain.login.xml;


import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;

@Setter
public class UserInfo {
    private String id;
    private String sitekey;

    @XmlElement
    public String getId() {
        return id;
    }

    @XmlElement
    public String getSitekey() {
        return sitekey;
    }

}

