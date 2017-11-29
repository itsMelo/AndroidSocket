package melo.com.androidsocket.socket;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import melo.com.androidsocket.common.Config;
import melo.com.androidsocket.listener.OnConnectionStateListener;
import melo.com.androidsocket.listener.OnMessageReceiveListener;
import melo.com.androidsocket.socket.tcp.TCPSocket;
import melo.com.androidsocket.socket.udp.UDPSocket;

/**
 * Created by melo on 2017/11/27.
 */

public class SocketManager {

    private static volatile SocketManager instance = null;
    private UDPSocket udpSocket;
    private TCPSocket tcpSocket;
    private Context mContext;

    private SocketManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static SocketManager getInstance(Context context) {
        // if already inited, no need to get lock everytime
        if (instance == null) {
            synchronized (SocketManager.class) {
                if (instance == null) {
                    instance = new SocketManager(context);
                }
            }
        }

        return instance;
    }

    public void startUdpConnection() {
        if (udpSocket == null) {
            udpSocket = new UDPSocket(mContext);
        }

        // 注册接收消息的接口
        udpSocket.addOnMessageReceiveListener(new OnMessageReceiveListener() {
            @Override
            public void onMessageReceived(String message) {
                handleUdpMessage(message);
            }
        });

        udpSocket.startUDPSocket();

    }

    /**
     * 处理 udp 收到的消息
     *
     * @param message
     */
    private void handleUdpMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String ip = jsonObject.optString(Config.TCP_IP);
            String port = jsonObject.optString(Config.TCP_PORT);
            if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                startTcpConnection(ip, port);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始 TCP 连接
     *
     * @param ip
     * @param port
     */
    private void startTcpConnection(String ip, String port) {
        if (tcpSocket == null) {// 保证收到消息后，只创建一次
            tcpSocket = new TCPSocket(mContext);
            tcpSocket.startTcpSocket(ip, port);

            tcpSocket.setOnConnectionStateListener(new OnConnectionStateListener() {
                @Override
                public void onSuccess() {// tcp 创建成功
                    udpSocket.stopHeartbeatTimer();
                }

                @Override
                public void onFailed(int errorCode) {// tcp 异常处理
                    switch (errorCode) {
                        case Config.ErrorCode.CREATE_TCP_ERROR:
                            break;
                        case Config.ErrorCode.PING_TCP_TIMEOUT:
                            udpSocket.startHeartbeatTimer();
                            tcpSocket = null;
                            break;
                    }
                }
            });
        }

    }

}
