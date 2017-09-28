package ganguo.oven.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Wilson
 */
public class StringUtils {

    public static final String VERSION_SEPERATOR = ".";

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str) || TextUtils.isEmpty(str.trim());
    }

    /**
     * judge string equals or not
     *
     * @param str
     * @param other
     * @return false if anyone is empty
     */
    public static boolean equals(String str, String other) {
        if (isEmpty(str) || isEmpty(other)) {
            return false;
        }
        return str.equals(other);
    }

    /**
     * judge string equals or not
     *
     * @param str
     * @param other
     * @return false if anyone is empty
     */
    public static boolean equalsIgnoreCase(String str, String other) {
        if (isEmpty(str) || isEmpty(other)) {
            return false;
        }
        return str.equalsIgnoreCase(other);
    }

    public static List<String> stringToList(String str, String seperator) {
        List<String> itemList = new ArrayList<String>();
        if (isEmpty(str)) {
            return itemList;
        }
        StringTokenizer st = new StringTokenizer(str, seperator);
        while (st.hasMoreTokens()) {
            itemList.add(st.nextToken());
        }

        return itemList;
    }
    
	/**
	 * 将byte转成16进制字符串，如传入00010011，则返回字符串"13"
	 */
	public static String get16FormatByte(byte temp){
		String str = Integer.toHexString(temp & 0xFF);
		if(str.length() == 1){
			str = "0" + str;
		}
		return str;
	}

}
