package com.aliyun.interaction.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.dingpaas.base.DPSAuthToken;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent;

public class MainActivity extends AppCompatActivity {

    private static final String ROOM_ID = "79e5cdb8-a35e-41a8-8321-c00fe0d8f8e0";
    private static final String USER_ID = "123";
    private static final String NICK = "用户" + USER_ID;

    private RoomChannel roomChannel;
    private ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLogin(View view) {
        RoomEngine.getInstance().auth(USER_ID, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("登录成功");
                Result<RoomChannel> result = RoomEngine.getInstance().getRoomChannel(ROOM_ID);
                if (!result.success) {
                    showToast(result.errorMsg);
                    return;
                }
                roomChannel = result.value;
                chatService = roomChannel.getPluginService(ChatService.class);

                chatService.addEventHandler(new SampleChatEventHandler() {
                    @Override
                    public void onCommentReceived(CommentEvent event) {
                        showToast(event.creatorNick + ": " + event.content);
                    }

                    @Override
                    public void onCustomMessageReceived(CustomMessageEvent event) {
                        showToast("收到自定义消息: " + event.data);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                showToast("登录失败: " + errorMsg);
            }
        });
    }

    public void onEnterRoom(View view) {
        roomChannel.enterRoom(NICK, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("进房间成功");
            }

            @Override
            public void onError(String errorMsg) {
                showToast("进房间失败: " + errorMsg);
            }
        });
    }

    public void onSendMessage(View view) {
        chatService.sendComment("1234567", null);
    }

    public void onSendCustomMessage(View view) {
        chatService.sendCustomMessageToAll("自定义消息内容", null);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}