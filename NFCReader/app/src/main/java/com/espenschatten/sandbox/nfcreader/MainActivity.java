package com.espenschatten.sandbox.nfcreader;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "MainActivity";
    private ImageView mNfcIconImageView;
    private Animation pulse;
    private NfcAdapter nfcAdapter;
    private TextView nfcDisplayTextView;
    private NdefMessage[] msgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initImageNfcAnimation();
        initAdapter();
        initTextView();
    }

    private void initTextView() {
        nfcDisplayTextView = (TextView) findViewById(R.id.nfc_tag_information);
    }

    private void initAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void initImageNfcAnimation() {
        mNfcIconImageView = (ImageView) findViewById(R.id.nfc_icon_white);
        pulse = AnimationUtils.loadAnimation(this, R.anim.scale);
        mNfcIconImageView.startAnimation(pulse);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
               msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
        }
        //process the msgs array
    }

    private void enableForegroundDispatchSystem() {

    }

    @Override
    public void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void disableForegroundDispatchSystem() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nfc_icon_white:
                mNfcIconImageView.startAnimation(pulse);
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "NFC Discovered!", Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            StringBuilder builder = new StringBuilder();
            for (NdefMessage msg : msgs) {
                builder.append("Retrieved information: ").append(msg.toString()).append("\n");
            }
            nfcDisplayTextView.setText(builder);
        }
    }
}
