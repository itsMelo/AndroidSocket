package melo.com.androidsocket.bean;


public class Users {

    private int softVersion;
    private int romVersion;
    private String imei;
    private String device;
    private String ip;
    private String loginTime;

    public int getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(int softVersion) {
        this.softVersion = softVersion;
    }

    public int getRomVersion() {
        return romVersion;
    }

    public void setRomVersion(int romVersion) {
        this.romVersion = romVersion;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }
}
