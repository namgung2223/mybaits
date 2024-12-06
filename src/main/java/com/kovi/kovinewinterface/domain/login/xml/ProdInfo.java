package com.kovi.kovinewinterface.domain.login.xml;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Setter
public class ProdInfo {
    private String type;
    private String exename;
    private String installurl;
    private String majorver;
    private String fullpackuser;
    private String aruser;

    @XmlAttribute
    public String getType() {
        return type;
    }


    @XmlElement
    public String getExename() {
        return exename;
    }


    @XmlElement
    public String getInstallurl() {
        return installurl;
    }


    @XmlElement
    public String getMajorver() {
        return majorver;
    }


    @XmlElement
    public String getFullpackuser() {
        return fullpackuser;
    }

    @XmlElement
    public String getAruser() {
        return aruser;
    }

}
