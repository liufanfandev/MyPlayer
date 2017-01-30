package com.coding.myplayer.activity;

import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coding.myplayer.R;
import com.coding.myplayer.bean.MP3Info;
import com.coding.myplayer.util.MediaUtils;
import com.coding.myplayer.util.MyPlayerApp;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

/**
 * 播放音乐的界面
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private TextView tv_play_time, tv_song_time;
    private ImageView iv_last, iv_switch, iv_next, iv_song_menu, iv_love;
    private SeekBar seekBar;
//    private List<MP3Info> mp3Infos;

    private static final int UPDATE_TIME = 0x1; //设置更新时间的标志

    private ShowSongImageFragment frag_song_image;

    private FragmentTransaction transaction;

    private MyPlayerApp myPlayerApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        myPlayerApp = (MyPlayerApp) getApplication();
        tv_play_time = (TextView) findViewById(R.id.tv_play_time);
        tv_song_time = (TextView) findViewById(R.id.tv_song_time);
        iv_switch = (ImageView) findViewById(R.id.iv_switch);
        iv_last = (ImageView) findViewById(R.id.iv_last);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        iv_song_menu = (ImageView) findViewById(R.id.iv_song_menu);
        iv_love = (ImageView) findViewById(R.id.iv_love);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        iv_switch.setOnClickListener(this);
        iv_last.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_song_menu.setOnClickListener(this);
        iv_love.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        myHander = new MyHander(this);
    }


    private static MyHander myHander;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindPlayService();
    }

    private static class MyHander extends android.os.Handler {

        private PlayActivity playActivity;

        public MyHander(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.tv_play_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    @Override
    public void publish(int progress) {
        Message msg = myHander.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHander.sendMessage(msg);
        seekBar.setProgress(progress);
    }

    @Override
    public void change(int position) {

        MP3Info mp3Info = playService.mp3Infos.get(position);
        seekBar.setProgress(0);
        seekBar.setMax((int) mp3Info.getDuration());
        tv_song_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        frag_song_image = ShowSongImageFragment.newInstance();
        frag_song_image.setArguments(bundle);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, frag_song_image);
        transaction.commit();


        if (playService.isPlaying()) {
            iv_switch.setImageResource(R.mipmap.pause);
        } else {
            iv_switch.setImageResource(R.mipmap.play);
        }
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                iv_song_menu.setImageResource(R.mipmap.order_play);
                iv_song_menu.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                iv_song_menu.setImageResource(R.mipmap.random_play);
                iv_song_menu.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                iv_song_menu.setImageResource(R.mipmap.single_play);
                iv_song_menu.setTag(PlayService.SINGLE_PLAY);
                break;
        }
        //初始化喜欢图标
        try {
            MP3Info likeMp3Info = myPlayerApp.dbUtils.findFirst(Selector.from(MP3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
            if (likeMp3Info != null) {
                int isLike = likeMp3Info.getIsLike();
                if (isLike == 1) {
                    iv_love.setImageResource(R.mipmap.xin_hong);
                }
            } else {
                iv_love.setImageResource(R.mipmap.xin_bai);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    private float lastX;
    private float nowX;
    private float length;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            lastX = event.getX();
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            nowX = event.getX();
            length = nowX- lastX;
            if (length> 300){
//                Toast.makeText(PlayActivity.this, "向右滑动", Toast.LENGTH_SHORT).show();
                playService.next();
            }else if (length <-300){
//                Toast.makeText(PlayActivity.this, "向左滑动", Toast.LENGTH_SHORT).show();
                playService.pre();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_switch: {
                if (playService.isPlaying()) {
                    iv_switch.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        iv_switch.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            }
            case R.id.iv_last: {
                playService.pre();
                break;
            }
            case R.id.iv_next: {
                playService.next();
                break;
            }

            case R.id.iv_love: {
                MP3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                try {
                    MP3Info likeMp3Info = myPlayerApp.dbUtils.findFirst(Selector.from(MP3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
                    if (likeMp3Info == null) {
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        myPlayerApp.dbUtils.save(mp3Info);
                        iv_love.setImageResource(R.mipmap.xin_hong);
                    } else {
                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            iv_love.setImageResource(R.mipmap.xin_bai);
                        } else {
                            likeMp3Info.setIsLike(1);
                            iv_love.setImageResource(R.mipmap.xin_hong);
                        }
                        myPlayerApp.dbUtils.update(likeMp3Info, "isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            }

            case R.id.iv_song_menu: {
                int mode = (int) iv_song_menu.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        iv_song_menu.setImageResource(R.mipmap.random_play);
                        iv_song_menu.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        iv_song_menu.setImageResource(R.mipmap.single_play);
                        iv_song_menu.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        iv_song_menu.setImageResource(R.mipmap.order_play);
                        iv_song_menu.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            }
        }
    }

    private Long getId(MP3Info mp3Info) {
        //初始收藏状态
        long id = 0;
        switch (playService.getChangePlayList()) {
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            case PlayService.LEAST_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
        }
        return id;
    }
}
