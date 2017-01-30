package com.coding.myplayer.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.coding.myplayer.util.DownloadUtils;
import com.coding.myplayer.bean.SearchInfo;


public class DownloadDialogFragment extends DialogFragment {

    private SearchInfo searchInfo; //当前要下载的歌曲
    private MainActivity mainActivity;

    public static DownloadDialogFragment newInstance(SearchInfo searchInfo){
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.searchInfo = searchInfo;
        return downloadDialogFragment;
    }

    private String[] items;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        items = new String[]{"下载","取消"};
    }

    public DownloadDialogFragment() {
        // Required empty public constructor
    }

    //创建对话框的事件方法
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        downloadMusic();
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();

    }

    //下载音乐
    private void downloadMusic() {
        Toast.makeText(mainActivity,"正在下载："+searchInfo.getTitle(),Toast.LENGTH_SHORT).show();
        DownloadUtils.getsInstance().setListener(new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownload(String mp3Url) {
                Toast.makeText(mainActivity,mp3Url+"下载成功",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(mainActivity,error,Toast.LENGTH_SHORT).show();
            }
        }).download(searchInfo);
    }



}
