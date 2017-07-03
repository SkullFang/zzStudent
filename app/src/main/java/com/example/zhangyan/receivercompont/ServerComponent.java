//package com.example.zhangyan.receivercompont;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.Rect;
//import android.util.Log;
//import android.view.View;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//
///**
// * Created by zhangyan on 2017/5/18.
// */
//
//
//public class ServerComponent implements Runnable{
//        private int PORT=7800; //端口
//        private int SEQMAX=10;
//        private int ImageBlockNumber=2;
//        private InetAddress address;
//        private int sendInterval=10;
//        private float compressRate=0.5F;
//        //以上是构造方法中可以更改的
//        private Bitmap b=null;
//        private boolean[] sendable=null;
//        private boolean toend;
//        private int nowseq=-1;
//        public ServerComponent(String tagetIp,int port,int seqmax,int imageBlockNumber){
//            try {
//                address=InetAddress.getByName(tagetIp);
//                PORT=port;
//                SEQMAX=seqmax;
//                ImageBlockNumber=imageBlockNumber;
//                initServer();//初始化其他东西
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//        }
//        public void stopSend()
//        {
//            toend = true;
//        }
//        private void initServer(){
//            sendable=new boolean[ImageBlockNumber];
//            for(int i=0;i<sendable.length;i++){
//                sendable[i]=false;
//            }
//            toend=false;
//        }
//        private void gainImage(Activity activity){
//            View view = activity.getWindow().getDecorView();
//            view.setDrawingCacheEnabled(true);
//            view.buildDrawingCache();
//            Bitmap b1 = view.getDrawingCache();
//
//            // 获取状态栏高度
//            Rect frame = new Rect();
//            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//            int statusBarHeight = frame.top;
//
//            // 获取屏幕长和高
//            int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//            int height = activity.getWindowManager().getDefaultDisplay().getHeight();
//            // 去掉标题栏
//            if(width>height){
//                int t=width;
//                width=height;
//                height=t;
//            }
//            b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height);
//            view.destroyDrawingCache();
//        }
//        private void createSenders(){
//            for(int i=0;i<ImageBlockNumber;i++){
//                Sender sender= null;
//                try {
//                    sender = new Sender(i);
//                    sender.start();
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//        @Override
//        public void run() {
//            this.toend=false;
//            this.gainImage(MainActivity);
//            createSenders();
//            while (!toend){
//                this.gainImage(MainActivity);
//                this.nowseq=(nowseq+1)%SEQMAX;
//                for(int i=0;i<sendable.length;i++){
//                    sendable[i]=true;
//                }
//                while (true){
//                    boolean isfinished=true;
//                    for(int i=0;i<sendable.length;i++){
//                        if(sendable[i])
//                            isfinished=false;
//                    }
//                    if(isfinished)
//                        break;
//                    try {
//                        Thread.sleep(sendInterval);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        private class Sender extends Thread{
//            private int senderNumber=-1;
//            private Bitmap bitmap=null;
//            private DatagramPacket packet=null;
//            private DatagramSocket socket=null;
//            public Sender(int senderNumber) throws SocketException {
//                this.senderNumber=senderNumber;
//                socket=new DatagramSocket();
//            }
//            public void prepareDatagram(){
//                this.bitmap=b;
//                ByteArrayOutputStream baos=new ByteArrayOutputStream();
//                int quality=50;
//
//                bitmap.compress(Bitmap.CompressFormat.JPEG,quality,baos);
//                byte[] data=baos.toByteArray();
//                packet=new DatagramPacket(data,data.length,address,PORT);
////                byte[] data="test".getBytes();
////                DatagramPacket dp=new DatagramPacket(data,data.length,address,port);
//            }
//
//            @Override
//            public void run() {
//                while (!toend){
//                    if(sendable[senderNumber]){
//                        prepareDatagram();
//                        try {
//                            socket.send(packet);
//                            Log.i("线程","success");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        sendable[senderNumber]=false;
//                    }
//                    else {
//                        try {
//                            Thread.sleep(sendInterval);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//}