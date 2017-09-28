package ganguo.oven.bluetooth;

import android.util.Log;

/**
 * Created by Tony on 1/18/15.
 */
public class ReceiveData {
    private byte deviceCode;        // 1byte D0 设备码: 燃气热水器-0x01 电热水器-0x02 烟熏机-0x03 两用炉-0x04
    private int password;           // 2byte D1-2 范围00001-65535； APP登录（或通讯）密码，由MCU随机产生，随数据包发送给APP及TFT，（也作为产品的地址或数据用于识别）。密码以十进制显示..D1，D2——值为0x00，  D3-D21 为查询数据
    private byte smokedStatus;      // 1byte D3 烟熏开关机状态: 烟熏功能启动——0x01； 烟熏功能关闭——0x00；
    private byte ovenStatus;        // 1byte D4 烧烤开关机状态: 烧烤功能启动——0x01； 烧烤功能关闭——0x00；
    private byte temperatureUnit;   // 1byte D5（低四位） 当前温度单位 ℉——0x00；  ℃——0x01；
    private byte doorOpenStatus;    // 1byte D5（高四位） 当前门开关状态，关门—0；开门—1
    private byte temp;              // 1byte D6 预留: 0x00；
    private byte woodStatus;        // 1byte D7 木饼状态: 正常——0x00； 缺饼——0x01；
    private byte temp2;             // 1byte D8 预留: 0x00；
    private int smokedHourTime;    // 1byte D9-10  烟熏剩余时间: D11——0x00-0x18(0-23小时)
    private int smokedMinuteTime;  // 1byte D9-10  烟熏剩余时间: D12——0x00-0x3b(0-59分钟)；
    private int ovenHourTime;      // 2byte D11-12 烧烤剩余时间: D11——0x00-0x18(0-23小时)；
    private int ovenMinuteTime;    // 2byte D11-12 烧烤剩余时间: D12——0x00-0x3b(0-59分钟)；
    private int settingTemperature; // 2byte D13-D14 设置温度: 0x0000-0xffff
    private int probeTemperature;   // 2byte D15-D16 箱体探头实测温度: 0x0000-0xffff，当温度探头未连接时用0xffff代替
    private int probeTemperatureA;  // 2byte D17-D18 探头A实测温度: 0x0000-0xffff，当温度探头未连接时用0xffff代替
    private int probeTemperatureB;  // 2byte D19-D20 探头B实测温度: 0x0000-0xffff，当温度探头未连接时用0xffff代替
    private byte malfunctionStatus; // 1byte D21 故障状态: 0xe0-0xe9,及其他定义代码
    private byte meatSelection;     // 1byte D22 肉类选择未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4 ，高4位代表探头1，低4位代表探头2 ，例如：0x14代表探头1是猪肉，探头2是羊肉
    private byte checkCode;         // 1byte D23 此字节为校验字节 等于所有字节相加取反后，再加1; 即 D22  =  ~(0xbb +D0+D1+….+D21) +1

    public void setData(byte[] data) {
//        byte header = data[0];
        deviceCode = data[1];
        password = ByteUtils.getIntFrom2ByteArray(new byte[]{data[2], data[3]});
        smokedStatus = data[4];
        ovenStatus = data[5];
        temperatureUnit = (byte) (data[6] % 2);
        doorOpenStatus = (byte) (data[6] >> 4) ;
        temp = data[7];
        woodStatus = data[8];
        temp2 = data[9];
        smokedHourTime = data[10] & 0xff;
        smokedMinuteTime = data[11] & 0xff;
        ovenHourTime = data[12] & 0xff;
        ovenMinuteTime = data[13] & 0xff;
        settingTemperature = ByteUtils.getIntFrom2ByteArray(new byte[]{data[14], data[15]});
        probeTemperature = ByteUtils.getIntFrom2ByteArray(new byte[]{data[16], data[17]});
        probeTemperatureA = ByteUtils.getIntFrom2ByteArray(new byte[]{data[18], data[19]});
        probeTemperatureB = ByteUtils.getIntFrom2ByteArray(new byte[]{data[20], data[21]});
        malfunctionStatus = data[22];
        meatSelection = data[23];
        checkCode = data[24];

    }

    public byte getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(byte deviceCode) {
        this.deviceCode = deviceCode;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
    }

    public byte getSmokedStatus() {
        return smokedStatus;
    }

    public void setSmokedStatus(byte smokedStatus) {
        this.smokedStatus = smokedStatus;
    }

    public byte getOvenStatus() {
        return ovenStatus;
    }

    public void setOvenStatus(byte ovenStatus) {
        this.ovenStatus = ovenStatus;
    }

    public byte getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(byte temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public byte getTemp() {
        return temp;
    }

    public void setTemp(byte temp) {
        this.temp = temp;
    }

    public byte getWoodStatus() {
        return woodStatus;
    }

    public void setWoodStatus(byte woodStatus) {
        this.woodStatus = woodStatus;
    }

    public byte getTemp2() {
        return temp2;
    }

    public void setTemp2(byte temp2) {
        this.temp2 = temp2;
    }

    public int getSmokedHourTime() {
        return smokedHourTime;
    }

    public void setSmokedHourTime(byte smokedHourTime) {
        this.smokedHourTime = smokedHourTime;
    }

    public int getSmokedMinuteTime() {
        return smokedMinuteTime;
    }

    public void setSmokedMinuteTime(byte smokedMinuteTime) {
        this.smokedMinuteTime = smokedMinuteTime;
    }

    public int getOvenHourTime() {
        return ovenHourTime;
    }

    public void setOvenHourTime(byte ovenHourTime) {
        this.ovenHourTime = ovenHourTime;
    }

    public int getOvenMinuteTime() {
        return ovenMinuteTime;
    }

    public void setOvenMinuteTime(byte ovenMinuteTime) {
        this.ovenMinuteTime = ovenMinuteTime;
    }

    public int getSettingTemperature() {
        return settingTemperature;
    }

    public void setSettingTemperature(int settingTemperature) {
        this.settingTemperature = settingTemperature;
    }

    public int getProbeTemperature() {
        return probeTemperature;
    }

    public void setProbeTemperature(int probeTemperature) {
        this.probeTemperature = probeTemperature;
    }

    public int getProbeTemperatureA() {
        return probeTemperatureA;
    }

    public void setProbeTemperatureA(int probeTemperatureA) {
        this.probeTemperatureA = probeTemperatureA;
    }

    public int getProbeTemperatureB() {
        return probeTemperatureB;
    }

    public void setProbeTemperatureB(int probeTemperatureB) {
        this.probeTemperatureB = probeTemperatureB;
    }

    public byte getMalfunctionStatus() {
        return malfunctionStatus;
    }

    public void setMalfunctionStatus(byte malfunctionStatus) {
        this.malfunctionStatus = malfunctionStatus;
    }

    public byte getMeatSelection() {
		return meatSelection;
	}

	public void setMeatSelection(byte meatSelection) {
		this.meatSelection = meatSelection;
	}

	public byte getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(byte checkCode) {
        this.checkCode = checkCode;
    }

	public byte getDoorOpenStatus() {
		return doorOpenStatus;
	}

	public void setDoorOpenStatus(byte doorOpenStatus) {
		this.doorOpenStatus = doorOpenStatus;
	}

	@Override
	public String toString() {
		return "ReceiveData [deviceCode=" + deviceCode + ", password=" + password + ", smokedStatus=" + smokedStatus
				+ ", ovenStatus=" + ovenStatus + ", temperatureUnit=" + temperatureUnit + ", doorOpenStatus="
				+ doorOpenStatus + ", temp=" + temp + ", woodStatus=" + woodStatus + ", temp2=" + temp2
				+ ", smokedHourTime=" + smokedHourTime + ", smokedMinuteTime=" + smokedMinuteTime + ", ovenHourTime="
				+ ovenHourTime + ", ovenMinuteTime=" + ovenMinuteTime + ", settingTemperature=" + settingTemperature
				+ ", probeTemperature=" + probeTemperature + ", probeTemperatureA=" + probeTemperatureA
				+ ", probeTemperatureB=" + probeTemperatureB + ", malfunctionStatus=" + malfunctionStatus
				+ ", meatSelection=" + meatSelection + ", checkCode=" + checkCode + "]";
	}
}
