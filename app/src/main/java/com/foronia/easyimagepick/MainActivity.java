package com.foronia.easyimagepick;

import android.os.Bundle;
import android.view.View;

import easyimagepick.foronia.com.library.ui.ImageUploadActivity;

public class MainActivity extends ImageUploadActivity implements View.OnClickListener{

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);
        setChildContentView(R.layout.activity_main);
    }

    @Override
    protected void init_datas() {
        super.init_datas();
    }

    @Override
    protected void findViews() {
        super.findViews();
    }

    @Override
    protected void set_datas(){
        super.set_datas();
        setLimit(3);//若不指定，默认为9
    }

    @Override
    protected void setOnclick(){
        super.setOnclick();
    }

    @Override
    public void onClick(View view) {
    }
}
