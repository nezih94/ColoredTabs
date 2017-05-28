package com.nezihyilmaz.coloredtabs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorTabLayout colorTabLayout=(ColorTabLayout)findViewById(R.id.colorTabs);
        colorTabLayout.setListener(new TabSelectionListener() {
            @Override
            public void onTabSelected(int position) {
                Log.d("position",String.valueOf(position));
            }
        });
    }
}
