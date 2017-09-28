package ganguo.oven.bluetooth;

import android.util.Log;

import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;

/**
 * Created by Tony on 1/28/15.
 */
public class OvenModule {
    private static final String TAG = OvenModule.class.getName();
    private static boolean mIsOn = false;
    private static int mTemperature = 0;

    /**
     * 设置时间 && Oven 开机
     *
     * @param hour
     * @param minute
     */
    public static void setOvenTime(int hour, int minute) {
        Config.putInt(Constants.OVEN_SETTING_TIMER_HOUR, hour);
		Config.putInt(Constants.OVEN_SETTING_TIMER_MINUTE, minute);
    }

    public static void setOvenTemperature(int temp) {
        mTemperature = temp;
    }
    
    /**
     * @param probe1Temp ℉
     * @param probe2Temp ℉
     * @param probe1Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     * @param probe2Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     */
    public static void setProbeTempAndMeat(int probe1Temp, int probe2Temp, int probe1Meat, int probe2Meat,byte unit){//63145 73165 71160 77170
    	if(unit == 0x00){//℉
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, probe1Temp % 1000);
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, probe2Temp % 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);
    	}else{
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, probe1Temp / 1000);
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, probe2Temp / 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
    	}
    	Config.putInt(Constants.SETTING_MEAT_A, probe1Meat);
    	Config.putInt(Constants.SETTING_MEAT_B, probe2Meat);
    	new Thread(){
        	public void run() {
        		try {
        	    	DeviceModule.sendAlertProbeTemperature();
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
			    	DeviceModule.setMeat();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	};
        }.start();
    }
    /**
     * @param probe1Temp ℉
     * @param probe1Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     * @param probe2Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     */
    public static void setProbeTempAndMeat1(int probe1Temp, int probe1Meat, int probe2Meat,byte unit){//63145 73165 71160 77170
    	if(unit == 0x00){//℉
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, probe1Temp % 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);
    	}else{
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, probe1Temp / 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
    	}
    	Config.putInt(Constants.SETTING_MEAT_A, probe1Meat);
    	Config.putInt(Constants.SETTING_MEAT_B, probe2Meat);
    	new Thread(){
    		public void run() {
    			try {
    				DeviceModule.sendAlertProbeTemperature1();
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
    				DeviceModule.setMeat();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		};
    	}.start();
    }
    /**
     * @param probe2Temp ℉
     * @param probe1Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     * @param probe2Meat 未/取消选择：0；猪肉：1；牛肉：2,；鸡肉：3；羊肉：4
     */
    public static void setProbeTempAndMeat2(int probe2Temp, int probe1Meat, int probe2Meat,byte unit){//63145 73165 71160 77170
    	if(unit == 0x00){//℉
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, probe2Temp % 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);
    	}else{
    		Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, probe2Temp / 1000);
    		Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
    	}
    	Config.putInt(Constants.SETTING_MEAT_A, probe1Meat);
    	Config.putInt(Constants.SETTING_MEAT_B, probe2Meat);
    	new Thread(){
        	public void run() {
        		try {
        	    	DeviceModule.sendAlertProbeTemperature2();
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
			    	DeviceModule.setMeat();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	};
        }.start();
    }

    public static void startOven() {
        AppContext.getInstance().getReceiveData().setOvenStatus((byte) 0x01);

        new Thread(){
        	public void run() {
        		try {
        			DeviceModule.setOvenTemperature(mTemperature);
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
					DeviceModule.setOvenTime(Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR), Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE));
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
					DeviceModule.setOvenStatus(true);
//					sleep(Constants.SEND_DATA_SLEEP_TIME);
					DeviceModule.sendAlertProbeTemperature();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	};
        }.start();
        

        AppContext.getInstance().getReceiveData().setOvenStatus((byte) 0x01);
        Log.i(TAG, "startOven");
    }

    public static void stopOven() {
    	Log.i("jLog", "stopOven");
        AppContext.getInstance().getReceiveData().setOvenStatus((byte) 0x00);
        DeviceModule.setOvenStatus(false);

        Log.i(TAG, "stopOven");
    }

    public static void setOvenStatus(boolean isOn) {
        mIsOn = isOn;
        if (!isOn) {
            AppContext.getInstance().getReceiveData().setOvenStatus((byte) 0x00);
        }
    }

    public static boolean isOvenOn() {
        return mIsOn;
    }
}
