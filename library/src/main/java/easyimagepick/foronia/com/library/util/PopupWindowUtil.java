package easyimagepick.foronia.com.library.util;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import easyimagepick.foronia.com.library.R;


/**
 * Created by Foronia on 2017/1/23.
 */

public class PopupWindowUtil implements View.OnClickListener{

    private Activity context;
    private LinearLayout ll_popup,item_three_line,item_video_line;
    private Button bt1,bt2,bt3,bt4,cancel;
    private RelativeLayout parent;
    private ItemOnclickListener itemOnclickListener;
    private PopupWindow pop;
    private String item1,item2,item3,item4;

    public PopupWindowUtil(final Activity context, ItemOnclickListener itemOnclickListener, String item1, String item2) {
        this.context = context;
        this.itemOnclickListener = itemOnclickListener;
        this.item1 = item1;
        this.item2 = item2;
        init();
    }

    public PopupWindowUtil(final Activity context, ItemOnclickListener itemOnclickListener, String item1, String item2, String item3) {
        this.context = context;
        this.itemOnclickListener = itemOnclickListener;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        init();
    }

    public PopupWindowUtil(final Activity context, ItemOnclickListener itemOnclickListener, String item1, String item2, String item3, String item4) {
        this.context = context;
        this.itemOnclickListener = itemOnclickListener;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        init();
    }

    private void init(){
        pop = new PopupWindow(context);
        View view = context.getLayoutInflater().inflate(R.layout.item_popupwindows, null);
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        parent = (RelativeLayout) view.findViewById(R.id.parent);
        bt1 = (Button) view
                .findViewById(R.id.item_popupwindows_camera);
        bt2 = (Button) view
                .findViewById(R.id.item_popupwindows_Photo);
        bt3 = (Button) view
                .findViewById(R.id.item_popupwindows_three);
        bt4 = (Button) view
                .findViewById(R.id.item_popupwindows_video);
        cancel = (Button) view
                .findViewById(R.id.item_popupwindows_cancel);
        item_three_line = (LinearLayout) view
                .findViewById(R.id.item_three_line);
        item_video_line = (LinearLayout) view
                .findViewById(R.id.item_video_line);
        parent.setOnClickListener(this);
        cancel.setOnClickListener(this);
        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);
        bt3.setOnClickListener(this);
        bt4.setOnClickListener(this);
        if (!item1.equals("")){
            bt1.setText(item1);
        }
        if (!item2.equals("")){
            bt2.setText(item2);
        }
        if (item3!=null){
            item_three_line.setVisibility(View.VISIBLE);
            bt3.setVisibility(View.VISIBLE);
            bt3.setText(item3);
        }
        if (item4!=null){
            item_video_line.setVisibility(View.VISIBLE);
            bt4.setVisibility(View.VISIBLE);
            bt4.setText(item4);
        }
    }

    public void start(View parentView){
        ll_popup.startAnimation(AnimationUtils.loadAnimation(context, R.anim.activity_translate_in));int navigationBar = 0;
        if (CheckNavigationBarUtil.checkDeviceHasNavigationBar(context.getApplicationContext())) {
            navigationBar = CheckNavigationBarUtil.getNavigationBarHeight(context.getApplicationContext());
        }
        pop.showAtLocation(parentView, Gravity.BOTTOM, 0, navigationBar);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.item_popupwindows_camera) {
            itemOnclickListener.itemOnclick1();
            pop.dismiss();
            ll_popup.clearAnimation();

        } else if (i == R.id.item_popupwindows_Photo) {
            itemOnclickListener.itemOnclick2();
            pop.dismiss();
            ll_popup.clearAnimation();

        } else if (i == R.id.item_popupwindows_three) {
            itemOnclickListener.itemOnclick3();
            pop.dismiss();
            ll_popup.clearAnimation();

        } else if (i == R.id.item_popupwindows_video) {
            itemOnclickListener.itemOnclick4();
            pop.dismiss();
            ll_popup.clearAnimation();

        } else if (i == R.id.item_popupwindows_cancel) {
            pop.dismiss();
            ll_popup.clearAnimation();

        } else if (i == R.id.parent) {
            pop.dismiss();
            ll_popup.clearAnimation();

        } else {
        }
    }

    public interface ItemOnclickListener{
        void itemOnclick1();
        void itemOnclick2();
        void itemOnclick3();
        void itemOnclick4();
    }
}
