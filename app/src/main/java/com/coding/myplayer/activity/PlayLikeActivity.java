package com.coding.myplayer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.coding.myplayer.R;
import com.coding.myplayer.adapter.MyMusicListAdapter;
import com.coding.myplayer.bean.MP3Info;
import com.coding.myplayer.util.MyPlayerApp;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class PlayLikeActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView mListView;
    private ArrayList<MP3Info> likeMp3Infos;
    private MyMusicListAdapter mAdapter;
    private boolean isChange = false;//表示当前列表是否是收藏列表

    private MyPlayerApp myPlayerApp;

    private PlayLikeActivity playLikeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_like);
        mListView = (ListView) findViewById(R.id.lv_like_song);
        myPlayerApp = (MyPlayerApp) getApplication();
        getActionBar().setTitle("喜欢的歌曲");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        init();

        mListView.setOnItemClickListener(this);
    }

    private void init() {
        try {
            List<MP3Info> list = myPlayerApp.dbUtils.findAll(Selector.from(MP3Info.class).where("isLike","=","1"));
            if (list == null || list.size() == 0){
                return;
            }
            likeMp3Infos = (ArrayList<MP3Info>) list;
            mAdapter = new MyMusicListAdapter(this, likeMp3Infos);
            mListView.setAdapter(mAdapter);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (playService.getChangePlayList() != PlayService.LIKE_MUSIC_LIST){
            playService.setMp3Infos(likeMp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case android.R.id.home:
//                Intent intent = new Intent(PlayLikeActivity.this,MainActivity.class);
//                startActivity(intent);
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
