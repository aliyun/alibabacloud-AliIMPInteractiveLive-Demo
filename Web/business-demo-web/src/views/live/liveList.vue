<template>
  <div class="page">
    <div class="page-nav">
      <div class="bread">直播列表</div>
      <div class="func-list">
        <el-button type="text" @click="$router.replace('/selectScene')">返回场景选择</el-button>
        <el-button type="text" @click="downloadPC">下载PC推流工具</el-button>
      </div>
    </div>
    <search-field title="使用指引">
      <Guide />
    </search-field>
    <div class="page-content">
      <div class="button-field">
        <el-button type="primary" size="small" @click="createLive">创建直播</el-button>
      </div>
      <LiveTable ref="liveTable" from="live"/>
    </div>
    <el-dialog
      title="创建直播"
      :visible.sync="dialogVisible"
      width="700px"
    >
      <span slot="title" class="dialog-title">
        <span class="title">创建直播</span>
        <el-popover
          placement="bottom-end"
          width="300"
          v-model="createVisible">
          <p>在您的服务端接入vPaaS的服务端SDK，调用CreateLiveRoom接口创建一场直播，可以自定义直播的信息。</p>
          <p>接口文档：<a href="https://help.aliyun.com/document_detail/331956.html" target="_blank">创建直播</a></p>
          <el-button  slot="reference" type="text">如何使用接口创建直播？</el-button>
        </el-popover>
      </span>
      <el-form label-position="right" label-width="105px" :model="createLiveModel" ref="form" :rules="rules">
        <el-form-item label="直播标题" required prop="title">
          <el-input v-model="createLiveModel.title" placeholder="最大长度32位"></el-input>
        </el-form-item>
        <el-form-item label="主播昵称">
          <el-input :readonly="true" :value="userNick"></el-input>
        </el-form-item>
        <el-form-item label="直播封面" prop="coverUrl">
          <el-input v-model="createLiveModel.coverUrl" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="浏览器标题">
          <el-input v-model="createLiveModel.roomTitle" placeholder="浏览器顶部的标题，即document.title"></el-input>
        </el-form-item>
        <el-form-item label="直播间背景图" prop="playerBackground">
          <el-input v-model="createLiveModel.playerBackground" placeholder="https://example.png // 直播中止时的背景"></el-input>
        </el-form-item>
        <el-form-item label="主播头像" prop="avatar">
          <el-input v-model="createLiveModel.avatar" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="网站小图标" prop="favicon">
          <el-input v-model="createLiveModel.favicon" placeholder="https://example.ico // 网站tab小图标"></el-input>
        </el-form-item>
        <el-form-item label="分享跳转链接" prop="shareUrl">
          <el-input v-model="createLiveModel.shareUrl" placeholder="https://example.com // 分享的链接，配置后显示分享按钮，点击会复制到剪贴板"></el-input>
        </el-form-item>
        <el-form-item label="公告">
          <el-input type="textarea" maxlength="256" show-word-limit v-model="createLiveModel.notice"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import searchField from '@/components/tools/searchField'
import Guide from '@/components/functional/guide'
import LiveTable from '@/components/functional/liveTable'
import { mapGetters } from 'vuex'
import { liveApi } from '@/api'
import { downloadPCMixin } from '@/mixins/downloadPC'

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
    createLiveModel: {
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
    dialogVisible: false,
    createVisible: false
  }),

  methods: {
    jumpToStandard(liveId) {
      liveApi.getStandardRoomJumpUrl({
        bizId: liveId,
        userId: this.userId,
        userNick: this.userNick,
      }).then((res) => {
        window.open(res.data.result.standardRoomJumpUrl)
        // console.log(res.data.result.standardRoomJumpUrl)
      })
    },

    createLive() {
      this.dialogVisible = true
    },

    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const loading = this.$loading()
          const extension = {}
          const options = {
            ...this.createLiveModel
          }
          extensionKeys.forEach(key => {
            if (options[key] || options[key] === 0) {
              extension[key] = options[key]
            }
          })
          options.anchorId = this.userId
          options.anchorNick = this.userNick
          liveApi.createLiveRoom({ // api封装时过滤了不需要的参数
            ...options,
            userId: this.userId,
            extension,
          }).then(res => {
            this.$message.success('创建成功')
            this.createLiveModel = {
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
            }
            this.dialogVisible = false
            this.pageNumber = 1
            this.status = 3
            this.$refs.liveTable.listLiveRooms()
          }).catch(err => {
            this.$message.error('创建失败，信息请看开发者工具报错')
            console.error(err)
          }).finally(() => {
            loading.close()
            this.$refs.form.resetFields();
          })
        } else {
          this.$message.error('请正确填写数据')
          return false
        }
      })
    }
  },

  computed: {
    ...mapGetters(['userId', 'userNick'])
  },

  components: {
    LiveTable, searchField, Guide
  }
}
</script>

<style scoped lang="less">
  .tips {
    display: block;
    font-size: 13px;
    color: #bbb;
    margin-bottom: 10px;
    text-align: center;
  }
  .ext-item {
    display: flex;
  }
  .dialog-title {
    .title {
      font-size: 14px;
      font-weight: 500;
      display: inline-block;
      margin-right: 30px;
    }
  }
</style>
