package ganguo.oven.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import ganguo.oven.R;
import ganguo.oven.activity.DeviceActivity;

/**
 * Created by Tony on 1/18/15.
 */
public class UIUtils {
    private static Dialog scanDialog;

    /**
     * generate dialog for searching device
     *
     * @return
     */
    public static Dialog showLoading(Activity activity, String tip) {
        hideLoading();

        int width = (int) (AndroidUtils.getScreenWidth(activity) * 0.8);
        int height = (int) activity.getResources().getDimension(R.dimen.bluetooth_seach_height);
        scanDialog = ViewUtils.createCustomDialog(activity, R.layout.dialog_bluetooth_search, width, height, R.style.dialog_loading);
        scanDialog.setCancelable(false);
        scanDialog.setCanceledOnTouchOutside(false);

        ImageView refreshIcon = (ImageView) scanDialog.findViewById(R.id.refreshIcon);
        Animation myAlphaAnimation = AnimationUtils.loadAnimation(activity, R.anim.loading);
        myAlphaAnimation.setInterpolator(new LinearInterpolator());
        refreshIcon.startAnimation(myAlphaAnimation);

        TextView tvTip = (TextView) scanDialog.findViewById(R.id.searchTips);
        tvTip.setText(tip);

        scanDialog.show();
        return scanDialog;
    }

    public static void hideLoading() {
        if (scanDialog != null) {
            scanDialog.dismiss();
            scanDialog = null;
        }
    }

    public static void showPromptDialong(final Activity activity) {
        new AlertDialog
                .Builder(activity)
                .setTitle("Tip")
                .setMessage("Disconnected for Device.")
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(activity, DeviceActivity.class));
                        activity.finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * 摄氏度 TO 华氏度
     * °F＝℃×9/5＋32
     *
     * @param cel
     * @return
     */
    public static int celsiusToFahrenheit(int cel) {
        return (int) (cel * 9.0f / 5.0f + 32);
    }

    /**
     * 华氏度 TO 摄氏度
     * 摄氏＝5/9（°F－32）
     *
     * @param fah
     * @return
     */
    public static int fahrenheitToCelsius(int fah) {
        return (int) ((fah - 32) * 5.0f / 9.0f);
    }

    /**
     * 1 to 01
     *
     * @param time
     * @return
     */
    public static String formateTime(int time) {
        return time < 10 ? "0" + time : time + "";
    }

}
