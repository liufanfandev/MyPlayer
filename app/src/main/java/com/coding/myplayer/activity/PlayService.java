package com.coding.myplayer.activity;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.coding.myplayer.bean.MP3Info;
import com.coding.myplayer.util.MediaUtils;
import com.coding.myplayer.util.MyPlayerApp;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现的功能：
 * 1、播放 2、暂停 3、上一首 4、下一首 5、获取当前播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    private int currentPosition;//当前正在播放的歌曲的位置
    ArrayList<MP3Info> mp3Infos;

    private MusicUpdateLister musicUpdateLister;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    private boolean isPause = false;

    public static final int MY_MUSIC_LIST = 1;
    public static final int LIKE_MUSIC_LIST = 2;
    public static final int LEAST_MUSIC_LIST = 3;
    private int ChangePlayList;

    //播放模式
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;



    private Random random = new Random();

    public void setMp3Infos(ArrayList<MP3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getChangePlayList() {
        return ChangePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.ChangePlayList = changePlayList;
    }

    public PlayService() {
    }

    public boolean isPause() {
        return isPause;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    //这样做可以方便的得到Playservice
    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyPlayerApp app = (MyPlayerApp) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMP3Infos(this);
        es.execute(updateStatusRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateLister != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateLister.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //播放
    public void play(int position) {
        MP3Info mp3Info;
        if (position < 0 || position >= mp3Infos.size()) {
           position = 0;
        }
        mp3Info = mp3Infos.get(position);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            saveCurrentTime(mp3Info);
            currentPosition = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (musicUpdateLister != null) {
            musicUpdateLister.onChange(currentPosition);
        }

    }

    private void saveCurrentTime(MP3Info mp3Info) {
        MyPlayerApp myPlayerApp = (MyPlayerApp) getApplication();
        try {
            MP3Info leastMp3Info = myPlayerApp.dbUtils.findFirst(Selector.from(MP3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
            if(leastMp3Info == null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                myPlayerApp.dbUtils.save(mp3Info);
            }else {
                leastMp3Info.setPlayTime(System.currentTimeMillis());
                myPlayerApp.dbUtils.update(leastMp3Info,"playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private Long getId(MP3Info mp3Info) {
        //初始收藏状态
        long id = 0;
        switch (getChangePlayList()) {
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

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next() {
        if (currentPosition + 1 > mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void pre() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //
    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            isPause = false;
        }
    }

    //判断是否正在播放
    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }


    //更新状态的接口(观察者模式的运用)
    public interface MusicUpdateLister {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public void setMusicUpdateLister(MusicUpdateLister musicUpdateLister) {
        this.musicUpdateLister = musicUpdateLister;
    }
}








