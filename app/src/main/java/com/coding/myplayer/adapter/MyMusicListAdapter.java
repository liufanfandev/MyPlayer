package com.coding.myplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.coding.myplayer.R;
import com.coding.myplayer.util.MediaUtils;
import com.coding.myplayer.bean.MP3Info;

import java.util.ArrayList;

import static com.coding.myplayer.R.id.iv_song_icon;

/**
 * Created by user on 2016/9/27.
 */

public class MyMusicListAdapter extends BaseAdapter implements Scrollable {

    private Context ctx;
    private ArrayList<MP3Info> mp3Infos;

    public MyMusicListAdapter(Context ctx, ArrayList<MP3Info> mp3Infos){

        this.ctx= ctx;
        this.mp3Infos = mp3Infos;
    }

    public void setMp3Infos(ArrayList<MP3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_music_list,null);
            vh = new ViewHolder();
            vh.iv_sing_icon = (ImageView) convertView.findViewById(iv_song_icon);
            vh.tv_song_title = (TextView) convertView.findViewById(R.id.tv_song_name);
            vh.tv_singer = (TextView) convertView.findViewById(R.id.tv_singer);
            vh.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        MP3Info mp3Info = mp3Infos.get(position);
        Bitmap albumBitmap = MediaUtils.getArtwork(ctx,mp3Info.getId(),mp3Info.getAlbumId(),true,true);
        vh.iv_sing_icon.setImageBitmap(albumBitmap);
        vh.tv_song_title.setText(mp3Info.getTitle());
        vh.tv_singer.setText(mp3Info.getArtist());
        vh.tv_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return Character.toString(mp3Infos.get(childposition).getTitle().charAt(0));
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    static class ViewHolder{
        TextView tv_song_title;
        TextView tv_singer;
        TextView tv_time;
        ImageView iv_sing_icon;
    }


}
