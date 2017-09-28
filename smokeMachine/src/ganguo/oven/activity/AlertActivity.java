package ganguo.oven.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
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
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.MediaUtils;
import ganguo.oven.utils.ShowNotification;
import ganguo.oven.utils.UIUtils;

public class AlertActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

	private ImageButton fromAlertToHomeBtn;
	private SeekBar seekA;
	private SeekBar seekB;
	private TextView temA, temLabelA;
	private TextView temB, temLabelB;
	private int start = 120;
	private EventBus mEventBus = EventBus.getDefault();
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

	/**
	 * EventBus
	 */
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
			} else {
				Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
			}
			setTempIfUnitChange();
			setTemp();
			break;
		}
	}

	private void setTemp(){//设置滑动条温度
		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();
		Log.i("testLog", alertTempA + " - " + alertTempB);
		if(alertTempA >= 32768 && alertTempA <= (32768 + 320)){
			seekA.setProgress(alertTempA - 32768 - start);
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, alertTempA - 32768);
		} else if(alertTempA == 65535){
			seekA.setProgress(0);
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, -1);
			temA.setText("-1");
		} else {
			if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A) == -1){
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 320);
			}
//			temA.setText("320");
		}
		if(alertTempB >= 32768 && alertTempB <= (32768 + 320)){
			seekB.setProgress(alertTempB - 32768 - start);
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, alertTempB - 32768);
		}else if(alertTempA == 65535){
			seekB.setProgress(0);
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, -1);
			temB.setText("-1");
		} else {
			if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B) == -1){
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 320);
			}
//			temB.setText("320");
		}
	}
	
	private void setTempIfUnitChange() {// 如果温度单位改变，则界面上的温度也随之改变
		if (Config.getBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, false) != Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
			if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {// 63145
																				// 71160
																				// 74165
																				// 77170
				// C
				int probeA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
				int probeB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
				int ca = 0, cb = 0;
				Log.i("setAlertTemperature", "fahrenheitToCelsius probeA:" + probeA + " probeB:" + probeB);
				switch (probeA) {
				case 145:
					ca = 63;
					break;
				case 160:
					ca = 71;
					break;
				case 165:
					ca = 74;
					break;
				case 170:
					ca = 77;
					break;
				default:
					ca = UIUtils.fahrenheitToCelsius(probeA);
					break;
				}
				switch (probeB) {
				case 145:
					cb = 63;
					break;
				case 160:
					cb = 71;
					break;
				case 165:
					cb = 74;
					break;
				case 170:
					cb = 77;
					break;
				default:
					Log.i("setAlertTemperature", "1fahrenheitToCelsius probeA:" + probeA + " probeB:" + probeB);
					cb = UIUtils.fahrenheitToCelsius(probeB);
					break;
				}
				if (ca >= 50) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, ca);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 50);
				}
				if (cb >= 50) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, cb);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 50);
				}

				Log.i("setAlertTemperature", "fahrenheitToCelsius ca:" + ca + " cb:" + cb);
			} else {
				// F
				int probeA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
				int probeB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
				int ca = 0, cb = 0;
				switch (probeA) {
				case 63:
					ca = 145;
					break;
				case 71:
					ca = 160;
					break;
				case 74:
					ca = 165;
					break;
				case 77:
					ca = 170;
					break;
				default:
					ca = UIUtils.celsiusToFahrenheit(probeA);
					break;
				}
				switch (probeB) {
				case 63:
					cb = 145;
					break;
				case 71:
					cb = 160;
					break;
				case 74:
					cb = 165;
					break;
				case 77:
					cb = 170;
					break;
				default:
					cb = UIUtils.celsiusToFahrenheit(probeB);
					break;
				}
				if (ca >= 120) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, ca);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 120);
				}
				if (cb >= 120) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, cb);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 120);
				}
				Log.i("setAlertTemperature", "celsiusToFahrenheit ca:" + ca + " cb:" + cb);
			}
			Config.putBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false));
			setTemUnit();
		}
	}

	@Override
	public void beforeInitView() {
		setContentView(R.layout.fragment_alert);
	}

	@Override
	public void initView() {
		fromAlertToHomeBtn = (ImageButton) findViewById(R.id.fromAlertToHomeBtn);
		temLabelA = (TextView) findViewById(R.id.temLabelA);
		temLabelB = (TextView) findViewById(R.id.temLabelB);
		seekA = (SeekBar) findViewById(R.id.seekA);
		seekB = (SeekBar) findViewById(R.id.seekB);
		temA = (TextView) findViewById(R.id.temA);
		temB = (TextView) findViewById(R.id.temB);
	}

	@Override
	public void initListener() {
		fromAlertToHomeBtn.setOnClickListener(singleClickListener);
		seekA.setOnSeekBarChangeListener(this);
		seekB.setOnSeekBarChangeListener(this);
	}

	private void setTemUnit() {
		if (Config.getBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, false)) {
			temLabelA.setText("℃");
			temLabelB.setText("℃");
			start = 50;
			seekA.setMax(110);
			seekB.setMax(110);
		} else {
			temLabelA.setText("℉");
			temLabelB.setText("℉");
			start = 120;
			seekA.setMax(200);
			seekB.setMax(200);
		}

		final int a = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
		final int b = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);

		seekA.post(new Runnable() {
			@Override
			public void run() {
				Log.e("testLog", "a"+a+ "-b"+b);
				if(a == -1){
					seekA.setProgress(0);
					temA.setText("-1");
				} else {
					seekA.setProgress(a - start);
					temA.setText(String.valueOf(a));
				}
				if(b == -1){
					seekB.setProgress(0);
					temB.setText("-1");
				} else {
					seekB.setProgress(b - start);
					temB.setText(String.valueOf(b));
				}
			}
		});
		Log.d("SETTING_ALERT_TEMPERATURE", "A:" + a + " B:" + b + " start:" + start);
	}

	@Override
	public void initData() {
		mEventBus.register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mEventBus.unregister(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		setTemUnit();
	}

	private OnSingleClickListener singleClickListener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View v) {
			switch (v.getId()) {
			case R.id.fromAlertToHomeBtn:
				// mainActivity.backToHomeSlide();
				finish();
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int temp = progress + start;

		Log.d("SETTING_ALERT_TEMPERATURE", "onProgressChanged " + temp);
		switch (seekBar.getId()) {
		case R.id.seekA:
			temA.setText(String.valueOf(temp));
			return;
		case R.id.seekB:
			temB.setText(String.valueOf(temp));
			return;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int temp = seekBar.getProgress() + start;

		Log.d("SETTING_ALERT_TEMPERATURE", "onStopTrackingTouch " + temp);
		switch (seekBar.getId()) {
		case R.id.seekA:
			if(Constants.isTestMode || mReceiveData.getOvenStatus() == 0x01){//烤箱已打开
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, temp);
				DeviceModule.setProbeTemperatureA(temp);
			} else {
				if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A) != -1){
					seekA.setProgress(seekA.getMax());
					temA.setText("320");
				}
			}
			break;
		case R.id.seekB:
			if(Constants.isTestMode || mReceiveData.getOvenStatus() == 0x01){//烤箱已打开
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, temp);
				DeviceModule.setProbeTemperatureB(temp);
			} else {
				if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B) != -1){
					seekB.setProgress(seekA.getMax());
					temB.setText("320");
				}
			}
			break;
		}
		Config.putBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false));
		mEventBus.post(new BleEvent(BleCommand.PROBE_TEMPERATURE_CHANGE));
	}


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
