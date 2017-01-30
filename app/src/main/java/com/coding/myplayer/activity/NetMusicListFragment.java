package com.coding.myplayer.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.coding.myplayer.R;
import com.coding.myplayer.adapter.MySearchListAdapter;
import com.coding.myplayer.util.Constant;
import com.coding.myplayer.bean.SearchInfo;
import com.coding.myplayer.util.SearchMusicUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class NetMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView mListView;
    private ImageView iv_song_icon;
    private TextView tv_song_name, tv_singer;
    private ArrayList<SearchInfo> searchInfos = new ArrayList<SearchInfo>();
    private MySearchListAdapter mAdapter;
    private LinearLayout ll_load;

    private MainActivity mainAcivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainAcivity = (MainActivity) getActivity();
    }

    public static NetMusicListFragment newInstance() {
        NetMusicListFragment mmlf = new NetMusicListFragment();
        return mmlf;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_net_music_list, null);
        ll_load = (LinearLayout) view.findViewById(R.id.ll_load);
        mListView = (ListView) view.findViewById(R.id.net_music_list);
        iv_song_icon = (ImageView) view.findViewById(R.id.iv_song_icon);
        tv_song_name = (TextView) view.findViewById(R.id.tv_song_name);
        tv_singer = (TextView) view.findViewById(R.id.tv_singer);

        loadData();//加载网络音乐
        mListView.setOnItemClickListener(this);
        return view;
    }


    public void loadData() {
        //执行异步任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL + Constant.BAIDU_DAYHOT);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position >= mAdapter.getSearchResults().size() || position < 0) return;
        showDownloadDialog(position);
    }

    private void showDownloadDialog(final int position) {

        DownloadDialogFragment downlaodDialogFragment = DownloadDialogFragment.newInstance(searchInfos.get(position));
        downlaodDialogFragment.show(getFragmentManager(), "download");
    }

    @Override
    public void onClick(View v) {

    }

    class LoadNetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_load.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            searchInfos.clear();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == 1) {
                mListView.setVisibility(View.VISIBLE);
                mAdapter = new MySearchListAdapter(mainAcivity, searchInfos);
                mListView.setAdapter(mAdapter);
                mListView.addFooterView(LayoutInflater.from(mainAcivity).inflate(R.layout.footerview, null));
            }
            ll_load.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();
                Elements songTitles = doc.select("span.song-title");
//                Elements artists = doc.select("span.singer");
                Elements artists = doc.select("span.author_list");
                for (int i = 0; i < songTitles.size()-1; i++) {
                    SearchInfo searchInfo = new SearchInfo();
                    Elements urls = songTitles.get(i).getElementsByTag("a");
                    searchInfo.setUrl(urls.get(0).attr("href"));
                    searchInfo.setTitle(urls.get(0).text());

                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    searchInfo.setArtist(artistElements.get(0).text());
                    searchInfo.setAlbum("热歌榜");
//                    Log.e("TAG","songTitles.size()"+songTitles.size());
//                    Log.e("TAG","artists.size()"+artists.size());
                    searchInfos.add(searchInfo);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }


    }

    public void SerachMusic(String str) {
        mListView.setVisibility(View.GONE);
        ll_load.setVisibility(View.VISIBLE);
        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {

            @Override
            public void onSearchResult(ArrayList<SearchInfo> results) {
                ArrayList<SearchInfo> sr = mAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
//              searchInfos.clear();
//              searchInfos.addAll(results);
                mAdapter.notifyDataSetChanged();
                ll_load.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        }).search(str, 1);
    }
}

