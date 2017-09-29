package ganguo.oven.bluetooth;

import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.db.Point;
import ganguo.oven.db.PointDao;
import ganguo.oven.utils.FileUtils;
import ganguo.oven.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import de.greenrobot.event.EventBus;

/**
 * Created by Tony on 1/18/15.
 */
public class BleService extends Service {
	public static boolean isNewDevice = true;
	public static final String TAG = "testLog";
	private static final long WORK_DELAYER = 2000;
	private int currentapiVersion = android.os.Build.VERSION.SDK_INT;

	private Handler mHandler = new Handler();
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothGatt mBluetoothGatt;
	public static List<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
	private boolean mScanning = false;
	private boolean mConnected = false;
	private boolean isConnect = false;
	private boolean stopReceiveData = false;

	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private BluetoothGattCharacteristic mWriteCharacteristic;

	private EventBus mEventBus = EventBus.getDefault();
	private PointDao pointDao;
	private FileUtils fileUtils;

	public void onEvent(final BleEvent event) {
		// if(!event.getCommand().equals("SCAN_FOUND")){
		// Log.d(TAG, "BleCommand: " + event.getCommand());
		// }

		switch (event.getCommand()) {
		case SCAN_START:
			scanLeDevice(true);
			Log.i(TAG, "scan_start...");
			break;
		case SCAN_STOP:
			scanLeDevice(false);
			Log.i(TAG, "scan_stop...");
			break;
		case CONNECT:
			Log.e("testLog", "case CONNECT");
			closeBluetoothGatt();
			mBluetoothDevice = (BluetoothDevice) event.getTarget();
			if (mBluetoothDevice != null) {
				connectToDevice(mBluetoothDevice);
			}
			break;
		case DISCONNECTED:
			closeBluetoothGatt();
			isConnect = false;
			break;
		case SETTING_COMMAND:
			settingDataToDevice((SettingData) event.getTarget());
			break;
		case START_SCAN_THREAD://开始后台搜索线程
			startScanThread();
			break;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mEventBus.register(this);
		// Initializes Bluetooth adapter.
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		// bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
		Log.d(TAG, "BLE onCreate...");
		pointDao = PointDao.getInstance();
		fileUtils = FileUtils.getInstance();
		fileUtils.initDirectory(Constants.LOG_FILE_NAME);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		scanLeDevice(false);
		closeBluetoothGatt();
		mEventBus.unregister(this);
		Log.d(TAG, "BLE onDestroy...");
	}

	public void connectToDevice(BluetoothDevice mDevice) {
		Log.e("testLog", "connectToDevice");
		
//		if(mDevice.getName() != null && mDevice.getName().startsWith(Constants.DEVICE_NAME)){//旧设备
//			isNewDevice = false;
//		} else {
//			isNewDevice = true;
//		}
		
		if (isNewDevice) {
			mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
			return ;
		}
		
		if (currentapiVersion < 21) {
			mBluetoothGatt = mDevice.connectGatt(this, true, mGattCallback);
		} else {
			mDevice = connect(mDevice);
		}
		if (getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED) {
			Log.d(TAG, "already connected to device");
			List<BluetoothGattService> list = mBluetoothGatt.getServices();
			if ((list == null) || (list.size() == 0)) {
				Log.d(TAG, "start discovering services");
				mBluetoothGatt.discoverServices();
			} else {
				mGattCallback.onServicesDiscovered(mBluetoothGatt,
						BluetoothGatt.GATT_SUCCESS);
			}
		} else {
			boolean init = mBluetoothGatt.connect();
			Log.i(TAG, "Try to connec to device, successfully? " + init);
			if (!init) {
				Log.i(TAG, "Disconnected from GATT server.");
				closeBluetoothGatt();
				mEventBus.post(new BleEvent(BleCommand.DISCONNECTED,
						mBluetoothGatt));
				mConnected = false;
				isConnect = false;
			}
		}
	}

	private BluetoothDevice connect(BluetoothDevice device) {
		Method connectGattMethod = null;
		try {
			connectGattMethod = device.getClass().getMethod("connectGatt",
					Context.class, boolean.class, BluetoothGattCallback.class,
					int.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		try {
			mBluetoothGatt = (BluetoothGatt) connectGattMethod.invoke(device,
					BleService.this, false, mGattCallback, 2);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return device;
	}

	public int getConnectionState(BluetoothDevice device) {
		BluetoothManager mgr = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		return mgr.getConnectionState(device, BluetoothProfile.GATT);
	}

	private boolean initCharacteristic() {
		try {
			mNotifyCharacteristic = BleUtils
					.getNotifyCharacteristic(mBluetoothGatt);
			mWriteCharacteristic = BleUtils
					.getWriteCharacteristic(mBluetoothGatt);
			return true;
		} catch (Exception e) {
			closeBluetoothGatt();
			mEventBus.post(new BleEvent(BleCommand.CONNECT_ERROR,
					"Not a smoker device"));
			mConnected = false;
			isConnect = false;
		}
		return false;
	}

	/**
	 * 开启后台搜索线程
	 */
	private void startScanThread(){//在这里可以写一个线程，搜索一下，暂停一下
		scanLeDevice(true);
	}
	
	Runnable commandDelayer = new Runnable() {
		@Override
		public void run() {
			stopReceiveData = false;
		}
	};

	public void settingDataToDevice(final SettingData setting) {
		if (mWriteCharacteristic == null || mBluetoothGatt == null)
			return;

		boolean isNotCloseOven = setting.getCommand() != DeviceCommand.OVEN_SWITCH && setting.getCommandData() != 0;//不是关闭烤箱
		boolean isNotCloseSmoke = setting.getCommand() != DeviceCommand.SMOKER_SWITCH && setting.getCommandData() != 0;//不是关闭烟熏
		
		if(setting.getCommand() != DeviceCommand.CONNECTED){//因为发送配对成功命令后，需要得到探针报警温度命令，所以这时不要延时接收命令
			stopReceiveData = true;
		} 

		mWriteCharacteristic.setValue(setting.toBytes());
		boolean isWrited = mBluetoothGatt
				.writeCharacteristic(mWriteCharacteristic);
		Log.d(TAG, "settingDataToDevice: " + isWrited + " " + setting);

		SystemClock.sleep(100);

		if(setting.getCommand() != DeviceCommand.CONNECTED){
			mHandler.removeCallbacks(commandDelayer);
			if(isNotCloseOven && isNotCloseSmoke){
				mHandler.postDelayed(commandDelayer, 1500);//将isCommandDelayers负值为false
			} else {
				mHandler.postDelayed(commandDelayer, 300);
			}
		}

		// Runnable runnable = new Runnable() {
		// @Override
		// public void run() {
		//
		// }
		// };
		// TaskUtil.singleExecutor(runnable);
	}

	public void closeBluetoothGatt() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
			}
			mBluetoothGatt = null;
		}
		Log.i(TAG, "closeBluetoothGatt...");
	}

	/**
	 * 扫描蓝牙 如果没有打开，则打开后连接
	 * 
	 * @param enable
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// BLE scanning
			if (mScanning)
				return;
			if (mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.startLeScan(mLeScanCallback);
				// bluetoothLeScanner.startScan(scanCallback);
			} else {
				mBluetoothAdapter.enable();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mBluetoothAdapter.startLeScan(mLeScanCallback);
						// bluetoothLeScanner.startScan(scanCallback);
					}
				}, 3000);
			}
			mScanning = true;
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			// bluetoothLeScanner.stopScan(scanCallback);
		}
	}

	BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			if (device == null)
				return;
			if (!mBluetoothDevices.contains(device)) {
				mBluetoothDevices.add(device);
			}
			mEventBus.post(new BleEvent(BleCommand.SCAN_FOUND, device));
		}
	};

	// Various callback methods defined by the BLE API.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt,
				int status, int newState) {
			Log.i(TAG, "连接回调 status=" + status
					+ " newState=" + newState);
//			if(status ==BluetoothGatt.GATT_SUCCESS ){
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					// connected
					mEventBus.post(new BleEvent(BleCommand.CONNECTED,
							mBluetoothGatt));
					mConnected = true;
					Log.i(TAG,
							"发现服务: "
									+ gatt.discoverServices());
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					Log.i(TAG, "Disconnected from GATT server.");
					closeBluetoothGatt();
					mEventBus.post(new BleEvent(BleCommand.DISCONNECTED,
							mBluetoothGatt));
					mConnected = false;
					isConnect = false;
				}
//			} else {
//				boolean init = mBluetoothGatt.connect();
//			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.i(TAG, "onServicesDiscovered status=" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				isConnect = true;
				startThreadToSaveData();
				// 获取服务失败，可能不是oven设备0
				if (!initCharacteristic())
					return;
				Log.i(TAG, "initCharacteristic suc");
				BleUtils.enableNotification(gatt, mNotifyCharacteristic);
				Log.i(TAG, "enableNotification suc");
				mEventBus.post(new BleEvent(BleCommand.SERVICES_DISCOVERED,
						mBluetoothGatt));

				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mEventBus.post(new BleEvent(BleCommand.NOTIFY,
								mBluetoothGatt));
					}
				}, WORK_DELAYER);
				Log.i(TAG, "postDelayed suc");
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			ByteUtils.printArrayList("sendData", characteristic.getValue());
		};

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			if (!isNewDevice && characteristic.getProperties() != BluetoothGattCharacteristic.PROPERTY_NOTIFY)
				return;
			ByteUtils.printArrayList("receiveData:", characteristic.getValue());
			receiveBytes(characteristic.getValue());
		}
	};

	ReceiveData mReceiveData = new ReceiveData();
	byte[] revBytes = null;

	private void receiveBytes(byte[] values) {
		// 发送命令时延时接收数据
		if (stopReceiveData)
			return;

		if (values == null)
			return;
		// 开始帧 合并 0xBB - 0x44 25 bytes 数据包
		if (ByteUtils.getIntFromByte(values[0]) == 0xBB) {
			revBytes = values;
		} else {
			// 不是帧结束，累加
			if (revBytes == null)
				return;

			int length = revBytes.length + values.length;
			byte[] temp = new byte[length];
			for (int i = 0; i < revBytes.length; i++) {
				temp[i] = revBytes[i];
			}
			for (int i = revBytes.length; i < length; i++) {
				temp[i] = values[i - revBytes.length];
			}
			revBytes = temp;
		}
		if (ByteUtils.getIntFromByte(values[values.length - 1]) == 0x44) {
			// 帧结束
			
			boolean judge = false;
			if((revBytes[10] & 0xff) >= 128){//返回设置时间则不校验
				judge = mConnected;
			} else {
				judge = mConnected && ByteUtils.checkData(revBytes);
			}
			
			if (judge) {
				mReceiveData.setData(revBytes);
				
				//可以在这里保存，烧烤设置时间，烟熏设置时间，探针1报警时间，探针2报警时间
				
				if(mReceiveData.getOvenHourTime() >= 128){
					Config.putInt(Constants.OVEN_SETTING_TIMER_HOUR, mReceiveData.getOvenHourTime() - 128);
				}
				if(mReceiveData.getOvenMinuteTime() >= 128){
					Config.putInt(Constants.OVEN_SETTING_TIMER_MINUTE, mReceiveData.getOvenMinuteTime() - 128);
				}
				if(mReceiveData.getSmokedHourTime() >= 128){
					Config.putInt(Constants.SMOKER_SETTING_TIMER_HOUR, mReceiveData.getSmokedHourTime() - 128);
				}
				if(mReceiveData.getSmokedMinuteTime() >= 128){
					Config.putInt(Constants.SMOKER_SETTING_TIMER_MINUTE, mReceiveData.getSmokedMinuteTime() - 128);
				}
				
				saveLogToFile();
				mEventBus.post(new BleEvent(BleCommand.NOTIFY_RECEIVE_DATA,
						mReceiveData));
			} else {
//				Log.w(TAG, " check: " + ByteUtils.checkData(revBytes)
//						+ " receiveData: " + mReceiveData);
			}
			revBytes = null;
		}
	}

	private void saveLogToFile() {
		String currentBeginTime = null;// 当前图表的开始时间
		String charts_list = "";
		Point p_charts_list = pointDao.findPointByX(12345);
		if (p_charts_list != null) {
			charts_list = p_charts_list.getMyDateList();
		}

		if (!charts_list.equals("")) {
			String[] list = charts_list.split("#");
			currentBeginTime = list[list.length - 1];
		} else {
			return;
		}
		if (getXByTime(currentBeginTime) != -100) {// 在十小时内，可以保存到log文件
			fileUtils.writeData(
					mReceiveData.getTemperatureUnit()
							+ ":"
							+ mReceiveData.getProbeTemperature()
							+ ":"
							+ mReceiveData.getProbeTemperatureA()
							+ ":"
							+ mReceiveData.getProbeTemperatureB()
							+ ":"
							+ mReceiveData.getOvenStatus()
							+ ":"
							+ mReceiveData.getSmokedStatus()
							+ ":"
							+ StringUtils.get16FormatByte(mReceiveData
									.getMalfunctionStatus()), currentBeginTime);
		}
	}

	private void startThreadToSaveData() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);// 连接成功后等待1秒再启动线程，因为连接成功后不一定马上有数据返回，这时mReceiveData为null
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (isConnect) {
					saveTempToDB();
				}
			};
		}.start();
	}

	private void saveTempToDB() {// 接收到数据后，将温度存进数据库
		int probe1Temp = 0;
		int probe2Temp = 0;
		int btiTemp = 0;

		int lastX = -1;
		int probe1TempAddNum = 0, probe2TempAddNum = 0, btiTempAddNum = 0;

		String currentBeginTime = null;// 当前图表的开始时间

		String charts_list = "";
		Point p_charts_list = pointDao.findPointByX(12345);
		if (p_charts_list != null) {
			charts_list = p_charts_list.getMyDateList();
		}

		if (!charts_list.equals("")) {
			String[] list = charts_list.split("#");
			currentBeginTime = list[list.length - 1];
		} else {
			currentBeginTime = new SimpleDateFormat("yyyyMMddHHmm")
					.format(new Date());
			Point p = new Point(12345, 0, 0, 0, "12345", currentBeginTime);
			p.insert();
		}

		while (isConnect) {

			if (mReceiveData.getTemperatureUnit() == 0x00) {
				if (!Constants.isUnitF) {
					Constants.isUnitF = true;
				}
			} else {
				if (Constants.isUnitF) {
					Constants.isUnitF = false;
				}
			}

			int probe1 = mReceiveData.getProbeTemperatureA();// 探针1温度
			int probe2 = mReceiveData.getProbeTemperatureB();// 探针2温度
			int temp = mReceiveData.getProbeTemperature();// 烤箱温度

			if (probe1 >= 50 && probe1 <= 320) {
				if (Constants.isUnitF) {// 将华氏度转成摄氏度
					probe1 = (int) ((probe1 - 32) / 1.8);
				}
				probe1Temp += probe1;
				probe1TempAddNum++;
			}
			if (probe2 >= 50 && probe2 <= 320) {
				if (Constants.isUnitF) {// 将华氏度转成摄氏度
					probe2 = (int) ((probe2 - 32) / 1.8);
				}
				probe2Temp += probe2;
				probe2TempAddNum++;
			}
			if (temp >= 50 && temp <= 320) {
				if (Constants.isUnitF) {// 将华氏度转成摄氏度
					temp = (int) ((temp - 32) / 1.8);
				}
				btiTemp += temp;
				btiTempAddNum++;
			}

			int x = getXByTime(currentBeginTime);
			if (x == -100) {// 重画一张新图表
				p_charts_list = pointDao.findPointByX(12345);
				charts_list = p_charts_list.getMyDateList();
				currentBeginTime = new SimpleDateFormat("yyyyMMddHHmm")
						.format(new Date());
				String newDateList = "";
				if (charts_list.length() == 51) {// 存储的图表超过4个
					// 删掉第一个
					List<Point> pointList = pointDao
							.getPointsByDate(charts_list.substring(0, 12));
					for (Point point : pointList) {
						point.delete();
					}
					fileUtils.deleteFile(charts_list.substring(0, 12));// 删除第一个的Log文件
					// 添加最新一个
					newDateList = charts_list.substring(13) + "#"
							+ currentBeginTime;
				} else {
					newDateList = charts_list + "#" + currentBeginTime;
				}

				p_charts_list.setMyDateList(newDateList);// 更新数据库列表
				p_charts_list.update();

				// 添加第一个数据
				probe1Temp = probe1TempAddNum == 0 ? 0 : probe1Temp
						/ probe1TempAddNum;
				probe2Temp = probe2TempAddNum == 0 ? 0 : probe2Temp
						/ probe2TempAddNum;
				btiTemp = btiTempAddNum == 0 ? 0 : btiTemp / btiTempAddNum;
				Point point = new Point(0, probe1Temp, probe2Temp, btiTemp,
						currentBeginTime, "");
				point.insert();

				lastX = 0;
				probe1Temp = 0;
				probe2Temp = 0;
				btiTemp = 0;
				probe1TempAddNum = probe2TempAddNum = btiTempAddNum = 0;

			} else if (x != lastX) {// 之前数据库没添加过，现在添加
				lastX = x;// 记录已添加的x

				Point p = pointDao.findPointByDateAndX(currentBeginTime, x);

				if (p == null) {// 数据库未插入此数据，现在插入
					probe1Temp = probe1TempAddNum == 0 ? 0 : probe1Temp
							/ probe1TempAddNum;
					probe2Temp = probe2TempAddNum == 0 ? 0 : probe2Temp
							/ probe2TempAddNum;
					btiTemp = btiTempAddNum == 0 ? 0 : btiTemp / btiTempAddNum;
					Point point = new Point(x, probe1Temp, probe2Temp, btiTemp,
							currentBeginTime, "");
					point.insert();
				}

				probe1Temp = 0;
				probe2Temp = 0;
				btiTemp = 0;
				probe1TempAddNum = probe2TempAddNum = btiTempAddNum = 0;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private int getXByTime(String previousTime) {
		String currentTime = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());

		int min_previous = Integer.parseInt(previousTime.substring(10, 12));
		int hour_previous = Integer.parseInt(previousTime.substring(8, 10));
		int day_previous = Integer.parseInt(previousTime.substring(6, 8));
		int mon_previous = Integer.parseInt(previousTime.substring(4, 6));
		int year_previous = Integer.parseInt(previousTime.substring(0, 4));

		int min_current = Integer.parseInt(currentTime.substring(10, 12));
		int hour_current = Integer.parseInt(currentTime.substring(8, 10));
		int day_current = Integer.parseInt(currentTime.substring(6, 8));
		int mon_current = Integer.parseInt(currentTime.substring(4, 6));
		int year_current = Integer.parseInt(currentTime.substring(0, 4));

		if (year_current == year_previous && mon_current == mon_previous
				&& day_current == day_previous) {// 在同一天，可直接比较分钟
			int minNum = min_current + hour_current * 60 - min_previous
					- hour_previous * 60;
			if (minNum <= Constants.MAX_MIN) {// 在600分钟内
				return minNum;
			} else {
				return -100;// 超过600分钟，重画一张新的图表
			}
		} else {// 不在同一天，判断是否是第二天
			boolean isNextDayStatus1 = year_current == year_previous
					&& mon_current == mon_previous
					&& day_current == day_previous + 1;// 同一个月的第二天
			boolean isNextDayStatus2 = false;// 上个月的最后一天和这个月的第一天
			boolean isNextDayStatus3 = year_current == year_previous + 1
					&& mon_current == 1 && mon_previous == 12
					&& day_current == 1 && day_previous == 31;// 上一年的最后一天和这一年的第一天
			if (year_current == year_previous) {// 同一年
				if ((mon_previous == 1 || mon_previous == 3
						|| mon_previous == 5 || mon_previous == 7
						|| mon_previous == 8 || mon_previous == 10)
						&& mon_current == mon_previous + 1
						&& day_current * 31 == day_previous) {
					// 1,3,5,7,8,10月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if ((mon_previous == 4 || mon_previous == 6
						|| mon_previous == 9 || mon_previous == 11)
						&& mon_current == mon_previous + 1
						&& day_current * 30 == day_previous) {
					// 4,6,9,7,11月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if (mon_previous == 2 && mon_current == mon_previous + 1
						&& day_current == 1) {
					if (year_current % 4 == 0) {// 闰年，2月有29号
						if (day_previous == 29) {// 2月的最后一天，和下一个月的第一天
							isNextDayStatus2 = true;
						}
					} else {
						if (day_previous == 28) {// 2月的最后一天，和下一个月的第一天
							isNextDayStatus2 = true;
						}
					}
				}
			}
			if (isNextDayStatus1 || isNextDayStatus2 || isNextDayStatus3) {// 是第二天，可以比较时间
				int minNum = 24 * 60 + min_current + hour_current * 60
						- min_previous - hour_previous * 60;
				if (minNum <= Constants.MAX_MIN) {// 在600分钟内
					return minNum;
				} else {
					return -100;// 超过600分钟，重画一张新的图表
				}
			} else {// 不是第二天，重画一张新的图表
				return -100;
			}
		}
	}

}
