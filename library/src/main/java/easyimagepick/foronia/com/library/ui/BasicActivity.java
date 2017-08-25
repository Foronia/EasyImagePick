package easyimagepick.foronia.com.library.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Created by Foronia on 2017/8/17 0017.
 */

public abstract class BasicActivity extends FragmentActivity {

//    public SharedPreferences preferences;
//    public String tokenId;
//    public MyProgressDialog myProgressDialog;
//    public String jsonStringFeedbackFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        initContentView(savedInstanceState);
        initBasic();
        init_datas();
        findViews();
        get_datas();
        set_datas();
        setOnclick();
    }

    private void initBasic(){
//        myProgressDialog = new MyProgressDialog(this);
//        preferences = SharePreferenceUtil.getPreferences();
//        tokenId = preferences.getString(SharePreferenceUtil.tokenId, "");
    }

    protected abstract void initContentView(Bundle savedInstanceState);

    protected abstract void init_datas();

    protected abstract void findViews();

    protected abstract void get_datas();

    protected abstract void set_datas();

    protected abstract void setOnclick();

}
