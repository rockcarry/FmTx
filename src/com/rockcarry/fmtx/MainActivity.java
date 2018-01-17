package com.rockcarry.fmtx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "FmTx";
    private static final int MIN_FMTX_FREQ = 880;
    private static final int MAX_FMTX_FREQ = 920;

    private Button   mBtnFreqInc   = null;
    private Button   mBtnFreqDec   = null;
    private CheckBox mBtnFmTxOnOff = null;
    private SeekBar  mBarFmTxFreq  = null;
    private TextView mTxtFmTxFreq  = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnFreqInc = (Button)findViewById(R.id.btn_freq_inc);
        mBtnFreqDec = (Button)findViewById(R.id.btn_freq_dec);
        mBtnFreqInc.setOnClickListener(mOnClickListener);
        mBtnFreqDec.setOnClickListener(mOnClickListener);

        mBtnFmTxOnOff = (CheckBox)findViewById(R.id.btn_fmonoff);
        mBtnFmTxOnOff.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BootReceiver.setFmTxFreq(MainActivity.this, isChecked, BootReceiver.getFmTxFreq(MainActivity.this));
            }
        });

        mBarFmTxFreq = (SeekBar)findViewById(R.id.sb_freq);
        mBarFmTxFreq.setMax(MAX_FMTX_FREQ - MIN_FMTX_FREQ);
        mBarFmTxFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int freq = MIN_FMTX_FREQ + seekBar.getProgress();
                mTxtFmTxFreq.setText(String.format("%.1f", freq / 10.0f));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                int freq = MIN_FMTX_FREQ + seekBar.getProgress();
                BootReceiver.setFmTxFreq(MainActivity.this, BootReceiver.getFmTxOnOff(MainActivity.this), freq);
            }
        });

        mTxtFmTxFreq = (TextView)findViewById(R.id.txt_freq);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_freq_inc:
                mBarFmTxFreq.setProgress(mBarFmTxFreq.getProgress() + 1);
                break;
            case R.id.btn_freq_dec:
                mBarFmTxFreq.setProgress(mBarFmTxFreq.getProgress() - 1);
                break;
            }
            int freq = MIN_FMTX_FREQ + mBarFmTxFreq.getProgress();
            mTxtFmTxFreq.setText(String.format("%.1f", freq / 10.0f));
            BootReceiver.setFmTxFreq(MainActivity.this, BootReceiver.getFmTxOnOff(MainActivity.this), freq);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBtnFmTxOnOff.setChecked(BootReceiver.getFmTxOnOff(this));
        mBarFmTxFreq .setProgress(BootReceiver.getFmTxFreq(this) - MIN_FMTX_FREQ);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

