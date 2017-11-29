package melo.com.androidsocket.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by melo on 2017/9/23.
 */

public class WifiUtil {

    private static final String TAG = "LocationUtils";

    private static volatile WifiUtil instance = null;

    private WifiManager mWifiManager;

    private Context mContext;

    private WifiUtil(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (WifiUtil.class) {
                if (instance == null) {
                    instance = new WifiUtil(context);
                }
            }
        }
        return instance;
    }

    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);

        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getLocalIPAddress() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return intToIp(wifiInfo.getIpAddress());
    }

    public String getServerIPAddress() {
        DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();
        return intToIp(mDhcpInfo.gateway);
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + ((i >> 24) & 0xFF);
    }

    /**
     * @return 优先获取网卡地址
     */
    public static String getBroadcastAddress() {
        String broadcast = getBroadcastAddress("p2p");
        if (broadcast == null) {
            return getBroadcastAddress("wlan0");
        }
        return broadcast;
    }

    /**
     * @param netCardName 网卡名称
     * @return 获取的广播地址
     */
    public static String getBroadcastAddress(String netCardName) {
        try {
            Enumeration<NetworkInterface> eni = NetworkInterface
                    .getNetworkInterfaces();
            while (eni.hasMoreElements()) {
                NetworkInterface networkCard = eni.nextElement();
                if (networkCard.getDisplayName().startsWith(netCardName)) {
                    List<InterfaceAddress> ncAddrList = networkCard
                            .getInterfaceAddresses();
                    Iterator<InterfaceAddress> ncAddrIterator = ncAddrList.iterator();
                    while (ncAddrIterator.hasNext()) {
                        InterfaceAddress networkCardAddress = ncAddrIterator.next();
                        InetAddress address = networkCardAddress.getAddress();
                        if (!address.isLoopbackAddress()) {
                            String hostAddress = address.getHostAddress();
                            if (hostAddress.indexOf(":") > 0) {
                                // case : ipv6
                                continue;
                            } else {
                                // case : ipv4
                                String broadcastAddress = networkCardAddress.getBroadcast().getHostAddress();
                                return broadcastAddress;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
