package com.nowfloats.smartretailer.HelperClasses;

/**
 * Created by NowFloats on 13-01-2017.
 */

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.BluetoothState;

import java.util.ArrayList;
import java.util.Set;

@SuppressLint("NewApi")
public class BluetoothSPP {
    // Listener for Bluetooth Status & Connection
    private BluetoothStateListener mBluetoothStateListener = null;
    private OnDataReceivedListener mDataReceivedListener = null;
    private BluetoothConnectionListener mBluetoothConnectionListener = null;
    private AutoConnectionListener mAutoConnectionListener = null;

    // Context from activity which call this class
    private Context mContext;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothService mChatService = null;

    // Name and Address of the connected device
    private String mDeviceName = null;
    private String mDeviceAddress = null;

    private boolean isAutoConnecting = false;
    private boolean isAutoConnectionEnabled = false;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isActivityDestroying = false;
    private boolean isServiceRunning = false;

    private String keyword = "";
    private boolean isAndroid = BluetoothState.DEVICE_ANDROID;

    private BluetoothConnectionListener bcl;
    private int c = 0;

    public BluetoothSPP(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public interface BluetoothStateListener {
        public void onServiceStateChanged(int state);
    }

    public interface OnDataReceivedListener {
        public void onDataReceived(byte[] data, String message);
    }

    public interface BluetoothConnectionListener {
        public void onDeviceConnected(String name, String address);

        public void onDeviceDisconnected();

        public void onDeviceNotPaired();

        public void onDeviceConnectionFailed();

        public void onBluetoothConnected();

        public void onBluetoothDisConnected();
    }

    public interface AutoConnectionListener {
        public void onAutoConnectionStarted();

        public void onNewConnection(String name, String address);
    }

    public boolean isBluetoothAvailable() {
        try {
            if (mBluetoothAdapter == null || mBluetoothAdapter.getAddress().equals(null))
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isServiceAvailable() {
        return mChatService != null;
    }

    public boolean isAutoConnecting() {
        return isAutoConnecting;
    }

    public boolean startDiscovery() {
        return mBluetoothAdapter.startDiscovery();
    }

    public boolean isDiscovery() {
        return mBluetoothAdapter.isDiscovering();
    }

    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    public void setupService() {
        mChatService = new BluetoothService(mContext, mHandler);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public int getServiceState() {
        if (mChatService != null)
            return mChatService.getState();
        else
            return -1;
    }

    public void startService(boolean isAndroid) {
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothState.STATE_NONE) {
                isServiceRunning = true;
                mChatService.start(isAndroid);
                BluetoothSPP.this.isAndroid = isAndroid;
            }
        }
    }

    public void stopService() {
        if (mChatService != null) {
            isServiceRunning = false;
            mChatService.stop();
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mChatService != null) {
                    isServiceRunning = false;
                    mChatService.stop();
                }
            }
        }, 500);
    }

    public void setDeviceTarget(boolean isAndroid) {
        stopService();
        startService(isAndroid);
        BluetoothSPP.this.isAndroid = isAndroid;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_WRITE:
                    break;
                case BluetoothState.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf);
                    if (readBuf != null && readBuf.length > 0) {
                        if (mDataReceivedListener != null)
                            mDataReceivedListener.onDataReceived(readBuf, readMessage);
                    }
                    break;
                case BluetoothState.MESSAGE_DEVICE_NAME:
                    mDeviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
                    mDeviceAddress = msg.getData().getString(BluetoothState.DEVICE_ADDRESS);
                    if (mBluetoothConnectionListener != null)
                        mBluetoothConnectionListener.onDeviceConnected(mDeviceName, mDeviceAddress);
                    isConnected = true;
                    break;
                case BluetoothState.MESSAGE_TOAST:
                    Toast.makeText(mContext, msg.getData().getString(BluetoothState.TOAST)
                            , Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothState.MESSAGE_STATE_CHANGE:
                    if (mBluetoothStateListener != null)
                        mBluetoothStateListener.onServiceStateChanged(msg.arg1);

                    if (msg.arg1 == BluetoothState.STATE_CLOSED) {
                        if (mBluetoothConnectionListener != null)
                            mBluetoothConnectionListener.onDeviceDisconnected();
                    } else {

                        if (isConnected && msg.arg1 != BluetoothState.STATE_CONNECTED) {
                            if (mBluetoothConnectionListener != null)
                                mBluetoothConnectionListener.onDeviceDisconnected();
                            if (isAutoConnectionEnabled) {
                                isAutoConnectionEnabled = false;
                                autoConnect(keyword);
                            }
                            isConnected = false;
                            mDeviceName = null;
                            mDeviceAddress = null;
                        }

                        if (!isConnecting && msg.arg1 == BluetoothState.STATE_CONNECTING) {
                            isConnecting = true;
                        } else if (isConnecting) {
                            if (msg.arg1 != BluetoothState.STATE_CONNECTED) {
                                if (mBluetoothConnectionListener != null) {

                                    if (isActivityDestroying)
                                        mBluetoothConnectionListener.onDeviceDisconnected();
                                    else mBluetoothConnectionListener.onDeviceConnectionFailed();
                                }
                            }
                            isConnecting = false;
                        }
                    }
                    break;
            }
        }
    };

    public void stopAutoConnect() {
        isAutoConnectionEnabled = false;
    }

    public void connect(Intent data) {
        String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device);
    }

    public void connect(String address) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice bluetoothDevice = null;
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(address)) {
                bluetoothDevice = device;
                break;
            }
        }
        if (bluetoothDevice != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
            mChatService.connect(device);
        } else {
            Intent notifyIntent =
                    mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notifyIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationHelper.showNotification(mContext, mContext.getString(R.string.scanner_not_added_title),
                    mContext.getString(R.string.scanner_not_added_content), true, pendingIntent, 23);

            if (mBluetoothConnectionListener != null)
                mBluetoothConnectionListener.onDeviceNotPaired();
        }
    }

    public void disconnect() {
        stopService();
        isActivityDestroying = true;
        if (mChatService != null) {
            isServiceRunning = false;
            mChatService.stop();
            if (mChatService.getState() == BluetoothState.STATE_NONE) {
                isServiceRunning = true;
                mChatService.start(BluetoothSPP.this.isAndroid);
            }
        }
        mContext.unregisterReceiver(mReceiver);
        mBluetoothAdapter.disable();
    }

    public void setBluetoothStateListener(BluetoothStateListener listener) {
        mBluetoothStateListener = listener;
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        mDataReceivedListener = listener;
    }

    public void setBluetoothConnectionListener(BluetoothConnectionListener listener) {
        mBluetoothConnectionListener = listener;
    }

    public void setAutoConnectionListener(AutoConnectionListener listener) {
        mAutoConnectionListener = listener;
    }

    public void enable() {
        mBluetoothAdapter.enable();
    }

    public void send(byte[] data, boolean CRLF) {
        if (mChatService.getState() == BluetoothState.STATE_CONNECTED) {
            if (CRLF) {
                byte[] data2 = new byte[data.length + 2];
                for (int i = 0; i < data.length; i++)
                    data2[i] = data[i];
                data2[data2.length - 2] = 0x0A;
                data2[data2.length - 1] = 0x0D;
                mChatService.write(data2);
            } else {
                mChatService.write(data);
            }
        }
    }

    public void send(String data, boolean CRLF) {
        if (mChatService.getState() == BluetoothState.STATE_CONNECTED) {
            if (CRLF)
                data += "\r\n";
            mChatService.write(data.getBytes());
        }
    }

    public String getConnectedDeviceName() {
        return mDeviceName;
    }

    public String getConnectedDeviceAddress() {
        return mDeviceAddress;
    }

    public String[] getPairedDeviceName() {
        int c = 0;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        String[] name_list = new String[devices.size()];
        for (BluetoothDevice device : devices) {
            name_list[c] = device.getName();
            c++;
        }
        return name_list;
    }

    public String[] getPairedDeviceAddress() {
        int c = 0;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        String[] address_list = new String[devices.size()];
        for (BluetoothDevice device : devices) {
            address_list[c] = device.getAddress();
            c++;
        }
        return address_list;
    }


    public void autoConnect(String keywordName) {
        if (!isAutoConnectionEnabled) {
            keyword = keywordName;
            isAutoConnectionEnabled = true;
            isAutoConnecting = true;
            if (mAutoConnectionListener != null)
                mAutoConnectionListener.onAutoConnectionStarted();
            final ArrayList<String> arr_filter_address = new ArrayList<String>();
            final ArrayList<String> arr_filter_name = new ArrayList<String>();
            String[] arr_name = getPairedDeviceName();
            String[] arr_address = getPairedDeviceAddress();
            for (int i = 0; i < arr_name.length; i++) {
                if (arr_name[i].contains(keywordName)) {
                    arr_filter_address.add(arr_address[i]);
                    arr_filter_name.add(arr_name[i]);
                }
            }

            bcl = new BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    bcl = null;
                    isAutoConnecting = false;
                }

                public void onDeviceDisconnected() {
                }

                @Override
                public void onDeviceNotPaired() {

                }

                public void onDeviceConnectionFailed() {
                    Log.e("CHeck", "Failed");
                    if (isServiceRunning) {
                        if (isAutoConnectionEnabled) {
                            c++;
                            if (c >= arr_filter_address.size())
                                c = 0;
                            connect(arr_filter_address.get(c));
                            Log.e("CHeck", "Connect");
                            if (mAutoConnectionListener != null)
                                mAutoConnectionListener.onNewConnection(arr_filter_name.get(c)
                                        , arr_filter_address.get(c));
                        } else {
                            bcl = null;
                            isAutoConnecting = false;
                        }
                    }
                }

                @Override
                public void onBluetoothConnected() {

                }

                @Override
                public void onBluetoothDisConnected() {

                }
            };

            setBluetoothConnectionListener(bcl);
            c = 0;
            if (mAutoConnectionListener != null)
                mAutoConnectionListener.onNewConnection(arr_name[c], arr_address[c]);
            if (arr_filter_address.size() > 0)
                connect(arr_filter_address.get(c));
            else
                Toast.makeText(mContext, "Device name mismatch", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (mBluetoothConnectionListener != null)
                            mBluetoothConnectionListener.onBluetoothDisConnected();
                        //Indicates the local Bluetooth adapter is off.
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        if (mBluetoothConnectionListener != null)
                            mBluetoothConnectionListener.onBluetoothConnected();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                        break;
                }
            }
        }
    };
}
