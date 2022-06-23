<template>
  <div class="page">
    <div class="page-nav">
      <div class="bread">直播列表 / 互动直播中控台</div>
      <div class="func-list">
        <el-button type="text" @click="downloadPC">下载PC推流工具</el-button>
      </div>
    </div>
    <div class="page-content">
      <div class="title">
        <i class="el-icon-back" @click="() => $router.replace('/live/liveList')" style="cursor: pointer;"></i>
        {{ liveDetail.title }}
      </div>
      <div class="btn-container">
        <el-button type="primary" size="small" @click="updateLive">更新直播信息</el-button>
        <el-button type="success" size="small" @click="startLive" v-show="liveDetail.status === 0">开启直播</el-button>
        <el-button type="danger" size="small" @click="stopLive" v-show="liveDetail.status === 1">停止直播</el-button>
        <el-button size="small" @click="jumpPC">PC主播端推流</el-button>
        <el-popover
          id="popover"
          placement="bottom"
          width="380"
          v-model="addressVisiable"
          style="margin-left: 10px;">
          <div class="address-list">
            <div class="item">
              <div class="name">Web端</div>
              <div class="content">
                <el-input :readonly="true" :value="shareLink" size="small" id="web-share"></el-input>
                <el-button size="small" type="primary" class="copy" @click="() => copyShare(shareLink)">复制</el-button>
              </div>
            </div>
          </div>
          <el-button slot="reference" size="small">观看地址 <i class="el-icon-caret-bottom"></i></el-button>
        </el-popover>
        <el-checkbox v-model="checked" style="margin-left: 20px" @change="refreshChange">是否每5秒更新信息</el-checkbox>
      </div>
      <div class="main-container">
        <div class="player-container">
          <live-data :liveDetail="liveDetail" :countTime="countTime"/>
          <div class="player">
            <div class="not-play" v-show="!playing"><span>{{ liveDetail.status === 2 ? '直播已结束' : '直播还未开始' }}</span></div>
            <div class="player-inner" id="player" v-show="playing"></div>
          </div>
          <div class="operation">
            <div class="item" @click="openNotice">
              <i class="el-icon-edit-outline"></i>
              <span>公告</span>
            </div>
            <div class="item" @click="goodVisible = true">
              <i class="el-icon-s-goods"></i>
              <span>商品</span>
            </div>
          </div>
        </div>
        <div class="chat-container">
          <el-tabs :stretch="true" v-model="activeName" type="border-card" class="tab-container">
            <el-tab-pane label="消息" name="message" :class="`message ${liveDetail.notice ? 'notice-hoverd-message' : ''}`">
              <chat ref="chat" :notice="liveDetail.notice" :commentList="commentList" :chatService="chatService" :banList="banList"  @banComment="handleBanComment"/>
            </el-tab-pane>
            <!-- <el-tab-pane label="禁言列表" name="users">
              <ban-list :banList="banList" />
            </el-tab-pane> -->
          </el-tabs>
        </div>
      </div>
    </div>
    <live-info :liveDetail="liveDetail"/>
    <el-dialog
      title="更新直播信息"
      :visible.sync="dialogVisible"
      width="700px"
    >
      <span slot="title" class="dialog-title">
        <span class="title">创建直播</span>
      </span>
      <el-form label-position="right" label-width="105px" :model="updateLiveModel" ref="form" :rules="rules">
        <el-form-item label="直播标题" required prop="title">
          <el-input v-model="updateLiveModel.title" placeholder="最大长度32位"></el-input>
        </el-form-item>
        <el-form-item label="主播昵称">
          <el-input :readonly="true" :value="userNick"></el-input>
        </el-form-item>
        <el-form-item label="直播封面" prop="coverUrl">
          <el-input v-model="updateLiveModel.coverUrl" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="浏览器标题">
          <el-input v-model="updateLiveModel.roomTitle" placeholder="浏览器顶部的标题，即document.title"></el-input>
        </el-form-item>
        <el-form-item label="直播间背景图" prop="playerBackground">
          <el-input v-model="updateLiveModel.playerBackground" placeholder="https://example.png // 直播中止时的背景"></el-input>
        </el-form-item>
        <el-form-item label="主播头像" prop="avatar">
          <el-input v-model="updateLiveModel.avatar" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="网站小图标" prop="favicon">
          <el-input v-model="updateLiveModel.favicon" placeholder="https://example.ico // 网站tab小图标"></el-input>
        </el-form-item>
        <el-form-item label="分享跳转链接" prop="shareUrl">
          <el-input v-model="updateLiveModel.shareUrl" placeholder="https://example.com // 分享的链接，配置后显示分享按钮，点击会复制到剪贴板"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </span>
    </el-dialog>
    <el-dialog
      title="商品列表"
      :visible.sync="goodVisible"
      width="500px">
      <div class="good-list">
        <div class="list-item" v-for="(item, index) in goodsList" :key="index">
          <div class="info-container">
            <div class="img" :style="`background-image: url('${item.img}')`"></div>
            <div class="info">
              <div class="name">{{item.name}}</div>
              <div class="price">￥{{item.price}}</div>
            </div>
          </div>
          <el-button @click="() => sendCustomMessage(item)" size="mini" type="primary" class="btn">上链接</el-button>
        </div>
      </div>
    </el-dialog>
    <el-dialog
      title="公告"
      :visible.sync="noticeVisible"
      width="500px">
      <el-input
        type="textarea"
        placeholder="请输入内容"
        v-model="updateNotice"
        maxlength="120"
        class="textarea"
        resize="none"
        :readOnly="!!liveDetail.notice"
        rows="4"
      >
      </el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="noticeVisible = false" size="small">取 消</el-button>
        <el-button type="primary" @click="deleteNoticeHandler" size="small" v-if="!!liveDetail.notice">下 架</el-button>
        <el-button type="primary" @click="updateNoticeHandler" size="small" v-else>发 布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { liveApi } from '@/api'
import { mapGetters } from 'vuex'
import { downloadPCMixin } from '@/mixins/downloadPC'
import LiveInfo from '@/components/functional/liveInfo'
import LiveData from '@/components/functional/liveData'
import Chat from '@/components/functional/chat'
import BanList from '@/components/functional/banList'
import config from '@/constants/config'

const { RoomEngine, EventNameEnum } = window.RoomPaasSdk;
const { appId, appKey } = config;
const mockGood = {
  img: 'https://img.alicdn.com/i2/2200612226170/O1CN01TMssu71vRuRT5yBMd_!!2200612226170.jpg_360x360xzq90.jpg_.webp',
  name: '0162元气女孩 瑜伽背心运动内衣',
  price: 3999
}
const validUrl = (rule, value, callback) => {
  if (value === '' || /[a-zA-z]+:\/\/[^\s]*/.test(value)) callback()
  else callback(new Error('请输入正确的url'))
}

const extensionKeys = [
  'roomTitle',
  'playerBackground',
  'avatar',
  'favicon',
  'shareUrl',
]

export default {
  mixins: [downloadPCMixin],

  data: () => ({
    liveId: '',
    liveDetail: {},
    updateLiveModel: {
      title: '',
      notice: '',
      coverUrl: '',
      anchorId: '',
      anchorNick: '',
      roomTitle: '', // document.title
      playerBackground: '', // pc直播中止时的背景
      avatar: '', // 直播间头像
      favicon: '',
      shareUrl: '' // 分享链接
    },
    rules: {
      title: [
        { required: true, message: '请输入直播标题', trigger: 'blur' },
        { max: 32, message: '最大长度32位', trigger: 'blur' },
      ],
      coverUrl: [
        { validator: validUrl, trigger: 'blur' }
      ],
      playerBackground: [
        { validator: validUrl, trigger: 'blur' }
      ],
      avatar: [
        { validator: validUrl, trigger: 'blur' }
      ],
      favicon: [
        { validator: validUrl, trigger: 'blur' }
      ],
      shareUrl: [
        { validator: validUrl, trigger: 'blur' }
      ]
    },
    refreshInterval: null,
    countInterval: null,
    countTime: 0,
    status: ['未开始', '直播中', '已结束'],
    dialogVisible: false,
    checked: false,
    activeName: 'message',
    comment: '',
    roomEngine: null,
    roomChannel: null,
    chatService: null,
    liveService: null,
    playing: false,
    goodVisible: false,
    goodsList: new Array(10).fill(mockGood),
    commentList: [],
    addressVisiable: false,
    shareLink: '',
    clipboard: null,
    banList: [],
    isMuteAll: false,
    noticeVisible: false,
    updateNotice: ''
  }),

  methods: {
    getLiveRoom() {
      return liveApi.getLiveRoom({ liveId: this.liveId }).then(res => {
        console.log(res.data.result)
        this.liveDetail = res.data.result
        this.playing = this.liveDetail.status === 1
        this.countTime = this.liveDetail.status === 1
          ? +new Date() - this.liveDetail.startTime : this.liveDetail.endTime - this.liveDetail.startTime
        return res.data.result
      })
    },

    generateUpdateModal() {
      Object.keys(this.updateLiveModel).forEach(key => {
        this.updateLiveModel[key] = this.liveDetail[key] || ''
      })
      Object.keys(this.liveDetail.extension).forEach(key => {
        this.updateLiveModel[key] = this.liveDetail.extension[key]
      })
    },

    updateLive() {
      this.dialogVisible = true
    },
    // 公告发布成功 才能下架
    deleteNoticeHandler() {
      this.roomChannel
        .updateNotice('')
        .then(() => {
          this.$message.success('公告下架成功')
          this.liveDetail.notice = ''
          this.updateNotice = this.liveDetail.notice
          this.noticeVisible = false
        })
        .catch((err) => {
          console.error(err)
          this.$message.error('公告下架失败')
        })
    },
    // 需要服务端接入登录成功之后才可以发布公告
    updateNoticeHandler() {
      if (!this.updateNotice) return this.$message.error('请输入公告')
      this.roomChannel
        .updateNotice(this.updateNotice)
        .then(() => {
          this.$message.success('公告发布成功')
          this.liveDetail.notice = this.updateNotice
          this.noticeVisible = false
        })
        .catch((err) => {
          console.error(err)
          this.$message.error('公告发布失败')
        })
    },

    openNotice() {
      this.updateNotice = this.liveDetail.notice
      this.noticeVisible = true
    },

    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const loading = this.$loading()
          const extension = {}
          const options = {
            ...this.updateLiveModel
          }
          extensionKeys.forEach(key => {
            if (options[key] || options[key] === 0) {
              extension[key] = options[key]
            }
          })
          liveApi.updateLiveRoom({ // api封装时过滤了不需要的参数
            ...options,
            userId: this.userId,
            extension,
            liveId: this.liveId
          }).then(res => {
            this.$message.success('更新成功')
            this.dialogVisible = false
            this.getLiveRoom()
          }).catch(err => {
            this.$message.error('更新失败，信息请看开发者工具报错')
            console.error(err)
          }).finally(() => {
            loading.close()
          })
        } else {
          this.$message.error('请正确填写数据')
          return false
        }
      })
    },

    onDialogClose() {
      this.generateUpdateModal()
    },

    stopLive() {
      this.$confirm('确定要停止该场直播吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(() => {
        return liveApi.stopLiveRoom({
          liveId: this.liveId,
          userId: 'admin'
        })
      }).then(() => {
        this.$message({
          type: 'success',
          message: '已停止直播'
        });
        return this.getLiveRoom()
      }).then(() => {
        this.generateUpdateModal()
        this.stopCountInterval()
        this.liveService.stopPlay()
      }).catch(err => {
        console.error(err)
      })
    },

    startLive() {
      this.$confirm('确定要开启该场直播吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return liveApi.publishLiveRoom({
          liveId: this.liveId,
          userId: 'admin'
        })
      }).then(() => {
        this.$message({
          type: 'success',
          message: '已开启直播，可以开始推流了'
        });
        return this.getLiveRoom()
      }).then(() => {
        this.generateUpdateModal()
        this.startCountInterval()
      })
    },

    startLocalPublish() {
      this.$confirm('确定要开启本地推流吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return liveApi.startLocalPublish({
          fileName: 'example.mp4',
          pushUrl: this.liveDetail.pushUrl
        })
      }).then(() => {
        this.$message({
          type: 'success',
          message: '成功，请查看服务端结果'
        });
      })
    },

    jumpPC() {
      liveApi.getStandardRoomJumpUrl({
        bizId: this.liveId,
        userId: this.userId,
        userNick: this.userNick,
        platform: this.UA.isMac ? 'mac' : 'win'
      }).then((res) => {
        const url = res.data.result.standardRoomJumpUrl
        window.protocolCheck(url, () => {
          this.$message.error('未检测到推流工具，请点击右上角链接下载推流工具')
        }, () => {
          window.open(url)
        })
      })
    },

    refreshChange(e) {
      e ? this.startRefreshInterval() : this.stopRefreshInterval()
    },

    startRefreshInterval() {
      if (this.refreshInterval) return
      this.refreshInterval = setInterval(() => {
        this.getLiveRoom()
      }, 5000)
    },

    stopRefreshInterval() {
      clearInterval(this.refreshInterval)
      this.refreshInterval = null
    },

    startCountInterval() {
      if (this.countInterval) return
      this.countInterval = setInterval(() => {
        this.countTime += 1000
      }, 1000)
    },

    stopCountInterval() {
      clearInterval(this.countInterval)
      this.countInterval = null
    },

    play() {
      if (this.player) this.player.play()
    },

    pause() {
      if (this.player) this.player.pause()
    },

    startLivePlay() {
      if (this.liveService.playerInstance) {
        this.liveService.destroy();
      }
      const config = {
        container: '#player',
        width: '100%',
        height: '100%',
        isLive: true,
        autoplay: true,
        controlBarVisibility: 'hover',
        useArtc: false,
        aliplayerSdkVer: '2.9.21',
      };
      this.liveService.setPlayerConfig(config);
      this.liveService.tryPlayLive();
    },

    onReceiveComment(d) {
      this.commentList.push({
        name: d.data.creatorNick,
        content: d.data.content,
        userId: d.data.creatorOpenId
      })
      this.chatGoBottom()
    },

    onMuteAll(d) {
      this.$refs.chat.isMuteAll = d.data.mute
    },

    async handleBanComment() {
      this.banList = (await this.chatService.listBanCommentUsers(1, 50)).banCommentUserModelList.map(item => item.userId)
    },

    chatGoBottom() {
      this.$nextTick(() => {
        const chatContainer = document.querySelector('.list');
        if (chatContainer) {
          const scrollHeight = chatContainer.scrollHeight || 0;
          const clientHeight = chatContainer.clientHeight || 0;
          chatContainer.scrollTo(0, scrollHeight - clientHeight);
        }
      })
    },

    onRoomEnter(d) {
      // if (d.data.userId === this.userId) return
      // if (d.data.enter) {
      //   this.userList.push({
      //     userId: d.data.userId,
      //     nick: d.data.nick
      //   })
      // } else {
      //   const index = this.userList.findIndex(item => item.userId === d.data.userId)
      //   this.$delete(this.userList, index)
      // }
    },

    livePublish() {
      this.getLiveRoom().then(res => {
        if (res.status === 1) {
          this.startCountInterval()
          this.startLivePlay()
        }
      })
    },

    liveStop() {
      this.liveService.stopPlay()
      this.liveDetail.status = 2
      this.playing = false
      this.stopCountInterval()
    },

    bindEvents() {
      if (!this.chatService) return
      this.chatService.on(EventNameEnum.PaaSChatReceiveComment, this.onReceiveComment)
      this.chatService.on(EventNameEnum.PaaSChatMuteAll, this.onMuteAll)
      this.liveService.on(EventNameEnum.PaaSLivePublish, this.livePublish)
      this.liveService.on(EventNameEnum.PaaSLiveStop, this.liveStop)
      this.liveService.on(EventNameEnum.PaaSLiveStreamStart, () => { this.$message.info('推流开始') })
      this.liveService.on(EventNameEnum.PaaSLiveStreamStop, () => { this.$message.info('推流暂停') })
      // this.roomChannel.on(EventNameEnum.PaaSRoomEnter, this.onRoomEnter)
    },

    async auth(roomId, status) {
      this.roomEngine = RoomEngine.getInstance();
      const deviceId = encodeURIComponent(this.roomEngine.getSingleDeviceId());
      const config = {
        appId,
        appKey,
        deviceId,
        userId: this.userId,
        authTokenCallback: async () => {
          return (await liveApi.getToken({
            appKey,
            userId: this.userId,
            deviceId
          })).data.result
        }
      }
      this.roomEngine.init(config)
      try {
        await this.roomEngine.auth()
        this.roomChannel = this.roomEngine.getRoomChannel(roomId)
        this.chatService = this.roomChannel.getPluginService('chat')
        this.liveService = this.roomChannel.getPluginService('live')
        this.bindEvents()
        await this.roomChannel.enterRoom(this.userNick)
        const commentList = (await this.chatService.listComment(1, 1, 50)).commentModelList
        this.commentList = commentList.map(item => ({
          name: item.creatorNick,
          content: item.content,
          userId: item.creatorId
        }))
        this.banList = (await this.chatService.listBanCommentUsers(1, 50)).banCommentUserModelList.map(item => item.userId)
        this.chatGoBottom()
        const chatDetail = (await this.chatService.getChatDetail())
        this.isMuteAll = chatDetail.muteAll
        if (status === 1) {
          this.startLivePlay()
        }
      } catch (err) {
        console.error(err)
      }
    },

    sendCustomMessage(goodsItem) {
      this.chatService.sendCustomMessageToAll(JSON.stringify({
        action: 'updateGoods',
        goodsDetail: {
          goods_id: '001',
          goods_image_url: 'https://gw.alicdn.com/imgextra/i4/O1CN011pKnYH1CQqqxe2pRi_!!6000000000076-2-tps-780-243.png',
        },
        showSeconds: 10
      })).then(() => {
        this.$message.success('发送成功')
      })
    },

    copyShare(text) {
      this.$copyText(text).then(e => {
        this.$message.success('已复制到剪贴板')
      }).catch(err => {
        console.error(err)
        this.$message.success('复制出错')
      })
    },

    createShareLink() {
      const json = {
        liveId: this.liveId,
        sharer: this.userNick,
        title: this.liveDetail.title,
      }
      const data = window.btoa(window.encodeURIComponent(JSON.stringify(json)))
      this.shareLink = `${window.location.origin}${window.location.pathname}#/share?data=${data}`
    },
  },

  computed: {
    ...mapGetters(['userId', 'userNick'])
  },

  components: {
    LiveInfo, LiveData, Chat, BanList
  },

  created() {
    this.liveId = this.$route.params.liveId
    this.getLiveRoom().then(res => {
      this.generateUpdateModal()
      this.auth(res.roomId, res.status)
      if (res.status === 1) this.startCountInterval()
      this.createShareLink()
    })
  },

  beforeDestroy() {
    this.stopRefreshInterval()
    this.stopCountInterval()
    if (this.roomChannel) {
      this.roomChannel.leaveRoom()
      this.roomEngine.logout()
    }
    if (this.clipboard) this.clipboard.destroy()
  },
}
</script>

<style scoped lang="less">
.container-common {
  // background-color: #1e2326;
  // border-radius: @border-radius;
  & > div {
    width: 100%;
  }
  display: flex;
  flex-direction: column;
  height: 800px;
}
.el-container {
  .el-tabs__content {
    background-color: #000;
  }
  .el-main {
    .page {
      min-width: 1000px;
      .page-content {
        padding: 20px 0;
        .title {
          font-size: 20px;
          font-weight: 500;
          margin-bottom: 13px;
          padding: 0 20px;
        }
        .btn-container {
          padding: 0 20px;
        }
        .main-container {
          position: relative;
          display: flex;
          justify-content: center;
          width: 100%;
          align-items: center;
          height: 100%;
          padding: 10px 0;
          .player-container {
            margin-right: 7px;
            flex-grow: 1;
            flex-shrink: 0;
            .container-common;
            flex-direction: column;
            justify-content: space-between;
            .player {
              flex-grow: 1;
              position: relative;
              .not-play {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: #fff;
                font-size: 20px;
                span {
                  position: absolute;
                  top: 50%;
                  left: 50%;
                  transform: translate(-50%, -50%);
                }
              }
              .player-inner {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
              }
            }
            .operation {
              height: 66px;
              border: 1px solid #eee;
              padding: 0 20px;
              display: flex;
              .item {
                height: 66px;
                width: 66px;
                text-align: center;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                cursor: pointer;
                &:hover {
                  background-color: #f2f2f2;
                }
                i {
                  font-size: 28px;
                  display: block;
                }
                span {
                  font-size: 13px;
                }
              }
            }
          }
          .chat-container {
            width: 370px;
            .container-common;
            .tab-container {
              height: 100%;
              position: relative;
            }
          }
        }
      }
    }
  }
}
.ext-item {
  display: flex;
}
.message {
  position: relative;
  height: 100%;
  padding-bottom: 110px;
  overflow: auto;
  &.notice-hoverd-message {
    padding-top: 45px;
  }
}
.dialog-title {
  .title {
    font-size: 14px;
    font-weight: 500;
    display: inline-block;
    margin-right: 30px;
  }
}
.good-list {
  max-height: 280px;
  overflow: auto;
  .list-item {
    display: flex;
    margin-bottom: 12px;
    align-items: center;
    justify-content: space-between;
    .info-container {
      display: flex;
      .img {
        width: 60px;
        height: 60px;
        background-size: cover;
        background-repeat: no-repeat;
      }
      .info {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        flex: 1;
        padding: 0 30px 0 5px;
        .name {
          font-size: 14px;
        }
        .price {
          font-size: 17px;
          font-weight: 500;
          color: #ff0000;
        }
      }
    }
    .btn {
      height: 34px;
    }
  }
}
.address-list {
  padding: 8px 4px;
  .item {
    .name {
      margin-bottom: 5px;
    }
    .content {
      display: flex;
      button {
        margin-left: 5px;
      }
    }
  }
}
</style>
