package easyimagepick.foronia.com.library.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.adapter.RecyclerAdapter;
import easyimagepick.foronia.com.library.customview.CommonDialog;
import easyimagepick.foronia.com.library.model.MsgEvent;
import easyimagepick.foronia.com.library.util.ImageBeanUtil;
import easyimagepick.foronia.com.library.util.PopupWindowUtil;
import easyimagepick.foronia.com.library.util.SDCardSaveImgUtil;
import easyimagepick.foronia.com.library.util.StaticValueUtil;

/**
 * Created by Foronia on 2017/8/17 0017.
 */

public class ImageUploadActivity extends BasicActivity {
    public SDCardSaveImgUtil mSdCardSaveImgUtil;
    private CommonDialog commonDialog;
    private Uri fileuri;
    private String filepath;
    private String filename;
    private RecyclerView mRecyclerView;
    private View parentView;
    public RecyclerAdapter mAdpater;
    public String kindergarten;
    public List<String> mlist = new ArrayList<>();
    private PopupWindowUtil popupWindowUtil;
    private int limit;
    private static final String ADDICON = "NOPICTURE";

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        parentView = LayoutInflater.from(this).inflate(R.layout.activity_image_upload, null);
        setContentView(parentView);
    }

    public void setChildContentView(int layoutResId) {
        FrameLayout fl_content = (FrameLayout) findViewById(R.id.fl_content);
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(layoutResId, null);
        fl_content.addView(v);
    }

    public void setLimit(int limit){
        this.limit = limit;
    }

    @Override
    protected void init_datas() {
        EventBus.getDefault().register(this);
//        kindergarten = preferences.getString(StringUtil.KINDERGARTEN, "");

        /**
         * 加号表示
         */
        mlist.add(ADDICON);

        /**
         * 文件夹地址
         */
        filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/";

        mSdCardSaveImgUtil = SDCardSaveImgUtil.getInstance();

        popupWindowUtil = new PopupWindowUtil(this, new PopupWindowUtil.ItemOnclickListener() {
            @Override
            public void itemOnclick1() {
                photo();
            }

            @Override
            public void itemOnclick2() {
                pickphoto();
            }

            @Override
            public void itemOnclick3() {

            }

            @Override
            public void itemOnclick4() {

            }
        },"","");
    }

    @Override
    protected void findViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_image);
    }

    @Override
    protected void get_datas() {

    }

    @Override
    protected void set_datas() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdpater = new RecyclerAdapter(this,mlist);
        mRecyclerView.setAdapter(mAdpater);
    }

    @Override
    protected void setOnclick() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onDataSynEvent(MsgEvent event) {
        if (limit==0){
            limit = 9;
        }
        if (event.getNum() != limit+1) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            popupWindowUtil.start(parentView);
        }
    }

    private void pickphoto() {
        Intent intent = new Intent(this, CircleAlbumActivity.class);
        intent.putExtra("limit", limit);
        startActivityForResult(intent, StaticValueUtil.PICK_PHOTO);
    }

    private void photo() {
        filename = UUID.randomUUID() + ".jpg";
        File f = new File(filepath + filename);
        fileuri = Uri.fromFile(f);
        Intent openCameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri);
        startActivityForResult(openCameraIntent, StaticValueUtil.TAKE_PHOTO);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode== KeyEvent.KEYCODE_BACK){
            actFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void actFinish(){
        if (mlist.size() > 1 || ImageBeanUtil.getImgCount() > 0 ) {
            commonDialog = new CommonDialog(this, new CommonDialog.GoCommonDialog() {
                @Override
                public void go() {
                    if (mlist.size() > 0) {
                        mlist.clear();
                        finish();
                    }
                    ImageBeanUtil.removeSelectAllImg();
                    commonDialog.dismiss();
                }

                @Override
                public void cancel() {
                    commonDialog.dismiss();
                }
            }, "是否放弃本次操作？", "是", "否");
            commonDialog.show();
        }else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case StaticValueUtil.TAKE_PHOTO:
                    mlist.add(0, filepath + filename);
                    mAdpater.notifyDataSetChanged();
                    /*try {
                        UpImgDatautil.addMapitem(filepath + filename, mSdCardSaveImgUtil.scalImage("", Uri.fromFile(new File(filepath + filename))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    /**
                     * 把拍照的图片也加到select中
                     */
                    ImageBeanUtil.addImg(filepath + filename);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filepath + filename))));
                    break;
                case StaticValueUtil.PICK_PHOTO:
                    mlist.clear();
                    mlist.add(ADDICON);
                    /**
                     * 界面的变化
                     */
                    mlist.addAll(0,ImageBeanUtil.getSelectImg());
                    mAdpater.notifyDataSetChanged();
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
