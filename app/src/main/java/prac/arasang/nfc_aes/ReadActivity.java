package prac.arasang.nfc_aes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by user on 2016-11-24.
 */

public class ReadActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ReadActivity";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefMessage;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;

    private TextView txtNormal, txtEncryption, txtDecryption;

    private String nfc_card = "";

    private void initView(){
        txtNormal = (TextView)findViewById(R.id.txtNormal);
        txtEncryption = (TextView)findViewById(R.id.txtEncryption);
        txtDecryption = (TextView)findViewById(R.id.txtDecryption);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initNFC();

        setContentView(R.layout.activity_read);

        initView();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initNFC(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNdefMessage = new NdefMessage(createNewTextRecord(nfc_card, Locale.ENGLISH, true));

        /**Create an intent with TAG data and deliver to this activity.**/
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            intentFilter.addDataType("*/*");
            mIntentFilters = new IntentFilter[]{intentFilter};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mNFCTechLists = new String[][]{new String[]{NfcF.class.getName()}};

    }

    public NdefRecord createNewTextRecord(String text, Locale locale, boolean encodeUtf8){
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;

        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    /**When NFC Card is tagged, this method is called.**/
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Toast.makeText(this, "NFC Tag Detect", Toast.LENGTH_LONG).show();

        String data = "";

        Parcelable[] arrayData = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (arrayData != null) {
            for(int i=0; i<arrayData.length; i++){
                NdefRecord[] records = ((NdefMessage) arrayData[i]).getRecords();


                try {
                    for (int j=0; j<records.length; j++){
                        if (records[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(records[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = records[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            data += (new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding));

                            txtNormal.setText(data);
                            txtEncryption.setText(AES.Encrypt(data, "plain text"));
                            txtDecryption.setText(AES.Decrypt(data, "plain text"));
                        }
                    }
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundNdefPush(this, mNdefMessage);
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundNdefPush(this);
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }
}
