package com.aliyun.roompaas.app.manager;

import android.text.TextUtils;

import com.alibaba.dingpaas.room.RoomUserModel;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.model.UserParam;
import com.aliyun.roompaas.rtc.RtcApplyUserParam;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcUserParam;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rtc用户管理器
 *
 * @author puke
 * @version 2021/6/15
 */
public class RtcUserManager {

    private static final String TAG = RtcUserManager.class.getSimpleName();

    // 房间用户列表的最大人数
    private static final int MAX_USER_COUNT_4_ROOM = 500;
    // Rtc用户列表的最大人数
    private static final int MAX_USER_COUNT_4_RTC = 200;
    // Rtc申请连麦用户列表的最大人数
    private static final int MAX_APPLY_USER_COUNT_4_RTC = 200;
    // 默认用户排序
    private static final List<RtcUserStatus> DEFAULT_USER_ORDER = Arrays.asList(
            RtcUserStatus.ACTIVE,
            RtcUserStatus.ON_JOINING,
            RtcUserStatus.APPLYING,
            RtcUserStatus.JOIN_FAILED,
            RtcUserStatus.LEAVE
    );

    private final RoomChannel roomChannel;
    private final RtcService rtcService;

    // userId -> User 映射关系
    private final Map<String, RtcUser> userId2User = new HashMap<>();

    // 用户排序方式
    private final List<RtcUserStatus> userOrder = DEFAULT_USER_ORDER;

    public RtcUserManager(RoomChannel roomChannel) {
        this.roomChannel = roomChannel;
        this.rtcService = roomChannel.getPluginService(RtcService.class);
    }

    /**
     * 更新用户
     *
     * @param user 变更的用户信息
     */
    public void updateUser(RtcUser user) {
        if (user == null) {
            return;
        }
        if (isTeacher(user.userId)) {
            // 忽略老师
            return;
        }

        Logger.i(TAG, String.format("setUserStatus, user=%s", JSON.toJSONString(user)));
        String userId = user.userId;
        RtcUser existsUser = userId2User.get(userId);
        if (existsUser == null) {
            userId2User.put(userId, user);
        } else {
            existsUser.status = user.status;
        }
    }

    /**
     * @return 返回已排序的用户列表
     */
    public List<RtcUser> getUserList() {
        List<RtcUser> userList = new ArrayList<>(userId2User.values());
        sortUserList(userList);
        return userList;
    }

    /**
     * 查询用户列表的分页数据
     *
     * @param queryRtcUserList 是否查询rtc用户列表
     * @param callback         结果回调
     */
    public void loadUserList(boolean queryRtcUserList, Callback<List<RtcUser>> callback) {
        UICallback<List<RtcUser>> uiCallback = new UICallback<List<RtcUser>>(callback);

        if (queryRtcUserList) {
            // 1. 请求Rtc成员列表 + 房间成员列表
            // 1.1 请求会议成员列表
            loadAllUserOfRtc(new Callback<List<ConfUserModel>>() {
                @Override
                public void onSuccess(List<ConfUserModel> rtcUserList) {
                    // 1.2 请求会议申请连麦成员列表
                    loadAllApplyUserOfRtc(new Callback<List<ConfUserModel>>() {
                        @Override
                        public void onSuccess(List<ConfUserModel> applyUserList) {
                            // 1.3 请求房间成员列表
                            loadAllUserOfRoom(new Callback<List<RoomUserModel>>() {
                                @Override
                                public void onSuccess(List<RoomUserModel> roomUserList) {
                                    // 1.4 merge会议成员列表、申请连麦列表和房间成员列表
                                    List<RtcUser> result = mergeRtcUserAndRoomUser(
                                            rtcUserList, applyUserList, roomUserList);
                                    // 1.5 返回merge后的用户列表数据
                                    uiCallback.onSuccess(result);
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    uiCallback.onError(errorMsg);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMsg) {
                            uiCallback.onError(errorMsg);
                        }
                    });
                }

                @Override
                public void onError(String errorMsg) {
                    uiCallback.onError(errorMsg);
                }
            });
        } else {
            // 2. 只请求房间成员列表数据
            loadAllUserOfRoom(new Callback<List<RoomUserModel>>() {
                @Override
                public void onSuccess(List<RoomUserModel> list) {
                    userId2User.clear();
                    // 房间用户列表的用户都是会议离开的状态, 不需要做排序处理
                    List<RtcUser> result = new ArrayList<>();
                    for (RoomUserModel userModel : list) {
                        if (isTeacher(userModel.openId)) {
                            // 忽略老师
                            continue;
                        }

                        RtcUser rtcUser = roomUser2RtcUser(userModel);
                        result.add(rtcUser);
                        userId2User.put(userModel.openId, rtcUser);
                    }
                    uiCallback.onSuccess(result);
                }

                @Override
                public void onError(String errorMsg) {
                    uiCallback.onError(errorMsg);
                }
            });
        }
    }

    @NotNull
    protected List<RtcUser> mergeRtcUserAndRoomUser(List<ConfUserModel> confUserModels,
                                                    List<ConfUserModel> applyUserList,
                                                    List<RoomUserModel> roomUserModels) {
        List<RtcUser> result = new ArrayList<>();
        // 清除老数据
        userId2User.clear();

        // 1. 会议成员列表
        boolean hasRtcUserList = CollectionUtil.isNotEmpty(confUserModels);
        if (hasRtcUserList) {
            for (ConfUserModel confUserModel : confUserModels) {
                String userId = confUserModel.userId;
                if (userId2User.containsKey(userId)) {
                    // 已添加过, 不再重复添加, 去重
                    continue;
                }
                if (isTeacher(userId)) {
                    // 忽略老师
                    continue;
                }

                RtcUser rtcUser = new RtcUser();
                rtcUser.userId = userId;
                rtcUser.nick = confUserModel.nickname;
                rtcUser.status = RtcUserStatus.of(confUserModel.status);

                userId2User.put(userId, rtcUser);
                result.add(rtcUser);
            }
        }

        // 2. 会议申请连麦成员列表
        boolean hasRtcApplyUserList = CollectionUtil.isNotEmpty(applyUserList);
        if (hasRtcApplyUserList) {
            for (ConfUserModel confUserModel : applyUserList) {
                String userId = confUserModel.userId;
                if (userId2User.containsKey(userId)) {
                    // 已添加过, 不再重复添加, 去重
                    continue;
                }
                if (isTeacher(userId)) {
                    // 忽略老师
                    continue;
                }

                RtcUser rtcUser = new RtcUser();
                rtcUser.userId = userId;
                rtcUser.nick = confUserModel.nickname;
                rtcUser.status = RtcUserStatus.APPLYING;

                userId2User.put(userId, rtcUser);
                result.add(rtcUser);
            }
        }

        // 3. 房间成员列表
        if (CollectionUtil.isNotEmpty(roomUserModels)) {
            for (RoomUserModel roomUserModel : roomUserModels) {
                String userId = roomUserModel.openId;
                if (isTeacher(userId)) {
                    // 忽略老师
                    continue;
                }

                RtcUser rtcUser = userId2User.get(userId);
                if (rtcUser != null) {
                    // 列表中已经存在当前用户时, 不再merge
                    if (TextUtils.isEmpty(rtcUser.nick)) {
                        rtcUser.nick = roomUserModel.nick;
                    }
                    continue;
                }

                rtcUser = new RtcUser();
                userId2User.put(userId, rtcUser);

                rtcUser.userId = userId;
                rtcUser.nick = roomUserModel.nick;
                rtcUser.status = RtcUserStatus.LEAVE;

                result.add(rtcUser);
            }
        }

        // 4. 有rtc相关用户列表时, 需要排序
        if (hasRtcUserList || hasRtcApplyUserList) {
            sortUserList(result);
        }

        return result;
    }

    private RtcUser roomUser2RtcUser(RoomUserModel roomUserModel) {
        RtcUser rtcUser = new RtcUser();
        rtcUser.userId = roomUserModel.openId;
        rtcUser.nick = roomUserModel.nick;
        rtcUser.status = RtcUserStatus.LEAVE;
        return rtcUser;
    }

    // 根据会议状态对用户列表排序
    private void sortUserList(List<RtcUser> userList) {
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }

        Collections.sort(userList, (o1, o2) -> {
            int index1 = userOrder.indexOf(o1.status);
            int index2 = userOrder.indexOf(o2.status);
            if (index1 < 0 && index2 < 0) {
                // 同时不包含o1和o2, 不排序
                return 0;
            } else if (index1 < 0) {
                // 不包含o1, o1排后面
                return 1;
            } else if (index2 < 0) {
                // 不包含o2, o2排后面
                return -1;
            } else {
                // 同时包含, 按顺序排列
                return index1 - index2;
            }
        });
    }

    // 查询房间在线列表
    private void loadAllUserOfRoom(Callback<List<RoomUserModel>> callback) {
        UserParam param = new UserParam();
        param.pageNum = 1;
        param.pageSize = MAX_USER_COUNT_4_ROOM;
        roomChannel.listUser(param, new Callback<PageModel<RoomUserModel>>() {
            @Override
            public void onSuccess(PageModel<RoomUserModel> data) {
                callback.onSuccess(data.list);
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    // 查询所有申请连麦成员
    private void loadAllApplyUserOfRtc(Callback<List<ConfUserModel>> callback) {
        RtcApplyUserParam param = new RtcApplyUserParam();
        param.pageNum = 1;
        param.pageSize = MAX_APPLY_USER_COUNT_4_RTC;
        rtcService.listRtcApplyUser(param, new Callback<PageModel<ConfUserModel>>() {
            @Override
            public void onSuccess(PageModel<ConfUserModel> data) {
                callback.onSuccess(data.list);
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    // 查询所有会议成员 (考虑到分页接口的merge逻辑较复杂, 这里取pageSize=300一次性拉取全量会议成员)
    private void loadAllUserOfRtc(Callback<List<ConfUserModel>> callback) {
        RtcUserParam param = new RtcUserParam();
        param.pageNum = 1;
        param.pageSize = MAX_USER_COUNT_4_RTC;
        rtcService.listRtcUser(param, new Callback<PageModel<ConfUserModel>>() {
            @Override
            public void onSuccess(PageModel<ConfUserModel> data) {
                callback.onSuccess(data.list);
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    public void removeUser(String userId) {
        userId2User.remove(userId);
    }

    public void addUser(RtcUser rtcUser) {
        if (!isTeacher(rtcUser.userId)) {
            userId2User.put(rtcUser.userId, rtcUser);
        }
    }

    public boolean hasUser(String userId) {
        return !TextUtils.isEmpty(userId) && userId2User.containsKey(userId);
    }

    private boolean isTeacher(String userId) {
        return roomChannel.isOwner(userId);
    }
}
