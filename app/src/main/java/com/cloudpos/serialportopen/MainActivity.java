package com.cloudpos.serialportopen;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.serialport.SerialPortDevice;
import com.cloudpos.serialport.SerialPortOperationResult;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public boolean isOpened = false;
    private static final String TAG = "SerialPortOpenDemo";

    private Button openBtn, closeBtn, writeBtn;
    private TextView textView, tv_clr, tv_log;
    private RadioGroup portModeRG, dataModeRG;
    private EditText et_send, et_baudrate;
    private Spinner baudrateSpinner, bytelenghtSpinner;
    private CheckBox cb_flowcontrol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_clr = (TextView) findViewById(R.id.tv_clr);
        tv_log = (TextView) findViewById(R.id.tv_log);
        tv_log.setText("" + getPkgName(this));
        openBtn = (Button) findViewById(R.id.open);
        closeBtn = (Button) findViewById(R.id.close);
        writeBtn = (Button) findViewById(R.id.write);
        portModeRG = (RadioGroup) findViewById(R.id.port_mode);
        dataModeRG = (RadioGroup) findViewById(R.id.data_mode);
        et_send = (EditText) findViewById(R.id.et_send);
        et_baudrate = (EditText) findViewById(R.id.et_baudrate);
        baudrateSpinner = (Spinner) findViewById(R.id.sp_badrate);
        bytelenghtSpinner = (Spinner) findViewById(R.id.sp_sendbyte);
        cb_flowcontrol = (CheckBox) findViewById(R.id.cb_flowcontrol);


        baudrateSpinner.setOnItemSelectedListener(new BaudSpinnerSelectedListener());
        baudrateSpinner.setSelection(5);
        bytelenghtSpinner.setOnItemSelectedListener(new ByteSpinnerSelectedListener());
        bytelenghtSpinner.setSelection(2);
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
        tv_clr.setOnClickListener(this);
    }

    class BaudSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String[] baudrates = getResources().getStringArray(R.array.baudrates);
            Logger.debug("m[baudrateSpinner] = " + baudrates[position]);
            et_baudrate.setText(baudrates[position]);
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private int byteCNT = 0;

    class ByteSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String[] bytelength = getResources().getStringArray(R.array.bytelength);
            Logger.debug("m[bytelenghtSpinner] = " + bytelength[position]);
            byteCNT = Integer.parseInt(bytelength[position]);
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    Thread thread;
    private boolean run;

    private void threadRead() {
        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (run) {
                        read();
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        } else {
            sendMsg(3, "read thread already run!!");
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(!msg.obj.toString().isEmpty()){
                        LogHelper.appendBlackMsg(msg.obj.toString(), textView);
                    }
                    break;
                case 2:
                    writeBtn.setEnabled(true);
                    closeBtn.setEnabled(true);
                    openBtn.setEnabled(false);
                    cb_flowcontrol.setEnabled(false);
                    baudrateSpinner.setEnabled(false);
                    bytelenghtSpinner.setEnabled(false);
                    disableRadioGroup(portModeRG);
                    disableRadioGroup(dataModeRG);
                    break;
                case 3:
                    tv_log.setText(msg.obj.toString());
                    break;
                case 4:
                    run = false;
                    openBtn.setEnabled(true);
                    enableRadioGroup(portModeRG);
                    enableRadioGroup(dataModeRG);
                    writeBtn.setEnabled(false);
                    closeBtn.setEnabled(false);
                    cb_flowcontrol.setEnabled(true);
                    baudrateSpinner.setEnabled(true);
                    bytelenghtSpinner.setEnabled(true);
                    break;
                case 5:
//                    read();
                    run = true;
                    threadRead();
//                    handler.sendEmptyMessageDelayed(5, 100L);
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            super.run();
//                            RadioButton rb_send = (RadioButton) findViewById(R.id.rb_send);
//                            if (rb_send.isChecked()) {
//                                //intiRead();
//                                send(byteCNT);
//                            } else {
//                                echo();
//                            }
//                        }
//                    }.start();
                    break;
                case 6:
                    if(msg.obj.toString().isEmpty()){

                    } else {
                        LogHelper.infoAppendMsg(msg.obj.toString(), textView);
                    }
                    break;
                case 7:
                    et_baudrate.setEnabled(false);
                    et_send.setEnabled(false);
                    et_send.setHint("cdc:CMD is get PinPad SN");
                    break;
            }
        }

    };

    private void intiRead() {
        byte[] arryData = new byte[256];
        int result = 0;
        while (true) {
            int read = 0;
            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(arryData.length, 1000);
                //todo
            } catch (DeviceException e) {
                e.printStackTrace();
            }
            if (read > 0) {
                //
            }
        }
    }

    SerialPortDevice device;

    private void open(int mode) {
        if (isOpened) {
            Log.d(TAG, "The serial port is already open");
            sendMsg(3, "already open");
        } else {
            boolean checked = cb_flowcontrol.isChecked();

            if (device == null) {
                device = (SerialPortDevice) POSTerminal.getInstance(this).getDevice("cloudpos.device.serialport");
            }
            int result = 0;//getModelName(mode)
            try {
                device.open(mode);
            } catch (DeviceException e) {
                sendMsg(3, "open failed");
                e.printStackTrace();
                return;
            }
            int baudrtate = 0;
            try {
                int flowMode = -999;
                if (checked) {
                    device.changeFlowControlMode(SerialPortDevice.FLOWCONTROL_RTSCTS_IN_OUT);
                } else {
                    flowMode = device.getFlowControlMode();
                }
                Logger.debug("SerialPort flowMode = " + flowMode);
                if (et_baudrate.getText().toString().isEmpty()) {
                    baudrtate = 115200;
                } else {
                    baudrtate = Integer.parseInt(et_baudrate.getText().toString());
                }
                device.changeSerialPortParams(baudrtate, 3, 0, 0);
                sendMsg(2, mode);
                sendMsg(3, "open success,baudrtate = " + baudrtate);
                isOpened = true;
                if (mode == 3) {
                    sendMsg(7, null);
                } else {
                    handler.sendEmptyMessageDelayed(5, 1L);
                }
            } catch (DeviceException e) {
                sendMsg(3, "setBaudrate failed, baudrtate = "+baudrtate);
                e.printStackTrace();
            }
        }
    }

    private void sendMsg(int code, Object msg) {
        handler.obtainMessage(code, msg).sendToTarget();
    }

    private void close() {
        try {
            handler.removeMessages(5);
            device.close();
            isOpened = false;
            sendMsg(3, "close success");
        } catch (DeviceException e) {
            Log.d(TAG, "SerialPort close result < 0");
//            sendMsg(3, "close failed");
            e.printStackTrace();
        }
    }

    int timeout = 1000;

    private void read() {
        byte[] arryData = new byte[256];
        try {
            SerialPortOperationResult serialPortOperationResult = device.waitForRead(arryData.length, timeout);
            byte[] data = serialPortOperationResult.getData();
            int dataLength = serialPortOperationResult.getDataLength();
            arryData = subByteArray(data, dataLength);
            for (byte b : arryData) {
                Logger.debug("arryData = " + b);
            }
            sendMsg(1, new String(arryData));
//            write(arryData);
//            write(null);
        } catch (DeviceException e) {
//            sendMsg(3, "read failed");
            e.printStackTrace();
        }
    }

    String int2hex(int intValue) {
        if (intValue < 10) {
            return "0" + Integer.toHexString(intValue);
        }
        return Integer.toHexString(intValue);
    }

    private void readCBC() {
        while (true) {
            byte[] arryData = new byte[1];
            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(arryData.length, timeout);
                byte[] data = serialPortOperationResult.getData();
                int dataLength = serialPortOperationResult.getDataLength();
                arryData = subByteArray(data, dataLength);
                if (arryData[0] == 0x02) {
//                    sendMsg(6, "  " + int2hex(arryData[0]) + "\n");
                    break;
                }
            } catch (DeviceException e) {
                sendMsg(3, "read failed");
                e.printStackTrace();
            }
        }

        int length;
        while (true) {
            byte[] arryData = new byte[2];
            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(arryData.length, timeout);
                byte[] data = serialPortOperationResult.getData();
                int dataLength = serialPortOperationResult.getDataLength();
                arryData = subByteArray(data, dataLength);
//                sendMsg(6, "  " + int2hex(arryData[0]));
//                sendMsg(6, "  " + int2hex(arryData[1]));
                int length1 = arryData[0];
                int length2 = arryData[1];
                length = length1 + length2;
                break;
            } catch (DeviceException e) {
                sendMsg(3, "read failed");
                e.printStackTrace();
            }
        }

        while (true) {

            byte[] arryData = new byte[length + 1];
            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(arryData.length, timeout);
                byte[] data = serialPortOperationResult.getData();
                int dataLength = serialPortOperationResult.getDataLength();
                sendMsg(1, "SN: ");

                for (int i = 5; i < data.length - 2; i++) {
//                    if (i % 5 == 0) {
//                      sendMsg(6, "\n");
//                    }
                    sendMsg(6, "  " + int2hex(data[i]).substring(1, 2));
                    Logger.debug("obtainMessage" + data[i]);
                }
                sendMsg(1, "\n");
                break;

            } catch (DeviceException e) {
                sendMsg(3, "read failed");
                e.printStackTrace();
            }

        }
    }

    private void write(byte[] arryData) {
        byte[] bytes = {
                0x02,//STX
                0x00, 0x03,//LENGTH
                0x00, 0x23,//CMD

/*
                //Function parameters +++
                0x02, 0x00, 0x00, 0x00,//ROW
                0x00, 0x00, 0x00, 0x00,//COLUMN
                0x00, 0x0A,//String length
                0x50, 0x72, 0x6F, 0x63, 0x65, 0x73, 0x73, 0x69, 0x6E, 0x67,//String
                0x00,//Attribute
                //Function parameters ---
*/

                0x20,//XOR

                0x03,//ETX
        };
        int result = -999;
        RadioButton cbc = (RadioButton) findViewById(R.id.rb_usb_cdc);
        if (cbc.isChecked()) {
            try {
                device.write(bytes, 0, bytes.length);
            } catch (DeviceException e) {
                sendMsg(3, "write failed");
                e.printStackTrace();
            }
        } else if (arryData != null) {
            try {
                device.write(arryData, 0, bytes.length);
            } catch (DeviceException e) {
                sendMsg(3, "write failed");
                e.printStackTrace();
            }
        } else {
            String editVal = et_send.getText().toString();
            if (editVal.isEmpty()) {
                sendMsg(3, "please input content!");
                return;
            }
            try {
                device.write(editVal.getBytes(), 0, editVal.getBytes().length);
            } catch (DeviceException e) {
                sendMsg(3, "write failed");
                e.printStackTrace();
            }
        }
        sendMsg(3, "write success");
        if (cbc.isChecked()) {
            readCBC();
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpened) {
            handler.removeMessages(5);
            close();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open:
                sendMsg(3, "");
                if (portModeRG.getCheckedRadioButtonId() == R.id.rb_usb_slave_serial) {
                    open(0);
                } else if (portModeRG.getCheckedRadioButtonId() == R.id.rb_usb_serial) {
                    open(1);
                } else if (portModeRG.getCheckedRadioButtonId() == R.id.rb_usb_cdc) {
                    open(3);
                }
                break;
            case R.id.close:
                sendMsg(4, null);
                close();
                break;
            case R.id.write:
                write(null);
                break;
            case R.id.tv_clr:
                textView.setText("");
                break;
        }
    }


    private byte[] tempBytes = new byte[40960];


    private String getSystemPropertie(String key) {
        Object bootloaderVersion = null;
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Log.i("systemProperties", systemProperties.toString());
            bootloaderVersion = systemProperties.getMethod("get", String.class, String.class).invoke(systemProperties, key, "");
            Log.i("bootloaderVersion", bootloaderVersion.getClass().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bootloaderVersion.toString();
    }

    public byte[] subByteArray(byte[] byteArray, int length) {
        byte[] arrySub = new byte[length];

        for (int i = 0; i < length; ++i) {
            arrySub[i] = byteArray[i];
        }
        return arrySub;
    }


    private void send(final int byteLength) {
//        byte[] arryData = new byte[1024];
        tempBytes = genarateBytes(byteLength);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    device.write(tempBytes, 0, byteLength);
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        int result = 0;
        int cnt_read;
        int tot_read = 0;
        byte[] readArryData = new byte[byteLength];
        long startTime = SystemClock.currentThreadTimeMillis();

        while (isOpened && tot_read != byteLength) {
            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(1, 100);
                int dataLength = serialPortOperationResult.getDataLength();

                if (dataLength > 0) {
                    tot_read += dataLength;
                    if (readArryData[0] != tempBytes[tot_read - 1]) {
                        result = 1;
                        break;
                    }
                } else {
                    result = 2;
                    break;
                }
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
        if (result == 0 && tot_read != byteLength) {
            result = 3;
        }
        long endTime = SystemClock.currentThreadTimeMillis();
        sendMsg(3, String.format("test speed tot: %d, result:%d ,seconds: %.3f", tot_read, result, (endTime - startTime) / 1000.0));
        closeBtn.callOnClick();
    }

    private byte[] genarateBytes(int byteCNT) {
        tempBytes = new byte[40960];
        byte startNumber = 48;
        for (int i = 0; i < byteCNT; i++) {
            tempBytes[i] = startNumber++;
            if (startNumber > 48 + 64) {
                startNumber = 48;
            }
        }
        return tempBytes;
    }

    private void echo() {
        int total_read = 0, cnt_read, cnt_write, err_read = 0, err_write = 0;
        byte[] arryData = new byte[1024];
        while (isOpened) {


            try {
                SerialPortOperationResult serialPortOperationResult = device.waitForRead(1, 100);
                int dataLength = serialPortOperationResult.getDataLength();
                if (dataLength > 0) {
                    total_read += dataLength;
                    try {
                        device.write(arryData, 0, dataLength);
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
            } catch (DeviceException e) {
                e.printStackTrace();
            }


        }
        sendMsg(3, String.format("echo tot: %d, err_r:%d, err_w: %d", total_read, err_read, err_write));
    }

    /**
     * 启用RadioGroup
     *
     * @param radioGroup
     */
    public void enableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(true);
        }
    }

    /**
     * 禁用RadioGroup
     *
     * @param radioGroup
     */
    public void disableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }
    }


    public String getPkgName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
}
