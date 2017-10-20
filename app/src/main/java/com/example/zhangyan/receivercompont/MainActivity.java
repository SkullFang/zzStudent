package com.example.zhangyan.receivercompont;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView img01;
    private ImageView img02;
    private ImageView img03;
    private ImageView img04;

    LockScreen lockScreen;
    Button lock;

    private Button bt_stop;
    private Button bt_send;
    /**
     * 表示要传到上一层的消息（实际上是上层类查看这个属性）
     */
    public String messageToUp = "";

    private int PORT = 9997;
    //定义了发送方发送数据包的编号最大值
    private int SEQMAX = 10;
    private int ImageBlockNumber = 4;//把图像分块：垂直方向分成ImageBlockNumber块
    private static Bitmap[] subImages = null;//这个是把bufferedImage分成份后存放
    //注意Robot这个类的使用，很重要的，这里可以控制鼠标运动和截图
    private int subHeight = 0;
    private int imageHeight = 800;//默认接收到的图片的高度为800
    private int imageWidth = 1280;//默认接收到的图片的宽度为1280
    private int nowseq = -1;//当前接收到的图片编号
    private InetAddress multicastIA = null;//多播用的D类IP地址
    private boolean toend = false;
    private boolean isReceiving = false;//表示当前是不是正在接收服务端的数据
    private boolean num1=false;
    private boolean num2=false;
    private boolean num3=false;
    private boolean num4=false;
    private static Bitmap[] BufferImage=null;
    private static boolean[] ss;
    private Thread th[]=new Thread[1];
    private static boolean stoppp=false;
    private String ipname;
    // 锁定屏幕

    //处理UI的handle
    private Handler handler= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("msg", String.valueOf(msg.what));
            if(msg.what==1){
                num1=true;

            }else if(msg.what==2){
                num2=true;

            }else if(msg.what==3){
                num3=true;

            }else if(msg.what==4){
                num4=true;

            }

            if (num1&&num2&&num3&&num4){

                img01.setImageBitmap(subImages[0]);
                img02.setImageBitmap(subImages[1]);
                img03.setImageBitmap(subImages[2]);
                img04.setImageBitmap(subImages[3]);

                num1=false;num2=false;num3=false;num4=false;

            }
//            switch (msg.what) {
//                case 1:
//                    img01.setImageBitmap(subImages[0]);
//                    break;
//                case 2:
//                    img02.setImageBitmap(subImages[1]);
//                    break;
//                case 3:
//                    img03.setImageBitmap(subImages[2]);
//                    break;
//                case 4:
//                    img04.setImageBitmap(subImages[3]);
//                    break;
//            }

        }
    };
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        new linkThread().start();//链接线程启动
//        sendFun();
        initView();
        initReceive();
        autolink();


//        screen();
//        closeBar();
//        linkPc();

    }
    private void autolink(){
        lockScreen=new LockScreen(this);
        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        ipname=data.getString("ipname");

        Log.i("ip",ipname);
        new ServerClient(ipname).start();//链接线程启动
    }
    //    private void linkPc(){
//        lockScreen=new LockScreen(this);
//        Button linkbtn=(Button)findViewById(R.id.link);
//        new linkThread().start();//链接线程启动
//        linkbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new linkThread().start();//链接线程启动
//            }
//        });
//    }
    private void closeBar() {
        try {
            String command;
            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = envlist.toArray(new String[0]);
            Process proc = Runtime.getRuntime().exec(
                    new String[] { "su", "-c", command }, envp);
            proc.waitFor();
        } catch (Exception ex) {
            // Toast.makeText(getApplicationContext(), ex.getMessage(),
            // Toast.LENGTH_LONG).show();
        }
    }
    public static void showBar() {
        try {
            String command;
            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = envlist.toArray(new String[0]);
            Process proc = Runtime.getRuntime().exec(
                    new String[] { "su", "-c", command }, envp);
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void screen(){
//        lockScreen=new LockScreen(this);
//        lock=(Button)findViewById(R.id.lock);
//        lock.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//    }


    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    /*
    接受心跳保持长链接
//     */
//    class reBreak extends Thread{
//        Socket socket;
//        reBreak(Socket socket){
//            this.socket=socket;
//        }
//        @Override
//        public void run() {
//            try {
//                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//                String str = null;
//                while ((str = br.readLine()) != null) {
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

    /*
    自动重连
     */
//    class autoSendBreak extends Thread{
//        Socket socket=null;
//        boolean isConnect=true;
//        String ipp=null;
//        autoSendBreak(Socket socket,String ipp){
//            this.socket=socket;
//            this.ipp=ipp;
//        }
//
//        @Override
//        public void run() {
//            while (isConnect) {
//                try {
//                    socket.sendUrgentData(0xFF);
//                } catch (IOException e) {
//                    isConnect=false;
//                    linkThread.interrupted();
//                    new linkThread(ipp).start();
//
//                    e.printStackTrace();
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
    public class ServerClient extends  Thread{
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        BufferedReader line;
        boolean running = true;
        private String IP=null;
        private int PORT;
        private void sendMessageH(String strInput) {
            if(strInput!=null){
            out.println(strInput);
            System.out.println("CLIENT PUT INFO: " + strInput);
            if ("exit".equals(strInput)) {
                running = false;
            }
            }
        }
        ServerClient(String ip){
            this.IP=ip;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(IP, 12370);
                new autoSendBreak(socket,IP).start();
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                line = new BufferedReader(new InputStreamReader(System.in));
                new ServerClient.ServerListener().start();

                while (running) {
                    sendMessageH(line.readLine());

                }

                line.close();
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class ServerListener extends Thread {
            private Socket sc;
            private boolean flagstart=false;
            private boolean flagend=false;
            private boolean flagscreen=false;
            private String tmp=null;
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (running) {
                    try {
                        if (in != null && in.ready()&& (tmp=in.readLine())!=null) {
                            String read = tmp;
                            if (!"".equals(read)) {
                                System.out.println("CLIENT GET: " + read);
                                final String finalStr = read;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("测试",finalStr);
                                        Log.i("本机IP",getHostIP());
                                        if (finalStr.equals("1")&&!flagstart) {
                                            lockScreen.show(true);
                                            closeBar();
                                            flagstart=true;
                                            flagend=false;
                                        }
                                        else if (finalStr.equals("2")&&!flagend) {
                                            lockScreen.show(false);
                                            showBar();
                                            flagend=true;
                                            flagstart=false;
                                        }
                                        else if (finalStr.equals(getHostIP())&&!flagscreen){
                                            Log.i("发送","启动");
                                            sendd(1);
                                            flagscreen=true;

                                        }
                                        else if(finalStr.equals("3")&&flagscreen){
                                            sendd(2);
                                            flagscreen=false;
                                        }

                                    }
                                });
                            }
                        }
                        sleep(100L);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        System.out.println("CLIENT EXCPETION: " + e.getMessage());
                        running = false;
                        break;
                    }
                }
                super.run();
            }
        }
        /*
        长链接+自动重连
         */
        class autoSendBreak extends Thread{
            Socket socket=null;
            boolean isConnect=true;
            String ipp=null;
            autoSendBreak(Socket socket,String ipp){
                this.socket=socket;
                this.ipp=ipp;
            }

            @Override
            public void run() {
                while (isConnect) {
                    try {
                        String test = "\n";
                        socket.getOutputStream().write(test.getBytes());
//                    socket.sendUrgentData(0xFF);
                    } catch (IOException e) {
                        isConnect=false;
                        ServerClient.interrupted();
                        ServerListener.interrupted();
                        new ServerClient(ipp).start();

                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }
        }
    }


//    class linkThread extends Thread{
//        private Socket sc;
//        private boolean flagstart=false;
//        private boolean flagend=false;
//        private boolean flagscreen=false;
//        private String ipp;
//        linkThread(String ipp){
//            this.ipp=ipp;
//            Log.i("ipp",ipp);
//        }
//        @Override
//        public void run() {
//            //初始化一个socket
//            try {
//                final InetAddress addr=InetAddress.getLocalHost();
//                sc = new Socket(ipp, 12370);
//                new autoSendBreak(sc,ipp).start();
////                save.getSocket(sc);
//                BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream(), "UTF-8"));
//                String str = null;
//                while ((str = br.readLine()) != null) {
//                    final String finalStr = str;
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i("测试",finalStr);
//                            Log.i("本机IP",getHostIP());
//                            if (finalStr.equals("1")&&!flagstart) {
//                                lockScreen.show(true);
//                                closeBar();
//                                flagstart=true;
//                                flagend=false;
//                            }
//                            else if (finalStr.equals("2")&&!flagend) {
//                                lockScreen.show(false);
//                                showBar();
//                                flagend=true;
//                                flagstart=false;
//                            }
//                            else if (finalStr.equals(getHostIP())&&!flagscreen){
//                                Log.i("发送","启动");
//                                sendd(1);
//                                flagscreen=true;
//
//                            }
//                            else if(finalStr.equals("3")&&flagscreen){
//                                sendd(2);
//                                flagscreen=false;
//                            }
//
//                        }
//                    });
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void initReceive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                toend = false;
                for (int i = 0; i < ImageBlockNumber; i++) {
                    try {
                        Receiver receiver = new Receiver(i);
                        receiver.start();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initView() {
        img01 = (ImageView) findViewById(R.id.img01);
        img02 = (ImageView) findViewById(R.id.img02);
        img03 = (ImageView) findViewById(R.id.img03);
        img04 = (ImageView) findViewById(R.id.img04);
//        bt_stop = (Button) findViewById(R.id.bt_stop);
//        bt_stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(view.getId()==R.id.bt_stop){
//                    toend=true;
//                }
////                sendFun();
//            }
//        });
        try {
            multicastIA = InetAddress.getByName("224.4.4.1");
            subImages = new Bitmap[ImageBlockNumber];
            BufferImage=new Bitmap[ImageBlockNumber*3+1];
            ss=new boolean[ImageBlockNumber*3+1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    private class Receiver extends Thread {
        /**
         * 此接收方的编号
         */

        private int senderNumber = -1;
        private DatagramPacket packet = null;
        private MulticastSocket multicastSocket = null;//用于组播传送的

        public Receiver(int senderNumber) throws SocketException, IOException {
            this.senderNumber = senderNumber;
            multicastSocket = new MulticastSocket(PORT + senderNumber);
            multicastSocket.setReceiveBufferSize(409600);
            multicastSocket.setSoTimeout(50000);//如果阻塞2秒还没接收到就超时
            multicastSocket.joinGroup(multicastIA);
        }

        public void run() {
            byte[] bytes = new byte[409600];
            //System.out.println("进入线程了");
            while (!toend) {
                try {
                    packet = new DatagramPacket(bytes, bytes.length);
                    if (!isReceiving) messageToUp = "没有接收到数据";
                    multicastSocket.receive(packet);
                    ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                    nowseq = input.read();
                    senderNumber = input.read();
//                    System.out.println(nowseq);
//                    System.out.println(senderNumber);
                    //System.out.println("nowseq="+nowseq+" senderNumber="+senderNumber);
                    subImages[senderNumber] = scaleBitmap(BitmapFactory.decodeStream(input),0.85F);
                    Message msg=new Message();
                    if (this.senderNumber == 0) {//0号线程负责处理屏幕大小的问题
                        subHeight = subImages[senderNumber].getHeight();
                        imageWidth = subImages[senderNumber].getWidth();
                        msg.what=1;
                        handler.sendMessage(msg);
                    } else if (this.senderNumber == 1) {
                        msg.what=2;
                        handler.sendMessage(msg);
                    } else if (this.senderNumber == 2) {
                        msg.what=3;
                        handler.sendMessage(msg);
                    } else if (this.senderNumber == 3) {
                        msg.what=4;
                        handler.sendMessage(msg);
                    }
                    isReceiving = true;
//                    Receiver.sleep(100);
                }    //try end
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("demo","客户端线程run出现错误");
                }
            }//end while
            //System.err.println("线程" + this.senderNumber + "退出了");
            messageToUp = "接收程序已终止";
        }
    }
    private void sendd(int i){
        int id;
        if(i==1) {
            ServerComponent server = new ServerComponent(ipname, 7899, 10, 2);
            Thread serverThread = new Thread(server);
            serverThread.start();
            th[0]=serverThread;
            Log.i("system", String.valueOf(serverThread.getId()));
            stoppp=false;
        }
        else{
            for(Thread t:th){
                System.out.println("Stop");
//                t.stop();
                stoppp=true;
                t.interrupt();
            }
        }
    }

    //-----测试
//    private void sendFun(){
//        bt_send= (Button) findViewById(R.id.bt_send);
//        bt_send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ServerComponent server=new ServerComponent("192.168.1.103",7800,10,2);
//                Thread serverThread=new Thread(server);
//                serverThread.start();
//            }
//        });
//    }
//    private void sendFun(){
//        bt_send= (Button) findViewById(R.id.bt_send);
//        bt_send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Thread th=new send();
//                th.start();
//            }
//        });
//    }
//    private class send extends Thread{
//        @Override
//        public void run() {
//            try {
//                InetAddress address=InetAddress.getByName("192.168.1.112");
//                int port=7800;
//                Bitmap b=takeScreenShot();
//                ByteArrayOutputStream baos=new ByteArrayOutputStream();
//                int quality=50;
//
//                b.compress(Bitmap.CompressFormat.JPEG,quality,baos);
//                byte[] data=baos.toByteArray();
//
//                DatagramPacket dp=new DatagramPacket(data,data.length,address,port);
////                byte[] data="test".getBytes();
////                DatagramPacket dp=new DatagramPacket(data,data.length,address,port);
//                DatagramSocket socket=new DatagramSocket();
//                socket.send(dp);
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            } catch (SocketException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    //----获取屏幕
//    private Bitmap takeScreenShot(){
//        // View是你需要截图的View
//        View view = getWindow().getDecorView();
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap b1 = view.getDrawingCache();
//
//        // 获取状态栏高度
//        Rect frame = new Rect();
//        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//        int statusBarHeight = frame.top;
//
//        // 获取屏幕长和高
//        int width = getWindowManager().getDefaultDisplay().getWidth();
//        int height = getWindowManager().getDefaultDisplay()
//                .getHeight();
//        // 去掉标题栏
//        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
//                - statusBarHeight);
//        view.destroyDrawingCache();
//        return b;
//    }
    /*
    发射屏幕
     */

    public class ServerComponent implements Runnable{
        private int PORT=7899; //端口
        private int SEQMAX=10;
        private int ImageBlockNumber=2;
        private InetAddress address;
        private int sendInterval=10;
        private float compressRate=0.5F;
        //以上是构造方法中可以更改的
        private Bitmap b=null;
        private boolean[] sendable=null;
        private boolean toend;
        private int nowseq=-1;
        public ServerComponent(String tagetIp,int port,int seqmax,int imageBlockNumber){
            try {
                address=InetAddress.getByName(tagetIp);
                PORT=port;
                SEQMAX=seqmax;
                ImageBlockNumber=imageBlockNumber;
                initServer();//初始化其他东西
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        public void stopSend()
        {
            toend = true;
        }
        private void initServer(){
            sendable=new boolean[ImageBlockNumber];
            for(int i=0;i<sendable.length;i++){
                sendable[i]=false;
            }
            toend=false;
        }
        private void gainImage(){  //获取屏幕有问题
//            View view = getWindow().getDecorView();
//            view.setDrawingCacheEnabled(true);
//            view.buildDrawingCache();
//            Bitmap b1 = view.getDrawingCache();
//
//            // 获取状态栏高度
//            Rect frame = new Rect();
//            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//            int statusBarHeight = frame.top;
//
//            // 获取屏幕长和高
//            int width = getWindowManager().getDefaultDisplay().getWidth();
//            int height = getWindowManager().getDefaultDisplay().getHeight();
//            // 去掉标题栏
//            if(width>height){
//                int t=width;
//                width=height;
//                height=t;
//            }
//            b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height);
//            view.destroyDrawingCache();
            //获取当前屏幕的大小
            int width = getWindow().getDecorView().getRootView().getWidth();
            int height = getWindow().getDecorView().getRootView().getHeight();
            //生成相同大小的图片
            b = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
            //找到当前页面的跟布局
            View view =  getWindow().getDecorView().getRootView();
            //设置缓存
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            //从缓存中获取当前屏幕的图片
            b = view.getDrawingCache();
        }
        private void createSenders(){
            for(int i=0;i<ImageBlockNumber;i++){
                Sender sender= null;
                try {
                    sender = new Sender(i);
                    sender.start();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            }
        }
        @Override
        public void run() {
            this.toend=false;
            this.gainImage();
            createSenders();
            while (!toend&&!stoppp){
                this.gainImage();
                this.nowseq=(nowseq+1)%SEQMAX;
                for(int i=0;i<sendable.length;i++){
                    sendable[i]=true;
                }
                while (true){
                    boolean isfinished=true;
                    for(int i=0;i<sendable.length;i++){
                        if(sendable[i])
                            isfinished=false;
                    }
                    if(isfinished)
                        break;
                    try {
                        Thread.sleep(sendInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        private class Sender extends Thread{
            private int senderNumber=-1;
            private Bitmap bitmap=null;
            private DatagramPacket packet=null;
            private DatagramSocket socket=null;
            public Sender(int senderNumber) throws SocketException {
                this.senderNumber=senderNumber;
                socket=new DatagramSocket();
            }
            public void prepareDatagram(){
                this.bitmap=b;
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                int quality=50;

                bitmap.compress(Bitmap.CompressFormat.JPEG,10,baos);
                byte[] data=baos.toByteArray();
                packet=new DatagramPacket(data,data.length,address,PORT);
//                byte[] data="test".getBytes();
//                DatagramPacket dp=new DatagramPacket(data,data.length,address,port);
            }

            @Override
            public void run() {
                while (!toend&&!stoppp){
                    System.out.print("interrupted="+isInterrupted());
                    if(sendable[senderNumber]){
                        prepareDatagram();
                        try {
                            socket.send(packet);
                            Log.i("线程","success");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendable[senderNumber]=false;
                    }
                    else {
                        try {
                            Thread.sleep(sendInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}