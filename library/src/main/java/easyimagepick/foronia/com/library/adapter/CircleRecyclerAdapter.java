package easyimagepick.foronia.com.library.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.util.ImageBeanUtil;

/**
 * Created by Foronia on 2016/8/6.
 */
public class CircleRecyclerAdapter extends RecyclerView.Adapter<CircleRecyclerAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mlist=new ArrayList<>();
    private String mDirPath;
    private OnItemClickListener mOnItemClickListener;
    private static int Img_Size=50;

   // private static Set<String> mSelectImg = new HashSet<String>();

    public CircleRecyclerAdapter(Context context, List<String> mImgs, String absolutePath) {
        mContext=context;
        mlist=mImgs;
        mDirPath=absolutePath;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.imageloader_gird_item_layout,parent,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    public static void setImg_Size(int img_Size) {
        Img_Size = img_Size;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.mImg.setImageResource(R.drawable.circle_imgload_zj);
        holder.mSelect.setImageResource(R.drawable.add_radio_uncheck);
        holder.mImg.setColorFilter(null);
      /*  ImageLoader.getIntance(10, ImageLoader.Type.LIFO).LoadImage(filepath,holder.mImg);*/
        final String filepath;
        if(mDirPath.equals("全部图片")){
            filepath=mlist.get(position);
        }else {
            filepath=mDirPath+"/"+mlist.get(position);
        }

        Glide.with(mContext)
                .load(filepath)
                .placeholder(R.drawable.circle_imgload_zj)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.mImg);
        if(mOnItemClickListener !=null){
            /*holder.mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onLook(position);
                }
            });*/
            holder.mImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return true;
                }
            });
            holder.mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //已经被选中的
                    if (ImageBeanUtil.checkIsIn(filepath)) {
                        ImageBeanUtil.removeImg(filepath);
                        holder.mImg.setColorFilter(null);
                        holder.mSelect.setImageResource(R.drawable.add_radio_uncheck);
                        mOnItemClickListener.onUnCheck(ImageBeanUtil.getImgCount());

                    } else {//未被选中的
                        if(ImageBeanUtil.getImgCount()<Img_Size){
                            ImageBeanUtil.addImg(filepath);
                            holder.mSelect.setImageResource(R.drawable.add_radio_check);
                            mOnItemClickListener.onCheck(ImageBeanUtil.getImgCount());
                        }else {
                            Toast.makeText(mContext, "最多添加"+Img_Size+"张", Toast.LENGTH_SHORT).show();
                        }

                    }


                    mOnItemClickListener.onImageCount(ImageBeanUtil.getImgCount());

                }
            });
        }

        if (ImageBeanUtil.checkIsIn(filepath)) {
            holder.mSelect.setImageResource(R.drawable.add_radio_check);
        }
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView mImg;
        ImageButton mSelect;
        public MyViewHolder(View itemView) {
            super(itemView);
            mSelect= (ImageButton) itemView.findViewById(R.id.id_item_select);
            mImg= (ImageView) itemView.findViewById(R.id.id_item_image);
        }
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener=mOnItemClickListener;
    }

    public  interface OnItemClickListener{
        void onLook(int position);
        void onCheck(int size);
        void onUnCheck(int size);
        void onLongClick(int position);
        void onImageCount(int count);
    }

}
