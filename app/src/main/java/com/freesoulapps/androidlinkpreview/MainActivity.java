package com.freesoulapps.androidlinkpreview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.freesoulapps.preview.android.Preview;

/**
 * Created by Alex on 11/12/2015.
 */
public class MainActivity extends Activity {
    EditText mInputLink;
    Button mBtnPreview;
    Preview mPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_test_layout);
        mInputLink=(EditText)findViewById(R.id.inputLink);
        mBtnPreview=(Button)findViewById(R.id.btnPreview);
        mPreview=(Preview)findViewById(R.id.preview);
        if(mBtnPreview!=null)
            mBtnPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mInputLink!=null&&mInputLink.getText()!=null&&!mInputLink.getText().toString().equals(""))
                    {
                        mPreview.setData(mInputLink.getText().toString());
                    }
                }
            });
        //mPreview.setData("http://www.wallashops.co.il/%D7%98%D7%9C%D7%95%D7%95%D7%99%D7%96%D7%99%D7%94-50-LED-Smart-TV-NEON-%D7%93%D7%92%D7%9D-NE-50FLED/pi2ICNE50FLEDSMART");
    }
}
