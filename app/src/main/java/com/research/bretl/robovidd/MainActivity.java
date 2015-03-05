package com.research.bretl.robovidd;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MainActivity extends ActionBarActivity {
    private static final int PORT = 3490;
    private static final int MAX_UDP_DATAGRAM_LEN = 64000;
    private ImageView im;
    private Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.d("rbv","Loading OpenCV shared libs failed, the app will crash");
        }
        im = (ImageView) findViewById(R.id.image_view);
        Thread server_th = new Thread(new ServerThread());
        server_th.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ServerThread implements Runnable {
        byte[] frame = new byte[MAX_UDP_DATAGRAM_LEN];
        DatagramPacket packet = new DatagramPacket(frame, frame.length);
        DatagramSocket socket = null;
        public void run() {
            try {
                socket = new DatagramSocket(PORT);
            } catch(IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket.receive(packet);
                    //Log.d("rbv", "frame size in bytes - " + packet.getLength());
                    Thread ui_update = new Thread(new UIUpdateThread(packet));
                    ui_update.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UIUpdateThread implements Runnable {
        private DatagramPacket packet;
        public UIUpdateThread (DatagramPacket packet) {
            this.packet = packet;
        }
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Mat frame = Highgui.imdecode(new MatOfByte(packet.getData()), 0);
                bm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(frame, bm);
                im.post(new Runnable() {
                    @Override
                    public void run() {
                        im.setImageBitmap(bm);
                    }
                });

            }
        }
    }
}