package com.aliyun.roompaas.app.viewmodel.inter;

import android.support.annotation.Nullable;
import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.whiteboard.exposable.ToolbarOrientation;


/**
 * Created by KyleCe on 2021/5/27
 */
public interface IWhiteBoardOperate {

    void whiteBoardProcess();

    String getRoomId();

    void openWhiteBoard(Callback<View> callback);

    void setToolbarOrientation(ToolbarOrientation orientation);

    void setToolbarVisibility(int visibility);

    void getScale(Callback<Float> callback);

    void setScale(float scale,@Nullable Runnable resultAction);

    void startWhiteboardRecording();

    IWhiteBoardOperate NULL = new IWhiteBoardOperate() {
        @Override
        public void whiteBoardProcess() {

        }

        @Override
        public String getRoomId() {
            return null;
        }

        @Override
        public void openWhiteBoard(Callback<View> callback) {

        }

        @Override
        public void setToolbarOrientation(ToolbarOrientation orientation) {

        }

        @Override
        public void setToolbarVisibility(int visibility) {

        }

        @Override
        public void getScale(Callback<Float> callback) {

        }

        @Override
        public void setScale(float scale, @Nullable Runnable resultAction) {

        }

        @Override
        public void startWhiteboardRecording() {

        }
    };
}
