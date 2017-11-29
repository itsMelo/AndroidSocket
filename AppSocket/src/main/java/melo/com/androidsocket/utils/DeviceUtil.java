package melo.com.androidsocket.utils;

import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


/**
 * 获取设备的信息
 *
 * @author melo
 */
public final class DeviceUtil {

    /**
     * <p><b>IMEI.</b></p> Returns the unique device ID, for example, the IMEI for GSM and the MEID
     * or ESN for CDMA phones. Return null if device ID is not available.
     * <p>
     * Requires Permission: READ_PHONE_STATE
     *
     * @param context
     * @return
     */
    public synchronized static String getDeviceId(Context context) {
        if (context == null) {
            return "";
        }

        String imei = "";

        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null || TextUtils.isEmpty(tm.getDeviceId())) {
                // 双卡双待需要通过phone1和phone2获取imei，默认取phone1的imei。
                tm = (TelephonyManager) context.getSystemService("phone1");
            }

            if (tm != null) {
                imei = tm.getDeviceId();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        return imei;
    }

    /**
     * Returns the serial number of the SIM, if applicable. Return null if it is
     * unavailable.
     * <p>
     * Requires Permission: READ_PHONE_STATE
     *
     * @param context
     * @return
     */
    public synchronized static String getSimSerialNumber(Context context) {
        if (context == null) {
            return "";
        }
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimSerialNumber();
    }

    /**
     * A 64-bit number (as a hex string) that is randomly generated on the
     * device's first boot and should remain constant for the lifetime of the
     * device. (The value may change if a factory reset is performed on the
     * device.)
     *
     * @param context
     * @return
     */
    public synchronized static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * 操作系统版本
     *
     * @return
     */
    public static String getOSversion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 设备商
     *
     * @return
     */
    public static String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * 设备型号
     *
     * @return
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 序列号
     *
     * @return
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    /**
     * SD CARD ID
     *
     * @return
     */
    public static synchronized String getSDcardID() {
        try {
            String sdCid = null;
            String[] memBlkArray = new String[]{"/sys/block/mmcblk0", "/sys/block/mmcblk1", "/sys/block/mmcblk2"};
            for (String memBlk : memBlkArray) {
                File file = new File(memBlk);
                if (file.exists() && file.isDirectory()) {
                    Process cmd = Runtime.getRuntime().exec("cat " + memBlk + "/device/cid");
                    BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
                    sdCid = br.readLine();
                    if (!TextUtils.isEmpty(sdCid)) {
                        return sdCid;
                    }
                }
            }
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取mac地址
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        if (context == null) {
            return "";
        }
        String mac = null;
        try {
            final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (null != info && info.getMacAddress() != null) {
                    mac = info.getMacAddress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }

    /**
     * 获取mac地址
     * 可以突破android6.0的限制
     *
     * @return
     */
    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    /**
     * 获取IMSI
     *
     * @param context
     * @return
     */
    public static String getIMSI(Context context) {

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return tm.getSubscriberId();

    }

    /**
     * get sim serial number
     */
    public static String getSimSerialNum(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        return tm.getSimSerialNumber();
    }

    /**
     * 获取屏幕的分辨率
     *
     * @param context
     * @return int array with 2 items. The first item is width, and the second is height.
     */
    public static int[] getScreenResolution(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        int[] resolution = new int[2];
        resolution[0] = dm.widthPixels;
        resolution[1] = dm.heightPixels;

        return resolution;
    }

    /**
     * 获取WIFI的Mac地址
     *
     * @param context
     * @return Wifi的BSSID即mac地址
     */
    public static String getWifiBSSID(Context context) {
        if (context == null) {
            return null;
        }

        String mac = null;
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wm.getConnectionInfo();
        if (info != null) {
            mac = info.getBSSID();// 获得本机的MAC地址
        }

        return mac;
    }

    public static String getPackageVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getPackageVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获取系统休眠时间。
     *
     * @return
     */
    public static int getScreenOffTimeOut(Context context) {
        int sleepTime;
        try {
            sleepTime = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            sleepTime = 15 * 1000;
        }
        return sleepTime;
    }


    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getCPUNumCores() {
        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    //Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * 获取系统参数
     *
     * @param configName
     * @return
     */

    public static String getSystemConf(String configName) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + configName);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(ir);
            String value = input.readLine();
            input.close();
            ir.close();
            process.destroy();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取硬件版本
     *
     * @return
     */
    public static String getHardwareVersion() {
        return getSystemConf("ro.hardware");
    }

    /**
     * 获取rom版本
     */
    public static String getRomVersion() {
        return getSystemConf("ro.mediatek.version.release");
    }

    /**
     * 获取hq rom版本
     */
    private static String gethqRomVersion() {
        return getSystemConf("ro.huaqin.version.release");
    }

    public static String getShowhqRomVersion() {
        String showHq = "hq";
        String hqRomVer = gethqRomVersion();
        if (TextUtils.isEmpty(hqRomVer) == false) {
            String[] s = hqRomVer.split("_");
            if (s != null && s.length >= 3) {
                showHq = s[2];
            }
        }
        return showHq;
    }

    /**
     * 获取installed apk版本
     */
    public static PackageInfo getInstalledAppInfo(Context context, String pname) {
        try {
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
            if (packages != null) {
                for (PackageInfo pinfo : packages) {
                    if (pinfo != null && pinfo.packageName.equals(pname)) {
                        return pinfo;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}