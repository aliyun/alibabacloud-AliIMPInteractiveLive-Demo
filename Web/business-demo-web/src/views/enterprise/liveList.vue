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
      <LiveTable ref="liveTable" from="enterprise"/>
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
        <el-form-item label="直播标题" prop="title">
          <el-input v-model="createLiveModel.title" placeholder="最大长度32位"></el-input>
        </el-form-item>
        <el-form-item label="主播昵称">
          <el-input :readonly="true" :value="userNick"></el-input>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="createLiveModel.preStartTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="timestamp"
            @change="selectPreStartTimeHandler">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="直播封面" prop="coverUrl">
          <el-input v-model="createLiveModel.coverUrl" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="主播头像" prop="anchorAvatarURL">
          <el-input v-model="createLiveModel.anchorAvatarURL" placeholder="https://example.png"></el-input>
        </el-form-item>
        <el-form-item label="主播简介" prop="anchorIntroduction">
          <el-input v-model="createLiveModel.anchorIntroduction" placeholder="主播简介"></el-input>
        </el-form-item>
        <el-form-item label="直播简介">
          <el-input type="textarea" maxlength="256" show-word-limit v-model="createLiveModel.liveIntroduction" placeholder="直播简介"></el-input>
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

const validLength = (maxLength) => {
  return (rule, value, callback) => {
    if (value.length < maxLength) callback()
    else callback(new Error('最大长度不能超过' + maxLength + '位'))
  }
}

const extensionKeys = [
  'anchorAvatarURL',
  'anchorIntroduction',
  'liveIntroduction',
  'preStartTime'
]

const initLiveModel = () => {
  return {
    title: '',
    notice: '',
    coverUrl: '',
    anchorId: '',
    anchorNick: '',
    anchorAvatarURL: '', // 直播间头像
    anchorIntroduction: '',
    liveIntroduction: '',
    preStartTime: +new Date()
  }
}

export default {
  mixins: [downloadPCMixin],

  data: () => ({
    createLiveModel: initLiveModel(),
    rules: {
      title: [
        { required: true, message: '请输入直播标题', trigger: 'blur' },
        { max: 32, message: '最大长度32位', trigger: 'blur' },
      ],
      coverUrl: [
        { validator: validUrl, trigger: 'blur' },
        { validator: validLength(256), trigger: 'blur' }
      ],
      anchorAvatarURL: [
        { validator: validUrl, trigger: 'blur' },
        { validator: validLength(256), trigger: 'blur' }
      ],
      anchorIntroduction: [
        { validator: validLength(256), trigger: 'blur' }
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

    selectPreStartTimeHandler(e) {
      if (e < +new Date()) {
        this.$message.error('不能选取之前的时间')
        this.createLiveModel.preStartTime = +new Date()
      }
    },

    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (this.createLiveModel.preStartTime < +new Date()) return this.$message.error('不能选取之前的时间')
        if (valid) {
          const loading = this.$loading()
          const extension = {}
          const options = {
            ...this.createLiveModel
          }
          extensionKeys.forEach(key => {
            if (options[key] || options[key] === 0) {
              extension[key] = options[key].toString()
            }
          })
          options.anchorId = this.userId
          options.anchorNick = this.userNick
          extension.anchorNick = this.userNick
          // mock 数据 liveId 是一样的
          liveApi.createLiveRoom({ // api封装时过滤了不需要的参数
            ...options,
            userId: this.userId,
            extension,
          }).then(res => {
            this.$message.success('创建成功')
            this.createLiveModel = initLiveModel()
            this.dialogVisible = false
            this.pageNumber = 1
            this.status = 3
            this.$refs.liveTable.listLiveRooms()
            this.$refs.form.resetFields();
          }).catch(err => {
            this.$message.error('创建失败，信息请看开发者工具报错')
            console.dir(err)
          }).finally(() => {
            loading.close()
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
