package com.example.musictest;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MusicServiceConn conn;
    private MusicService.MyBinder binder;
    private SeekBar sbProgress;
    private TextView tvPlayTime;
    private TextView tvTotalTime;
    private Button play_btn;
    private Button r_btn;
    private Timer timer;
    private ListView mListView;
    private MyBaseAdapter mAdapter;


    public int num = -1;        // 获取需要的item序号
    public int length;          // 文件内音频文件的个数
    public int state = 0;       // 播放模式的状态
    public int state_p = 0;     // 暂停按钮的状态


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.m_list);
        sbProgress = findViewById(R.id.sb_progress);
        tvPlayTime = findViewById(R.id.tv_play_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        play_btn = findViewById(R.id.btn_pause);
        r_btn = findViewById(R.id.btn_random);



        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_previous).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_random).setOnClickListener(this);


        conn = new MusicServiceConn();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        ActivityCompat.requestPermissions(this, new String[]
                {"android.permission.READ_EXTERNAL_STORAGE"}, 1);
        mAdapter = new MyBaseAdapter();
        mListView.setAdapter(mAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 使用getItemAtPosition()方法获取所点击适配器对应的Item对象且将其名称存储为路径
                String name = (String) mListView.getItemAtPosition(position);
                num = position;
                ImageView imageView = (ImageView) view.findViewById(R.id.item_image);
                imageView.setBackgroundResource(R.drawable.playing);
                Toast.makeText(MainActivity.this, "正在播放：" + name, Toast.LENGTH_SHORT).show();
                File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                File file = new File(musicPath, name + ".mp3");
                String path = file.getAbsolutePath();
                if (state_p != 1){
                    state_p = 1;
                    play_btn.setBackgroundResource(R.drawable.pause);
                }
                binder.play(path);
                mAdapter.notifyDataSetChanged();
                addTimer();
                }
        });

        sbProgress.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            // 拖动过程中的事件
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }
            // 开始拖动时的事件
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            // 结束拖动时的事件
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binder.seekTo(sbProgress.getProgress());
                if (sbProgress.getProgress() == sbProgress.getMax()){
                    if (state_p != 1){
                        state_p = 1;
                        play_btn.setBackgroundResource(R.drawable.pause);
                    }
                    state(state);
                }
            }
        } );
    }


    private class MusicServiceConn implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainActivity.this.binder = (MusicService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pause:
                if (state_p == 0){
                    state_p = 1;
                    play_btn.setBackgroundResource(R.drawable.pause);
                    File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    String name = (String) mAdapter.getItem(0);
                    num = 0;
                    File temp = new File(musicPath, name);
                    String path = temp.toString() + ".mp3";
                    binder.play(path);
                    addTimer();
                    break;
                }else if (state_p == 1){
                    state_p = 2;
                    play_btn.setBackgroundResource(R.drawable.play);
                    binder.pause();
                    break;
                } else if (state_p == 2){
                state_p = 1;
                play_btn.setBackgroundResource(R.drawable.pause);
                binder.pause();
                break;
            }

            case R.id.btn_previous:
                if (state == 1){
                    random();
                    mListView.setSelection(num);
                }else{
                    previous();
                }
                play_btn.setBackgroundResource(R.drawable.pause);
                state_p = 1;
                break;

            case R.id.btn_next:
                if (state == 1){
                    random();
                    mListView.setSelection(num);
                }else if (state == 2){
                    next();
                }else if (state == 3){
                    next();
                }else if (state == 0){
                    order();
                }
                play_btn.setBackgroundResource(R.drawable.pause);
                state_p = 1;
                break;

            case R.id.btn_random:
                if (state == 0){
                    state = 1;
                    r_btn.setBackgroundResource(R.drawable.random);
                    Toast.makeText(MainActivity.this,"已切换为随机播放",Toast.LENGTH_SHORT).show();
                }else if (state == 1){
                    state = 2;
                    r_btn.setBackgroundResource(R.drawable.loop_one);
                    Toast.makeText(MainActivity.this,"已切换为单曲循环模式",Toast.LENGTH_SHORT).show();
                }else if (state == 2){
                    state = 3;
                    r_btn.setBackgroundResource(R.drawable.loop);
                    Toast.makeText(MainActivity.this,"已切换为循环播放模式",Toast.LENGTH_SHORT).show();
                }else if (state == 3){
                    state = 0;
                    r_btn.setBackgroundResource(R.drawable.order);
                    Toast.makeText(MainActivity.this,"已切换为顺序播放",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void addTimer(){
        if(timer == null){
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int currPos = binder.getCurrPos();
                    int duration = binder.getDuration();
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currPos",currPos);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            };
            timer.schedule(task,0,500);
        }
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int currPos = bundle.getInt("currPos");
            sbProgress.setMax(duration);
            sbProgress.setProgress(currPos);
            tvPlayTime.setText(TimeUtil.formatTime(currPos));
            tvTotalTime.setText(TimeUtil.formatTime(duration));
            if (duration <= currPos + 200) {
                state(state);
            }
        }
    };


    protected void onDestroy(){
        unbindService(conn);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    // 遍历存储文件下文件名以 .mp3 结尾的文件加入data数组
    public ArrayList<String> returnList(){
        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        final File [] s = musicPath.listFiles();
        final ArrayList<String> data = new ArrayList<String>();
        String type = ".mp3";
        try{
            for(int i=0; i<s.length; i++){
                if(s[i].getName().trim().toLowerCase().endsWith(type)){
                    String temp = s[i].getName();
                    temp = temp.substring(0, temp.length()-4);
                    data.add(temp);
                }
            }
        }catch (Exception e){
        }
        length = data.size();
        return data;
    }


    // 获取文件路径以及当前播放文件的Item下标，通过全局变量num更改播放的歌曲。
    public void previous(){
        if (num == 0){
            num = length;
        }
        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String name = (String) mAdapter.getItem(num - 1);
        num = num - 1;
        File temp = new File(musicPath, name);
        String path = temp.toString() + ".mp3";
        binder.play(path);
        mAdapter.notifyDataSetChanged();
    }


    public void next(){
        if (num == length - 1){
            num = -1;
        }
        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String name = (String) mAdapter.getItem(num + 1);
        num = num + 1;
        File temp = new File(musicPath, name);
        String path = temp.toString() + ".mp3";
        binder.play(path);
        mAdapter.notifyDataSetChanged();
    }


    public void order() {
        if (num == length - 1) {
            Toast.makeText(MainActivity.this, "当前已是最后一首歌曲", Toast.LENGTH_SHORT).show();
        } else {
            File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            String name = (String) mAdapter.getItem(num + 1);
            num = num + 1;
            File temp = new File(musicPath, name);
            String path = temp.toString() + ".mp3";
            binder.play(path);
            mAdapter.notifyDataSetChanged();
        }
    }


    public void random(){
        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        Random random = new Random();
        int num2;
        do {
            num2 = num;
            num = random.nextInt(length + 1);
        }while (num == num2);
        if (num >= length){
            num = num -1;
        }
        String name = (String) mAdapter.getItem(num);
        File temp = new File(musicPath, name);
        String path = temp.toString() + ".mp3";
        binder.play(path);
        mAdapter.notifyDataSetChanged();
    }


    public void loop(){
        if(num == length - 1){
            num = 0;
        }
        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String name = (String) mAdapter.getItem(num);
        File temp = new File(musicPath, name);
        String path = temp.toString() + ".mp3";
        binder.play(path);
        mAdapter.notifyDataSetChanged();
    }


    public void state(int state){
        if (state == 1) {
            random();
        } else if (state == 0) {
            if (num == length - 1){
            }else {
                order();
                 }
        } else if (state == 2) {
                loop();
        } else if (state == 3) {
                next();
            }
        }



    // 适配器部分
    final String[] names = (String[]) returnList().toArray(new String[returnList().size()]);
    class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int position) {
            return names[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(MainActivity.this, R.layout.item, null);
            TextView mTextView = (TextView) view.findViewById(R.id.item_tv);
            mTextView.setText(names[position]);
            if (num == position){
                ImageView imageView = (ImageView) view.findViewById(R.id.item_image);
                imageView.setBackgroundResource(R.drawable.playing);
            }
            return view;
        }
    }
}

