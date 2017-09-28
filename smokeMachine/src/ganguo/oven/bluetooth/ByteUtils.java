package ganguo.oven.bluetooth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.util.Log;

/**
 * Created by Tony on 1/18/15.
 */
public class ByteUtils {

    /**
     * The Constant HEXES.
     */
    private static final String HEXES = "0123456789ABCDEF";

    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] shortToBytes(short value) {
        byte[] returnByteArray = new byte[2];
        returnByteArray[0] = (byte) (value & 0xff);
        returnByteArray[1] = (byte) ((value >>> 8) & 0xff);
        return returnByteArray;
    }

    public static int bytesToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToBytes(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    /**
     * Gets a pretty representation of a Byte Array as a HEX String.
     * <p/>
     * Sample output: [01, 30, FF, AA]
     *
     * @param array the array
     * @return the string
     */
    public static String bytesToHexString(final byte[] array) {
        final StringBuffer sb = new StringBuffer();
        boolean firstEntry = true;
        sb.append('[');

        for (final byte b : array) {
            if (!firstEntry) {
                sb.append(", ");
            }
            sb.append(HEXES.charAt((b & 0xF0) >> 4));
            sb.append(HEXES.charAt((b & 0x0F)));
            firstEntry = false;
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Checks to see if a byte arry starts with another byte array.
     *
     * @param array  the array
     * @param prefix the prefix
     * @return true, if successful
     */
    public static boolean doesArrayBeginWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a byte array with a length of 2 into an int
     *
     * @param input the input
     * @return the int from the array
     */
    public static int getIntFrom2ByteArray(byte[] input) {
        final byte[] result = new byte[4];

        result[0] = 0;
        result[1] = 0;
        result[2] = input[0];
        result[3] = input[1];

        return ByteUtils.getIntFromByteArray(result);
    }

    /**
     * Converts a byte to an int, preserving the sign.
     * <p/>
     * For example, FF will be converted to 255 and not -1.
     *
     * @param bite the bite
     * @return the int from byte
     */
    public static int getIntFromByte(final byte bite) {
        return Integer.valueOf(bite & 0xFF);
    }

    /**
     * Converts a byte array to an int.
     *
     * @param bytes the bytes
     * @return the int from byte array
     */
    public static int getIntFromByteArray(final byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Converts a byte array to a long.
     *
     * @param bytes the bytes
     * @return the long from byte array
     */
    public static long getLongFromByteArray(final byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }


    /**
     * Inverts an array
     *
     * @param array the array
     * @return the byte[]
     */
    public static byte[] invertArray(byte[] array) {
        final int size = array.length;
        byte temp;

        for (int i = 0; i < size / 2; i++) {
            temp = array[i];
            array[i] = array[size - 1 - i];
            array[size - 1 - i] = temp;
        }

        return array;
    }

    /**
     * 效验数据有效性
     * D22 此字节为校验字节 等于所有字节相加取反后，再加1; 即 D22  =  ~(0xbb +D0+D1+….+D21) +1
     *
     * @return
     */
    public static boolean checkData(byte[] data) {
        byte check = data[data.length - 2];
        byte res = 0;
        for (int i = 0; i < data.length - 2; i++) {
            res += data[i];
        }
        res = (byte) (~res + 1);
        boolean isOk = res == check;
        if (!isOk) {
            Log.e("testLog", (res & 0xff) + " check " + (check & 0xff) + " data: " + bytesToHexString(data));
        }
        return isOk;
    }

    public static byte verifySettingData(byte[] data) {
        byte res = 0;
        for (int i = 0; i < data.length - 2; i++) {
            res += data[i];
        }
        res = (byte) (~res + 1);

        return res;
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
	public static void printArrayList(String tag, ArrayList<byte[]> list){
		for (byte[] bytes : list) {
			String str = "";
			for (int i = 0; i < bytes.length; i++) {
				str += get16FormatByte(bytes[i]) + " ";
			}
			printLog(tag, str);
		}
	}
	public static void printArrayList(String tag, byte[] bytes){
		String str = "";
		for (int i = 0; i < bytes.length; i++) {
			str += get16FormatByte(bytes[i]) + " ";
		}
		printLog(tag, str);
	}
	public static void printLog(String tag, String str){
        Log.i("jLog"+tag, tag + ":" +str);
	}
}
