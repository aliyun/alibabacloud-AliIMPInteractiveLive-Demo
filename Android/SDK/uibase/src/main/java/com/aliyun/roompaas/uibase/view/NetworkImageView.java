package com.aliyun.roompaas.uibase.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.ThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络图片
 */
public class NetworkImageView extends AppCompatImageView {
    public static final int GET_DATA_SUCCESS = 1;
    public static final int GET_DATA_NETWORK_ERROR = 2;
    public static final int GET_DATA_SERVER_ERROR = 3;

    public NetworkImageView(Context context) {
        super(context);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    setImageBitmap(bitmap);
                    break;
                case GET_DATA_NETWORK_ERROR:
                case GET_DATA_SERVER_ERROR:
                    Logger.e("NetworkImageView download is error.");
                    break;
            }
        }
    };

    public void setImageUrl(final String imageUrl) {
        if (imageUrl != null) {
            ThreadUtil.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    InputStream inputStream = null;
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        int code = connection.getResponseCode();
                        if (code == 200) {
                            inputStream = connection.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            Message msg = Message.obtain();
                            msg.obj = bitmap;
                            msg.what = GET_DATA_SUCCESS;
                            handler.sendMessage(msg);
                        } else {
                            handler.sendEmptyMessage(GET_DATA_SERVER_ERROR);
                        }
                    } catch (Exception e) {
                        handler.sendEmptyMessage(GET_DATA_NETWORK_ERROR);
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
}
