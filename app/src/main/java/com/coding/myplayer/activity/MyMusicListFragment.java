package com.coding.myplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.coding.myplayer.R;
import com.coding.myplayer.adapter.MyMusicListAdapter;
import com.coding.myplayer.bean.MP3Info;
import com.coding.myplayer.util.MediaUtils;

import java.util.ArrayList;

/**
 * Created by user on 2016/9/27.
 */

public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView mListView;
    private ImageView iv_song_icon;
    private TextView tv_song_name, tv_singer;
    private ImageView iv_switch, iv_next;
    private ArrayList<MP3Info> mMP3Infos;
    private MyMusicListAdapter mAdapter;
    private LinearLayout ll_song_title;
    private QuickScroll mQuickScroll;

    private MainActivity mainAcivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainAcivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment mmlf = new MyMusicListFragment();
        return mmlf;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        mListView = (ListView) view.findViewById(R.id.my_music_list);
        iv_song_icon = (ImageView) view.findViewById(R.id.iv_song_icon);
        tv_song_name = (TextView) view.findViewById(R.id.tv_song_name);
        tv_singer = (TextView) view.findViewById(R.id.tv_singer);
        iv_switch = (ImageView) view.findViewById(R.id.iv_switch);
        iv_next = (ImageView) view.findViewById(R.id.iv_next);
        ll_song_title = (LinearLayout) view.findViewById(R.id.ll_song_title);
        mQuickScroll = (QuickScroll) view.findViewById(R.id.quickscroll);

        mListView.setOnItemClickListener(this);
        iv_song_icon.setOnClickListener(this);
        iv_switch.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        ll_song_title.setOnClickListener(this);

        return view;
    }

    public void initQuickScroll() {
        mQuickScroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE,mListView,mAdapter,QuickScroll.STYLE_HOLO);
        mQuickScroll.setFixedSize(1);
        mQuickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP,48);
        mQuickScroll.setPopupColor(QuickScroll.BLUE_LIGHT,QuickScroll.BLUE_LIGHT_SEMITRANSPARENT,1, Color.WHITE,1);
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        mainAcivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定播放服务
        mainAcivity.unbindPlayService();
    }

    /**
     * 加载本地音乐的列表
     */
    public void loadData() {
        mMP3Infos = MediaUtils.getMP3Infos(mainAcivity);
//        mMP3Infos = mainAcivity.playService.mp3Infos;
        mAdapter = new MyMusicListAdapter(mainAcivity, mMP3Infos);
        mListView.setAdapter(mAdapter);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainAcivity.playService.getChangePlayList() != PlayService.MY_MUSIC_LIST){
            mainAcivity.playService.setMp3Infos(mMP3Infos);
            mainAcivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        mainAcivity.playService.play(position);
    }

    //回调播放状态下的UI设置
    public void changeUIStatusOnPlay(int position) {

        if (position >= 0 && position < mainAcivity.playService.mp3Infos.size()) {
            MP3Info mp3Info = mainAcivity.playService.mp3Infos.get(position);
            tv_song_name.setText(mp3Info.getTitle());
            tv_singer.setText(mp3Info.getArtist());
            if (mainAcivity.playService.isPlaying()) {
                iv_switch.setImageResource(R.mipmap.player_btn_pause_normal);
            } else {
                iv_switch.setImageResource(R.mipmap.player_btn_play_normal);
            }
            Bitmap albumBitmap = MediaUtils.getArtwork(mainAcivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            iv_song_icon.setImageBitmap(albumBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_switch: {
                if (mainAcivity.playService.isPlaying()) {
                    iv_switch.setImageResource(R.mipmap.player_btn_play_normal);
                    mainAcivity.playService.pause();
                } else {
                    if (mainAcivity.playService.isPause()) {
                        iv_switch.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainAcivity.playService.start();
                    } else {
                        mainAcivity.playService.play(mainAcivity.playService.getCurrentPosition());
                    }
                }
            }
            break;
            case R.id.iv_next:
                mainAcivity.playService.next();
                break;
            case R.id.ll_song_title:
                Intent intent = new Intent(mainAcivity, PlayActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_song_icon:
                Intent intent2 = new Intent(mainAcivity, PlayActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
