package ganguo.oven.bluetooth;

import android.util.Log;
import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;

/**
 * Created by Tony on 1/21/15.
 */
public class DeviceModule {

    private static EventBus mEventBus = EventBus.getDefault();

    /**
     * 发送查询命令
     */
    public static void sendQueryCmd() {
        SettingData setting = new SettingData();
        setting.setActionType((byte)0x01);
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }
    
    public static void setSmokerStatus(boolean isOn) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.SMOKER_SWITCH);
        if (isOn) {
            setting.setCommandData((short) 0x0001);
        } else {
            setting.setCommandData((short) 0x0000);
        }
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }

    public static void setOvenStatus(boolean isOn) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.OVEN_SWITCH);
        if (isOn) {
            setting.setCommandData((short) 0x0001);
        } else {
        	Log.i("jLog", "setOvenStatus false");
            setting.setCommandData((short) 0x0000);
        }
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }

    /**
     * 设置时间 && Smoker 开机
     *
     * @param hour
     * @param minute
     */
    public static void setSmokerTime(int hour, int minute) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.SMOKER_TIME);
        byte[] times = {(byte) minute, (byte) hour};
        setting.setCommandData(ByteUtils.bytesToShort(times));
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));

        sendAlertProbeTemperature();
    }

    /**
     * 设置时间 && Oven 开机
     *
     * @param hour
     * @param minute
     */
    public static void setOvenTime(int hour, int minute) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.OVEN_TIME);
        byte[] times = {(byte) minute, (byte) hour};
        setting.setCommandData(ByteUtils.bytesToShort(times));
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));

        sendAlertProbeTemperature();
    }

    /**
     * 设置Oven温度
     *
     * @param temperature
     */
    public static void setOvenTemperature(int temperature) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.OVEN_TEMPERATURE);
        setting.setCommandData((short) temperature);
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }
    
    /**
     * 设置肉类
     */
    public static void setMeat() {
    	int probe1Meat = Config.getInt(Constants.SETTING_MEAT_A);
    	int probe2Meat = Config.getInt(Constants.SETTING_MEAT_B);
    	switch (probe1Meat) {
		case 1://羊
			probe1Meat = 4;
			break;
		case 2://牛
			probe1Meat = 2;
			break;
		case 3://鱼
			probe1Meat = 5;
			break;
		case 4://猪
			probe1Meat = 1;
			break;
		case 5://鸡
			probe1Meat = 3;
			break;
		}
    	switch (probe2Meat) {
    	case 1:
    		probe2Meat = 4;
    		break;
    	case 2:
    		probe2Meat = 2;
    		break;
    	case 3:
    		probe2Meat = 5;
    		break;
    	case 4:
    		probe2Meat = 1;
    		break;
    	case 5:
    		probe2Meat = 3;
    		break;
    	}
    	SettingData setting = AppContext.getInstance().getSettingData();
    	setting.setCommand(DeviceCommand.MEAT);
    	setting.setCommandData((short) (probe2Meat + probe1Meat * 16));
    	mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }

    /**
     * 发送探头温度
     */
/*    public static void sendAlertProbeTemperature() {
        int alertTempA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
        int alertTempB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);

        if (alertTempA > 49) {
        	if(alertTempA == Constants.closeProbeTemp){
        		setProbeTemperatureA(alertTempA);
        	}else{
        		if(ChartFragment.isUnitF){
        			setProbeTemperatureA(alertTempA % 1000);
        		} else {
        			setProbeTemperatureA(alertTempA / 1000);
        		}
        	}
        }
        if (alertTempB > 49) {
        	if(alertTempB == Constants.closeProbeTemp){
        		setProbeTemperatureB(alertTempB);
        	}else{
        		if(ChartFragment.isUnitF){
        			setProbeTemperatureB(alertTempB % 1000);
        		} else {
        			setProbeTemperatureB(alertTempB / 1000);
        		}
        	}
        }
    }*/
    /**
     * 发送探头温度
     */
    public static void sendAlertProbeTemperature() {
        int alertTempA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
        int alertTempB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);

        if (alertTempA > 49) {
            setProbeTemperatureA(alertTempA);
        }
        if (alertTempB > 49) {
            setProbeTemperatureB(alertTempB);
        }
    }
    /**
     * 发送探头1温度
     */
    public static void sendAlertProbeTemperature1() {
    	int alertTempA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
    	
    	if (alertTempA > 49) {
    		setProbeTemperatureA(alertTempA);
    	}
    }
    /**
     * 发送探头2温度
     */
    public static void sendAlertProbeTemperature2() {
        int alertTempB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);

        if (alertTempB > 49) {
            setProbeTemperatureB(alertTempB);
        }
    }
    /**
     * 设置时间 && Oven 开机
     *
     * @param temperature
     */
    public static void setProbeTemperatureA(int temperature) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.PROBE_TEMPERATURE_A);
        setting.setCommandData((short) temperature);
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }

    /**
     * 设置时间 && Oven 开机
     *
     * @param temperature
     */
    public static void setProbeTemperatureB(int temperature) {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.PROBE_TEMPERATURE_B);
        setting.setCommandData((short) temperature);
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }

    /**
     * APP启动时，请求控制器发送登录密码,同时控制器显示密码
     */
//    public static void requestPassword() {
//        SettingData setting = AppContext.getInstance().getSettingData();
//        setting.setCommand(DeviceCommand.REQUEST_PWD);
//        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
//    }

    /**
     * APP启动时，请求控制器发送登录密码,同时控制器显示密码
     */
    public static void sendConnected() {
        SettingData setting = AppContext.getInstance().getSettingData();
        setting.setCommand(DeviceCommand.CONNECTED);
        mEventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
    }
}
