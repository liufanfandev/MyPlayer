package com.coding.myplayer.activity;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coding.myplayer.R;
import com.coding.myplayer.bean.MP3Info;
import com.coding.myplayer.util.MediaUtils;

import java.util.List;

/**
 * 在播放界面显示歌曲名字和信息的fargment
 */
public class ShowSongImageFragment extends Fragment {

    private TextView tv_song_name;
    private ImageView iv_song_icon;

    private List<MP3Info> mp3Infos;

    private PlayActivity playActivity;
    private MainActivity mainActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        playActivity = (PlayActivity) context;
    }

    public static ShowSongImageFragment newInstance() {
        ShowSongImageFragment ssif = new ShowSongImageFragment();
        return ssif;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_iamge, null);

        tv_song_name = (TextView) view.findViewById(R.id.tv_song_name);
        tv_song_name.setMaxLines(1);
        iv_song_icon = (ImageView) view.findViewById(R.id.iv_song_icon);
//        loadData();


        Bundle bundle = getArguments();//从activity传过来的Bundle
        if(bundle!=null){
            changeUIStatusOnPlay(bundle.getInt("position"));
        }

        return view;
    }

    public void loadData() {
        mp3Infos = mainActivity.playService.mp3Infos;
    }




    public void changeUIStatusOnPlay(int position) {
        mp3Infos = playActivity.playService.mp3Infos;
        if (position >= 0 && position < mp3Infos.size()) {
            MP3Info mp3Info = mp3Infos.get(position);
            tv_song_name.setText(mp3Info.getTitle());
            Bitmap albumBitmap = MediaUtils.getArtwork(getActivity(), mp3Info.getId(), mp3Info.getAlbumId(), true, false);
            iv_song_icon.setImageBitmap(albumBitmap);
        }
    }


}
