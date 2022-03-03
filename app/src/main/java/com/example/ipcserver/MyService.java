package com.example.ipcserver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

public class MyService extends Service {

    private static final String TAG=MyService.class.getSimpleName();

    private int mRandomNumber;
    private boolean mIsRandomGeneratorOn;

    private final int MIN=0;
    private final int MAX=100;

    public static final int GET_RANDOM_NUMBER_TAG = 0;

    private Messenger randomNumberMessenger = new Messenger(new RandomNumberRequestHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getPackage().equals("com.example.ipcclient")) {
            Toast.makeText(this, "bounded for valid package", Toast.LENGTH_SHORT).show();
            return randomNumberMessenger.getBinder();
        } else {
            Toast.makeText(this, "invalid package", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIsRandomGeneratorOn =true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                startRandomNumberGenerator();
            }
        }).start();
        return START_STICKY;
    }

    private void stopRandomNumberGenerator(){
        mIsRandomGeneratorOn =false;
        Toast.makeText(getApplicationContext(),"Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        stopRandomNumberGenerator();
    }

    private void startRandomNumberGenerator(){
        while (mIsRandomGeneratorOn){
            try{
                Thread.sleep(1000);
                if(mIsRandomGeneratorOn){
                    mRandomNumber =new Random().nextInt(MAX)+MIN;
                    Log.i(TAG,"Random Number: "+mRandomNumber);
                }
            }catch (InterruptedException e){
                Log.i(TAG,"Thread Interrupted");
            }

        }
    }

    public int getRandomNumber(){
        return mRandomNumber;
    }

    private class RandomNumberRequestHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case GET_RANDOM_NUMBER_TAG:
                    Message messageSendRandomNumber = Message.obtain(null, GET_RANDOM_NUMBER_TAG);
                    messageSendRandomNumber.arg1 = getRandomNumber();
                    try {
                        msg.replyTo.send(messageSendRandomNumber);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
