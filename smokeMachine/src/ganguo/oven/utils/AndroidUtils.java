package ganguo.oven.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.List;

public class AndroidUtils {

	public static Boolean isActivityVisibility(Context context,String activityName){
		ActivityManager manager = (ActivityManager)   context.getSystemService(Context.ACTIVITY_SERVICE);    
		List<RunningTaskInfo> runningTasks = manager .getRunningTasks(1);    
		RunningTaskInfo cinfo = runningTasks.get(0);    
		ComponentName component = cinfo.topActivity;    
		Log.e("testLog", component.getClassName());
		return activityName.equals(component.getClassName());
	}
	
    public static void toast(Context context, String message) {
        if (context == null) return;

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastInMiddle(Context context, String message) {
        if (context == null) return;

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void toastLong(Context context, String message) {
        if (context == null) return;

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 弹出键盘
     */
    public static void openInput(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 关闭键盘
     */
    public static void closeInput(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus()
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static float getDimenResource(Activity activity, int dimenId) {
        return activity.getResources().getDimension(dimenId);
    }

    public static void closeVirtualKeyBoard(final Activity activity) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity.getCurrentFocus() != null) {
                    InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        }, 500);

    }

    /**
     * 获取app版本号
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getVersionName(Context context, String packageName) {
        try {
            PackageInfo packInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 检查应用程序是否已安装应用程序
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean checkApkExist(Context context, String packageName) {
        if (StringUtils.isEmpty(packageName))
            return false;
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 比较两个app版本号，看是否需要更新
     *
     * @param targetVersion
     * @param baseVersion
     * @return true if targetVersion > baseVersion,return true if haven't
     * install
     */
    public static boolean needToUpdate(String targetVersion, String baseVersion) {
        if (StringUtils.isEmpty(targetVersion)) {
            return false;//empty target, return false
        }
        if (StringUtils.isEmpty(baseVersion)) {
            return true;// not install, return true
        }
        List<String> targetItems = StringUtils.stringToList(targetVersion, StringUtils.VERSION_SEPERATOR);
        List<String> baseItems = StringUtils.stringToList(baseVersion, StringUtils.VERSION_SEPERATOR);
        Log.e("targetItems", targetItems.toString());
        Log.e("baseItems", baseItems.toString());

        if (CollectionUtils.isEmpty(targetItems) || CollectionUtils.isEmpty(baseItems)) {
            return false;
        }

        final int targetSize = targetItems.size();
        final int baseSize = baseItems.size();
        final int total = targetSize > baseSize ? targetSize : baseSize;

        for (int i = 0; i < total; i++) {
            int targetV = (i >= targetSize) ? 0 : Integer.parseInt(targetItems.get(i));
            int baseV = (i >= baseSize) ? 0 : Integer.parseInt(baseItems.get(i));
            if (targetV > baseV) {
                return true;
            }

        }
        return false;
    }

    /**
     * 获取当前程序版本名称
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // Get the package info
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return versionName;
    }

}
