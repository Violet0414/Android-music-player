package com.example.musictest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;


public class MusicService extends Service {
    private MediaPlayer mediaPlayer;


    class MyBinder extends Binder{
        public void play(String filePath) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                else if (mediaPlayer != null){      //当切歌时调用reset()方法置闲mediaPlayer对象
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);    //重置数据源
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                else {
                    if (!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void pause(){
            if(mediaPlayer != null){
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                else
                    mediaPlayer.start();
            }
        }


        public void seekTo(int position){           // 拖动播放进度条
            if (mediaPlayer != null){
                mediaPlayer.seekTo(position);
            }
        }


        public int getCurrPos(){
            if (mediaPlayer != null){
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }


        public int getDuration(){
            if (mediaPlayer != null){
                return mediaPlayer.getDuration();
            }
            return 0;
        }
    }


    public void onDestory(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }


    public MusicService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
}

