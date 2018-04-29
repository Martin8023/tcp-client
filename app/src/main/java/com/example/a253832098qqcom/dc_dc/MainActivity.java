package com.example.a253832098qqcom.dc_dc;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private String defaultip = "192.168.1.244";
    private int defaultport = 2333;
    private Button btsetvoltage,btconnect,btwificonnect;
    private TextView tvvoltage,tvcurrent;
    private EditText edvoltage,edtcpip,edtcpport,edwifissid,edwifipswd;
    private MyBtnClicker myBtnClicker = new MyBtnClicker();
    private TcpClient tcpClient = new TcpClient(defaultip,defaultport);
    private boolean btconnectflage=false;
    private class MyBtnClicker implements View.OnClickListener{
        private float fl = 0.00001f;
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_set_voltage:
                    if(!btconnectflage){
                        Toast.makeText(MainActivity.this,"please connect tcp!",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        new Thread(new Runnable(){
                            @Override
                            public void run() {
                                tcpClient.send("V"+(Float.parseFloat(edvoltage.getText().toString())+fl));
                            }
                        }).start();
                    }
                    break;
                case R.id.bt_connect_tcp:
                    if(btconnectflage){
                        btconnectflage=false;
                        btconnect.setText("连接");
                        tcpClient.tcpunconnect();
                    }
                    else {
                        btconnectflage = true;
                        btconnect.setText("断开");
                        tcpClient.setagrv(edtcpip.getText().toString(),Integer.parseInt(edtcpport.getText().toString()));
                        new Thread(tcpClient).start();
                    }
                    break;
                case R.id.bt_wifi_connect:
                    if(btconnectflage) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                tcpClient.send("AT+CWJAP=" + "\"" + edwifissid.getText().toString() + "\"" +","
                                        + "\"" +edwifipswd.getText().toString()+ "\"");
                            }
                        }).start();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"please connect tcp!",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindID();
    }

    private void bindID(){
        btsetvoltage = findViewById(R.id.bt_set_voltage);
        btsetvoltage.setOnClickListener(myBtnClicker);
        btconnect = findViewById(R.id.bt_connect_tcp);
        btconnect.setOnClickListener(myBtnClicker);
        btwificonnect = findViewById(R.id.bt_wifi_connect);
        btwificonnect.setOnClickListener(myBtnClicker);
        tvvoltage = findViewById(R.id.tv_voltage_value);
        tvcurrent = findViewById(R.id.tv_current_value);
        edvoltage = findViewById(R.id.ed_voltage);
        edtcpip = findViewById(R.id.ed_ip);
        edtcpport = findViewById(R.id.ed_port);
        edwifipswd = findViewById(R.id.ed_wifi_pswd);
        edwifissid = findViewById(R.id.ed_wifi_ssid);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    String[] getstring  = (msg.obj.toString()).split("-");
                    tvvoltage.setText(getstring[0]);
                    tvcurrent.setText(getstring[1]);
                    break;
            }
        }
    };

    public class TcpClient implements Runnable{
        String serverIP=defaultip;
        int serverPort=defaultport;
        boolean isrun=false;
        private DataInputStream dis;
        private PrintWriter pw;

        public TcpClient(String ip,int port){
            this.serverIP=ip;
            this.serverPort=port;
        }

        public void tcpunconnect(){
            isrun=false;
        }

        public void send (String msg){
            pw.println(msg);
            pw.flush();
        }

        public void setagrv(String ip,int port){
            serverIP=ip;
            serverPort=port;
        }

        @Override
        public void run() {
            int rcvLen;
            byte buff[] = new byte[4096];
            String rcvMsg;
            InputStream is;
            try{
                Socket socket = new Socket(serverIP,serverPort);
                socket.setSoTimeout(5000);
                isrun=true;
                pw = new PrintWriter(socket.getOutputStream(),true);
                is = socket.getInputStream();
                dis = new DataInputStream(is);

            }catch (Exception e){
                btconnectflage=false;
                e.printStackTrace();
            }
            while (isrun)
            {
                try{
                    rcvLen = dis.read(buff);
                    rcvMsg = new String(buff,0 ,rcvLen,"utf-8");
                    Message message = Message.obtain();
                    message.what=1;
                    message.obj=rcvMsg;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
