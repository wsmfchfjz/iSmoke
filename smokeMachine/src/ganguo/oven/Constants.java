package ganguo.oven;

/**
 * Created by Tony on 1/19/15.
 */
public class Constants {
	//这是修改分支
    public static final boolean isDev = false;

    public static final String OVEN_SETTING_TIMER_HOUR = "oven_setting_timer_hour";
    public static final String OVEN_SETTING_TIMER_MINUTE = "oven_setting_timer_minute";
    public static final String SMOKER_SETTING_TIMER_HOUR = "smoker_setting_timer_hour";
    public static final String SMOKER_SETTING_TIMER_MINUTE = "smoker_setting_timer_minute";
    
    public static final String DEVICE_MAC = "device_mac";
    public static final String SETTING_PASSWORD = "setting_password";
    public static final String SETTING_TEMPERATURE_UNIT = "setting_temperature_unit";
    public static final String SETTING_ALERT_TEMPERATURE_UNIT = "setting_alert_temperature_unit";
    public static final String SETTING_ALERT_TEMPERATURE_A = "setting_alert_temperature_a";
    public static final String SETTING_ALERT_TEMPERATURE_B = "setting_alert_temperature_b";
    public static final String SETTING_MEAT_A = "setting_meat_a";
    public static final String SETTING_MEAT_B = "setting_meat_b";
    
    public static final String LAST_MEAT_A = "last_meat_a";
    public static final String LAST_MEAT_B = "last_meat_b";
    public static final String LAST_MEAT_TEMP_A = "last_meat_temp_a";
    public static final String LAST_MEAT_TEMP_B = "last_meat_temp_b";
    
    //以下用来存储上一次设备状态
    public static final String LAST_TEMP = "last_temp";
    public static final String LAST_TEMP_UNIT = "last_temp_unit";
    public static final String LAST_TEMP_A = "last_temp_a";
    public static final String LAST_TEMP_B = "last_temp_b";
    public static final String LAST_OVEN_TIME = "last_oven_time";
    public static final String LAST_SMOKE_TIME = "last_smoke_time";
    public static final String LAST_SETTING_TEMP = "last_setting_temp";
    
    public static final String CLOSE_DOOR_WARN = "All the controls have been disabled. Please close the smoker door before continuing the operation.";
    
    /**
     * 图表列表
     */
    public static final String CHARTS_LIST = "my_charts_list_201507061533";
    /**
     * 关闭探针温度
     */
	public static final int closeProbeTemp = 320320;
	/**
	 * 当前温度单位是否是℉
	 */
	public static boolean isUnitF = false;;

//	public static final String DEVICE_NAME = "BLE-SPP";
	public static final String DEVICE_NAME = "iSmoke";
//	public static final String DEVICE_NAME = "iSmoker";
	public static final String LOG_FILE_NAME = "SmokeMachineAppLog";
	/**
	 * 当前是否测试模式
	 */
	public static boolean isTestMode = false;
	/**
	 * 横坐标最大分钟数
	 */
	public static final int MAX_MIN = 600;
	/**
	 * 搜索界面，SCAN_SECOND毫秒内搜索不到iSmoker开头的设备，则跳到体验模式
	 */
	public static final int SCAN_SECOND = 5000;
	/**
	 * 发送两个命令之间的间隔时间
	 */
	public static final int SEND_DATA_SLEEP_TIME = 100;
    
}
