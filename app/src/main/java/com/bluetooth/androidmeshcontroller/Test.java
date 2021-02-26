package com.bluetooth.androidmeshcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Test extends AppCompatActivity {

    private TextView tvOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tvOut = findViewById(R.id.tvOut);
        showOutput();
    }

    private void showOutput() {
        int[] juice = {2,3};
        int[] capacity ={3,4};
        int maxMix = 0;
        // write your code in Java SE 8
        for(int i=0;i<juice.length;i++)
        {
            int mixx = 1;
            int capacityLeft =   capacity[i] - juice[i];
            if(capacityLeft <= 0)
            {
                continue;
            }else
            {
                for(int j = 0; j<capacity.length;j++)
                {
                    if(i == j) continue;

                    if(juice[j] <= capacityLeft)
                    {
                        capacityLeft = capacityLeft - juice[j];
                        mixx++;
                    }else
                    {
                        if(maxMix < mixx)
                            maxMix = mixx;

                        mixx =1;
                        capacityLeft = capacity[i]-juice[i];
                    }
                }
            }
            if(maxMix < mixx)
                maxMix = mixx;
        }
        out("Max Flavours mix  = "+maxMix);
    }

    private void out(String s)
    {
        tvOut.setText("Output:\n"+s);
    }
}