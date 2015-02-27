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
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends ActionBarActivity {
    private ServerSocket server_sock;
    private static final int PORT = 3490;
    private static final int IMGSIZE = 921600;
    private ImageView im;
    private Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.d("rbv","Loading OpenCV failed, the app will crash");
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
        private Socket client_sock;
        public void run() {
            try {
                server_sock = new ServerSocket(PORT);

            } catch(IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                Log.d("rbv", "loopin loopin loopin");
                try {
                    client_sock = server_sock.accept();
                    Thread ui_update = new Thread(new UIUpdateThread(client_sock));
                    ui_update.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UIUpdateThread implements Runnable {
        private Socket client_sock;
        public UIUpdateThread (Socket client_sock) {
            this.client_sock = client_sock;
        }
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int times = 0;
                byte[] frame_bytes = new byte[IMGSIZE];
                Mat frame = Mat.zeros(480, 640, CvType.CV_8UC3);
                try {
                    DataInputStream input = new DataInputStream(client_sock.getInputStream());
                    input.readFully(frame_bytes, 0, IMGSIZE);
                    Log.d("rbv", "received " + (++times) + " image(s)");
                    input = new DataInputStream(client_sock.getInputStream());
                    input.readFully(frame_bytes, 0, IMGSIZE);
                    int ptr = 0;
                    for (int i = 0; i < frame.rows(); i++) {
                        for (int j = 0; j < frame.cols(); j++) {
                            frame.put(i, j, new byte[]{frame_bytes[ptr + 2], frame_bytes[ptr + 1], frame_bytes[ptr]});
                            ptr += 3;
                        }
                    }
                    bm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(frame, bm);
                    im.post(new Runnable() {
                        @Override
                        public void run() {
                            im.setImageBitmap(bm);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}