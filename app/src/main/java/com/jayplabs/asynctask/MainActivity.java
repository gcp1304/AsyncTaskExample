package com.jayplabs.asynctask;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import com.jayplabs.asynctask.HeadlessFragment.TaskListener;

public class MainActivity extends AppCompatActivity implements TaskListener, OnClickListener{

    private ProgressDialog mProgressDialog;
    private HeadlessFragment mFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        mFragment = new HeadlessFragment();
        getSupportFragmentManager().beginTransaction().add(mFragment, "Head").commit();

    }

    @Override
    public void onTaskStarted() {
        mProgressDialog = ProgressDialog.show(this, "Loading", "Please wait for a moment!");
    }

    @Override
    public void onTaskFinished(String result) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start) {
            mFragment.startTask();
        }

    }


}
