package com.poseidon.butterknifedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.poseidon.butterknife.ButterKnife;
import com.poseidon.butterknife_annotation.BindView;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.textview1)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // 构造TestActivityViewBinding对象即可
        ButterKnife.bind(this);
    }
}