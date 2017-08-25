package easyimagepick.foronia.com.library.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.model.MsgEvent;
import easyimagepick.foronia.com.library.ui.CircleImgDetailActivity;
import easyimagepick.foronia.com.library.util.ImageBeanUtil;
import easyimagepick.foronia.com.library.util.UpImgDatautil;

import static easyimagepick.foronia.com.library.util.StringUtil.ADDICON;

/**
 * Created by Foronia on 2017/8/17 0017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mlist;

    public RecyclerAdapter(Context context, List<String> mlist) {
        this.mContext = context;
        this.mlist = mlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.add_circle_v2_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (mlist.get(position).equals(ADDICON)) {
            holder.mImg.setImageResource(R.drawable.add_circle_img);
            holder.mDelete.setVisibility(View.GONE);
        } else {
            holder.mDelete.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(mlist.get(position))
                    .into(holder.mImg);
        }
        holder.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.itemimg);
                holder.mImg.startAnimation(animation);
                if (mlist.get(position).equals(ADDICON)) {
                    MsgEvent msgEvent = new MsgEvent();
                    msgEvent.setNum(mlist.size());
                    EventBus.getDefault().post(msgEvent);
                } else {
                    Intent intent = new Intent(mContext, CircleImgDetailActivity.class);
                    intent.putStringArrayListExtra("imglist", (ArrayList<String>) mlist);
                    intent.putExtra("position", position);
                    intent.putExtra("action", "preview");
                    mContext.startActivity(intent);
                }
            }
        });


        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageBeanUtil.removeImg(mlist.get(position));
                UpImgDatautil.removeMapitem(mlist.get(position));
                mlist.remove(position);
                RecyclerAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mImg;
        ImageView mPlay;
        ImageView mDelete;

        public MyViewHolder(View itemView) {
            super(itemView);
            mImg = (ImageView) itemView.findViewById(R.id.add_circle_v2_img);
            mPlay = (ImageView) itemView.findViewById(R.id.add_circle_play);
            mDelete = (ImageView) itemView.findViewById(R.id.add_circle_v2_delete);
        }
    }
}
