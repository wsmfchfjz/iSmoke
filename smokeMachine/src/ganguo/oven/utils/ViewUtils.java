package ganguo.oven.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by Wilson on 14-7-8.
 */
public class ViewUtils {
    //TODO:// remember to init dialog_loading:R.style.dialog_loading
    public static Dialog createCustomDialog(Context context, int layoutId, Integer width, Integer height, int dialogStyleId) {
        Dialog dialog = new Dialog(context, dialogStyleId);
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        if (width != null) {
            lp.width = width;
        }
        if (height != null) {
            lp.height = height;
        }

        dialogWindow.setAttributes(lp);

        return dialog;
    }

    public static PopupWindow createCustomPopupWindow(Context context, int layoutId, Integer width, Integer height, Integer animationStyleId) {
        View popupView = LayoutInflater.from(context).inflate(layoutId, null);
        PopupWindow targetWindow = new PopupWindow(popupView, width, height, true);
        targetWindow.setTouchable(true);
        targetWindow.setOutsideTouchable(true);
        targetWindow.setFocusable(true);
        targetWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        if (animationStyleId != null) {
            targetWindow.setAnimationStyle(animationStyleId);
        }
        return targetWindow;
    }

    public static View addOriginalViewToActivity(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();
        View anchor = new View(activity);
        anchor.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        rootView.addView(anchor, 0);
        return anchor;
    }
}
