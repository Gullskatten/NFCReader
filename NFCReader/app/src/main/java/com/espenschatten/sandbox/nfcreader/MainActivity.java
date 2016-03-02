package com.espenschatten.sandbox.nfcreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView mNfcIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageNfcAnimation();
    }

    private void initImageNfcAnimation() {
        mNfcIconImageView = (ImageView) findViewById(R.id.nfc_icon_white);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.scale);
        mNfcIconImageView.startAnimation(pulse);
    }

}
