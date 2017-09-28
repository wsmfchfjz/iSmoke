package ganguo.oven.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;

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
import ganguo.oven.db.Point;
import ganguo.oven.db.PointDao;
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.MediaUtils;
import ganguo.oven.utils.ShowNotification;
import ganguo.oven.utils.TimerUtil;
import ganguo.oven.utils.UIUtils;
import ganguo.oven.view.NoScrollViewPager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChartActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, OnChartGestureListener {

	private int mCurrentOrientation;

	private ImageButton toSettingsBtn, toHomeBtn;
	private Button stopBtn, toAlertBtn;
	private CheckBox checkBoxA, checkBoxB, checkBoxBox;
	private TextView setTemValue, setTemValueUnit, currentTemperature, currentTemperatureA, currentTemperatureB, currentTemperatureUnit, smokerTime, ovenTime, chartLabelTem, chartLabelTime;
	private TextView setTemLabel;
	private EventBus mEventBus = EventBus.getDefault();
	private ReceiveData mReceiveData = AppContext.getInstance().getReceiveData();

	private NoScrollViewPager viewPager;
	private PointDao pointDao;
	private boolean showProbe1Line, showProbe2Line, showTempLine;
	private ArrayList<String> xVals120, xVals240, xVals360, xVals480, xVals600;
	private List<LineChart> chartList;
	private int currentChartListIndex;
	private List<String> chartDateList;// chart对应的存储日期列表

	private int MAX_MIN;// 横坐标最大分钟数
	private int currentMaxX;
	public boolean isUnitF;
	
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
			if (mReceiveData.getTemperatureUnit() == 0x00) {
				if (!isUnitF) {
					isUnitF = true;
					Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);
					for (int i = chartList.size() - 1; i >= 0; i--) {
						YAxis leftAxis = chartList.get(i).getAxisLeft();
						leftAxis.setAxisMaxValue(549f);// y轴最大值
						updateTempLineNum();
					}
				}
			} else {
				if (isUnitF) {
					isUnitF = false;
					Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);
					for (int i = chartList.size() - 1; i >= 0; i--) {
						YAxis leftAxis = chartList.get(i).getAxisLeft();
						leftAxis.setAxisMaxValue(275f);// y轴最大值
						updateTempLineNum();
					}
				}
			}
			setViewData();
			setTemp();

			break;
		case STOP_RECODE:
			// reset
			chartList.get(chartList.size() - 1).clearValues();
			chartList.get(chartList.size() - 1).invalidate();
			break;
		case DISCONNECTED:
			// 连接断开
			isShownAlert = false;
			break;
		}
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
	
	private void setViewData() {

		setTemValue.setText(mReceiveData.getSettingTemperature() + "");
		currentTemperature.setText(mReceiveData.getProbeTemperature() + "");
		Config.putString(Constants.LAST_TEMP, mReceiveData.getProbeTemperature() + "");

		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();

		// 设置温度单位 ℉——0x00； ℃——0x01；
		String tempUnit = mReceiveData.getTemperatureUnit() == 0x00 ? "℉" : "℃";

		setTemValueUnit.setText(tempUnit);
		currentTemperatureUnit.setText(tempUnit);
		Config.putString(Constants.LAST_TEMP_UNIT, tempUnit);

		chartLabelTem.setText(tempUnit);
		currentTemperatureA.setText(alertTempA > 1000 ? "---" + tempUnit : alertTempA + tempUnit);
		currentTemperatureB.setText(alertTempB > 1000 ? " / ---" + tempUnit : " / " + alertTempB + tempUnit);
		Config.putString(Constants.LAST_TEMP_A, alertTempA == 0xFFFF ? "---" + tempUnit : alertTempA + tempUnit);
		Config.putString(Constants.LAST_TEMP_B, alertTempB == 0xFFFF ? " / ---" + tempUnit : " / " + alertTempB + tempUnit);

		if (mReceiveData.getOvenStatus() == 0x01) {
			setTemValue.setVisibility(View.VISIBLE);
			setTemValueUnit.setVisibility(View.VISIBLE);
			setTemLabel.setVisibility(View.VISIBLE);
			setTemValue.setText(mReceiveData.getSettingTemperature() + "");
			setTemValueUnit.setText(tempUnit);
			Config.putString(Constants.LAST_TEMP_UNIT, tempUnit);
			setTemLabel.setText("Set temperature:");

			currentTemperature.setTextColor(Color.parseColor("#00b6ed"));
			currentTemperatureUnit.setTextColor(Color.parseColor("#00b6ed"));
			smokerTime.setTextColor(Color.parseColor("#fff2b3"));
			ovenTime.setTextColor(Color.parseColor("#fff2b3"));
		} else {
			setTemValue.setVisibility(View.GONE);
			setTemValueUnit.setVisibility(View.GONE);
			setTemLabel.setVisibility(View.GONE);
			setTemValue.setText("");
			setTemValueUnit.setText("");
			setTemLabel.setText("");

			currentTemperature.setTextColor(Color.WHITE);
			currentTemperatureUnit.setTextColor(Color.WHITE);
			smokerTime.setTextColor(Color.WHITE);
			ovenTime.setTextColor(Color.WHITE);
		}

		// Smoker 时间
		if (mReceiveData.getSmokedStatus() == 0x01) {
			if (mReceiveData.getSmokedHourTime() >= 0 && mReceiveData.getSmokedMinuteTime() >= 0) {
				String smokerTimeStr = (UIUtils.formateTime(mReceiveData.getSmokedHourTime())) + " : " + UIUtils.formateTime(mReceiveData.getSmokedMinuteTime());
				smokerTime.setText(smokerTimeStr);
				Config.putString(Constants.LAST_SMOKE_TIME, smokerTimeStr);
			}
		} else {
			smokerTime.setText("00 : 00");
			Config.putString(Constants.LAST_SMOKE_TIME, "00 : 00");
		}

		// Oven 时间
		if (mReceiveData.getOvenStatus() == 0x01) {
			if(mReceiveData.getOvenHourTime() >= 0 && mReceiveData.getOvenMinuteTime() >= 0){
				String ovenTimeStr = UIUtils.formateTime(mReceiveData.getOvenHourTime()) + " : " + UIUtils.formateTime(mReceiveData.getOvenMinuteTime());
				ovenTime.setText(ovenTimeStr);
				Config.putString(Constants.LAST_OVEN_TIME, ovenTimeStr);
			}
		} else {
			ovenTime.setText("00 : 00");
			Config.putString(Constants.LAST_OVEN_TIME, "00 : 00");
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mEventBus.register(this);
	}

//	@Override
//	public void onStop() {
//		super.onStop();
//		mEventBus.unregister(this);
//	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mEventBus.unregister(this);
	}

	@Override
	public void beforeInitView() {
		mCurrentOrientation = getResources().getConfiguration().orientation;
		if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.fragment_chart);
		} else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.fragment_chart_landscape);
		}
		isUnitF = Constants.isUnitF;// 初始化温度单位
		MAX_MIN = Constants.MAX_MIN;
		pointDao = PointDao.getInstance();
	}

	@Override
	public void initView() {
		toSettingsBtn = (ImageButton) findViewById(R.id.toSettingsBtn);
		toHomeBtn = (ImageButton) findViewById(R.id.toHomeBtn);

		setTemValue = (TextView) findViewById(R.id.setTemValue);
		setTemLabel = (TextView) findViewById(R.id.setTemLabel);
		setTemValueUnit = (TextView) findViewById(R.id.setTemValueUnit);
		currentTemperature = (TextView) findViewById(R.id.currentTemperature);
		currentTemperatureA = (TextView) findViewById(R.id.currentTemperatureA);
		currentTemperatureB = (TextView) findViewById(R.id.currentTemperatureB);
		currentTemperatureUnit = (TextView) findViewById(R.id.currentTemperatureUnit);
		smokerTime = (TextView) findViewById(R.id.smokerTime);
		ovenTime = (TextView) findViewById(R.id.ovenTime);
		chartLabelTem = (TextView) findViewById(R.id.chartLabelTem);
		chartLabelTime = (TextView) findViewById(R.id.chartLabelTime);
		stopBtn = (Button) findViewById(R.id.stopBtn);

		checkBoxA = (CheckBox) findViewById(R.id.checkBoxA);
		checkBoxB = (CheckBox) findViewById(R.id.checkBoxB);
		checkBoxBox = (CheckBox) findViewById(R.id.checkBoxBox);

		toAlertBtn = (Button) findViewById(R.id.toAlertBtn);

		chartList = new ArrayList<LineChart>();
		chartDateList = new ArrayList<String>();

		showProbe1Line = showProbe2Line = showTempLine = true;

		viewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
		/*
		 * if(Constants.isTestMode){ isUnitF = true; initTestChart(); }else{
		 * initCharts(); }
		 */
		initCharts();
		if (Constants.isTestMode) {
			setTemValue.setText("120");
			chartLabelTem.setText("℃");
			if (Config.getString(Constants.LAST_OVEN_TIME) != null) {
				ovenTime.setText(Config.getString(Constants.LAST_OVEN_TIME));
			}
			if (Config.getString(Constants.LAST_SMOKE_TIME) != null) {
				smokerTime.setText(Config.getString(Constants.LAST_SMOKE_TIME));
			}
			if (Config.getString(Constants.LAST_TEMP) != null) {
				currentTemperature.setText(Config.getString(Constants.LAST_TEMP));
			}
			if (Config.getString(Constants.LAST_TEMP_A) != null) {
				currentTemperatureA.setText(Config.getString(Constants.LAST_TEMP_A));
			}
			if (Config.getString(Constants.LAST_TEMP_B) != null) {
				currentTemperatureB.setText(Config.getString(Constants.LAST_TEMP_B));
			}
			if (Config.getString(Constants.LAST_TEMP_UNIT) != null) {
				currentTemperatureUnit.setText(Config.getString(Constants.LAST_TEMP_UNIT));
			}
		} 
	}

	private void initTestChart() {
		xVals120 = new ArrayList<String>();
		for (int i = 0; i <= MAX_MIN / 5; i++) {
			xVals120.add(i + "");
		}
		chartDateList.add("initTestChart");
		initChartByDate(chartDateList.get(0));
		viewPager.setAdapter(new MyAdapter());
		currentChartListIndex = 0;
		viewPager.setCurrentItem(currentChartListIndex);
	}

	private void initCharts() {
		String charts_list = "";
		Point p_charts_list = pointDao.findPointByX(12345);
		if (p_charts_list != null) {
			charts_list = p_charts_list.getMyDateList();
		} else {
			if(Constants.isTestMode){
				initTestChart();
				return;
			}
		}
		chartList.clear();// 容易报空指针
		chartDateList.clear();
		xVals120 = new ArrayList<String>();
		xVals240 = new ArrayList<String>();
		xVals360 = new ArrayList<String>();
		xVals480 = new ArrayList<String>();
		xVals600 = new ArrayList<String>();
		for (int i = 0; i <= MAX_MIN / 5; i++) {
			xVals120.add(i + "");
			xVals240.add(i + "");
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = MAX_MIN / 5 + 1; i <= MAX_MIN / 5 * 2; i++) {
			xVals240.add(i + "");
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = MAX_MIN / 5 * 2 + 1; i <= MAX_MIN / 5 * 3; i++) {
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = MAX_MIN / 5 * 3 + 1; i <= MAX_MIN / 5 * 4; i++) {
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = MAX_MIN / 5 * 4 + 1; i <= MAX_MIN; i++) {
			xVals600.add(i + "");
		}
		if (!charts_list.equals("")) {
			for (String chartDate : charts_list.split("#")) {
				chartDateList.add(chartDate);
				Log.i("btiLogChart", chartDate);
			}
			for (int i = 0; i < chartDateList.size(); i++) {
				Log.i("btiLogChart", chartDateList.get(i));
				initChartByDate(chartDateList.get(i));
			}
		} else {
			return;
		}
		viewPager.setAdapter(new MyAdapter());
		currentChartListIndex = chartList.size() - 1;
		viewPager.setCurrentItem(currentChartListIndex);
	}

	private void initChartByDate(String date) {
		LineChart mChart = new LineChart(this);
		chartList.add(mChart);

		mChart.setOnChartGestureListener(this);
		mChart.setDrawGridBackground(false);
		// no description text
		mChart.setDescription("");
		mChart.setNoDataText("");
		mChart.setNoDataTextDescription("");
		mChart.setTouchEnabled(true);// 设置是否可以触摸
		mChart.setDragEnabled(true);// 是否可以拖拽
		mChart.setScaleEnabled(true);// 是否可以缩放
		mChart.setPinchZoom(true);// true表示按两个手指比例缩放，false表示按xy轴比例缩放
		mChart.getLegend().setEnabled(false);
		mChart.setScaleYEnabled(false);// y轴不可拉伸

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.removeAllLimitLines();
		if (isUnitF) {
			leftAxis.setAxisMaxValue(549f);// y轴最大值
		} else {
			leftAxis.setAxisMaxValue(275f);// y轴最大值
		}
		leftAxis.setAxisMinValue(0.001f);// y轴最小值
		leftAxis.setLabelCount(10);
		leftAxis.setStartAtZero(false);
		leftAxis.enableGridDashedLine(0f, 0f, 0f);
		leftAxis.setAxisLineColor(Color.WHITE);
		leftAxis.setAxisLineWidth(1.6f);
		leftAxis.setGridColor(Color.parseColor("#888888"));
		leftAxis.setGridLineWidth(0.5f);
		leftAxis.setTextColor(Color.WHITE);
		mChart.getAxisRight().setEnabled(false);
		XAxis xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(Color.WHITE);
		xAxis.setAxisLineWidth(1.6f);
		xAxis.setGridColor(Color.parseColor("#888888"));
		xAxis.setGridLineWidth(0.5f);
		xAxis.setTextColor(Color.WHITE);

		LineData data = getNewLineData(date, mChart);
		if (data != null) {
			mChart.setData(data);
			mChart.invalidate();
			mChart.setHighlightEnabled(false);// 设置是否可以高亮
		}
	}

	/**
	 * 读取数据库中的数据，得到最新的LineData
	 */
	private LineData getNewLineData(String date, LineChart mChart) {
		ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
		ArrayList<String> xVals = xVals120;// 初始化x，默认120
		List<Point> pointList = pointDao.getPointsByDate(date);
		for (Point point : pointList) {
			Log.i("btiLogChart", "db x:" + point.getX() + " temp:" + point.getBtiTemp());
		}

		// 画一条透明的曲线
		ArrayList<Entry> yValsNull = new ArrayList<Entry>();
		yValsNull.add(new Entry(1, 0));
		yValsNull.add(new Entry(600, 1));
		LineDataSet set = new LineDataSet(yValsNull, "");
		set.setColor(Color.parseColor("#00000000"));
		set.setDrawValues(false);// 不显示具体数值
		set.setDrawCircles(false);// 不显示具体圆圈
		dataSets.add(set);
		if (Constants.isTestMode) {
			currentMaxX = MAX_MIN / 5;
			XAxis xAxis = mChart.getXAxis();
			xAxis.setLabelsToSkip(currentMaxX / 12 - 1);
		}
		if (pointList != null && pointList.size() > 0) {
			int x = MAX_MIN;// 默认最大值600

			if (date.equals(chartDateList.get(chartDateList.size() - 1))) {// 最新的图表
				x = getXByTime(date);
				if (x == -100) {
					x = MAX_MIN;
				}
				XAxis xAxis = mChart.getXAxis();
				if (x <= MAX_MIN / 5) {
					currentMaxX = MAX_MIN / 5;
				} else if (x > MAX_MIN / 5 && x <= MAX_MIN / 5 * 2) {
					xVals = xVals240;
					currentMaxX = MAX_MIN / 5 * 2;
				} else if (x > MAX_MIN / 5 * 2 && x <= MAX_MIN / 5 * 3) {
					xVals = xVals360;
					currentMaxX = MAX_MIN / 5 * 3;
				} else if (x > MAX_MIN / 5 * 3 && x <= MAX_MIN / 5 * 4) {
					xVals = xVals480;
					currentMaxX = MAX_MIN / 5 * 4;
				} else if (x > MAX_MIN / 5 * 4 && x <= MAX_MIN) {
					xVals = xVals600;
					currentMaxX = MAX_MIN;
				}
				xAxis.setLabelsToSkip(currentMaxX / 12 - 1);
			} else {
				xVals = xVals600;
				XAxis xAxis = mChart.getXAxis();
				xAxis.setLabelsToSkip(MAX_MIN / 12 - 1);
			}

			// 3条曲线
			if (showProbe1Line) {
				initLineData(pointList, date, dataSets, x, 1);
			}
			if (showProbe2Line) {
				initLineData(pointList, date, dataSets, x, 2);
			}
			if (showTempLine) {
				initLineData(pointList, date, dataSets, x, 3);
			}
			LineData data = new LineData(xVals, dataSets);
			return data;
		}
		LineData data = new LineData(xVals, dataSets);
		return data;
	}

	private ArrayList<LineDataSet> initLineData(List<Point> pointList, String date, ArrayList<LineDataSet> dataSets, int x, int tempType) {
		String color = "";
		switch (tempType) {
		case 1:
			color = "#26ff4a";
			break;
		case 2:
			color = "#ffb951";
			break;
		case 3:
			color = "#00b6ed";
			break;
		}
		List<ArrayList<Entry>> yList = new ArrayList<ArrayList<Entry>>();
		yList.add(new ArrayList<Entry>());
		int yListIndex = 0;
		int pointListIndex = 0;
		boolean isFullLine = true;// 是实线
		for (int i = 0; i <= pointList.get(pointList.size() - 1).getX(); i++) {
			if (pointList.get(pointListIndex).getX() == i) {
				if (isFullLine) {
				} else {
					int firstX = yList.get(yListIndex).get(0).getXIndex();
					float firstY = yList.get(yListIndex).get(0).getVal();
					for (int j = firstX; j <= i; j++) {
						yList.get(yListIndex).add(new Entry(firstY, j));// 添加所有点
					}
					dataSets.add(getLine(false, yList.get(yListIndex), Color.parseColor(color)));// 添加虚线
					isFullLine = true;
					yList.add(new ArrayList<Entry>());
					yListIndex++;
				}
				int y = 0;
				switch (tempType) {
				case 1:
					y = pointList.get(pointListIndex).getProbe1Temp();
					Log.i("btichart", "1:" + y);
					break;
				case 2:
					y = pointList.get(pointListIndex).getProbe2Temp();
					Log.i("btichart", "2:" + y);
					break;
				case 3:
					y = pointList.get(pointListIndex).getBtiTemp();
					Log.i("btichart", "t:" + y);
					break;
				}
				if (isUnitF) {// 将摄氏度转成华氏度
					if (y != 0) {
						y = (int) (y * 1.8 + 32);
					}
				}
				yList.get(yListIndex).add(new Entry(y, i));
				pointListIndex++;
			} else {
				if (isFullLine) {
					dataSets.add(getLine(true, yList.get(yListIndex), Color.parseColor(color)));// 添加实线
					isFullLine = false;
					ArrayList<Entry> yValsnext = new ArrayList<Entry>();// 虚线
					yList.add(yValsnext);
					yListIndex++;
					yList.get(yListIndex).add(yList.get(yListIndex - 1).get(yList.get(yListIndex - 1).size() - 1));
				} else {
				}
			}
			if (i == pointList.get(pointList.size() - 1).getX()) {
				dataSets.add(getLine(true, yList.get(yListIndex), Color.parseColor(color)));// 添加实线
			}
		}
		ArrayList<Entry> yValsEnd = new ArrayList<Entry>();// 虚线
		yValsEnd.add(yList.get(yListIndex).get(yList.get(yListIndex).size() - 1));
		int firstX = yValsEnd.get(0).getXIndex();
		float firstY = yValsEnd.get(0).getVal();
		for (int i = firstX + 1; i <= x; i++) {
			yValsEnd.add(new Entry(firstY, i));// 添加所有点
		}
		dataSets.add(getLine(false, yValsEnd, Color.parseColor(color)));// 添加虚线

		return dataSets;
	}

	private LineDataSet getLine(boolean isFullLine, ArrayList<Entry> yVals, int color) {
		LineDataSet set = new LineDataSet(yVals, "");
		if (!isFullLine) {
			set.enableDashedLine(5, 50, 0);
		}
		set.setColor(color);
		set.setLineWidth(2f);
		set.setCircleSize(3f);
		set.setDrawCircleHole(false);
		set.setValueTextSize(9f);
		set.setFillAlpha(65);
		set.setFillColor(Color.BLACK);
		set.setDrawValues(false);// 不显示具体数值
		set.setDrawCircles(false);// 不显示具体圆圈

		return set;
	}

	/**
	 * 更新温度曲线数量
	 */
	private void updateTempLineNum() {
		if (chartDateList.size() > 0) {
			LineData dataCur = getNewLineData(chartDateList.get(currentChartListIndex), chartList.get(currentChartListIndex));// 先画当前页面
			chartList.get(currentChartListIndex).setData(dataCur);
			chartList.get(currentChartListIndex).invalidate();
			chartList.get(currentChartListIndex).setHighlightEnabled(false);
			for (int i = chartList.size() - 1; i >= 0; i--) {
				if (i != currentChartListIndex) {
					LineData data = null;
					if (i < chartList.size() - 1) {
						data = getNewLineData(chartDateList.get(i), chartList.get(i));
					} else {// 重画最新的图表
						data = getNewLineData(chartDateList.get(chartDateList.size() - 1), chartList.get(i));
					}
					chartList.get(i).setData(data);
					chartList.get(i).invalidate();
					chartList.get(i).setHighlightEnabled(false);
				}
			}
		}
	}

	@Override
	public void initListener() {
		checkBoxA.setOnCheckedChangeListener(this);
		checkBoxB.setOnCheckedChangeListener(this);
		checkBoxBox.setOnCheckedChangeListener(this);

		stopBtn.setOnClickListener(singleClickListener);
		toAlertBtn.setOnClickListener(singleClickListener);
		toHomeBtn.setOnClickListener(singleClickListener);
		toSettingsBtn.setOnClickListener(singleClickListener);
	}

	@Override
	public void initData() {
		if (!Constants.isTestMode) {
			TimerUtil.setInterval(timerRecord, 60000);
		}
	}

	private OnSingleClickListener singleClickListener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
			case R.id.toSettingsBtn:
				intent.setClass(ChartActivity.this, SettingUnitActiviy.class);
				startActivity(intent);
				canNotify = false;
				break;
			case R.id.toHomeBtn:
				finish();
				break;
			case R.id.toAlertBtn:
				intent.setClass(ChartActivity.this, AlertActivity.class);
				startActivity(intent);
				canNotify = false;
				break;
			case R.id.stopBtn:
				mEventBus.post(new BleEvent(BleCommand.STOP_RECODE));
				finish();
				break;
			}
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.checkBoxA:
			if (isChecked) {
				showProbe1Line = true;
			} else {
				showProbe1Line = false;
			}
			break;
		case R.id.checkBoxB:
			if (isChecked) {
				showProbe2Line = true;
			} else {
				showProbe2Line = false;
			}
			break;
		case R.id.checkBoxBox:
			if (isChecked) {
				showTempLine = true;
			} else {
				showTempLine = false;
			}
			break;
		}
		updateTempLineNum();
	}

	private Runnable timerRecord = new Runnable() {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (chartDateList != null) {
						int x = getXByTime(chartDateList.get(chartDateList.size() - 1));
						if (x == -100) {// 重画一张新图表
							initCharts();
						} else {// 重画最新图表
							if (chartList != null && chartList.size() > 0) {
								LineData data = getNewLineData(chartDateList.get(chartDateList.size() - 1), chartList.get(chartList.size() - 1));
								chartList.get(chartList.size() - 1).setData(data);
								chartList.get(chartList.size() - 1).invalidate();
								chartList.get(chartList.size() - 1).setHighlightEnabled(false);
							}
						}
					}

				}
			});
		}
	};

	@Override
	public void onChartLongPressed(MotionEvent me) {
		Log.i("LongPress", "Chart longpressed.");
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		Log.i("DoubleTap", "Chart double-tapped.");
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		Log.i("SingleTap", "Chart single-tapped.");
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
		Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
		XAxis xAxis = chartList.get(currentChartListIndex).getXAxis();
		if (chartList.get(currentChartListIndex).getViewPortHandler().canZoomOutMoreX()) {
			viewPager.setNoScroll(true);
			xAxis.resetLabelsToSkip();
		} else {
			viewPager.setNoScroll(false);
			if (currentChartListIndex == chartList.size() - 1) {
				if (currentMaxX == 0) {
					currentMaxX = MAX_MIN / 5 - 1;
				}
				xAxis.setLabelsToSkip(currentMaxX / 12 - 1);
			} else {
				xAxis.setLabelsToSkip(MAX_MIN / 12 - 1);
			}
		}
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
		Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
	}

	class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return chartList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// super.destroyItem(container, position, object);
			((NoScrollViewPager) container).removeView(chartList.get(position));
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			return "title";
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			((NoScrollViewPager) container).addView(chartList.get(position));
			return chartList.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public void setPrimaryItem(View container, int position, Object object) {
			currentChartListIndex = position;
		}

	}

	private int getXByTime(String previousTime) {
		String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

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

		if (year_current == year_previous && mon_current == mon_previous && day_current == day_previous) {// 在同一天，可直接比较分钟
			int minNum = min_current + hour_current * 60 - min_previous - hour_previous * 60;
			if (minNum <= MAX_MIN) {// 在600分钟内
				return minNum;
			} else {
				return -100;// 超过600分钟，重画一张新的图表
			}
		} else {// 不在同一天，判断是否是第二天
			boolean isNextDayStatus1 = year_current == year_previous && mon_current == mon_previous && day_current == day_previous + 1;// 同一个月的第二天
			boolean isNextDayStatus2 = false;// 上个月的最后一天和这个月的第一天
			boolean isNextDayStatus3 = year_current == year_previous + 1 && mon_current == 1 && mon_previous == 12 && day_current == 1 && day_previous == 31;// 上一年的最后一天和这一年的第一天
			if (year_current == year_previous) {// 同一年
				if ((mon_previous == 1 || mon_previous == 3 || mon_previous == 5 || mon_previous == 7 || mon_previous == 8 || mon_previous == 10) && mon_current == mon_previous + 1 && day_current * 31 == day_previous) {
					// 1,3,5,7,8,10月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if ((mon_previous == 4 || mon_previous == 6 || mon_previous == 9 || mon_previous == 11) && mon_current == mon_previous + 1 && day_current * 30 == day_previous) {
					// 4,6,9,7,11月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if (mon_previous == 2 && mon_current == mon_previous + 1 && day_current == 1) {
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
				int minNum = 24 * 60 + min_current + hour_current * 60 - min_previous - hour_previous * 60;
				if (minNum <= MAX_MIN) {// 在600分钟内
					return minNum;
				} else {
					return -100;// 超过600分钟，重画一张新的图表
				}
			} else {// 不是第二天，重画一张新的图表
				return -100;
			}
		}
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
    	Log.e("testLog", "code:"+code);
        if(code != lastErrorCode){
        	Log.e("testLog", "lastErrorCode:"+code);
        	switch (code) {
        	case 0xe1:
        		// 0xe1:箱体探头错误
            	Log.e("testLog", "0xe1:"+code);
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
