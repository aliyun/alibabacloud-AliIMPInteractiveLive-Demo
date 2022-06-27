import './App.less';
import qs from 'qs'
import Config from './constants/config';
import { message } from 'antd';
import { React, Component } from 'react';
import { changeFavicon, getUrlWithString, splitSearch } from './utils'
import RoomInfo from './roomInfo'
import Interaction from './interaction'
// http://localhost:3001/?nick=999&liveId=d3af4bcf-ba6d-4607-8784-84d65ebc4a3d&userId=c74ad41822e2028a480a#/
const { EventNameEnum } = window.RoomPaasSdk;
const query = splitSearch(window.location.search);
let liveId = query.liveId?.toString() || '';
const maxAnameCount = 6;
const maxCommentCount = 600;
export default class App extends Component {
    state = {
        nickname: decodeURI(query.nick?.toString()) || '',
        userId: query.userId?.toString() || '',
        messageArray: [],
        currentRoomDetail: {
            title: '直播间',
            onlineCount: 0,
            ownerId: '',
            pluginInstanceModelList: [],
            roomId: '',
            uv: 0,
            notice: '',
            playerBackground: ''
        },
        isLiving: false,
        likeCount: 0,
        isMuteSelf: false,
        needUserClick: true,
        chartInfo: [],
        userStatus: {},
        goodImg: {},
        goodShowTime: null
    }
    stopPlay = () => {
        if (window.liveService.playerInstance) {
            window.liveService.stopPlay();
        }
    };
    userStartPlay = () => {
        this.setState({
            needUserClick: false
        })
        window.liveService.startPlay();
    };
    startLive = () => {
        if (window.liveService.playerInstance) {
            window.liveService.destroy();
        }
        const config = {
            container: '#player',
            width: '100%',
            height: '100%',
            isLive: true,
            autoplay: true,
            controlBarVisibility: 'hover',
        };
        window.liveService.setPlayerConfig(config);
        window.liveService.tryPlayLive();
    };
    leaveRoom = () => {
        if (!window.roomChannel) return;
        window.roomChannel.leaveRoom();
        if (this.state.isLiving) {
            window.liveService.stopPlay();
        }
    };
    hideGoodCard = () => {
        this.setState({
            goodImg: {}
        })
    };
    setLikeCounts = (val) => {
        this.setState({
            likeCount: val
        })
    };
    hideUser = () => {
        this.setState({
            userStatus: {}
        })
    };
    addMsgItem = (messageItem, messages) => {
        messageItem.content = getUrlWithString(messageItem.content);
        messages ?
            this.setState({
                messageArray: [messageItem, ...messages]
            })
            : this.setState({
                messageArray: [messageItem, ...this.state.messageArray]
            })
    };
    bindEvents = () => {
        window.roomChannel.on(EventNameEnum.PaaSRoomEnter, (e) => {
            if (this.state.userId.toString() === e.data.userId.toString()) return;
            const level = e.data.userId.length === 1 ? 1 : (e.data.userId.length < 5 ? 2 : 3)
            const messageItem = {
                nickname: e.data.nick,
                content: `${e.data.enter ? '来了' : '离开了房间'}`,
                level: level
            };
            const roomDetail = {
                ...this.state.currentRoomDetail,
                onlineCount: e.data.onlineCount,
            };
            this.setState({
                currentRoomDetail: roomDetail,
                userStatus: messageItem
            })

        });
        window.chatService.on(EventNameEnum.PaaSChatCustomMessage, (e) => {
            this.setState({
                goodImg: e.data.goodsDetail,
                goodShowTime: e.data.showSeconds
            })
        })
        window.chatService.on(EventNameEnum.PaaSChatReciveLike, (e) => {
            const { likeCount = 0 } = e.data || {};
            this.setState({
                likeCount: likeCount
            })
        });
        window.chatService.on(EventNameEnum.PaaSChatReciveComment, (e) => {
            if (this.state.userId.toString() === e.data.creatorOpenId.toString()) return;
            window.chatService.listComment(0, 1, 20).then(res => {
                const messages = [];
                let list = res.commentModelList.map(item => {
                    return item.content
                })
                let contentList = []
                for (var i = 0; i < list.length; i++) {
                    if (list[i] == list[i + 1]) {
                        contentList.push(list[i]);
                    }
                }
                res.commentModelList.forEach((item) => {
                    messages.push({
                        nickname: item.creatorNick,
                        content: getUrlWithString(item.content),
                        level: item.creatorId.length === 1 ? 1 : (item.creatorId.length < 5 ? 2 : 3),
                        isPlus: contentList.includes(item['content']),
                        isConcerned: false,
                        color: '#' + Math.floor(Math.random() * (2 << 23)).toString(16)
                    });
                });
                this.setState({
                    messageArray: messages
                })
                if (this.state.messageArray.length > maxCommentCount) {
                    let newArr = [];
                    newArr = this.state.messageArray.slice(0, maxCommentCount / 2);
                    this.setState({
                        messageArray: newArr
                    })
                }
            })
        }
        );
        window.liveService.on(EventNameEnum.PaaSLivePublish, (e) => {
            const messageItem = {
                nickname: '',
                content: '直播开始',
            };
            this.addMsgItem(messageItem);
            this.setState({
                isLiving: true
            })
            this.startLive();
        });
        window.liveService.on(EventNameEnum.PaaSLiveStop, (e) => {
            const messageItem = {
                nickname: '',
                content: '直播结束',
            };
            this.addMsgItem(messageItem);
            this.setState({
                isLiving: false
            })
        });
        window.liveService.on(EventNameEnum.PaaSPlayerEvent, (data) => {
            if (data.eventName === 'onM3u8Retry' || data.eventName === 'liveStreamStop') {
                this.setState({
                    isLiving: false
                })
            }
            if (data.eventName === 'playing') {
                this.setState({
                    needUserClick: false,
                    isLiving: true
                })
            }
        });
        window.chatService.on(EventNameEnum.PaaSChatCustomMessage, (e) => {
            // 用来监听推流/断流消息
            if (e.data.EventType === 'system.live.publishDone') {
                // 断流
                this.stopPlay();
                // setIsLiving(false);
                this.setState({
                    isLiving: false
                })
                return;
            }
            if (e.data.EventType === 'system.live.publish') {
                // 推流
                // setIsLiving(true);
                this.setState({
                    isLiving: true
                })
                this.startLive();
                return;
            }
        });
    };
    componentDidMount = () => {
        const { RoomEngine } = window.RoomPaasSdk;
        const roomEngine = RoomEngine.getInstance();
        const hide = message.loading('正在加载中...', 3);
        const deviceId = roomEngine.getSingleDeviceId();
        const { appId, appKey, origin } = Config
        const queryString = qs.stringify({
            // 如果验签，属性顺序需与服务端一致
            appId,
            appKey,
            deviceId: encodeURIComponent(deviceId),
            userId: this.state.userId,
        });
        console.log('===appId', queryString);
        const authTokenCallback = () => {
            const path = '/api/login/getToken'; // 客户服务端接口
            // fetch API返回一个Promise
            return fetch(`${origin}${path}?${queryString}`) // 您可以自己定义接口需要的数据
                .then((res) => res.json())
                .then((res) => {
                    if (res) {
                        // 需要保证数据结构符合AuthToken
                        const authToken = res.result; // 这里需要根据您服务端返回的数据结构来决定返回什么属性
                        return authToken;
                    }
                    throw new Error('没有获取到Token');
                })
                .catch((err) => {
                    console.error(err);
                });
        }
        const config = {
            appId,
            appKey,
            deviceId,
            authTokenCallback
        }
        // 传入配置
        roomEngine.init(config);
        // 建立长链接
        roomEngine.auth(this.state.userId).then(() => {
            // 在这里获取RoomChannel实例进行房间操作
            roomEngine.getLiveDetail(liveId).then((liveDetail) => {
                console.log(liveDetail, '===liveDetail==', this.state.nickname, liveId);
                window.roomChannel = roomEngine.getRoomChannel(liveDetail.roomId);
                window.liveService = window.roomChannel.getPluginService('live');
                window.chatService = window.roomChannel.getPluginService('chat');
                console.log(window.roomChannel, '===window.roomChannel===', this.state.nickname);
                window.roomChannel
                    .enterRoom(this.state.nickname)
                    .then((roomDetail) => {
                        console.log(roomDetail, '===');
                        if (roomDetail.extension) {
                            if (roomDetail.extension.roomTitle)
                                document.title = roomDetail.extension.roomTitle;
                            if (roomDetail.extension.favicon)
                                changeFavicon(roomDetail.extension.favicon);
                        }
                        this.setState({
                            currentRoomDetail: roomDetail
                        })
                        return window.chatService.listComment(0, 1, 20);
                    })
                    .then((res) => {
                        const messages = [];
                        let list = res.commentModelList.map(item => {
                            return item.content
                        })
                        let contentList = []
                        for (var i = 0; i < list.length; i++) {
                            if (list[i] == list[i + 1]) {
                                contentList.push(list[i]);
                            }
                        }
                        res.commentModelList.forEach((item) => {
                            const color = '#' + Math.floor(Math.random() * (2 << 23)).toString(16);
                            messages.push({
                                nickname: item.creatorNick,
                                content: getUrlWithString(item.content),
                                level: item.creatorId.length === 1 ? 1 : (item.creatorId.length < 5 ? 2 : 3),
                                isPlus: contentList.includes(item['content']),
                                isConcerned: false,
                                color: color
                            });
                        });
                        this.setState({
                            messageArray: messages
                        })
                        return Promise.all([
                            window.liveService.getLiveDetail(),
                            window.chatService.getChatDetail(),
                        ]);
                    })
                    .then((res) => {
                        this.setState({
                            chartInfo: res
                        })
                        window.liveService.on(EventNameEnum.PaaSPlayerEvent, (data) => {
                            if (data.eventName === 'error') console.log(data);
                        });
                        if (res[0].status === 1) {
                            // 正在直播中
                            this.setState({
                                isLiving: true
                            })
                            // 开启拉流播放
                            this.startLive();
                        }
                        this.setState({
                            likeCount: res[1].likeCount || 0,
                            isMuteSelf: res[1].mute
                        })
                        // 绑定事件
                        this.bindEvents()
                    })
                    .catch((err) => {
                        console.error(err);
                        message.error('加载失败，请检查参数！');
                    })
                    .finally(() => {
                        hide();
                    });
                window.addEventListener(
                    'beforeunload',
                    () => {
                        this.leaveRoom();
                    },
                    true,
                );
                return () => {
                    document.documentElement.style.background = '#fff';
                    this.leaveRoom();
                };
            })
        })
    };
    render() {
        const { userId, needUserClick, currentRoomDetail, isLiving, messageArray, isMuteSelf, chartInfo, nickname, likeCount, userStatus, goodShowTime, goodImg } = this.state
        return (
            <div className="App">
                <div className="player-container">
                    <div className="player" id="player"></div>
                </div>
                <div className="live-container">
                    <RoomInfo roomDetail={currentRoomDetail}></RoomInfo>
                    <Interaction
                        messageArray={messageArray}
                        isMuteSelf={isMuteSelf}
                        chartInfo={chartInfo}
                        nickname={nickname}
                        userId={userId}
                        likeCount={likeCount}
                        userStatus={userStatus}
                        goodShowTime={goodShowTime}
                        goodImg={goodImg}
                        maxAnameCount={maxAnameCount}
                        setLikeCounts={this.setLikeCounts}
                        addMsgItem={this.addMsgItem}
                        hideGoodCard={this.hideGoodCard}
                        hideUser={this.hideUser}
                    ></Interaction>
                </div>
                {!isLiving && (
                    <div className="nolive">
                        <img alt='' src="https://img.alicdn.com/imgextra/i1/O1CN01pgziS925R7tXtb86t_!!6000000007522-55-tps-238-127.svg" />
                        <span style={{ fontSize: "0.8rem" }}>主播正在路上，请稍等～</span>
                    </div>
                )}
                {needUserClick && isLiving && (
                    <div className="need-click-wrap">
                        <div className="wrap-inner">
                            <img alt='' src="https://img.alicdn.com/imgextra/i1/O1CN01CIquYT1L3L1GSwvqH_!!6000000001243-2-tps-714-393.png" />
                            <p style={{ fontSize: "0.8rem" }}>直播即将开始</p>
                            <p style={{ fontSize: "0.8rem" }}>请确认您处于Wifi环境，移动网络会产生流量费用</p>
                            <div className="click-btn" onClick={this.userStartPlay}>
                                我已知晓
                            </div>
                        </div>
                    </div>
                )}
            </div>
        )
    }
}
