package melo.com.androidsocket.socket.tcp;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import melo.com.androidsocket.common.Config;
import melo.com.androidsocket.listener.OnConnectionStateListener;
import melo.com.androidsocket.utils.HeartbeatTimer;

/**
 * Created by melo on 2017/11/28.
 */

public class TCPSocket {

    private static final String TAG = "TCPSocket";

    private Context mContext;
    private ExecutorService mThreadPool;
    private Socket mSocket;
    private BufferedReader br;
    private PrintWriter pw;
    private HeartbeatTimer timer;
    private long lastReceiveTime = 0;

    private OnConnectionStateListener mListener;

    private static final long TIME_OUT = 15 * 1000;
    private static final long HEARTBEAT_MESSAGE_DURATION = 2 * 1000;


    public TCPSocket(Context context) {
        this.mContext = context;

        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * Config.POOL_SIZE);
        // 记录创建对象时的时间
        lastReceiveTime = System.currentTimeMillis();
    }

    public void startTcpSocket(final String ip, final String port) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (startTcpConnection(ip, Integer.valueOf(port))) {// 尝试建立 TCP 连接
                    if (mListener != null) {
                        mListener.onSuccess();
                    }
                    startReceiveTcpThread();
                    startHeartbeatTimer();
                } else {
                    if (mListener != null) {
                        mListener.onFailed(Config.ErrorCode.CREATE_TCP_ERROR);
                    }
                }
            }
        });
    }

    public void setOnConnectionStateListener(OnConnectionStateListener listener) {
        this.mListener = listener;
    }

    /**
     * 创建接收线程
     */
    private void startReceiveTcpThread() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String line = "";
                try {
                    while ((line = br.readLine()) != null) {
                        handleReceiveTcpMessage(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 处理 tcp 收到的消息
     *
     * @param line
     */
    private void handleReceiveTcpMessage(String line) {
        Log.d(TAG, "接收 tcp 消息：" + line);
        lastReceiveTime = System.currentTimeMillis();
    }

    private void sendTcpMessage(String json) {
        pw.println(json);
        Log.d(TAG, "tcp 消息发送成功...");
    }

    /**
     * 启动心跳
     */
    private void startHeartbeatTimer() {
        if (timer == null) {
            timer = new HeartbeatTimer();
        }
        timer.setOnScheduleListener(new HeartbeatTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                Log.d(TAG, "timer is onSchedule...");
                long duration = System.currentTimeMillis() - lastReceiveTime;
                Log.d(TAG, "duration:" + duration);
                if (duration > TIME_OUT) {//若超过十五秒都没收到我的心跳包，则认为对方不在线。
                    Log.d(TAG, "tcp ping 超时，对方已经下线");
                    stopTcpConnection();
                    if (mListener != null) {
                        mListener.onFailed(Config.ErrorCode.PING_TCP_TIMEOUT);
                    }
                } else if (duration > HEARTBEAT_MESSAGE_DURATION) {//若超过两秒他没收到我的心跳包，则重新发一个。
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Config.MSG, Config.PING);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendTcpMessage(jsonObject.toString());
                }
            }

        });
        timer.startTimer(0, 1000 * 2);
    }

    public void stopHeartbeatTimer() {
        if (timer != null) {
            timer.exit();
            timer = null;
        }
    }

    /**
     * 尝试建立tcp连接
     *
     * @param ip
     * @param port
     */
    private boolean startTcpConnection(final String ip, final int port) {
        try {
            if (mSocket == null) {
                mSocket = new Socket(ip, port);
                mSocket.setKeepAlive(true);
                mSocket.setTcpNoDelay(true);
                mSocket.setReuseAddress(true);
            }
            InputStream is = mSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = mSocket.getOutputStream();
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
            Log.d(TAG, "tcp 创建成功...");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void stopTcpConnection() {
        try {
            stopHeartbeatTimer();
            if (br != null) {
                br.close();
            }
            if (pw != null) {
                pw.close();
            }
            if (mThreadPool != null) {
                mThreadPool.shutdown();
                mThreadPool = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
