package com.coding.myplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coding.myplayer.R;
import com.coding.myplayer.bean.SearchInfo;

import java.util.ArrayList;

import static com.coding.myplayer.R.id.iv_song_icon;

/**
 * Created by user on 2016/9/27.
 */

public class MySearchListAdapter extends BaseAdapter{

    private Context ctx;
    private ArrayList<SearchInfo> searchResults;

    public MySearchListAdapter(Context ctx, ArrayList<SearchInfo> searchResults){

        this.ctx= ctx;
        this.searchResults = searchResults;
    }

    public ArrayList<SearchInfo> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchInfo> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_search_list,null);
            vh = new ViewHolder();
            vh.iv_sing_icon = (ImageView) convertView.findViewById(iv_song_icon);
            vh.tv_song_title = (TextView) convertView.findViewById(R.id.tv_song_name);
            vh.tv_singer = (TextView) convertView.findViewById(R.id.tv_singer);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        SearchInfo searchInfo  = searchResults.get(position);
        vh.iv_sing_icon.setImageResource(R.mipmap.app_logo2);
        vh.tv_song_title.setText(searchInfo.getTitle());
        vh.tv_singer.setText(searchInfo.getArtist());

        return convertView;
    }

    static class ViewHolder{
        TextView tv_song_title;
        TextView tv_singer;
        ImageView iv_sing_icon;
    }


}
