package easyimagepick.foronia.com.library.customview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.model.ImageLoaderBean;
import easyimagepick.foronia.com.library.util.ImageLoader;

/**
 * Created by Foronia on 2016/8/4.
 */
public class ListImageDirPopupWindow extends PopupWindow {

    private int mWidth;
    private int mHeight;
    private View mConvertView;
    private ListView mListView;
    private List<ImageLoaderBean> mDatas;

    public ListImageDirPopupWindow(Context context, List<ImageLoaderBean> mList) {
        calWidthAndHeight(context);
        mConvertView= LayoutInflater.from(context).inflate(R.layout.popup_layout,null);
        mDatas=mList;
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        initViews(context);
        initEvent();

    }

    private void initViews(Context context) {
        mListView= (ListView) mConvertView.findViewById(R.id.id_list_dir);
        mListView.setAdapter(new ListDirAdapter(context,mDatas));
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mListener !=null){
                    mListener.onSelect(mDatas.get(position));
                }
            }
        });
    }

    public  OnDirSelectListener mListener;

    public void setDirSelectOnListener(OnDirSelectListener mListener){
        this.mListener=mListener;
    }

    public interface OnDirSelectListener{
        void onSelect(ImageLoaderBean folderBean);
    }

    private void calWidthAndHeight(Context context) {
        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mWidth=outMetrics.widthPixels;
        mHeight= (int) (outMetrics.heightPixels*0.7);

    }

    private class ListDirAdapter extends ArrayAdapter<ImageLoaderBean> {
        private LayoutInflater mInflater;
        private List<ImageLoaderBean> mDatas;

        public ListDirAdapter(Context context, List<ImageLoaderBean> objects) {
            super(context,0,objects);
            mInflater=LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
                viewHolder=new ViewHolder();
                convertView=mInflater.inflate(R.layout.item_popup,parent,false);
                viewHolder.mImg= (ImageView) convertView.findViewById(R.id.id_dir_item_image);
                viewHolder.mDirName=(TextView) convertView.findViewById(R.id.id_dir_item_name);
                viewHolder.mDirCount=(TextView) convertView.findViewById(R.id.id_dir_item_count);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            ImageLoaderBean bean=getItem(position);
            viewHolder.mImg.setImageResource(R.drawable.pictures_no);
            ImageLoader.getIntance().LoadImage(bean.getFirstImgPath(),viewHolder.mImg);
            viewHolder.mDirName.setText(bean.getMname());
            viewHolder.mDirCount.setText(bean.getMcount()+"");
            return convertView;
        }

        private class ViewHolder{
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;

        }
    }
}
