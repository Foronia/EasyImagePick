package easyimagepick.foronia.com.library.customview;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import easyimagepick.foronia.com.library.R;


/**
 * @description 基本的dialog,只有一个view的可以直接用
 * Created by Foronia on 2016/6/20.
 */
public class BaseDialog extends Dialog {

    protected Context context;
    protected View view;
    private LayoutParams layoutParams;

    public BaseDialog(Context context, View view, LayoutParams layoutParams) {
        super(context, R.style.BaseDialog);
        this.context = context;
        this.view = view;
        this.layoutParams = layoutParams;
        initBase();
    }

    private void initBase() {
        setContentView(view, layoutParams);
        setCanceledOnTouchOutside(false);
    }
}
