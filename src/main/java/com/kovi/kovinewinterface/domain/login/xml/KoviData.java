package com.kovi.kovinewinterface.domain.login.xml;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@Setter
@XmlRootElement(name = "kovidata")
@XmlType(propOrder = { "userinfo", "prodinfos" })
public class KoviData {

    private UserInfo userinfo;
    private List<ProdInfo> prodinfos = new ArrayList<>();

    @XmlElement
    public UserInfo getUserinfo() {
        return userinfo;
    }

    @XmlElement(name = "prodinfo")
    public List<ProdInfo> getProdinfos() {
        return prodinfos;
    }

    public void addProdinfo(ProdInfo prodInfo) { this.prodinfos.add(prodInfo);}
}
