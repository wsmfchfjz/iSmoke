package ganguo.oven.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Tony on 1/24/15.
 */
public class BleUtils {
    private static final String TAG = BleUtils.class.getName();

    private final static String sPREFIX = "0000";
    private final static String sPOSTFIX = "-0000-1000-8000-00805f9b34fb";
    /* ISSC Proprietary */
    public final static UUID SERVICE_ISSC_PROPRIETARY = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
//    public final static UUID CHR_CONNECTION_PARAMETER = UUID.fromString("49535343-6DAA-4D02-ABF6-19569ACA69FE");
//    public final static UUID CHR_AIR_PATCH = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");
    public final static UUID CHR_ISSC_TRANS_TX = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    public final static UUID CHR_ISSC_TRANS_RX = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");
//    public final static UUID CHR_ISSC_MP = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");

    /* Client Characteristic Configuration Descriptor */
    public final static UUID DES_CLIENT_CHR_CONFIG = uuidFromStr("2902");

    public static UUID uuidFromStr(String str) {
        if (!str.matches(".{4}")) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(sPREFIX);
            sb.append(str);
            sb.append(sPOSTFIX);
            return UUID.fromString(sb.toString());
        }
    }

    public static boolean enableNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // XXX: we did not app this request to queue, so the system call might not work correctly
        // if we send request to fast
        boolean set = gatt.setCharacteristicNotification(characteristic, true);
        Log.d(TAG, "set notification:" + set);
        BluetoothGattDescriptor dsc = characteristic.getDescriptor(DES_CLIENT_CHR_CONFIG);
        dsc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        boolean success = gatt.writeDescriptor(dsc);
        Log.d(TAG, "writing enable descriptor:" + success);

        return success;
    }

    public static boolean disableNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // XXX: we did not app this request to queue, so the system call might not work correctly
        // if we send request to fast
        boolean set = gatt.setCharacteristicNotification(characteristic, false);
        Log.d(TAG, "set notification:" + set);
        BluetoothGattDescriptor dsc = characteristic.getDescriptor(DES_CLIENT_CHR_CONFIG);
        dsc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean success = gatt.writeDescriptor(dsc);
        Log.d(TAG, "writing disable descriptor:" + success);

        return success;
    }

    public static BluetoothGattCharacteristic getNotifyCharacteristic(BluetoothGatt gatt) {
        return gatt.getService(SERVICE_ISSC_PROPRIETARY).getCharacteristic(CHR_ISSC_TRANS_TX);
    }

    public static BluetoothGattCharacteristic getWriteCharacteristic(BluetoothGatt gatt) {
        return gatt.getService(SERVICE_ISSC_PROPRIETARY).getCharacteristic(CHR_ISSC_TRANS_RX);
    }
}
