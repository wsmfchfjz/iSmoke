package ganguo.oven.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.activity.MainActivity;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.BleService;
import ganguo.oven.bluetooth.DeviceModule;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.utils.AndroidUtils;
import ganguo.oven.utils.MediaUtils;
import ganguo.oven.utils.StringUtils;
import ganguo.oven.utils.UIUtils;
import ganguo.oven.utils.ViewUtils;

/**
 * Created by Wilson on 14-7-9.
 */
@TargetApi(18)
public class DeviceListFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView listView;
    private Dialog pinDialog;
    private EventBus mEventBus = EventBus.getDefault();
    private ReceiveData mReceiveData = new ReceiveData();
    private BluetoothDevice curDevice;
    private Handler mHandler = new Handler();
    private Runnable scanTimeout = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;

//            startActivity(new Intent(getActivity(), SettingActivity.class));
            Constants.isTestMode = true;
            Intent main = new Intent(getActivity(), MainActivity.class);
            startActivity(main);
            getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            getActivity().finish();
        }
    };

    private boolean disconnectByMyself;
    
    @Override
    public int getLayoutId() {
        return R.layout.fragment_device_list;
    }

    @Override
    public String getMyTAG() {
        return "DeviceListFragment";
    }

    @Override
    public void beforeInitView() {
    	/*String currentBeginTime = new SimpleDateFormat("yyyyMMddHHmm")//添加测试数据
		.format(new Date());
    	Point p = new Point(12345, 0, 0, 0, "12345", currentBeginTime);
    	p.insert();
    	
    	for (int i = 0; i < 20; i++) {
    		Point point = new Point(i, 80+i, 90+i, 100+i,
    				currentBeginTime, "");
    		point.insert();
		}*/
    }

    @Override
    public void initView() {
        listView = (ListView) getView().findViewById(R.id.listView);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listView.setAdapter(mLeDeviceListAdapter);
        listView.setOnItemClickListener(this);
        // 如果搜索超过60s，跳转到设置界面，现在改为跳到主界面
        mHandler.postDelayed(scanTimeout, Constants.SCAN_SECOND);

//        listView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent main = new Intent(getActivity(), MainActivity.class);
//                startActivity(main);
//                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                getActivity().finish();
//            }
//        }, 5000);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        UIUtils.showLoading(getActivity(), "Connecting to Device...");
        Log.i("528", "click list");
        //点击之后就停止搜索
//        mEventBus.post(new BleEvent(BleCommand.SCAN_STOP));//停止搜索会降低连接成功率，先注释。
    	curDevice = (BluetoothDevice) parent.getItemAtPosition(position);
        mEventBus.post(new BleEvent(BleCommand.CONNECT, curDevice));
		disconnectByMyself = false;
        isFirstReceiveData = true;
//        mHandler.removeCallbacks(scanTimeout);//点击了则取消体验模式的进入
        // connecting timeout
        parent.postDelayed(new Runnable() {
            @Override
            public void run() {
                UIUtils.hideLoading();
                if (pinDialog == null)
                    AndroidUtils.toast(getActivity(), "Timed out");
            }
        }, 60000);
    }

    /**
     * 关闭键盘
     */
    public void closeInput(Window window) {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(window.getCurrentFocus()
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmPIN:
                closeInput(pinDialog.getWindow());
                pinDialog.dismiss();
                if (mReceiveData == null || mReceiveData.getPassword() == 0) {
                    AndroidUtils.toast(getActivity(), "connect device error!");
                    disconnectByMyself = true;
                    mEventBus.post(new BleEvent(BleCommand.DISCONNECTED));
                    return;
                }
                EditText editText = (EditText) pinDialog.findViewById(R.id.PINText);
                String pwdStr = editText.getText().toString();
                int pwd = 0;
                try {
                    pwd = Integer.parseInt(pwdStr);
                } catch (Exception e) {
                    AndroidUtils.toast(getActivity(), "Password error!");
                    disconnectByMyself = true;
                    mEventBus.post(new BleEvent(BleCommand.DISCONNECTED));
                    editText.setText("");
                    return;
                }
                if (mReceiveData.getPassword() > 0 && pwd > 0 && mReceiveData.getPassword() == pwd) {
                    Config.putInt(Constants.SETTING_PASSWORD, mReceiveData.getPassword());
                    toMainActivity(true);
                } else {
                    AndroidUtils.toast(getActivity(), "Password error!");
                    disconnectByMyself = true;
                    mEventBus.post(new BleEvent(BleCommand.DISCONNECTED));
                }
                editText.setText("");
                return;
        }
    }

    private void toMainActivity(boolean showDialog) {
        Constants.isTestMode = false;
        // 发送已经连接成功
        DeviceModule.sendConnected();
        
        if(showDialog){
        	String text = "iSmoke TM app is in display mode now.The smoking cycle must be initiated from the touchscreen on the smoker first,in order to control the smoker.";
        	AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle("Attention").setMessage(text).setCancelable(false).setNegativeButton("OK", new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int which) {
        			Intent main = new Intent(getActivity(), MainActivity.class);
        			startActivity(main);
        			getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        			getActivity().finish();
        		}
        	}).create();
        	alertDialog.show();
        } else {
        	Intent main = new Intent(getActivity(), MainActivity.class);
			startActivity(main);
			getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
			getActivity().finish();
        }
    }
    

    private Dialog makePinDialog() {
        if (pinDialog != null) {
            return pinDialog;
        }
        int width = (int) (AndroidUtils.getScreenWidth(getActivity()) * 0.9);
        int height = (int) getActivity().getResources().getDimension(R.dimen.pin_dialog_height);

        pinDialog = ViewUtils.createCustomDialog(getActivity(), R.layout.dialog_pin, width, height, R.style.dialog_loading);
        pinDialog.setCancelable(false);
        Button confirmPIN = (Button) pinDialog.findViewById(R.id.confirmPIN);
        confirmPIN.setOnClickListener(this);

        return pinDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        mEventBus.register(this);
        mEventBus.post(new BleEvent(BleCommand.SCAN_START));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BluetoothDevice> bluetoothDevices = BleService.mBluetoothDevices;
                if (bluetoothDevices != null && !bluetoothDevices.isEmpty()) {
                	int num = bluetoothDevices.size();
                	for (int i = 0; i < num; i++) {
                		if (StringUtils.equals(bluetoothDevices.get(i).getAddress(), Config.getString(Constants.DEVICE_MAC))) {
                			curDevice = bluetoothDevices.get(i);
                            mEventBus.post(new BleEvent(BleCommand.CONNECT, curDevice));
                    		disconnectByMyself = false;
                            isFirstReceiveData = true;
                        }
					}
//                    for (BluetoothDevice device : bluetoothDevices) {
//                        if (StringUtils.equals(device.getAddress(), Config.getString(Constants.DEVICE_MAC))) {
//                            mEventBus.post(new BleEvent(BleCommand.CONNECT, device));
//                        }
//                    }
                }
            }
        }, 1000);
        Log.d("DEVICE_LIST", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();

        if(!Constants.isTestMode){//不是测试模式
        	mEventBus.post(new BleEvent(BleCommand.SCAN_STOP));
        }
        mEventBus.unregister(this);

        UIUtils.hideLoading();
        if (pinDialog != null)
            pinDialog.dismiss();

        Log.d("DEVICE_LIST", "onStop");
    }

    /**
     * EventBus
     */
    public void onEventMainThread(BleEvent event) {
        switch (event.getCommand()) {
            // 发现蓝牙设备
            case SCAN_FOUND:
                mLeDeviceListAdapter.addDevices((BluetoothDevice) event.getTarget());
                break;
            case CONNECTED:
                BluetoothGatt gatt = (BluetoothGatt) event.getTarget();
                if (gatt != null && gatt.getDevice() != null) {
//                    AndroidUtils.toast(getActivity(), "Device Address: " + gatt.getDevice().getAddress());
                    // 记住 mac 地址，用于自动连接时匹配连接
                    Config.putString(Constants.DEVICE_MAC, gatt.getDevice().getAddress());
                }
                break;
            case SERVICES_DISCOVERED:
                // 请求密码，设备显示密码框
//                DeviceModue.requestPassword();
                break;
            case NOTIFY:
                break;
            case NOTIFY_RECEIVE_DATA:
                mReceiveData = (ReceiveData) event.getTarget();
                if(isFirstReceiveData){
                	isFirstReceiveData = false;
                    UIUtils.hideLoading();
                    if (mReceiveData == null) return;
                    
                    // 先判读本地密码，如果一样就直接跳转到主界面
                    Log.d("LocalPassword", "revPwd" + mReceiveData.getPassword() + " localPwd:" + Config.getInt(Constants.SETTING_PASSWORD));
//                    toMainActivity();
                    if (mReceiveData.getPassword() != 0 && mReceiveData.getPassword() == Config.getInt(Constants.SETTING_PASSWORD)) {
                        toMainActivity(false);
                    } else {
                        makePinDialog().show();
                    }
                }
                break;
            case DISCONNECTED://断开后就进行重连
//                UIUtils.hideLoading();
            	if(!disconnectByMyself){//不是自己手动断开才需要重连
            		new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							mEventBus.post(new BleEvent(BleCommand.CONNECT, curDevice));
							disconnectByMyself = false;
						}
					}, 500);//防止不断重连的现象，给0.5秒的时间间隔
            	}
            	isFirstReceiveData = true;
                break;
            case CONNECT_ERROR:
                AndroidUtils.toast(getActivity(), event.getTarget() + "");
                UIUtils.hideLoading();
                break;
        }
    }
    
    private boolean isFirstReceiveData = true;

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getActivity().getLayoutInflater();
        }

        public void addDevices(BluetoothDevice bleDevice) {
            if (bleDevice != null && bleDevice.getName() != null && bleDevice.getName().startsWith(Constants.DEVICE_NAME) && !mLeDevices.contains(bleDevice) ) {
                mLeDevices.add(bleDevice);
                mLeDeviceListAdapter.notifyDataSetChanged();
                mHandler.removeCallbacks(scanTimeout);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceItemSample);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            String name = getDevice(i).getName();
            if (StringUtils.isEmpty(name)) {
                name = getDevice(i).getAddress();
            }
            viewHolder.deviceName.setText(name);
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
    }

}
