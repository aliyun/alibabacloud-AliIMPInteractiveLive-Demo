package com.aliyun.roompaas.app.delegate.rtc;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.classroom.StudentListAdapter;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.IReset;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.rtc.RtcStreamEventHelper;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;
import com.aliyun.roompaas.uibase.util.ViewUtil;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by KyleCe on 2021/9/13
 */
public class StudentRtcDelegate implements IDestroyable, IReset, IDisplayVideo {
    private StudentListAdapter adapter;

    private View pageUp;
    private View pageDown;

    public static final int DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT = 4;

    private final List<RtcStreamEvent> fullDataList = new ArrayList<>();
    private final RtcStreamEvent[] focusingDataArray = new RtcStreamEvent[DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT];
    private int currentPageIndex;

    private RtcSubscribeDelegate rtcSubscribeDelegate;
    private Context context;
    private RecyclerView studentRTCRV;
    private Set<String> displayIds = new HashSet<>();
    private Reference<RoomChannel> roomChannelRef;
    private final String userId;

    public StudentRtcDelegate(@NonNull Activity activity, RoomChannel roomChannel, RtcService rtcService) {

        context = activity.getApplicationContext();
        adapter = new StudentListAdapter(roomChannel, context);
        adapter.setSubStatusQuery(this);
        initStudentList(activity);
        rtcSubscribeDelegate = new RtcSubscribeDelegate(rtcService, roomChannel);
        roomChannelRef = new WeakReference<>(roomChannel);
        this.userId = roomChannel.getUserId();
    }

    public void setItemClickListener(StudentListAdapter.ItemClickListener itemClickListener) {
        adapter.setItemClickListener(itemClickListener);
    }

    void initStudentList(@NonNull Activity activity) {
        studentRTCRV = activity.findViewById(R.id.studentRTCViewList);
        pageUp = activity.findViewById(R.id.pageUp);
        pageDown = activity.findViewById(R.id.pageDown);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        studentRTCRV.setLayoutManager(llm);
        studentRTCRV.setAdapter(adapter);

        ViewUtil.bindClickActionWithClickCheck(pageUp, this::pageUp);
        ViewUtil.bindClickActionWithClickCheck(pageDown, this::pageDown);
    }

    private void pageUp() {
        currentPageIndex--;

        refreshFocusing();
    }

    private void pageDown() {
        currentPageIndex++;

        refreshFocusing();
    }

    private void refreshFocusing() {
        List<RtcStreamEvent> toFill = fullDataList.subList(currentPageIndex * DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT
                , Math.min(fullDataList.size(), (currentPageIndex + 1) * DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT));
        remoteDisplayIds(Arrays.asList(focusingDataArray));
        for (int i = 0, size = focusingDataArray.length; i < size; i++) {
            adapter.detachedPreview(focusingDataArray[i]);
            rtcSubscribeDelegate.unsubscribe(focusingDataArray[i]);
            focusingDataArray[i] = Utils.isIndexInRange(i, toFill) ? toFill.get(i) : null;
        }
        adapter.refreshData(focusingDataArray);

        rtcSubscribeDelegate.subscribe(Arrays.asList(focusingDataArray));
        addDisplayIds(Arrays.asList(focusingDataArray));

        updatePageIndicator();
    }

    @NonNull
    private List<RtcStreamEvent> toUnsubscribe() {
        List<RtcStreamEvent> result = new ArrayList<>(fullDataList);
        for (RtcStreamEvent event : focusingDataArray) {
            result.remove(event);
        }
        return result;
    }

    public void updateData(@NonNull RtcStreamEvent event) {
        int position = Utils.parseIndex(event, fullDataList);
        if (position < 0) {
            fullDataList.add(event);
        } else {
            //interceptDetailIfVital(event, fullDataList.get(position));
            fullDataList.set(position, event);
        }

        boolean refreshExisting = false;
        int index = Utils.parseIndex(event, focusingDataArray);
        if (index != -1) {
            focusingDataArray[index] = event;
            adapter.refreshData(index, event);
            refreshExisting = true;
        } else {
            int firstNullIndex = Utils.firstNull(focusingDataArray);
            if (firstNullIndex != -1) {
                focusingDataArray[firstNullIndex] = event;
                adapter.refreshData(firstNullIndex, event);
            }
        }

        if (!refreshExisting) {
            updatePageIndicator();
        }
    }

    private void interceptDetailIfVital(@NonNull RtcStreamEvent newE, @NonNull RtcStreamEvent oldE) {
        if (newE.aliVideoCanvas != null && oldE.aliVideoCanvas != null) {
            if (newE.aliVideoCanvas.view == null && oldE.aliVideoCanvas.view != null) {
                newE.aliVideoCanvas = oldE.aliVideoCanvas;
            }
        } else if (newE.aliVideoCanvas == null) {
            newE.aliVideoCanvas = oldE.aliVideoCanvas;
        }

        newE.userName = !isSelf(oldE.userId) ? Utils.acceptFirstNotEmpty(oldE.userName, newE.userName) : RtcDelegate.NICK4SELF;
        newE.closeMic = !isSelf(oldE.userId) ? oldE.closeMic : newE.closeMic;
        newE.closeCamera = !isSelf(oldE.userId) ? oldE.closeCamera : newE.closeCamera;
    }

    private void updatePageIndicator() {
        int count;
        if ((count = fullDataList.size()) <= DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT) {
            hidePageIndicator();
            rtcSubscribeDelegate.subscribe(fullDataList);
            addDisplayIds(fullDataList);
            return;
        }
        int maxPageIndex = (count - 1) / DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT;

        ViewUtil.switchVisibilityIfNecessary(currentPageIndex != 0, pageUp);
        ViewUtil.switchVisibilityIfNecessary(currentPageIndex < maxPageIndex, pageDown);
    }

    private void hidePageIndicator() {
        ViewUtil.setGone(pageUp, pageDown);
    }

    public void updateLocalMic(String uid, boolean muteLocalMic) {
        adapter.updateLocalMic(uid, muteLocalMic);
    }

    public void updateLocalCamera(String uid, boolean muteLocalCamera) {
        adapter.updateLocalCamera(uid, muteLocalCamera);
    }

    public void removeData(Collection<String> userIdCol) {
        if (Utils.isEmpty(userIdCol)) {
            return;
        }

        List<RtcStreamEvent> list = new ArrayList<>(userIdCol.size());
        Iterator<String> iterator = userIdCol.iterator();
        boolean shownMatches = false;
        for (int i = 0; iterator.hasNext(); i++) {
            String is = iterator.next();
            if (!TextUtils.isEmpty(is)) {
                RtcStreamEvent event = RtcStreamEventHelper.asRtcStreamEvent(is);
                list.add(event);

                if (!shownMatches) {
                    shownMatches = Utils.parseIndex(event, focusingDataArray) != -1;
                }

            }
        }

        fullDataList.removeAll(list);
        if (shownMatches) {
            refreshFocusing();
        }
    }

    public void removeData(String userId) {
        RtcStreamEvent event = RtcStreamEventHelper.asRtcStreamEvent(userId);
        fullDataList.remove(event);
        int index = Utils.parseIndex(event, focusingDataArray);
        if (index != -1) {
            refreshFocusing();
        }
    }

    public void removeAll() {
        reset();
        adapter.removeAll();
    }

    private void remoteDisplayIds(List<RtcStreamEvent> eventList) {
        for (RtcStreamEvent event : eventList) {
            if (event == null || TextUtils.isEmpty(event.userId)) {
                continue;
            }
            displayIds.remove(event.userId);
        }
    }

    private void addDisplayIds(List<RtcStreamEvent> eventList) {
        for (RtcStreamEvent event : eventList) {
            if (event == null || TextUtils.isEmpty(event.userId)) {
                continue;
            }
            displayIds.add(event.userId);
        }
    }

    private boolean isSelf(String uid) {
        return userId.equals(uid);
    }

    private boolean isOwner(String uid) {
        RoomChannel roomChannel = Utils.getRef(roomChannelRef);
        return roomChannel != null && roomChannel.isOwner(uid);
    }

    @Override
    public boolean showDisplayVideo(String uid) {
        return displayIds.contains(uid);
    }

    @Override
    public void reset() {
        currentPageIndex = 0;
        rtcSubscribeDelegate.unsubscribe(displayIds);
        Utils.clear(displayIds);
        rtcSubscribeDelegate.unsubscribe(Arrays.asList(focusingDataArray));
        fullDataList.clear();
        Arrays.fill(focusingDataArray, null);
        hidePageIndicator();
    }

    @Override
    public void destroy() {
        removeAll();
        Utils.destroy(rtcSubscribeDelegate, adapter);
    }
}
