package com.freesoulapps.preview.android;

import android.content.Context;
import android.os.Handler;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Alex on 11/12/2015.
 */
public class Preview extends RelativeLayout {
    private static String TAG = Preview.class.getSimpleName();
    private ImageView mImgViewImage;
    private TextView mTxtViewTitle;
    private TextView mTxtViewDescription;
    private TextView mTxtViewSiteName;
    private Context mContext;
    private Handler mHandler;
    private String mTitle=null;
    private String mDescription=null;
    private String mImageLink=null;
    private String mSiteName=null;

    public Preview(Context context) {
        super(context);
        initialize(context);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Preview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context){
        mContext=context;
        inflate(context, R.layout.preview_layout_new, this);
        mImgViewImage=(ImageView)findViewById(R.id.imgViewImage);
        mTxtViewTitle=(TextView)findViewById(R.id.txtViewTitle);
        mTxtViewDescription=(TextView)findViewById(R.id.txtViewDescription);
        mTxtViewSiteName=(TextView)findViewById(R.id.txtViewSiteName);
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void setData(String title,String description,String image, String site)
    {
        mTitle=title;
        mDescription=description;
        mImageLink=image;
        mSiteName=site;
        if (mTitle != null) {
            Log.v(TAG, mTitle);
            if(mTitle.length()>=50)
                mTitle=mTitle.substring(0,49)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewTitle.setText(mTitle);
                }
            });
        }
        if (mDescription != null) {
            Log.v(TAG, mDescription);
            if(mDescription.length()>=100)
                mDescription=mDescription.substring(0,99)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewDescription.setText(mDescription);
                }
            });

        }
        if (mImageLink != null) {
            Log.v(TAG, mImageLink);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mContext)
                            .load(mImageLink)
                            .into(mImgViewImage);
                }
            });

        }
        if (mSiteName != null) {
            Log.v(TAG, mSiteName);
            if(mSiteName.length()>=30)
                mSiteName=mSiteName.substring(0,29)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewSiteName.setText(mSiteName);
                }
            });
        }
    }

    public void setData(final String url)
    {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Request request, IOException throwable) {
                Log.e(TAG,throwable.getMessage());
            }

            @Override public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Elements titleElements;
                Elements descriptionElements;
                Elements imageElements;
                Elements siteElements;
                Document doc = null;
                doc = Jsoup.parse(response.body().string());
                titleElements = doc.select("title");
                descriptionElements = doc.select("meta[name=description]");
                imageElements = doc.select("meta[property=og:image]");
                siteElements=doc.select("meta[property=og:site_name]");

                if (titleElements != null && titleElements.size() > 0) {
                    mTitle = titleElements.get(0).text();
                }
                if (descriptionElements != null && descriptionElements.size() > 0) {
                    mDescription = descriptionElements.get(0).attr("content");
                }
                if (imageElements != null && imageElements.size() > 0) {
                    mImageLink = imageElements.get(0).attr("content");
                }
                else
                {
                    imageElements = doc.select("img[data-old-hires]");
                    if (imageElements != null && imageElements.size() > 0) {
                        mImageLink = imageElements.get(0).attr("data-old-hires");
                    }
                }
                if(siteElements!=null&& siteElements.size()>0)
                {
                    mSiteName = siteElements.get(0).attr("content");
                }

                if (mTitle != null) {
                    Log.v(TAG, mTitle);
                    if(mTitle.length()>=50)
                        mTitle=mTitle.substring(0,49)+"...";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTxtViewTitle.setText(mTitle);
                        }
                    });
                }
                if (mDescription != null) {
                    Log.v(TAG, mDescription);
                    if(mDescription.length()>=100)
                        mDescription=mDescription.substring(0,99)+"...";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTxtViewDescription.setText(mDescription);
                        }
                    });

                }
                if (mImageLink != null) {
                    Log.v(TAG, mImageLink);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext)
                                    .load(mImageLink)
                                    .into(mImgViewImage);
                        }
                    });

                }
                if(url.toLowerCase().contains("amazon"))
                    if(mSiteName==null||mSiteName.equals(""))
                        mSiteName="Amazon";
                if (mSiteName != null) {
                    Log.v(TAG, mSiteName);
                    if(mSiteName.length()>=30)
                        mSiteName=mSiteName.substring(0,29)+"...";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTxtViewSiteName.setText(mSiteName);
                        }
                    });
                }
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

}
