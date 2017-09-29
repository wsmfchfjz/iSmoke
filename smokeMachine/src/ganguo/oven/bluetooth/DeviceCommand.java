package ganguo.oven.bluetooth;

/**
 * Created by Tony on 1/21/15.
 */
public class DeviceCommand {

    public static final byte SMOKER_SWITCH = 0x01;  // 烟熏功能控制	用于开启/停止烟熏功能
    public static final byte OVEN_SWITCH = 0x02;    // 烧烤功能控制	用于开启/停止烧烤功能
    public static final byte TEMPERATURE_UNIT = 0x03;    // 温度单位选择	用于℃/℉切换控制
    public static final byte ATCION_SEND_CAKE = 0x04;    // 送料控制	用于启动一次送饼动作
    public static final byte SMOKER_TIME = 0x05;         // 设置烟熏工作时间	用于设置烟熏工作时间
    public static final byte OVEN_TIME = 0x06;           // 设置烧烤工作时间	用于设置烧烤工作时间
    public static final byte OVEN_TEMPERATURE = 0x07;  // 设置烧烤温度	用于设置烟熏烧烤温度
    public static final byte PROBE_TEMPERATURE_A = 0x08;    // 设置温度探头A--食物温度目标值	用于食物温度目标值，当达到食物目标温度-5F时提示
    public static final byte PROBE_TEMPERATURE_B = 0x09;    // 设置温度探头B--食物温度目标值	用于食物温度目标值，当达到食物目标温度-5F时提示
    public static final byte CONNECTED = 0x0A;    // 配对成功	App获得控制权限后发送
    public static final byte MEAT = 0x0b;    // 肉类
//    public static final byte REQUEST_PWD = 0x0F;    // APP启动时，请求控制器发送登录密码,同时控制器显示密码

}
