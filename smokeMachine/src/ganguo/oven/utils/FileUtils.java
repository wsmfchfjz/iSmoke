package ganguo.oven.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class FileUtils {

	/** 保存文件绝对路径 */
	private String DIRECTORY = "";

	/** 保存Log信息的字符串缓存 */
	private StringBuffer BUFFER = null;

	/** 回车 */
	private static final String LINE = "\r\n";

	private static FileUtils instance = null;
	
	public static FileUtils getInstance() {
		if (instance == null) {
			synchronized (FileUtils.class) {
				if (instance == null)
					instance = new FileUtils();
			}
		}
		return instance;
	}
	
	public FileUtils() {
		BUFFER = new StringBuffer();
	}

	/**
	 * 初始化保存文件目录
	 */
	public void initDirectory(String directoryName) {
		if (!StringUtils.isEmpty(directoryName))
			DIRECTORY = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + directoryName + "/";
		
		File directory = new File(DIRECTORY);
		if (!directory.exists())
			directory.mkdirs();
		directory = null;
	}

	public void writeData(String data, String fileName) {
		try {
			
			BUFFER.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(": ").append(data).append(LINE);
			int length = BUFFER.length();
			File tempFile = new File(DIRECTORY);
			if (!tempFile.exists())
				tempFile.mkdirs();

			FileWriter file;
			String filePath = DIRECTORY + fileName + ".log";

			File tmpFile = new File(filePath);
			if (!tmpFile.exists())
				createNewFile(filePath);

			file = new FileWriter(filePath, true);

			file.write(BUFFER.toString());
			file.flush();
			file.close();
			file = null;

			BUFFER.delete(0, length);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deleteFile(String fileName){
		String filePath = DIRECTORY + fileName + ".log";
		File f = new File(filePath);  // 输入要删除的文件位置
		if(f.exists())
		    f.delete();
	}

	private void createNewFile(String filePath) throws Exception {
		FileOutputStream fos = new FileOutputStream(filePath);
		fos.write("".getBytes());
		fos.flush();
		fos.close();
	}

	/**
	 * 释放本实例
	 */
	public static void recycle() {
		instance = null;
	}

}
