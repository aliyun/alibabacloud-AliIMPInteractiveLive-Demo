const { RoomEngine, EventNameEnum } = window.RoomPaasSdk

// 解析url获取参数
const paramObj = {}
window.location.search.substring(1).split('&').map(item => {
    paramObj[item.split('=')[0]] = item.split('=')[1]
})
window.initUserId = paramObj.userId || ''

window.onload = () => {
    // 设置配置参数，userId及roomId请提前获取
    const userId = window.initUserId || 'testname'
    const roomId = 'ae8a015b-f7a9-484f-972c-d3abcf7edeef'
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
        authTokenCallback: async() => {
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
                    return res.result
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
        }catch {
            createNode(`执行初始化roomEngine实例操作失败`)
        }
        try {
            roomEngineInstance.auth(userId) 
            // roomEngineInstance.setIsAutoInitRtc(false) // 关闭自动加入rtc
            createNode(`执行登录操作, 当前userId为${userId}`)
        }
        catch {
            createNode('登录失败')
        }
    }

    // 4. 监听roomEngine的状态 （可选）
    const addevtListener = () => {
        try {
            roomEngineInstance.setEventHandler({
                onEngineEvent(e) {
                    console.log('[roomEngineInstance onEngineEvent]', e);
                },
            })
            createNode('添加事件监听成功')
        }catch {
            createNode('添加事件监听失败')
        }
    }

    // 5. 通过roomId获取/创建对应的roomChannel实例
    const getRoomChannel = () => {
        try {
            const roomChannel = roomEngineInstance.getRoomChannel(roomId);
            window.roomChannel = roomChannel;
            createNode('获取房间频道')
        }catch {
            createNode('获取房间频道失败')
        }
        try {
            window.rtcService = window.roomChannel.getPluginService('rtc')
            createNode('注册rtc服务')
        }catch {
            createNode('注册rtc服务失败')
        }
    }

    // 6. 进入房间
    const enterRoom = () => {
        roomChannel.enterRoom(userId).then(roomDetail => {
            createNode(`进入房间，得到roomDetail如下${JSON.stringify(roomDetail)}`)
        }).catch(err => {
            createNode(`进入房间失败，错误信息如下${err}}`)
        })
    }

    // 7. JoinChannel
    const joinchannel = () => {
        window.rtcService.joinChannel(userId).then(() => {
            createNode('joinChannel成功')
        }).catch(() => {
            createNode('joinChannel失败')
        })
    }

    // 8. 连麦相关(demo只设计两人连麦场景，其他请按需求开发)
    const inviteJoinChannel = () => {
        // 获得房间内其他人id
        let otherUserId;
        try {
            window.roomChannel.listUser(1, 50).then(res => {
                const userList = res.userList
                userList.map((item) => {
                    if (item.userId === userId) return
                    otherUserId = item.userId
                })
            })
            if (!otherUserId) {
                createNode('当前无人在房间内')
                return
            }
            createNode(`正在获取连麦用户id: ${otherUserId}`)
        }catch{
            createNode('获取id失败')
        }
        window.rtcService.inviteJoinChannel([{userId,tenantId: '',nickname: userId,}]).then(res => {
            createNode('邀请已发出')
        }).catch(() => {
            createNode('邀请失败')
        })
    }

    const applyJoinChannel = () => {
        try {
            window.rtcService.applyJoinChannel(true)
            createNode('申请连麦中')
        }catch {
            createNode('申请连麦失败')
        }
    }

    const kickUser = () => {
        try {
            window.rtcService.kickUserFromChannel([otherUserId])
            createNode(`用户${otherUserId}已被踢出`)
        }catch {
            createNode('踢出用户失败')
        }
    }

    const stopChannel = () => {
        try {
            window.rtcService.leaveRtc()
            createNode('已结束连麦')
        }catch {
            createNode('结束连麦失败')
        }
    }

    const startListen = () => {
        createNode('已开启监听')
        // 监听连麦申请
        window.rtcService.on(EventNameEnum.PaaSRtcApply, (d) => {
            createNode('收到连麦申请')
            window.rtcService.handleApplyJoinChannel(d.data.applyUser.userId, true).then(() => {
              window.rtcService.inviteJoinChannel([
                {
                  userId: d.data.applyUser.userId,
                  tenantId: '',
                  nickname: d.data.applyUser.userId,
                },
              ])
              createNode('同意连麦')
            }).catch(() => {
                createNode('处理连麦失败')
            })
        })
        // 监听连麦邀请
        window.rtcService.on(EventNameEnum.PaaSRtcInvite, (d) => {
            if (!d.data.calleeList) return
            if (!d.data.calleeList.find((item) => item.userId === userId)) return
            createNode('收到连麦邀请')
            try {
                window.rtcService.joinChannel(userId)
                createNode('开始连麦')
            }catch {
                createNode('连麦失败')
            }
        })
        // 监听踢出用户(访客用)
        window.rtcService.on(EventNameEnum.PaaSRtcKickUser, (d) => {
            d.data.userList.forEach((item) => {
                if (item.userId === user.userId) {
                  window.rtcService.leaveRtc()
                  createNode('您已被踢出房间')
                } 
            })
        })
    }

    // 9. 其他操作
    // 开启预览
    const startPreview = () => {
        try {
            window.rtcService.startRtcPreview(document.querySelector('#preview'))
            createNode('开启预览成功')
        }catch {
            createNode('开启预览失败')
        }
    }

    // 结束预览
    const stopPreview = () => {
        try {
            window.rtcService.stopRtcPreview(document.querySelector('#preview'))
            createNode('结束预览成功')
        }catch {
            createNode('结束预览失败')
        }
    }

    // 开关摄像头
    const openCamera = () => {
        try {
            window.rtcService.setMuteCamera(false)
            createNode('摄像头已开启')
        }catch {
            createNode('摄像头开启失败')
        }
    }
    const closeCamera = () => {
        try {
            window.rtcService.setMuteCamera(true)
            createNode('摄像头已关闭')
        }catch {
            createNode('摄像头关闭失败')
        }
    }

    // 开关音频
    const openRadio = () => {
        try {
            window.rtcService.setMutePush(false)
            createNode('音频已开启')
        }catch {
            createNode('音频开启失败')
        }
    }
    const closeRadio = () => {
        try {
            window.rtcService.setMutePush(true)
            createNode('音频已关闭')
        }catch {
            createNode('音频关闭失败')
        }
    }

    // 10, 退出rtc
    const leaveRtc = () => {
        try {
            window.rtcService.leaveRtc()
            createNode('已退出RTC')
        }catch {
            createNode('退出RTC失败')
        }
    }

    // 离开房间
    const leaveRoom = () => {
        try {
            roomChannel.leaveRoom()
            createNode('离开房间')
        }catch {
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
    document.querySelector('#addeventlistener').onclick = () => {
        addevtListener()
    }
    document.querySelector('#get-roomchannel').onclick = () => {
        getRoomChannel()
    }
    document.querySelector('#enter-room').onclick = () => {
        enterRoom()
    }
    document.querySelector('#join-channel').onclick = () => {
        joinchannel()
    }
    document.querySelector('#start-preview').onclick = () => {
        startPreview()
    }
    document.querySelector('#stop-preview').onclick = () => {
        stopPreview()
    }
    document.querySelector('#invite-joinchannel').onclick = () => {
        inviteJoinChannel()
    }
    document.querySelector('#apply-joinchannel').onclick = () => {
        applyJoinChannel()
    }
    document.querySelector('#kick-user').onclick = () => {
        kickUser()
    }
    document.querySelector('#stop-channel').onclick = () => {
        stopChannel()
    }
    document.querySelector('#start-listen').onclick = () => {
        startListen()
    }
    document.querySelector('#open-camera').onclick = () => {
        openCamera()
    }
    document.querySelector('#close-camera').onclick = () => {
        closeCamera()
    }
    document.querySelector('#open-radio').onclick = () => {
        openRadio()
    }
    document.querySelector('#close-radio').onclick = () => {
        closeRadio()
    }
    document.querySelector('#leave-rtc').onclick = () => {
        leaveRtc()
    }
    document.querySelector('#leave-room').onclick = () => {
        leaveRoom()
    }
}