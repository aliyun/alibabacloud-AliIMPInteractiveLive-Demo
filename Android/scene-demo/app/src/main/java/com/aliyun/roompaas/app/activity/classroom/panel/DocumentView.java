package com.aliyun.roompaas.app.activity.classroom.panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.classroom.ClassroomActivity;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.roompaas.whiteboard.exposable.ToolbarOrientation;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


/**
 * @author puke
 * @version 2021/5/24
 */
public class DocumentView extends BasePanelView {
    public static final String TAG = "DocumentView";
    private boolean isWhiteBoardOpen = false;
    private TextView switchWhiteBoard;
    private IWhiteBoardOperate whiteBoardOperate;
    private Reference<ClassroomActivity> classroomActivityRef;

    public DocumentView(@NonNull Context context) {
        super(context);
        classroomActivityRef = new WeakReference<>(context instanceof ClassroomActivity ? (ClassroomActivity) context : null);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_doc_view, null, false);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initView(view);

        if (context instanceof IWhiteBoardOperate) {
            whiteBoardOperate = (IWhiteBoardOperate) context;
        }
    }

    private void initView(View root) {
        switchWhiteBoard = root.findViewById(R.id.switchWhiteBoard);
        switchWhiteBoard.setOnClickListener(this::switchWhiteBoardClicked);
    }

    @NonNull
    @Override
    protected String getText() {
        return "";
    }

    public void switchWhiteBoardClicked(View view) {
        isWhiteBoardOpen = !isWhiteBoardOpen;
        switchWhiteBoard.setText(isWhiteBoardOpen ? "????????????" : "????????????");

        if (isWhiteBoardOpen) {
            openWhiteBoard(view);
        } else {
            closeWhiteBoard();
        }
    }

    private void openWhiteBoard(View view) {
        if (whiteBoardOperate == null) {
            return;
        }

        whiteBoardOperate.openWhiteBoard(new Callback<View>() {
            @Override
            public void onSuccess(View data) {
                Log.i(TAG, "openWhiteBoard: onSuccess");
                int count = getChildCount();
                boolean alreadyAdded = false;
                String roomId = whiteBoardOperate.getRoomId();
                whiteBoardOperate.setToolbarOrientation(ToolbarOrientation.TOOLBAR_ORIENTATION_BOTTOM);
                ClassroomActivity act = Utils.getRef(classroomActivityRef);
                whiteBoardOperate.setToolbarVisibility(act != null && act.isOwner() ? View.VISIBLE : View.GONE);
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    Object tag = child.getTag(R.integer.viewTagIdForRoomId);
                    if (!TextUtils.isEmpty(roomId) && roomId.equals(tag)) {
                        alreadyAdded = true;
                        child.setVisibility(View.VISIBLE);
                    }
                }
                if (!alreadyAdded) {
                    ViewUtil.removeSelfSafely(data);
                    addView(data, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    reloadPageIfPossible(data);
                    data.setTag(R.integer.viewTagIdForRoomId, roomId);
                }
            }

            private void reloadPageIfPossible(View child) {
                if (child instanceof WebView) {
                    reload((WebView) child);
                } else if (child instanceof ViewGroup) {
                    ViewGroup vp = (ViewGroup) child;
                    int count = vp.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View c = vp.getChildAt(i);
                        if (c instanceof WebView) {
                            reload((WebView) c);
                            break;
                        } else if (c instanceof ViewGroup) {
                            reloadPageIfPossible(c);
                        }
                    }
                }
            }

            private void reload(WebView wv) {
                wv.reload();
            }

            @Override
            public void onError(String errorMsg) {
                Log.i(TAG, "openWhiteBoard: onError");
            }
        });
    }

    private void closeWhiteBoard() {
        String roomId = whiteBoardOperate != null ? whiteBoardOperate.getRoomId() : "";

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            Object tag = child.getTag(R.integer.viewTagIdForRoomId);
            if (!TextUtils.isEmpty(roomId) && roomId.equals(tag)) {
                ViewUtil.setInvisible(child);
            }
        }
    }
}
