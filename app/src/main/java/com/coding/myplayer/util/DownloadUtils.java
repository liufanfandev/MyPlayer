package com.coding.myplayer.util;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.coding.myplayer.bean.SearchInfo;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by user on 2016/11/2.
 */

public class DownloadUtils {

    private static final String DOWNLOAD_URL = "/download?_o=%2Fsearch%2Fsong";
    private static final int SUCCESS_LRC = 1;  //下载歌词成功
    private static final int FAILED_LRC = 2;  //下载歌词失败
    private static final int SUCCESS_MP3 = 3;  //下载歌曲成功
    private static final int FAILED_MP3 = 4;   //下载歌曲失败
    private static final int GET_MP3_URL = 5;  //成功获取下载的MP3的url
    private static final int GET_FAILED_MP3_URL = 6;   //获取下载的MP3失败
    private static final int MUSIC_EXISTS = 7;    //下载的音乐已存在
    private static final int GET_SIZE_FAILED = 8;

    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;

    //设置回调的监听器对象
    public DownloadUtils setListener(OnDownloadListener mListener) {
        this.mListener = mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownloadUtils getsInstance() {
        if (sInstance == null) {
            try {
                sInstance = new DownloadUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private DownloadUtils() throws ParserConfigurationException {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    public void download(final SearchInfo searchInfo) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mListener != null) mListener.onDownload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if (mListener != null) mListener.onFailed("歌词下载失败");
                        break;
                    case GET_SIZE_FAILED:
                        if (mListener != null) mListener.onFailed("SIZE为0");
                        break;
                    case GET_MP3_URL:
                        downloadMusic(searchInfo, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null) mListener.onFailed("下载失败，该歌曲为VIP类型");
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null) mListener.onDownload(searchInfo.getTitle() + "已下载");
                        String url = Constant.BAIDU_URL + searchInfo.getUrl();
                        downloadLRC(url, searchInfo.getTitle(), this);
                        break;
                    case FAILED_MP3:
                        if (mListener != null) mListener.onFailed(searchInfo.getTitle() + "下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) mListener.onFailed("音乐已存在");
                        break;
                }
            }
        };
        getDownloadMusicURL(searchInfo, handler);
    }

    private void downloadLRC(final String url, final String title,final  Handler handler) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements elements = doc.select("div.lyric-content");
                    String lrcURL = elements.attr("data-lrclink");
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory()+Constant.DIR_LAC);
                    if (!lrcDirFile.exists()){
                        lrcDirFile.mkdirs();
                    }
                    lrcURL = Constant.BAIDU_URL+lrcURL;
                    String target = lrcDirFile +"/"+ title+".lrc";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()){
                        PrintStream ps = new PrintStream(new File(target));
                        byte[] bytes = response.body().bytes();
                        ps.write(bytes,0,bytes.length);
                        ps.close();
                        handler.obtainMessage(SUCCESS_LRC,target).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(FAILED_LRC).sendToTarget();
                }
            }
        });
    }

    //真正下载音乐的方法
    private void downloadMusic(final SearchInfo searchInfo, final String url, final Handler handler) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdirs();
                }
                String mp3url = Constant.BAIDU_URL + url;
                String target = musicDirFile + "/" + searchInfo.getTitle() + ".mp3";
                File fileTarget = new File(target);

                if (fileTarget.exists()) {
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    //使用OkHttpClient完成网络请求
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }

            }
        });
    }

    //获取下载的音乐的URL
    private void getDownloadMusicURL(final SearchInfo searchInfo, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = Constant.BAIDU_URL + "/song/" + searchInfo.getUrl().substring(searchInfo.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Log.e("TAG",""+doc);
//                    Elements elements = doc.select("a[data-btndata]");
                    Elements elements = doc.select("songinfo");
                    if (elements.size() <= 0) {

                        handler.obtainMessage(GET_SIZE_FAILED).sendToTarget();
                        return;
                    }
                    for (Element e : elements) {
                        if (e.attr("href").contains(".mp3")) {
                            String result = e.attr("href");
                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
                            msg.sendToTarget();
                            return;
                        }
                        if (e.attr("href").startsWith("/vip")) {
                            elements.remove(e);
                        }
                    }
                    if (elements.size() <= 0) {
                        handler.obtainMessage(GET_SIZE_FAILED).sendToTarget();
                        return;
                    }
                    String result = elements.get(0).attr("href");
                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
                    msg.sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }
            }
        });
    }


    //自定义下载事件监听器
    public interface OnDownloadListener {

        public void onDownload(String mp3Url);

        public void onFailed(String error);
    }
}
