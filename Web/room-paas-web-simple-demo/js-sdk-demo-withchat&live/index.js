const { RoomEngine, EventNameEnum } = window.RoomPaasSdk

window.onload = () => {
    // 设置配置参数，userId及roomId请提前获取
    const userId = 'testname1'
    const roomId = '891bc1de-5a32-4997-a12a-62b527e4871b'
    const path = '/api/login/getToken' // 这里写客户自己的api地址path
    const appKey = config_appKey
    const appId = config_appId

    // 工具方法: 添加子节点
    const createNode = (text, fatherNode = document.querySelector('.console-area')) => {
        const element = document.createElement('div')
        element.innerText = text
        fatherNode.appendChild(element) 
    }

    // 1. 获取roomEngine实例，并获取唯一设备id
    const roomEngineInstance = RoomEngine.getInstance();
    const deviceId = roomEngineInstance.getSingleDeviceId();

    // 2. 初始化roomEngine配置
    const config = {
        appKey,
        appId,
        deviceId, // 设备id, 在第1步获取
        authTokenCallback: async () => {// 这个函数就是函数本身，传入时不要执行
            return fetch(
                `${config_origin}${path}?appId=${appId}&appKey=${appKey}&deviceId=${deviceId}&userId=${userId}`,
            )
                .then((res) => res.json())
                .then((res) => {
                    if (res.responseSuccess) {
                        // 这里需要根据客户appserver返回的数据结构取数据
                        // return的值数据结构为：
                        /* {
                            accessToken: string;
                            refreshToken: string;
                            accessTokenExpiredTime: number;
                        } */
                        // 不是只有accessToken！
                        return res.result // async函数的返回值是Promise(result)
                    }
                    throw new Error(res.message)
                })
                .catch((err) => {
                    console.error(err);
                });
        }   // 获取鉴权信息
    }
    
    // 3. 登录账号，建立ws长链接
    const doAuth = () => {
        try {
            roomEngineInstance.init(config) // 使用配置信息初始化RoomEngine的配置
            createNode(`执行初始化roomEngine实例操作，得到deviceId为${deviceId}`)
        } catch {
            createNode(`执行初始化roomEngine实例操作失败`)
        }
        try {
            roomEngineInstance.auth(userId) 
            roomEngineInstance.setIsAutoInitRtc(false) // 关闭自动加入rtc
            createNode('执行登录操作')
        } catch {
            createNode('登录失败')
        }
    }
    
    const enterRoom = () => {
        // 5. 通过roomId获取/创建对应的roomChannel实例
        try {
            const roomChannel = roomEngineInstance.getRoomChannel(roomId);
            window.roomChannel = roomChannel;
            createNode('获取房间频道')
        } catch {
            createNode('获取房间频道失败')
        }
        try {
            window.chatService = window.roomChannel.getPluginService('chat')
            createNode('注册chat服务')
        } catch {
            createNode('注册chat服务失败')
        }
        try {
            window.chatService = window.roomChannel.getPluginService('live')
            createNode('注册live服务')
        } catch {
            createNode('注册live服务失败')
        }
        // 6. 监听事件
        bindEvents()
        // 7. 进入房间
        roomChannel.enterRoom(userId).then(roomDetail => {
            createNode(`进入房间，得到roomDetail如下${JSON.stringify(roomDetail)}`)
        }).catch(err => {
            createNode(`进入房间失败，错误信息如下${err}}`)
        })
    }

    // 8. 创建并配置播放器
    const createPlayer = () => {
        try {
            window.liveService.setPlayerConfig({
                container: '#J_player'
            })
            createNode('在id="play"节点处创建播放器')
        } catch {
            createNode('创建播放器失败')
        }
        try {
            window.liveService.tryPlayLive()
            createNode('尝试播放')
        } catch {
            createNode('播放失败')
        }
    }

    // 9. 发布、拉取评论
    const submitComment = (text=`评论发布测试${new Date().getTime()}`) => {
        window.chatService.sendComment(text).then(res => {
            createNode(`评论发布成功, ${JSON.stringify(res)}`)
        }).catch(err => {
            createNode(`评论发布失败, ${JSON.stringify(err)}`)
        })
    }

    const pullComments = () => {
        window.chatService.listComment(0, 1, 50).then((res) => {
            createNode(`评论拉取成功, ${JSON.stringify(res)}`)
        }).catch(err => {
            createNode(`评论发布失败, ${JSON.stringify(err)}`)
        })
    }

    const bindEvents = () => {
        window.chatService.on(EventNameEnum.PaaSChatReciveComment, d => {
            if (d.data.creatorOpenId === userId) return
            createNode(`收到评论, ${d.data.content}`)
            createNode(d.data.content, document.querySelector('.chat-area'))
        })

        window.chatService.on(EventNameEnum.PaaSChatReciveLike, d => {
            console.log(d)
            createNode('有人点赞')
        })
    }
    
    // 10. 点赞
    const sendLike = () => {
        try {
            window.chatService.sendLike()
            createNode('点赞成功')
        } catch {
            createNode('点赞失败')
        }
    }

    // 离开房间
    const leaveRoom = () => {
        try {
            roomChannel.leaveRoom()
            createNode('离开房间')
        } catch {
            createNode('离开房间失败')
        }
    }

    // 监听关闭或刷新页面事件，自动调用离开房间逻辑
    window.addEventListener('beforeunload', () => {
        leaveRoom()
    })

    // 绑定点击事件
    document.querySelector('#login').onclick = () => {
        doAuth()
    }
    document.querySelector('#enter-room').onclick = () => {
        enterRoom()
    }
    document.querySelector('#create-player').onclick = () => {
        createPlayer()
    }
    document.querySelector('#submit-comment').onclick = () => {
        submitComment()
    }
    document.querySelector('#submit').onclick = () => {
        const innerText = document.querySelector('#input').value
        if (innerText) {
            submitComment(document.querySelector('#input').value)
            document.querySelector('#input').value = ''
            createNode(innerText, document.querySelector('.chat-area'))
        }
    }
    document.querySelector('#pull-comments').onclick = () => {
        pullComments()
    }
    document.querySelector('#send-like').onclick = () => {
        sendLike()
    }
    document.querySelector('#leave-room').onclick = () => {
        leaveRoom()
    }
}