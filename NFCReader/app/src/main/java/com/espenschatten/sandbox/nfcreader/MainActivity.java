package com.espenschatten.sandbox.nfcreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements NdefTaskListener {
    private TextView mTextView;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.nfc_tag_information);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;

        }
        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("NFC is disabled.");
        } else {
            mTextView.setText("NFC information will appear here!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();

    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    @Override
    protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

        Toast.makeText(getApplicationContext(), "- NFC received -", Toast.LENGTH_SHORT).show();
        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(parcelables != null && parcelables.length > 0) {
                readFromNdefMessages((NdefMessage)parcelables[0]);
            }
        }
    }

    private void readFromNdefMessages(NdefMessage parcelable) {
        NdefRecord[] ndefRecords = parcelable.getRecords();

        if(ndefRecords != null && ndefRecords.length > 0) {
            String tagContent = getTextFromNdefRecord(ndefRecords[0]);

            mTextView.setText(tagContent);
        }
    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;

        try {
            byte[] payload = ndefRecord.getPayload();
            boolean hasEncodedFormatUTF8 = (payload[0] & 128) == 0;
            String textEncoding = getString(hasEncodedFormatUTF8);

            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize+1, payload.length - languageSize - 1, textEncoding);

         } catch (UnsupportedEncodingException e) {
            Log.e("MainActivity", "Unsupported encoding..");
        }

        return tagContent;
    }

    @NonNull
    private String getString(boolean expression) {
        return (expression) ? "UTF-8" : "UTF-16";
    }

    public void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[]{};
        String[][] techList = new String[][]{};

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techList);
    }

    public void disableForegroundDispatchSystem() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNfcInformationRetrieved(String result) {
        mTextView.setText(result);
    }

    @Override
    public void onNfcInformationNotRetrieved(String reason) {
        mTextView.setText(reason);
    }
}
