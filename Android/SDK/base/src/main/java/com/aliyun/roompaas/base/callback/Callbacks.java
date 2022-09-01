package com.aliyun.roompaas.base.callback;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CommonUtil;

/**
 * 回调函数实现类
 *
 * @author puke
 * @version 2021/5/27
 */
public class Callbacks {

    /**
     * 支持打Log
     *
     * @param <T>
     */
    public static class Log<T> implements Callback<T> {

        private final String tag;
        private final String action;

        public Log(String tag, String action) {
            this.tag = tag;
            this.action = action;
        }

        @Override
        public void onSuccess(T data) {
            Logger.i(tag, String.format("%s success, data: %s", action, JSON.toJSONString(data)));
        }

        @Override
        public void onError(String errorMsg) {
            Logger.e(tag, String.format("%s error, message: %s", action, errorMsg));
        }
    }

    /**
     * 支持弹Toast
     *
     * @param <T>
     */
    public static class Toast<T> implements Callback<T> {

        private final Context context;
        private final String action;

        public Toast(Context context, String action) {
            this.context = context;
            this.action = action;
        }

        @Override
        public void onSuccess(T data) {
            CommonUtil.showToast(context, action + "成功");
        }

        @Override
        public void onError(String errorMsg) {
            CommonUtil.showToast(context, action + "失败: " + errorMsg);
        }
    }

    /**
     * 支持外部Lambda语法糖调用
     *
     * @param <T>
     */
    public static class Lambda<T> implements Callback<T> {

        private final CallbackWrapper<T> wrapper;

        public interface CallbackWrapper<T> {
            void onCall(boolean success, T data, String errorMsg);
        }

        public Lambda(CallbackWrapper<T> callback) {
            this.wrapper = callback;
        }

        @Override
        public void onSuccess(T data) {
            if (wrapper != null) {
                wrapper.onCall(true, data, null);
            }
        }

        @Override
        public void onError(String errorMsg) {
            if (wrapper != null) {
                wrapper.onCall(false, null, errorMsg);
            }
        }
    }

    public static class PosLambda<T> implements Callback<T> {

        private final CallbackWrapper<T> wrapper;

        public interface CallbackWrapper<T> {
            void onCall(T data);
        }

        public PosLambda(CallbackWrapper<T> callback) {
            this.wrapper = callback;
        }

        @Override
        public void onSuccess(T data) {
            if (wrapper != null) {
                wrapper.onCall(data);
            }
        }

        @Override
        public void onError(String errorMsg) {
            // do nothing
        }
    }
}
