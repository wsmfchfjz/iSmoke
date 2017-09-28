package ganguo.oven.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.ByteUtils;
import ganguo.oven.bluetooth.DeviceModule;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.fragment.BaseFragment;
import ganguo.oven.fragment.MainFragment;
import ganguo.oven.utils.AndroidUtils;
import ganguo.oven.utils.MediaUtils;
import ganguo.oven.utils.ShowNotification;


public class MainActivity extends BaseActivity {
    public static MainActivity instance = null;
//    private BaseFragment[] fragments = {new MainFragment(), new AlertFragment(), new SettingsFragment()};
    private MainFragment fragments = new MainFragment();
    private ReceiveData mReceiveData = AppContext.getInstance().getReceiveData();
    private EventBus mEventBus = EventBus.getDefault();
    private boolean isAlertWood = true;

    private boolean cancleAlertDialogByMyself = false;
    private boolean isShowExitDialog4NoOKDialog = false;
    private AlertDialog alertExitDialog;
    private boolean isShowBISQUETTEDialog;//是否显示马达错误提示框。如果是的话，点击OK后，发送烟熏开启命令。
    private int lastErrorCode;//保存上一个错误码。
    AlertDialog alertDialog = null,alertDialog4OpenDoor = null;
    boolean isShownAlert = false;
    private boolean isVisibility ;
    private boolean canNotify;
    private boolean isShowCloseDoorWarn;

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_home);
    }

    @Override
    public void initView() {
        instance = this;
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
    	replaceDeviceFragment(fragments, null);
    }
    
    private void replaceDeviceFragment(BaseFragment fragment, Boolean slideToRightAnim) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (slideToRightAnim != null && slideToRightAnim.booleanValue()) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        if (slideToRightAnim != null && !slideToRightAnim.booleanValue()) {
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        }
        BaseFragment frag = fragments;
        if (fragment != frag && fragment.isAdded()) {
            transaction.hide(frag);
        }
        
        // 如果已经添加过就直接显示
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.mainFrame, fragment, fragment.getMyTAG());
        }
        transaction.addToBackStack(fragment.getMyTAG());
        transaction.commitAllowingStateLoss();
    }

    public void backToHomeSlide() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
    }

    public void backToHome() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
    }

    public void changeToChart() {
    	canNotify = false;
    	Intent intent = new Intent(MainActivity.this,ChartActivity.class);
    	startActivity(intent);
    }

    public void changeToMain() {
    	replaceDeviceFragment(fragments, null);
    }
    
    public void changeToAlert() {
    	canNotify = false;
        Intent intent = new Intent(MainActivity.this,AlertActivity.class);
    	startActivity(intent);
    }

    public void changeToSettings() {
//        replaceDeviceFragment(fragments[2], true);
    	canNotify = false;
        Intent intent = new Intent(MainActivity.this,SettingUnitActiviy.class);
    	startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        // 大于1，不仅仅是主页面
        if (fm.getBackStackEntryCount() > 1) {
            backToHome();
            return;
        }
        showExitDialog();
    }

    private void showExitDialog() {
        AlertDialog alertDialog = new AlertDialog
                .Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeviceModule.setOvenStatus(false);
                        DeviceModule.setSmokerStatus(false);

                        AppContext.getInstance().exit();
                    }
                })
                .create();
        alertDialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventBus.register(this);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	fragments.reFreshProbeView();
    	isVisibility = true;
    	canNotify = true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mEventBus.unregister(this);
    }

    /**
     * EventBus
     */
    public void onEventMainThread(BleEvent event) {
        switch (event.getCommand()) {
            case NOTIFY_RECEIVE_DATA:
                // 数据接收
                mReceiveData = (ReceiveData) event.getTarget();
                if (mReceiveData == null) return;
                setAlertTip();
                break;
            case DISCONNECTED:
                // 连接断开
            	isShownAlert = false;
                MediaUtils.stopAll();
                
                startActivity(new Intent(this, DeviceActivity.class));
                finish();
//                UIUtils.showPromptDialong(this);

                isShownAlert = false;
                break;
            case PROBE_TEMPERATURE_CHANGE:
                isShownAlert = false;
                break;
        }
    }
    
    private void showExitDialog4NoOKDialog() {
    		isShowExitDialog4NoOKDialog = true;
    		alertExitDialog = new AlertDialog
    				.Builder(this)
    				.setTitle("Exit")
    				.setMessage("Are you sure you want to exit?")
    				.setCancelable(false)
    				.setNegativeButton("No", new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						dialog.cancel();
    			    		isShowExitDialog4NoOKDialog = false;
    					}
    				})
    				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						DeviceModule.setOvenStatus(false);
    						DeviceModule.setSmokerStatus(false);
    						
    						AppContext.getInstance().exit();
    					}
    				})
    				.create();
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
    
    
    @Override
    protected void onPause() {
    	super.onPause();
    	isVisibility = false;
    }

    public void hideAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            isShownAlert = false;
            alertDialog = null;
        }
    }

}
