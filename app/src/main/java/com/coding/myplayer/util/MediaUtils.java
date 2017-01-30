package com.coding.myplayer.util;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.coding.myplayer.R;
import com.coding.myplayer.bean.MP3Info;

/**
 * 媒体工具类
 */

public class MediaUtils {

    private static List<String> lstFile = new ArrayList<String>();
    //获取专辑封面的uri
    private static final Uri albumArtUri = Uri
            .parse("content://media/external/audio/albumart");

    /*
     * 根据Id查询歌曲的信息 查询单曲的模式
     */
    public static MP3Info getMp3Info(Context context, long _id) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media._ID + "=" + _id, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        MP3Info mp3Info = null;

        if (cursor.moveToNext()) {
            mp3Info = new MP3Info();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));// 音乐的id
            String title = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE));// 音乐的标题
            String artist = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)));// 音乐的歌手
            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));// 音乐的专辑
            long albumId = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));  //专辑的id
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));// 获取歌曲的时长
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));// 获取歌曲的大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));// 获取歌曲的路径
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));// 是否为音乐

            // 把音乐添加到对象当中
            if (isMusic == 0) {
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Info.setIsMusic(isMusic);
            }
        }
        cursor.close();
        return mp3Info;
    }

    /*
     * 查询歌曲的id
     */
    public static long[] getMp3InfoId(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DURATION + ">=180000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        long[] ids = null;
        if (cursor != null) {

            ids = new long[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {

                cursor.moveToNext();
                ids[i] = cursor.getLong(0);
            }

        }
        cursor.close();
        return ids;
    }

    /*
     *
     * 查询歌曲的信息 保存在集合当中
     */
    public static ArrayList<MP3Info> getMp3Info(Context context) {

        ArrayList<MP3Info> mp3Infos = new ArrayList<MP3Info>();

        // ͨ通过使用ContentResolver来查询音乐中的音乐文件
        ContentResolver resolver = context.getContentResolver();
        // 音乐文件的路径
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 排序的方式
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        // 查询出那些需要的信息
        String[] projection = {
                MediaStore.Audio.Media.TITLE, // 歌曲名称
                MediaStore.Audio.Media.ARTIST, // 歌手
                MediaStore.Audio.Media.DATA,  // 歌曲在sd卡中的绝对路径
                MediaStore.Audio.Media.DURATION, // 歌曲的时长
                MediaStore.Audio.Media._ID, // 歌曲id
                MediaStore.Audio.Media.ALBUM, // 歌曲专辑
                MediaStore.Audio.Media.ALBUM_ID, // 歌曲专辑id
        };
        // 查询手机中的音乐文件
        Cursor cursor = resolver.query(uri, projection, null, null, sortOrder);
        while (cursor.moveToNext()) {
            MP3Info mp3Info = new MP3Info();

            String title = cursor.getString(0); // 获取歌曲的名称
            String artist = cursor.getString(1);// 获取歌曲演唱者
            String path = cursor.getString(2);// 获取歌曲的路径
            long duration = cursor.getLong(3);    // 获取歌曲的时长
            long _id = cursor.getLong(4); // 获取歌曲的id
            String album = cursor.getString(5);// 获取歌曲专辑
            long albumId = cursor.getLong(6); // 获取歌曲专辑id

            // 当歌曲的演唱者未知时
            if (artist.endsWith("<unknown>")) {
                artist = "未知艺术家";
            }
            // 当遇到自己录制的音乐文件或者比较小的铃声时，就不获取(小于20s)
            if (duration > 20000) {
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setUrl(path);
                mp3Info.setDuration(duration);
                mp3Info.setId(_id);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
            }

            mp3Infos.add(mp3Info); // 将一首歌放入List集合
        }

        return mp3Infos;
    }


    /*
     * 往list集合添加map对象，对每一个Map对象对应着一个音乐的所有属性
     */
    public static List<HashMap<String, String>> getMusicMap3(
            List<MP3Info> mp3Infos) {

        List<HashMap<String, String>> mp3List = new ArrayList<HashMap<String, String>>();
        for (Iterator<MP3Info> iterator = mp3Infos.iterator(); iterator
                .hasNext(); ) {
            MP3Info mp3Info = iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", mp3Info.getTitle());
            map.put("Artist", mp3Info.getArtist());
            map.put("album", mp3Info.getAlbum());
            map.put("albumId", mp3Info.getAlbumId() + "");
            map.put("duration", formatTime(mp3Info.getDuration()));
            map.put("size", mp3Info.getSize() + "");
            map.put("url", mp3Info.getUrl());
        }
        return mp3List;
    }

 
 /*
   * 转换时间格式，将毫秒转换为分和秒
   */

    public static String formatTime(long time) {

        String min = time / (1000 * 60) + "";

        String sec = time % (1000 * 60) + "";

        if (min.length() < 2) {

            min = "0" + time / (1000 * 60) + "";
        } else {

            min = time / (1000 * 60) + "";
        }

        if (sec.length() == 4) {

            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {

            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {

            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {

            sec = "0000" + (time % (1000 * 60)) + "";
        }

        return min + ":" + sec.trim().substring(0, 2);
    }


    /**
     * 获取默认专辑的图片
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        Options opts = new Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) { // 返回小图片
            return BitmapFactory.decodeStream(context.getResources()
                    .openRawResource(R.drawable.ic_launcher), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources()
                .openRawResource(R.mipmap.music_album), null, opts);
    }

    /**
     * 从文件中获取专辑封面位图
     */
    private static Bitmap getArtworkFromFile(Context context, long songid,
                                             long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException(
                    "Must specify an album or a song id");
        }
        try {
            Options options = new Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // ֻ只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到的图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 目标定为在800poxel的画面上显示
            //调用computeSampleSize得到的图片放缩的比例
            options.inSampleSize = 100;
            // 读入bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            // 根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑封面位图对象
     */
    public static Bitmap getArtwork(Context context, long song_id,
                                    long album_id, boolean allowdefalut, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                Options options = new Options();
                // 先制定原始大小
                options.inSampleSize = 1;
                // ֻ只进行大小的判断
                options.inJustDecodeBounds = true;
                // 调用此方法得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if (allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的放缩
     */
    public static int computeSampleSize(Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }


    /**
     * ��ѯ���еĸ�����Ϣ��������list��MediaStore.Audio.Media.DURATION + ">=180000",
     */
    public static ArrayList<MP3Info> getMP3Infos(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        ArrayList<MP3Info> mMP3Infos = new ArrayList<MP3Info>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MP3Info mMP3Info = new MP3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {
                mMP3Info.setId(id);
                mMP3Info.setTitle(title);
                mMP3Info.setArtist(artist);
                mMP3Info.setAlbum(album);
                mMP3Info.setAlbumId(albumId);
                mMP3Info.setDuration(duration);
                mMP3Info.setSize(size);
                mMP3Info.setUrl(url);
                mMP3Infos.add(mMP3Info);
            }
        }
        cursor.close();
        return mMP3Infos;

    }

}











