package com.espenschatten.sandbox.nfcreader;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NdefTaskListener {

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
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.NFC);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            handleIntent(getIntent());
        } else {
            nfcDisplayTextView.setText("The application does not have the permission to use NFC.");
        }

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
        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    public void onPause() {
        stopForegroundDispatch(this, nfcAdapter);
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nfc_icon_white:
                pulse = AnimationUtils.loadAnimation(this, R.anim.scale);
                mNfcIconImageView.startAnimation(pulse);
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if ("text/plain".equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(tag);

            } else {
                Log.d(LOG_TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this).execute(tag);
                    break;
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    public void onNfcInformationRetrieved(String result) {
        nfcDisplayTextView.setText(result);
    }

    @Override
    public void onNfcInformationNotRetrieved(String reason) {
        nfcDisplayTextView.setText(reason);
    }
}
