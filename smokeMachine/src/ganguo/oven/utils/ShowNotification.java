package ganguo.oven.utils;




import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import ganguo.oven.R;
import ganguo.oven.activity.MainActivity;

public class ShowNotification {

	public static final int ID_BACKGROUND = 10000;//后台服务，总是运行在状态栏通知
	
	private Context context;
	private NotificationManager notificationManager;
	private static ShowNotification myShowNotification;
	public static ShowNotification getInstence(Context context){
		if(myShowNotification == null){
			myShowNotification = new ShowNotification(context);
		}
		return myShowNotification;
	}
	private ShowNotification(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
	}

	public void showNotification(int id, String title, String message) {//用于后台运行
		PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0,  
                new Intent(context, ganguo.oven.activity.MainActivity.class), 0);  
		
        // 通过Notification.Builder来创建通知，注意API Level  
        // API16之后才支持  
        Notification notify3 = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_app)
                .setTicker("Notification!")  
                .setContentTitle(title)  
                .setContentText(message)
                .setContentIntent(pendingIntent3).build(); // 需要注意build()是在API  
                                                                        // level16及之后增加的，API11可以使用getNotificatin()来替代  
        notify3.flags |= Notification.FLAG_AUTO_CANCEL; // FLAG_AUTO_CANCEL表明当通知被用户点击时，通知将被清除。  
        notificationManager.notify(id, notify3);// 步骤4：通过通知管理器来发起通知。如果id不同，则每click，在status哪里增加一个提示  
	}

	/**
	 * 取消通知
	 * 
	 * @param tag
	 * @param id
	 */
	public void cancelNotification(int id) {
		notificationManager.cancel(id);
	}

	/**
	 * 取消通知
	 * 
	 * @param tag
	 * @param id
	 */
	public void cancelNotification(String tag, int id) {
		notificationManager.cancel(tag, id);
	}

	
}
