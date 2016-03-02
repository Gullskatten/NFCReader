package com.espenschatten.sandbox.nfcreader;

public interface NdefTaskListener {

    void onNfcInformationRetrieved(String result);

    void onNfcInformationNotRetrieved(String reason);

}
