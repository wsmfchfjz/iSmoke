package ganguo.oven;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;


/**
 * 程序配置文件（读取和写入）
 * <p/>
 * Created by zhihui_chen on 14-8-4.
 */
public class Config {

    /**
     * 开发环境配置信息
     */
    public static boolean DEV_MODE = false;

    /**
     * 数据目录
     */
    public static String DATA_PATH = "oven";

    /**
     * 图片目录
     */
    public static String IMAGES_PATH = "images";

    /**
     * 图片缓存目录 ImageLoader
     */
    public static String IMAGE_CACHE_PATH = "imageCache";

    /**
     * 临时目录名称
     */
    public final static String APP_TEMP_PATH = "temp";

    private static final String TAG = Config.class.getName();
    private static Context context = null;

    public static void register(Context context) {
        Config.context = context;
    }

    /**
     * 是否存在SD卡
     *
     * @return
     */
    public static boolean isExsitSDCard() {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 取得空闲SD卡空间大小
     *
     * @return MB
     */
    public static long getAvailaleSize() {
        File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs stat = new StatFs(path.getPath());
        /* 获取block的SIZE */
        long blockSize = stat.getBlockSize();
        /* 空闲的Block的数量 */
        long availableBlocks = stat.getAvailableBlocks();
        /* 返回bit大小值 */
        return availableBlocks * blockSize / 1024 / 1024;
    }

    /**
     * 获取app数据目录
     *
     * @return
     */
    public static String getDataPath() {
        // 判断是否挂载了SD卡
        String dataPath = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            dataPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + DATA_PATH
                    + File.separator;
        } else {
            File basePath = context.getFilesDir();
            if (basePath == null) {
                basePath = context.getCacheDir();
            }
            dataPath = basePath.getAbsolutePath()
                    + File.separator
                    + DATA_PATH
                    + File.separator;
        }
        File file = new File(dataPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dataPath;
    }

    /**
     * 获取程序图片目录
     *
     * @return
     */
    public static String getImagePath() {
        String images = getDataPath() + IMAGES_PATH + File.separator;
        File fileDir = new File(images);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return images;
    }

    /**
     * 获取程序图片缓存目录 不可见图片 ImageLoader
     *
     * @return
     */
    public static String getImageCachePath() {
        String images = getDataPath() + IMAGE_CACHE_PATH + File.separator;
        File fileDir = new File(images);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return images;
    }

    /**
     * 获取程序临时目录
     *
     * @return
     */
    public static String getTempPath() {
        String temp = getDataPath() + APP_TEMP_PATH + File.separator;
        File fileDir = new File(temp);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return temp;
    }

    /**
     * 获取Preference设置
     */
    public static SharedPreferences getSharedPreferences() {
        if (Config.context == null) {
            Log.e(TAG, "配置类没有注册上AppContext(下文环境)");
        }
        return PreferenceManager.getDefaultSharedPreferences(Config.context);
    }

    /**
     * 写入配置信息，需要最后面进行 commit()
     *
     * @param key
     * @param value
     * @return
     */
    public static void putString(String key, String value) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 写入配置信息，需要最后面进行 commit()
     *
     * @param key
     * @param value
     * @return
     */
    public static void putInt(String key, int value) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 写入配置信息，需要最后面进行 commit()
     *
     * @param key
     * @param value
     * @return
     */
    public static void putLong(String key, long value) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 写入配置信息，需要最后面进行 commit()
     *
     * @param key
     * @param value
     * @return
     */
    public static void putBoolean(String key, boolean value) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 读取配置信息
     *
     * @param key
     * @return
     */
    public static boolean getBoolean(String key, boolean def) {
        return getSharedPreferences().getBoolean(key, def);
    }

    /**
     * 读取配置信息
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    /**
     * 读取配置信息
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    /**
     * 读取配置信息
     *
     * @param key
     * @return
     */
    public static long getLong(String key) {
        return getSharedPreferences().getLong(key, 0L);
    }

    /**
     * 删除配置信息，可以同时删除多个
     *
     * @param keys
     */
    public static void remove(String... keys) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    /**
     * 清除所有配置文件
     */
    public static void clearAll() {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }
}
