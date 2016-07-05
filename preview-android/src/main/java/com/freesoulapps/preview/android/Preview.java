package com.freesoulapps.preview.android;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.victor.loading.rotate.RotateLoading;

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
    private TextView mTxtViewMessage;
    private Context mContext;
    private Handler mHandler;
    private String mTitle=null;
    private String mDescription=null;
    private String mImageLink=null;
    private String mSiteName=null;
    private String mSite;
    private String mLink;
    private RotateLoading mLoadingDialog;
    private FrameLayout mFrameLayout;
    private PreviewListener mListener;

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
        inflate(context, R.layout.preview_layout, this);
        mImgViewImage=(ImageView)findViewById(R.id.imgViewImage);
        mTxtViewTitle=(TextView)findViewById(R.id.txtViewTitle);
        mTxtViewDescription=(TextView)findViewById(R.id.txtViewDescription);
        mTxtViewSiteName=(TextView)findViewById(R.id.txtViewSiteName);
        mLoadingDialog=(RotateLoading)findViewById(R.id.rotateloading);
        mTxtViewMessage=(TextView)findViewById(R.id.txtViewMessage);

        mFrameLayout=(FrameLayout)findViewById(R.id.frameLoading);
        mFrameLayout.setVisibility(GONE);
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void setListener(PreviewListener listener)
    {
        this.mListener=listener;
    }

    public void setData(String title,String description,String image, String site)
    {
        clear();
        mTitle=title;
        mDescription=description;
        mImageLink=image;
        mSiteName=site;
        if (getTitle() != null) {
            Log.v(TAG, getTitle());
            if(getTitle().length()>=50)
                mTitle= getTitle().substring(0,49)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewTitle.setText(getTitle());
                }
            });
        }
        if (getDescription() != null) {
            Log.v(TAG, getDescription());
            if(getDescription().length()>=100)
                mDescription= getDescription().substring(0,99)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewDescription.setText(getDescription());
                }
            });

        }
        if (getImageLink() != null&&!getImageLink().equals("")) {
            Log.v(TAG, getImageLink());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mContext)
                            .load(getImageLink())
                            .into(mImgViewImage);
                }
            });

        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mContext)
                            .load(R.drawable.noimage)
                            .into(mImgViewImage);
                }
            });
        }
        if (getSiteName() != null) {
            Log.v(TAG, getSiteName());
            if(getSiteName().length()>=30)
                mSiteName= getSiteName().substring(0,29)+"...";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtViewSiteName.setText(getSiteName());
                }
            });
        }
    }

    public void setData(final String url)
    {
        if(!TextUtils.isEmpty(url)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFrameLayout.setVisibility(VISIBLE);
                    mLoadingDialog.start();
                }
            });
            clear();

            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException throwable) {
                        Log.e(TAG, throwable.getMessage());
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);


                        Elements titleElements;
                        Elements descriptionElements;
                        Elements imageElements;
                        Elements siteElements;
                        Elements linkElements;
                        String site = "";
                        Document doc = null;
                        doc = Jsoup.parse(response.body().string());
                        titleElements = doc.select("title");
                        descriptionElements = doc.select("meta[name=description]");
                        if (url.contains("bhphotovideo")) {
                            imageElements = doc.select("image[id=mainImage]");
                            site = "bhphotovideo";
                        } else if (url.contains("www.amazon.com/gp/aw/d")) {
                            imageElements = doc.select("image[id=mainImage]");
                            site = "www.amazon.com/gp/aw/d";
                        } else if (url.contains("www.amazon.com/")) {
                            imageElements = doc.select("img[data-old-hires]");
                            site = "www.amazon.com/";
                        } else if (url.contains("m.clove.co.uk")) {
                            imageElements = doc.select("img[id]");
                            site = "m.clove.co.uk";
                        } else if (url.contains("www.clove.co.uk")) {
                            imageElements = doc.select("li[data-thumbnail-path]");
                            site = "www.clove.co.uk";
                        } else
                            imageElements = doc.select("meta[property=og:image]");
                        mImageLink = getImageLinkFromSource(imageElements, site);
                        siteElements = doc.select("meta[property=og:site_name]");
                        linkElements = doc.select("meta[property=og:url]");

                        if (titleElements != null && titleElements.size() > 0) {
                            mTitle = titleElements.get(0).text();
                        }
                        if (descriptionElements != null && descriptionElements.size() > 0) {
                            mDescription = descriptionElements.get(0).attr("content");
                        }
                        if (linkElements != null && linkElements.size() > 0) {
                            mLink = linkElements.get(0).attr("content");
                        } else {
                            linkElements = doc.select("link[rel=canonical]");
                            if (linkElements != null && linkElements.size() > 0) {
                                mLink = linkElements.get(0).attr("href");
                            }
                        }
                        if (siteElements != null && siteElements.size() > 0) {
                            mSiteName = siteElements.get(0).attr("content");
                        }

                        if (getTitle() != null) {
                            Log.v(TAG, getTitle());
                            if (getTitle().length() >= 50)
                                mTitle = getTitle().substring(0, 49) + "...";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtViewTitle.setText(getTitle());
                                }
                            });
                        }
                        if (getDescription() != null) {
                            Log.v(TAG, getDescription());
                            if (getDescription().length() >= 100)
                                mDescription = getDescription().substring(0, 99) + "...";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtViewDescription.setText(getDescription());
                                }
                            });

                        }
                        if (getImageLink() != null && !getImageLink().equals("")) {
                            Log.v(TAG, getImageLink());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(mContext)
                                            .load(getImageLink())
                                            .into(mImgViewImage);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(mContext)
                                            .load(R.drawable.noimage)
                                            .into(mImgViewImage);
                                }
                            });
                        }
                        if (url.toLowerCase().contains("amazon"))
                            if (getSiteName() == null || getSiteName().equals(""))
                                mSiteName = "Amazon";
                        if (getSiteName() != null) {
                            Log.v(TAG, getSiteName());
                            if (getSiteName().length() >= 30)
                                mSiteName = getSiteName().substring(0, 29) + "...";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtViewSiteName.setText(getSiteName());
                                }
                            });
                        }

                        Log.v(TAG, "Link: " + getLink());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mLoadingDialog.isStart())
                                    mLoadingDialog.stop();
                                mFrameLayout.setVisibility(GONE);
                            }
                        });

                        mListener.onDataReady(Preview.this);
                    }
                });
            }
            catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingDialog.isStart())
                            mLoadingDialog.stop();
                        mFrameLayout.setVisibility(GONE);
                    }
                });


            }

        }
    }

    public void setMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(message==null)
                    mTxtViewMessage.setVisibility(GONE);
                else
                    mTxtViewMessage.setVisibility(VISIBLE);
                mTxtViewMessage.setText(message);
            }
        });
    }

    public void setMessage(final String message, final int color)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(message==null)
                    mTxtViewMessage.setVisibility(GONE);
                else
                    mTxtViewMessage.setVisibility(VISIBLE);
                mTxtViewMessage.setTextColor(color);
                mTxtViewMessage.setText(message);
            }
        });
    }

    private String getImageLinkFromSource(Elements elements,String site)
    {
        String imageLink=null;
        if (elements != null && elements.size() > 0) {
            switch (site)
            {
                case "m.clove.co.uk":
                case "bhphotovideo":
                    imageLink = elements.get(0).attr("src");
                    break;
                case "www.amazon.com/gp/aw/d":

                    break;
                case "www.amazon.com/":
                    imageLink = elements.get(0).attr("data-old-hires");
                    break;
                case "www.clove.co.uk":
                    imageLink="https://www.clove.co.uk"+elements.get(0).attr("data-thumbnail-path");
                    break;
                default:
                    imageLink = elements.get(0).attr("content");
                    break;
            }

        }
        return imageLink;
    }

    private void clear()
    {
        mImgViewImage.setImageResource(0);
        mTxtViewTitle.setText("");
        mTxtViewDescription.setText("");
        mTxtViewSiteName.setText("");
        mTxtViewMessage.setText("");
        mTitle=null;
        mDescription=null;
        mImageLink=null;
        mSiteName=null;
        mSite=null;
        mLink=null;
    }

    public interface PreviewListener {
        public void onDataReady(Preview preview);
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public String getSiteName() {
        return mSiteName;
    }

    public String getSite() {
        return mSite;
    }

    public String getLink() {
        return mLink;
    }
}
