package com.zhg.bluetoothdemo0630;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onMainClick(View view){
        switch (view.getId()){
            case R.id.btn_scanBLE:
                startActivity(new Intent(MainActivity.this,BluetoothActivity.class));
                break;
            default:
                break;
        }
    }
}
