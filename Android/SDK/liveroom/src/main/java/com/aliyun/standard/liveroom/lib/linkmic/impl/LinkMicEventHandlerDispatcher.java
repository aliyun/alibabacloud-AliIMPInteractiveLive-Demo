package com.aliyun.standard.liveroom.lib.linkmic.impl;

import android.view.View;

import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.List;

/**
 * @author puke
 * @version 2022/4/27
 */
class LinkMicEventHandlerDispatcher implements LinkMicEventHandler {

    private final EventHandlerManager<LinkMicEventHandler> dispatcher;

    public LinkMicEventHandlerDispatcher(EventHandlerManager<LinkMicEventHandler> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onJoinedSuccess(final View view) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onJoinedSuccess(view);
            }
        });
    }

    @Override
    public void onLeftSuccess() {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onLeftSuccess();
            }
        });
    }

    @Override
    public void onUserJoined(final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onUserJoined(users);
            }
        });
    }

    @Override
    public void onUserLeft(final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onUserLeft(users);
            }
        });
    }

    @Override
    public void onCameraStreamAvailable(final String userId, final boolean isAnchor, final View view) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onCameraStreamAvailable(userId, isAnchor, view);
            }
        });
    }

    @Override
    public void onRemoteCameraStateChanged(final String userId, final boolean open) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onRemoteCameraStateChanged(userId, open);
            }
        });
    }

    @Override
    public void onRemoteMicStateChanged(final String userId, final boolean open) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onRemoteMicStateChanged(userId, open);
            }
        });
    }

    @Override
    public void onInvited(final LinkMicUserModel inviter, final List<LinkMicUserModel> invitedUsers) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onInvited(inviter, invitedUsers);
            }
        });
    }

    @Override
    public void onInviteCanceledForMe() {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onInviteCanceledForMe();
            }
        });
    }

    @Override
    public void onInviteRejected(final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onInviteRejected(users);
            }
        });
    }

    @Override
    public void onApplied(final boolean newApplied, final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onApplied(newApplied, users);
            }
        });
    }

    @Override
    public void onApplyCanceled(final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onApplyCanceled(users);
            }
        });
    }

    @Override
    public void onApplyResponse(final boolean approve, final String userId) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onApplyResponse(approve, userId);
            }
        });
    }

    @Override
    public void onKicked(final List<LinkMicUserModel> users) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onKicked(users);
            }
        });
    }

    @Override
    @Deprecated
    public void onSelfMicAllowed(final boolean allowed) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onSelfMicAllowed(allowed);
            }
        });
    }

    @Override
    public void onSelfMicClosedByAnchor() {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onSelfMicClosedByAnchor();
            }
        });
    }

    @Override
    public void onAnchorInviteToOpenMic() {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onAnchorInviteToOpenMic();
            }
        });
    }

    @Override
    public void onAllMicAllowed(final boolean allowed) {
        dispatcher.dispatch(new EventHandlerManager.Consumer<LinkMicEventHandler>() {
            @Override
            public void consume(LinkMicEventHandler handler) {
                handler.onAllMicAllowed(allowed);
            }
        });
    }
}
