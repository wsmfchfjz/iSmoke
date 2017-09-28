package ganguo.oven.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.ByteUtils;
import ganguo.oven.bluetooth.DeviceModule;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.bluetooth.SettingData;
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.AndroidUtils;
import ganguo.oven.utils.MediaUtils;
import ganguo.oven.utils.ShowNotification;

public class SettingUnitActiviy extends BaseActivity {
	private ImageButton fromSettingToHomeBtn;
	private ImageView iv_temperature_unit;
	private View moreGroup1, moreGroup2, moreGroup3, moreGroup4, moreGroup5;
	private EventBus eventBus = EventBus.getDefault();
	private ReceiveData mReceiveData = AppContext.getInstance().getReceiveData();
	
	private boolean cancleAlertDialogByMyself = false;
	private boolean isShowExitDialog4NoOKDialog = false;
	private AlertDialog alertExitDialog;
	private boolean isShowBISQUETTEDialog;//是否显示马达错误提示框。如果是的话，点击OK后，发送烟熏开启命令。
	private int lastErrorCode;//保存上一个错误码。
	AlertDialog alertDialog = null, alertDialog4OpenDoor = null;
	boolean isShownAlert = false;
	private boolean isAlertWood = true;
    private boolean isVisibility ;
    private boolean canNotify;
    private boolean isShowCloseDoorWarn;

    @Override
    protected void onResume() {
    	super.onResume();
    	isVisibility = true;
    	canNotify = true;
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	isVisibility = false;
    }
    
	public void onEventMainThread(BleEvent event) {
		switch (event.getCommand()) {
		case NOTIFY_RECEIVE_DATA:
			// 数据接收
			mReceiveData = (ReceiveData) event.getTarget();
			if (mReceiveData == null)
				return;
			setAlertTip();
			if (mReceiveData.getTemperatureUnit() == 0x00) {// ℉
				Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);
				iv_temperature_unit.setImageResource(R.drawable.more_button_of);
			} else {
				Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
				iv_temperature_unit.setImageResource(R.drawable.more_button_oc);
			}
			setTemp();
			break;
		}
	}

	@Override
	public void beforeInitView() {
		setContentView(R.layout.fragment_setting);
	}

	@Override
	public void initView() {
		moreGroup1 = findViewById(R.id.moreGroup1);
		moreGroup2 = findViewById(R.id.moreGroup2);
		moreGroup3 = findViewById(R.id.moreGroup3);
		moreGroup4 = findViewById(R.id.moreGroup4);
		moreGroup5 = findViewById(R.id.moreGroup5);

		fromSettingToHomeBtn = (ImageButton) findViewById(R.id.fromSettingToHomeBtn);

		iv_temperature_unit = (ImageView) findViewById(R.id.iv_temperature_unit);

		if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
			// ℃
			iv_temperature_unit.setImageResource(R.drawable.more_button_oc);
		} else {
			// ℉
			iv_temperature_unit.setImageResource(R.drawable.more_button_of);
		}

		 String version = AndroidUtils.getAppVersionName(this);
		 ((TextView)findViewById(R.id.tv_version)).setText("Version " +
				 version);
	}

	@Override
	public void initListener() {
		fromSettingToHomeBtn.setOnClickListener(singleClickListener);
		if(!Constants.isTestMode){
			iv_temperature_unit.setOnClickListener(singleClickListener);
		}
		moreGroup1.setOnClickListener(singleClickListener);
		moreGroup2.setOnClickListener(singleClickListener);
		moreGroup3.setOnClickListener(singleClickListener);
		moreGroup4.setOnClickListener(singleClickListener);
		moreGroup5.setOnClickListener(singleClickListener);
	}

	@Override
	public void initData() {
		eventBus.register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		eventBus.unregister(this);
	}

	private OnSingleClickListener singleClickListener = new OnSingleClickListener() {

		@Override
		public void onSingleClick(View v) {
			switch (v.getId()) {
			case R.id.fromSettingToHomeBtn:
				finish();
				return;
			case R.id.iv_temperature_unit:
				if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
					// ℉
					iv_temperature_unit.setImageResource(R.drawable.more_button_of);
					Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);

					SettingData setting = AppContext.getInstance().getSettingData();
					setting.setCommand((byte) 0x03);
					setting.setCommandData((short) 0x00);
					eventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
				} else {
					// ℃
					iv_temperature_unit.setImageResource(R.drawable.more_button_oc);
					Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);

					SettingData setting = AppContext.getInstance().getSettingData();
					setting.setCommand((byte) 0x03);
					setting.setCommandData((short) 0x01);
					eventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
				}
				break;
			case R.id.moreGroup1:
				// Bradley Home: www.bradleysmoker.com

				Uri uri = Uri.parse("http://www.bradleysmoker.com");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);

				break;
			case R.id.moreGroup2:
				// Recipes: https://www.north-america.bradleysmoker.com/recipes/
				 Uri uri2 = Uri.parse("http://www.bradleysmoker.com/appfiles/BS916_manual_v1.0.pdf");
				 Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
				 startActivity(intent2);

				break;
			case R.id.moreGroup3:

				Uri uri3 = Uri.parse("https://www.north-america.bradleysmoker.com/recipes/");
				Intent intent3 = new Intent(Intent.ACTION_VIEW, uri3);
				startActivity(intent3);

				break;
			case R.id.moreGroup4:

				Intent it = new Intent(Intent.ACTION_CALL);
				it.setData(Uri.parse("tel:10086"));
				startActivity(it);

				break;
			case R.id.moreGroup5:
				Intent data = new Intent(Intent.ACTION_SENDTO);
				data.setData(Uri.parse("mailto:1@qq.com"));
				data.putExtra(Intent.EXTRA_SUBJECT, "");
				data.putExtra(Intent.EXTRA_TEXT, "");
				startActivity(data);
				break;
			}
		}
	};

	private void showExitDialog4NoOKDialog() {
		isShowExitDialog4NoOKDialog = true;
		alertExitDialog = new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit?").setCancelable(false).setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				isShowExitDialog4NoOKDialog = false;
			}
		}).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DeviceModule.setOvenStatus(false);
				DeviceModule.setSmokerStatus(false);

				AppContext.getInstance().exit();
			}
		}).create();
		alertExitDialog.show();
	}

	private void setTemp(){//设置滑动条温度
		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();
		if(alertTempA >= 32768 && alertTempA <= (32768 + 320)){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, alertTempA - 32768);
		}
		if(alertTempB >= 32768 && alertTempB <= (32768 + 320)){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, alertTempB - 32768);
		}
	}


    private void setAlertTip() {
    	
    	if(mReceiveData.getDoorOpenStatus() == 1){//开门
    		if((alertDialog4OpenDoor != null && !alertDialog4OpenDoor.isShowing()) || alertDialog4OpenDoor == null){
    			if(!isShowExitDialog4NoOKDialog ){
    				if(isVisibility){
    					MediaUtils.playAlert(this);
    					cancleAlertDialogByMyself = false;
    					alertDialog4OpenDoor = new AlertDialog.Builder(this).setTitle("WARNING").setMessage(Constants.CLOSE_DOOR_WARN)
    							.setOnCancelListener(new OnCancelListener() {
    								@Override
    								public void onCancel(DialogInterface dialog) {
    									if (!cancleAlertDialogByMyself) {
    										showExitDialog4NoOKDialog();
    									}
    								}
    							}).create();
    					alertDialog4OpenDoor.setCanceledOnTouchOutside(false);
    					alertDialog4OpenDoor.show();
    				} else {
    					if(canNotify){
    						if(!isShowCloseDoorWarn){
    							isShowCloseDoorWarn = true;
    							ShowNotification.getInstence(getApplicationContext()).showNotification(101,"WARNING",Constants.CLOSE_DOOR_WARN);
    						}
    					}
    				}
//					alertDialog4OpenDoor.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {  .setNegativeButton("OK", null)
//    					@Override
//    					public void onClick(View v) {
//    						MediaUtils.stopAll();
//    					}
//    				});
    				return;
    			}
    		}
    	} else if(mReceiveData.getDoorOpenStatus() == 0){//关门
			isShowCloseDoorWarn = false;
    		if(alertDialog4OpenDoor != null && alertDialog4OpenDoor.isShowing()){
    			if(isVisibility){
    				MediaUtils.stopAll();
    				cancleAlertDialogByMyself = true;
    				alertDialog4OpenDoor.cancel();
    			}
    		}
    	}
		int code = ByteUtils.getIntFromByte(mReceiveData.getMalfunctionStatus());
		Log.d("setAlertTip", "code:" + code);
		
		// 已经提示着
		if (isShownAlert){//
			//如果错误码等于0，则取消提示
			if(code == 0 && lastErrorCode == 0xe4){
				if(alertDialog != null){
					alertDialog.dismiss();
				}
				isShownAlert = false;
				MediaUtils.stopAll();
			}
			return;
		}
        if (mReceiveData.getWoodStatus() == 0x01 && isAlertWood) {
            isAlertWood = false;
            showAlertDialog(102,"WARNING", "ADD BISQUETTES TO GENERATOR");
            return;
        } else if (mReceiveData.getWoodStatus() == 0x00) {
            isAlertWood = true;
        }
        if(code != lastErrorCode){
        	switch (code) {
        	case 0xe1:
        		// 0xe1:箱体探头错误
        		showAlertDialog(code,"WARNING", "CABLES NOT CONNECTED PROPERLY");
        		break;
        	case 0xe2:
        		// 0xe2:探头a短路
        		showAlertDialog(code,"WARNING", "Probe 1:\nCABLES NOT CONNECTED PROPERLY");
        		break;
        	case 0xe3:
        		// 0xe3:探头b短路
        		showAlertDialog(code,"WARNING", "Probe 2:\nCABLES NOT CONNECTED PROPERLY");
        		break;
        	case 0xe4:
        		// 0xe4:马达错误
        		showAlertDialog(code,"WARNING", "PLEASE CHECK YOUR SMOKER,JAMMED BISQUETTE");
        		isShowBISQUETTEDialog = true;
        		break;
        	case 0xe5:
        		// 0xe5:箱体探头超温错误
        		showAlertDialog(code,"WARNING", "THE OVEN IS TOO HOT,PLEASE CHECK THE OVEN");
        		break;
        	case 0x06:
        		// 0x06:a探头接近设定温度提示
        		showAlertDialog(code,"ALERT", getText(R.string.probe1_temp_alert).toString());
        		break;
        	case 0x07:
        		// 0x07:b探头接近设定温度提示
        		showAlertDialog(code,"ALERT", getText(R.string.probe2_temp_alert).toString());
        		break;
        	case 0x08:
        		// 0x08:a探头到达温度
        		showAlertDialog(code,"ALERT", "Probe 1:\nYour food is done.ENJOY!");
        		break;
        	case 0x09:
        		// 0x09:b探头到达温度
        		showAlertDialog(code,"ALERT", "Probe 2:\nYour food is done.ENJOY!");
        		break;
        	case 0x0a:
        		// 0x09:定时到
        		showAlertDialog(code,"ALERT", "Your food is done.ENJOY!");
        		break;
        	case 0x0b:
        		// 0x0b:加水
        		showAlertDialog(code,"REMINDER", "PLEASE CHECK AND REFILL THE WATER BOWL");
        		break;
        	}
        }
        lastErrorCode = code;
    }


	/**
	 * 提示温度已经超过
	 * 
	 * @param text
	 */
    public void showAlertDialog(int code, String title, String text) {
        if (isShownAlert) return;

        if(isVisibility){
        	isShownAlert = true;
    		MediaUtils.playWarning(this);
        	alertDialog = new AlertDialog
        			.Builder(this)
        			.setTitle(title)
        			.setMessage(text)
        			.setCancelable(false)
        			.setNegativeButton("OK", new DialogInterface.OnClickListener() {
        				@Override
        				public void onClick(DialogInterface dialog, int which) {
        					isShownAlert = false;
        					MediaUtils.stopAll();
        					if(isShowBISQUETTEDialog){
        						isShowBISQUETTEDialog = false;
        						DeviceModule.setSmokerStatus(true);
        					}
        				}
        			})
        			.create();
        	alertDialog.show();
        } else {
        	if(canNotify){
        		ShowNotification.getInstence(getApplicationContext()).showNotification(code,title,text);
        	}
        }
    }

}
