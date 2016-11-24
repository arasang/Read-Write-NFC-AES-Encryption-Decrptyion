package prac.arasang.nfc_aes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOG_TAG = "MainActivity";

    private Button btnRead;
    private Button btnWrite;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    public void initView(){
        btnRead = (Button)findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);
        btnWrite = (Button)findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnRead) {
            intent = new Intent(MainActivity.this, ReadActivity.class);
            startActivity(intent);
        }

        if (v == btnWrite) {
            intent = new Intent(MainActivity.this, WriteActivity.class);
            startActivity(intent);
        }
    }
}
