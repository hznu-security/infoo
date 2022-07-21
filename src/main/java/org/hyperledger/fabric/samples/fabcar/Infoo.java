package org.hyperledger.fabric.samples.fabcar;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.sql.Timestamp;

@DataType()
public class Infoo {
    @Property()
    String ta;    //安全域标识
    @Property()
    String address;   // 云端地址
    @Property()
    String ct;       // 密钥密文
    @Property()
    String mstr;      // M写成字符串
    @Property()
    String rho;       // ρ
    @Property()
    String hash;       // 哈希
    @Property()
    String time;  // 时间

    public Infoo(String ta, String address, String ct, String mstr, String rho, String hash, String time) {
        this.ta = ta;
        this.address = address;
        this.ct = ct;
        this.mstr = mstr;
        this.rho = rho;
        this.hash = hash;
        this.time = time;
    }

    public String getTa() {
        return ta;
    }

    public void setTa(String ta) {
        this.ta = ta;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCt() {
        return ct;
    }

    public void setCt(String ct) {
        this.ct = ct;
    }

    public String getMstr() {
        return mstr;
    }

    public void setMstr(String mstr) {
        this.mstr = mstr;
    }

    public String getRho() {
        return rho;
    }

    public void setRho(String rho) {
        this.rho = rho;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
