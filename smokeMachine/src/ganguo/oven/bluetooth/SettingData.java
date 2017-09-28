package ganguo.oven.bluetooth;

import android.util.Log;

/**
 * 0xBB 187 开始帧
 * 0x44 68 结束帧
 * <p/>
 * Created by Tony on 1/19/15.
 */
public class SettingData {
    private byte actionType = 0x02;        // 1byte 通信类型 0x02:设置 0x01:查询 当为0x01 查询时，D0-D4可以为任意，MCU将不作解析。
    private byte deviceCode = 0x03;        // 1byte 值0x03，（固定不变） 设备码: 燃气热水器-0x01 电热水器-0x02 烟熏机-0x03 两用炉-0x04
    private byte replyAddress = 0x00;      // 1byte 值0x00,  应答地址(预留0x00)
    private byte command;                   // 1byte 命令码，APP→控制器
    private short commandData;                // 2byte 命令内容
    private byte checkCode;                 // 1byte 校验码 等于所有字节相加取反后，再加1; 即 D5= ~(0xBB + 通信类型+D0+D1+….+D4) +1


    public byte[] toBytes() {
        byte[] data = new byte[9];
        data[0] = (byte) 0xBB;

        data[1] = actionType;
        data[2] = deviceCode;
        data[3] = replyAddress;
        data[4] = command;

        byte[] cmd = ByteUtils.shortToBytes(commandData);
        data[5] = cmd[1];
        data[6] = cmd[0];
        data[7] = ByteUtils.verifySettingData(data);

        data[8] = 0x44;

        ByteUtils.checkData(data);
//        Log.i("SettingData", "bytes " + data.length + " " + ByteUtils.bytesToHexString(data));
        return data;
    }

    public byte getActionType() {
        return actionType;
    }

    public void setActionType(byte actionType) {
        this.actionType = actionType;
    }

    public byte getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(byte deviceCode) {
        this.deviceCode = deviceCode;
    }

    public byte getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(byte replyAddress) {
        this.replyAddress = replyAddress;
    }

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public short getCommandData() {
        return commandData;
    }

    public void setCommandData(short commandData) {
        this.commandData = commandData;
    }

    public byte getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(byte checkCode) {
        this.checkCode = checkCode;
    }

    @Override
    public String toString() {
        return "SettingData{" +
                "actionType=" + actionType +
                ", deviceCode=" + deviceCode +
                ", replyAddress=" + replyAddress +
                ", command=" + command +
                ", commandData=" + commandData +
                ", checkCode=" + checkCode +
                '}';
    }
}
