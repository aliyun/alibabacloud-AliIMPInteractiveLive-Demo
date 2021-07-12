package com.aliyun.roompaas.app.activity.classroom.panel;

import android.text.TextUtils;
import android.util.Pair;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.classroom.ClassroomActivity;
import com.aliyun.roompaas.app.helper.RecyclerViewHelper;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * @author puke
 * @version 2021/5/24
 */
public class StudentView extends FrameLayout {

    private final RecyclerViewHelper<RtcUser> recyclerViewHelper;
    private WeakReference<ClassroomActivity> activityRef;

    public StudentView(@NonNull ClassroomActivity activity) {
        super(activity);
        activityRef = new WeakReference<>(activity);
        inflate(activity, R.layout.view_student, this);
        RecyclerView recyclerView = findViewById(R.id.student_recycler_view);
        recyclerViewHelper = RecyclerViewHelper.of(
                recyclerView, R.layout.item_student, (holder, model, position, itemCount) -> {
                    // 获取子View
                    TextView nick = holder.getView(R.id.item_title);
                    TextView status = holder.getView(R.id.item_status);
                    Button action = holder.getView(R.id.item_action);

                    // 获取当前用户数据信息
                    // 是否是老师
                    boolean isTeacher = activity.isOwner();
                    // 是否已入会
                    boolean isJoined = activity.isJoined();

                    // 设置昵称
                    String nickName = (TextUtils.isEmpty(model.nick) ? model.userId : model.nick);
                    nickName = nickName.equals(activity.getUserId()) ? nickName + "(我)" : nickName;
                    nick.setText(nickName);

                    boolean isTeacherSelf = isTeacherSelf(model);
                    // 设置会议状态 (仅限老师或入会的人才展示状态)
                    if ((isTeacher || isJoined) && !isTeacherSelf) {
                        status.setVisibility(VISIBLE);
                        final String statusText;
                        if (model.status == RtcUserStatus.JOIN_FAILED) {
                            // 入会失败的状态, 跟"离会"保持一致
                            statusText = RtcUserStatus.LEAVE.getDesc();
                        } else {
                            statusText = model.status.getDesc();
                        }
                        status.setText(statusText);
                    } else {
                        status.setVisibility(INVISIBLE);
                    }

                    // 设置Action按钮 (仅限老师才展示Action)
                    if (isTeacher && !isTeacherSelf) {
                        action.setVisibility(VISIBLE);
                        switch (model.status) {
                            case ACTIVE:
                                action.setText("挂断");
                                action.setOnClickListener(v -> activity.onKickFromChannel(model.userId));
                                break;
                            case ON_JOINING:
                                action.setText("再次邀请");
                                action.setOnClickListener(v -> activity.onInviteUser(model));
                                break;
                            case APPLYING:
                                action.setText("处理连麦申请");
                                action.setOnClickListener(v -> DialogUtil.showCustomDialog(
                                        activity, "是否允许上麦?",
                                        new Pair<>("同意", () -> activity.onHandleUserApply(model, true)),
                                        new Pair<>("拒绝", () -> activity.onHandleUserApply(model, false))
                                ));
                                break;
                            case LEAVE:
                            case JOIN_FAILED:
                                action.setText("邀请连麦");
                                action.setOnClickListener(v -> activity.onInviteUser(model));
                                break;
                            default:
                                action.setOnClickListener(null);
                                break;
                        }
                    } else {
                        action.setVisibility(GONE);
                    }

                    holder.itemView.setOnClickListener(v -> activity.onUserClick(model));
                }
        );
    }

    private boolean isTeacherSelf(RtcUser model) {
        ClassroomActivity act = activityRef != null ? activityRef.get() : null;
        String userId = act != null ? act.getUserId() : "";
        boolean isOwner = act != null && act.isOwner();
        return isOwner && TextUtils.equals(userId, model.userId);
    }

    public void setData(List<RtcUser> newData) {
        recyclerViewHelper.setData(newData);
    }

    public void addData(RtcUser model) {
        List<RtcUser> dataList = recyclerViewHelper.getDataList();
        for (RtcUser rtcUser : dataList) {
            if (TextUtils.equals(rtcUser.userId, model.userId)) {
                // 当前列表也有该用户, 不做添加处理
                return;
            }
        }
        recyclerViewHelper.addData(Collections.singletonList(model));
    }

    public List<RtcUser> getData() {
        return recyclerViewHelper.getDataList();
    }
}
