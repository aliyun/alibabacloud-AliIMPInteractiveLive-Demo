import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, connect, Dispatch } from 'umi'
import { addZero, getImageSize } from '@/utils'
import { message, Popover, Modal } from 'antd'
import { ExclamationCircleOutlined } from '@ant-design/icons'
import { usePersistFn, useClickAway } from 'ahooks'
import ShowUpload from './components/ShowUpload'

import Emitter from '@/utils/emitter'
import styles from './index.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  dispatch: Dispatch
  boardType: 'pure' | 'full'
  linking?: boolean // 是否连麦中
}

interface whiteBoardTool {
  label: string
  type: string
  icon?: string
  state?: any
  showing?: Function
  toolList?: any
  ref?: any
  showSetting?: (item?: any) => any
  callback?: () => any
  handler?: (e: any) => any
  disable?: boolean
  typeList?: string
}

interface whiteBoardPageInfo {
  whiteboardPage: number
  groupPage: number
  group: string
}

interface uploadObj {
  fileName: string
  showProgess: boolean
  percent: number
  failure: boolean
  finished: string
}
enum finishedStatus {
  FileUploadStart = 'file-upload-start', // 文件上传开始
  FileTranscodeStart = 'file-transcode-start', // 文件上传完成未转码
  FileUploadFailure = 'file-upload-failure', // 文件上传失败
  FileInsertStart = 'file-insert-start', // 文件转码完成未插入
  FileTranscodeFailure = 'file-transcode-failure', // 文件转码失败
  FileInsertFinish = 'file-insert-finish', // 文件插入白板完成
  FileInsertFailure = 'file-insert-failure', // 文件插入白板失败
}

const { EventNameEnum } = window.RoomPaasSdk
const { Canvas, AliyunBoard } = window.aliyunBoardSDK
const avaliableCoursewareTypeList = ['ppt', 'pptx', 'pdf']
const avaliableImageTypeList = ['png', 'jpg', 'jpeg']
const wbDefaultWidth = 2267
const wbDefaultHeight = 1275

const WhiteBoard: FC<PageProps> = ({ room, status, dispatch, boardType, linking }) => {
  const [page, setPage] = useState(1)
  const [loaded, setLoaded] = useState(false)
  const [inputPage, setInputPage] = useState(1)
  const [currentTool, setCurrentTool] = useState('画笔')
  const [previewList, setPreviewList] = useState<string[]>([])
  const [groupList, setGroupList] = useState<any[]>([])
  const [allIntraDocsInfo, setAllIntraDocsInfo] = useState<any[]>([])
  const [slidebarExpand, setSlidebarExpand] = useState(true) // 是否将sidebar展开，负责白板视图时收起与放开
  const [uploadFileList, setUploadFileList] = useState<uploadObj[]>([])
  const [showpenSetting, setShowpenSetting] = useState(false)
  const [showtextSetting, setShowtextSetting] = useState(false)
  const [showlaserPenSetting, setShowlaserPenSetting] = useState(false)
  const [showcolorSetting, setShowcolorSetting] = useState(false)
  const [pensize, setPensize] = useState(2)
  const [initPensize, setinitPensize] = useState(30)
  const [currentPen, setCurrentPen] = useState('')
  const [fontsize, setFontsize] = useState(5)
  const [initFontsize, setinitFontsize] = useState(30)
  const [currentLaser, setCurrentLaser] = useState('')
  const [currentColor, setCurrentColor] = useState('#FF0000')
  const [showDelButton, setShowDelButton] = useState(-1)
  const [isLoading, setIsLoading] = useState(false)

  const toolPenRef = useRef<HTMLDivElement>(null)
  const toolTextRef = useRef<HTMLDivElement>(null)
  const toolLaserRef = useRef<HTMLDivElement>(null)
  const toolColorRef = useRef<HTMLDivElement>(null)
  const scrollAxis = useRef<HTMLDivElement>(null)
  const scrollWheel = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const whiteBoardWrapperRef = useRef(null)
  const scale = useRef(1)
  const maxPage = useRef(1)
  const allDocsInfo = useRef<any[]>([])
  const isConvert = useRef(false)
  const fileList = useRef<string[]>([])
  const { confirm } = Modal

  const getDocumentData = async () => {
    const { documentAccessInfo } = await window.wbService.getWbToken(room.docKey)
    delete documentAccessInfo.wsDomain
    return documentAccessInfo
  }

  const getPreviewUrl = usePersistFn((e) => {
    console.log('getPreviewUrl', e)
    if (!e) return e
    const path = e.split('?')[0]
    for (let i in allDocsInfo.current) {
      const res = allDocsInfo.current[i].urlList.find((item: any) => {
        const newPath = item.split('?')[0]
        return newPath === path
      })
      if (res) return Promise.resolve(res)
    }
    for (let i in allIntraDocsInfo) {
      const res = allIntraDocsInfo[i].urlList.find((item: any) => {
        const newPath = item.split('?')[0]
        return newPath === path
      })
      if (res) return Promise.resolve(res)
    }
    return Promise.resolve(e)
  })

  const aliyunBoardConfig = {
    getDocumentData,
    getPreviewUrl,
    docKey: room.docKey,
    syncMode: 1,
    maxSceneCount: 100000,
    minScale: 0.1,
    maxScale: 3,
    fitMode: 0,
    forceSync: true,
    viewportSync: false,
    pointerMultiSelect: false,
    enableGesture: true,
    pointerTrailEnable: false,
    schema: {
      size: {
        width: 40,
        height: 22.5,
        dpi: 144,
      },
      slides: [
        {
          id: '1',
          background: 'rgba(255, 255, 255, 1)',
        },
      ],
    },
  }

  const aliyunBoardRef = useRef<any>(null)

  const paintTools: whiteBoardTool[] = [
    {
      label: '点选',
      type: 'icon',
      icon: '#icon-zhizhen',
      callback: () => {
        setCurrentTool('点选')
        aliyunBoardRef.current.setToolType('pointer')
      },
    },
    {
      label: '框选',
      type: 'icon',
      icon: '#icon-kuangxuan',
      callback: () => {
        setCurrentTool('框选')
        aliyunBoardRef.current.setToolType('multiSelect')
      },
    },
    {
      label: '画笔',
      type: 'withsetting',
      icon: '#icon-huabi',
      ref: toolPenRef,
      state: showpenSetting,
      showing: setShowpenSetting,
      toolList: {
        mainTool: [
          {
            label: '画笔',
            type: 'pen',
            icon: '#icon-qianbi-danchuyangshi',
            useful: true,
          },
          // {
          //   label: '曲线',
          //   type: '',
          //   icon: '#icon-qianbi-quxian',
          //   useful: false,
          // },
          {
            label: '直线',
            type: 'straight',
            icon: '#icon-qianbi-zhixian',
            useful: true,
          },
        ],
        appendTool: [
          {
            label: '矩形',
            type: 'rect',
            icon: '#icon-zhengfangxing',
          },
          {
            label: '圆角矩形',
            type: 'roundRect',
            icon: '#icon-zhengfangxing-yuanjiao',
          },
          {
            label: '椭圆',
            type: 'circle',
            icon: '#icon-yuanxing',
          },
          {
            label: '三角形',
            type: 'triangle',
            icon: '#icon-sanjiaoxing',
          },
          {
            label: '直角三角形',
            type: 'rightTriangle',
            icon: '#icon-sanjiaoxing-zhijiao',
          },
        ],
      },
      showSetting: (item: any) => {
        return (
          <div className={styles['setting-conponent']} ref={scrollAxis}>
            <div className={styles.slider}>
              <div className={styles['slider-number']}>{pensize}</div>
              <div className={styles['slider-body']}>
                <div
                  className={styles.scrollwheel}
                  ref={scrollWheel}
                  style={{ left: initPensize + 'px' }}
                  onMouseDown={(e) => {
                    mouseDownHandler(e, 'pen')
                  }}
                >
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-hudongbaiban-gundonglun"></use>
                  </svg>
                </div>
                <div className={styles.scrollaxis}>
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-hudongbaiban-gundongzhou"></use>
                  </svg>
                </div>
              </div>
            </div>
            <div className={styles['mainset-bar']}>
              {item.toolList.mainTool.map((item: any, index: number) => {
                return (
                  <div
                    className={`${styles['main-item']} ${currentPen === item.label ? styles['mainitem-active'] : ''}`}
                    key={index}
                    onClick={(e) => {
                      e.stopPropagation()
                      if (item.useful) {
                        changePenTool(item.type, e)
                        setCurrentPen(item.label)
                      } else {
                        message.info('暂不支持')
                      }
                    }}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                )
              })}
            </div>
            <div className={styles['append-bar']}>
              {item.toolList.appendTool.map((item: any, index: number) => {
                return (
                  <div
                    className={`${styles['append-item']} ${
                      currentPen === item.label ? styles['appenditem-active'] : ''
                    }`}
                    key={index}
                    onClick={(e) => {
                      changePenTool(item.type, e)
                      setCurrentPen(item.label)
                      // message.info('暂不支持')
                    }}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                )
              })}
            </div>
          </div>
        )
      },
      callback: () => {
        setCurrentTool('画笔')
        // aliyunBoardRef.current.setToolType('pen')
        setShowpenSetting(true)
      },
    },
    {
      label: '文本',
      type: 'withsetting',
      icon: '#icon-wenben1',
      ref: toolTextRef,
      state: showtextSetting,
      showing: setShowtextSetting,
      toolList: {
        mainTool: [
          {
            label: '文本',
            type: 'text',
            icon: '#icon-ic_baiban_text',
          },
        ],
        appendTool: [
          {
            label: '加粗',
            type: 'fontWeight',
            icon: '#icon-jiacu',
            useful: true,
          },
          {
            label: '倾斜',
            type: 'fontStyle',
            icon: '#icon-xieti',
            useful: true,
          },
          {
            label: '下划线',
            type: 'textDecoration',
            icon: '#icon-xiahuaxian',
            useful: true,
          },
        ],
      },
      showSetting: (item) => {
        return (
          <div className={styles['setting-conponent']} ref={scrollAxis}>
            <div className={styles.slider}>
              <div className={styles['slider-number']}>{fontsize}</div>
              <div className={styles['slider-body']}>
                <div
                  className={styles.scrollwheel}
                  ref={scrollWheel}
                  style={{ left: initFontsize + 'px' }}
                  onMouseDown={(e) => {
                    mouseDownHandler(e, 'text')
                  }}
                >
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-hudongbaiban-gundonglun"></use>
                  </svg>
                </div>
                <div className={styles.scrollaxis}>
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-hudongbaiban-gundongzhou"></use>
                  </svg>
                </div>
              </div>
            </div>
            <div className={styles['mainset-bar']}>
              {item.toolList.mainTool.map((item: any, index: number) => {
                return (
                  <div
                    className={styles['main-item']}
                    onClick={(e) => {
                      changeTextTool(e)
                    }}
                    key={index}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                )
              })}
            </div>
            <div className={styles['append-bar']}>
              {item.toolList.appendTool.map((item: any, index: number) => {
                return (
                  <div
                    className={`${styles['append-item']}`}
                    key={index}
                    onClick={(e) => {
                      if (item.useful) {
                        changeSelectStyle(item.type, e)
                      } else {
                        message.info('暂不支持')
                      }
                    }}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                )
              })}
            </div>
          </div>
        )
      },
      callback: () => {
        setCurrentTool('文本')
        setShowtextSetting(true)
      },
    },
    {
      label: '调色盘',
      type: 'withsetting',
      icon: '#icon-ic_baiban_color',
      ref: toolColorRef,
      state: showcolorSetting,
      showing: setShowcolorSetting,
      toolList: {
        colorTool: [
          '#FF0000', // 红色
          '#F5A623', // 橙色
          '#F8E71C', // 黄色
          '#7ED321', // 绿色
          '#00C1DE', // 蓝色
          '#2D51E9', // 靛色
          '#9013FE', // 紫色
          '#FFA3A3', // 粉色
          '#FFFFFF', // 白色
          '#9B9B9B', // 灰色
          '#4A4A4A', // 深灰色
          '#000000', // 黑色
        ],
      },
      showSetting: (item) => {
        return (
          <div className={styles['setting-conponent']} ref={scrollAxis}>
            <div className={styles['color-bar']}>
              {item.toolList.colorTool.map((color: any, index: number) => {
                return (
                  <div
                    className={`${styles['color-item']} ${currentColor === color ? styles['color-active'] : ''}`}
                    style={{ backgroundColor: color }}
                    key={index}
                    onClick={(e) => {
                      changeColor(color, e)
                      setCurrentColor(color)
                    }}
                  ></div>
                )
              })}
            </div>
          </div>
        )
      },
      callback: () => {
        const strokeList = ['pen', 'straight', 'rect', 'roundRect', 'circle', 'triangle', 'rightTriangle', 'text']
        const currentToolName = aliyunBoardRef.current.currentToolName
        console.log(currentToolName)
        if (strokeList.includes(currentToolName)) {
          setShowcolorSetting(true)
          return
        }
        message.info('当前模块不可设置颜色')
      },
    },
    {
      label: '箭头',
      type: 'icon',
      icon: '#icon-jiantou',
      callback: () => {
        setCurrentTool('箭头')
        aliyunBoardRef.current.setToolType('straight')
        aliyunBoardRef.current.setCurrentBrush({ endArrow: true })
      },
    },
    {
      label: '激光笔',
      type: 'withsetting',
      icon: '#icon-jiguangbi',
      ref: toolLaserRef,
      state: showlaserPenSetting,
      showing: setShowlaserPenSetting,
      toolList: {
        mainTool: [
          {
            label: '激光笔',
            type: 'laserPen',
            icon: '#icon-jiguangbi',
            useful: true,
          },
          {
            label: '激光笔(细)',
            type: 'laser',
            icon: '#icon-jiguangbi-2',
            useful: true,
          },
        ],
      },
      showSetting: (item) => {
        return (
          <div className={styles['setting-conponent']} ref={scrollAxis}>
            <div className={styles['mainset-bar']}>
              {item.toolList.mainTool.map((item: any, index: number) => {
                return (
                  <div
                    className={`${styles['main-item']} ${currentLaser === item.type ? styles['mainitem-active'] : ''}`}
                    key={index}
                    onClick={(e) => {
                      if (item.useful) {
                        changeLaser(item.type, e)
                        setCurrentLaser(item.type)
                      } else {
                        message.info('暂不支持')
                      }
                    }}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                )
              })}
            </div>
          </div>
        )
      },
      callback: () => {
        setCurrentTool('激光笔')
        setShowlaserPenSetting(true)
      },
    },
  ]

  const fileTools: whiteBoardTool[] = [
    {
      label: '插入图片',
      type: 'upload',
      icon: '#icon-tupian',
      handler: (e: any) => {
        const file = e.target.files[0]
        if (!file) {
          return message.error('上传终止')
        }
        const fileInfo = file.name.split('.')
        if (fileInfo.length < 2) {
          return message.error('文件缺少后缀名')
        }
        const fileName = fileInfo[0]
        const fileType = fileInfo[fileInfo.length - 1]
        if (!avaliableImageTypeList.includes(fileType)) {
          return message.error('文件格式不正确')
        }
        const updateFile = [
          {
            fileName: fileName,
            showProgess: true,
            percent: 0,
            failure: false,
            finished: finishedStatus.FileUploadStart,
          },
        ]
        setUploadFileList(updateFile)
        isConvert.current = false
        window.docService
          .upload(file)
          .then((docId: string) => {
            updateFile[0].finished = finishedStatus.FileTranscodeStart
            setUploadFileList([...updateFile])
            let pageList: whiteBoardPageInfo[]
            window.wbService
              .reportWhiteboardPageOperate('add', docId, page, 1, 'img')
              .then((d: any) => {
                pageList = d.pageList
                return Promise.all([
                  generateDocIdsToGetAllDocs(d.pageList),
                  generateDocIdsToGetAllDocs(d.intraPageResourceList),
                ])
              })
              .then((res: any) => {
                const [docsInfo, intraDocsInfo] = res
                setAllIntraDocsInfo(intraDocsInfo)
                allDocsInfo.current = docsInfo || []
                setGroupList(generateGroupList(pageList, docsInfo))
                setPreviewList(generatePreviewList(pageList, docsInfo))
                return Promise.all([window.docService.getUrlList(docId), getImageSize(file)])
              })
              .then((res: any) => {
                const [docInfo, imageSize] = res
                const { urlList } = docInfo
                let { width, height } = imageSize
                if (width > wbDefaultWidth) {
                  height = (height * 1000) / width
                  width = 1000
                } else if (height > wbDefaultHeight) {
                  width = (width * 800) / height
                  width = 800
                }
                aliyunBoardRef.current.addImage({
                  width,
                  height,
                  href: urlList[0],
                })
                // setShowProgess(false)
                updateFile[0].showProgess = false
                updateFile[0].finished = finishedStatus.FileInsertFinish
                setUploadFileList([...updateFile])
                message.success('上传成功')
                if (inputRef.current) {
                  inputRef.current.value = ''
                }
              })
          })
          .catch((err: any) => {
            console.error(err)
            const updateFile = [...uploadFileList]
            updateFile[0].finished = finishedStatus.FileInsertFailure
            message.error('上传失败')
            if (inputRef.current) {
              inputRef.current.value = ''
            }
          })
      },
    },
    {
      label: '上传PPT/课件',
      type: 'upload',
      icon: '#icon-shangchuan1',
      handler: (e: any) => {
        const file = e.target.files[0]
        if (!file) {
          return message.error('上传终止')
        }
        const fileInfo = file.name.split('.')
        if (fileInfo.length < 2) {
          return message.error('文件缺少后缀名')
        }
        const fileName = fileInfo[0]
        const fileType = fileInfo[fileInfo.length - 1]
        if (!avaliableCoursewareTypeList.includes(fileType)) {
          return message.error('文件格式不正确')
        }
        if (fileList.current.includes(fileName)) {
          message.info('文件已上传')
          if (inputRef.current) {
            inputRef.current.value = ''
          }
          return
        }
        let aliOss
        // setFileName(file.name)
        // setShowProgess(true)
        const updateFile = [
          {
            fileName: fileName,
            showProgess: true,
            percent: 0,
            failure: false,
            finished: finishedStatus.FileUploadStart,
          },
        ]
        setUploadFileList(updateFile)
        isConvert.current = true
        window.docService
          .upload(file)
          .then((docId: string) => {
            updateFile[0].finished = finishedStatus.FileTranscodeStart
            setUploadFileList([...updateFile])
            fileList.current.push(fileName)
            window.docService.convert(docId, 'jpg', fileName).then((d: any) => {
              dispatch({
                type: 'status/setCurrentDocId',
                payload: d.targetDocId,
              })
            }) // 转码
            dispatch({
              type: 'status/setIsDocConverting',
              payload: true,
            })
            if (inputRef.current) {
              inputRef.current.value = ''
            }
          })
          .catch((err: any) => {
            console.log(err)
            if (inputRef.current) {
              inputRef.current.value = ''
            }
            fileList.current.pop()
          })
      },
    },
  ]

  const clearTools: whiteBoardTool[] = [
    {
      label: '橡皮擦',
      type: 'icon',
      icon: '#icon-xiangpi',
      callback: () => {
        setCurrentTool('橡皮擦')
        aliyunBoardRef.current.setToolType('eraser')
      },
    },
    {
      label: '清除画板',
      type: 'icon',
      icon: '#icon-qingchu',
      callback: () => {
        aliyunBoardRef.current.clearBoard()
      },
    },
  ]

  const zTools1: whiteBoardTool[] = [
    {
      label: '撤销',
      type: 'icon',
      icon: '#icon-chehui',
      callback: () => aliyunBoardRef.current.undo(),
    },
  ]

  const zTools2: whiteBoardTool[] = [
    {
      label: '重做',
      type: 'icon',
      icon: '#icon-zhongzuo',
      callback: () => aliyunBoardRef.current.redo(),
    },
    // {
    //   label: '全屏',
    //   type: 'icon',
    //   icon: '#icon-kuangxuan2',
    //   disable: true,
    //   callback: () => message.info('暂不支持'),
    // },
  ]

  const scaleTools: whiteBoardTool[] = [
    {
      label: '放大',
      type: 'icon',
      icon: '#icon-fangda1',
      disable: scale.current >= aliyunBoardConfig.maxScale - 0.1, // 白板的bug，实际上将要达到maxScale的时候就报错了
      callback: () => {
        if (scale.current >= aliyunBoardConfig.maxScale - 0.1) return
        doSetScale(scale.current + 0.1)
      },
    },
    {
      label: '缩小',
      type: 'icon',
      icon: '#icon-suoxiao2',
      disable: scale.current <= aliyunBoardConfig.minScale,
      callback: () => {
        if (scale.current <= aliyunBoardConfig.minScale + 0.1) return
        doSetScale(scale.current - 0.1)
      },
    },
    // {
    //   label: '截屏',
    //   type: 'icon',
    //   icon: '#icon-jieping',
    //   disable: true,
    //   callback: () => message.info('暂不支持'),
    // },
  ]

  const pageTools: whiteBoardTool[] = [
    {
      label: '前一页',
      type: 'icon',
      icon: '#icon-xiangzuofanye',
      disable: page <= 1,
      callback: () => {
        if (page <= 1) return
        doGotoScene(page - 1)
      },
    },
    {
      label: '页码',
      type: 'page',
      callback: () => {},
    },
    {
      label: '后一页',
      type: 'icon',
      icon: '#icon-xiangyoufanyesvg',
      disable: page === maxPage.current,
      callback: () => {
        if (page === maxPage.current) return
        doGotoScene(page + 1)
      },
    },
  ]

  const fileTools2: whiteBoardTool[] = [
    {
      label: '新增一页',
      type: 'icon',
      icon: '#icon-tianjiawenjian',
      callback: () => {
        aliyunBoardRef.current.addScene()
        // 上报页码映射
        let pageList: whiteBoardPageInfo[]
        window.wbService
          .reportWhiteboardPageOperate('add', 'wb', page, 1, 'page')
          .then((d: any) => {
            pageList = d.pageList
            return Promise.all([
              generateDocIdsToGetAllDocs(d.pageList),
              generateDocIdsToGetAllDocs(d.intraPageResourceList),
            ])
          })
          .then((res: any) => {
            const [docsInfo, intraDocsInfo] = res
            setAllIntraDocsInfo(intraDocsInfo)
            allDocsInfo.current = docsInfo || []
            setGroupList(generateGroupList(pageList, docsInfo))
            setPreviewList(generatePreviewList(pageList, docsInfo))
          })
        maxPage.current += 1
        setPage(page + 1)
        setInputPage(Number(inputPage) + 1)
      },
    },
  ]

  const studentTools: whiteBoardTool[] = [
    {
      label: '放大',
      type: 'icon',
      icon: '#icon-fangda1',
      disable: scale.current >= aliyunBoardConfig.maxScale - 0.1, // 白板的bug，实际上将要达到maxScale的时候就报错了
      callback: () => {
        if (scale.current >= aliyunBoardConfig.maxScale - 0.1) return
        doSetScale(scale.current + 0.1)
      },
    },
    {
      label: '缩小',
      type: 'icon',
      icon: '#icon-suoxiao2',
      disable: scale.current <= aliyunBoardConfig.minScale,
      callback: () => {
        if (scale.current <= aliyunBoardConfig.minScale + 0.1) return
        doSetScale(scale.current - 0.1)
      },
    },
  ]

  const leftTools = [paintTools, fileTools, clearTools]
  const bottomTools = [zTools1, zTools2, scaleTools, pageTools, fileTools2]
  const studentBottomTools = [studentTools]

  const mouseDownHandler = (e: any, state: 'text' | 'pen') => {
    let offsetX = e.nativeEvent.offsetX
    let currentPensize: number
    const Area = scrollAxis.current as HTMLDivElement
    const Wheel = scrollWheel.current as HTMLDivElement
    Area.onmousemove = (e) => {
      let moveX = e.clientX - Area.offsetLeft - offsetX - 70
      if (moveX <= 15) {
        moveX = 15
      }
      if (moveX >= 60) {
        moveX = 60
      }
      Wheel.style.left = moveX + 'px'
      switch (state) {
        case 'pen':
          setinitPensize(moveX)
          currentPensize = Math.floor(Wheel.offsetLeft / 5) - 2
          setPensize(currentPensize)
          break
        case 'text':
          setinitFontsize(moveX)
          setFontsize(Math.floor(Wheel.offsetLeft / 5) - 2)
          break
        default:
          break
      }
    }
    document.onmouseup = () => {
      Area.onmousemove = null
      document.onmouseup = null
      switch (state) {
        case 'pen':
          aliyunBoardRef.current.setCurrentBrush({ strokeWidth: currentPensize })
          setShowpenSetting(false)
          break
        case 'text':
          changeSelectStyle()
          setShowtextSetting(false)
          break
        default:
          break
      }
    }
  }

  const changePenTool = (tool: string, e?: any) => {
    e && e.stopPropagation && e.stopPropagation()
    aliyunBoardRef.current.setToolType(tool)
    if (tool === 'straight') {
      aliyunBoardRef.current.setCurrentBrush({ endArrow: false })
    }
    aliyunBoardRef.current.setCurrentBrush({ strokeWidth: pensize, stroke: currentColor })
    setShowpenSetting(false)
  }

  const changeTextTool = (e: any) => {
    e.stopPropagation()
    aliyunBoardRef.current.setToolType('text')
    aliyunBoardRef.current.setCurrentBrush({ fontSize: fontsize * 12, stroke: currentColor })
    setShowtextSetting(false)
  }

  const changeLaser = (tool: string, e?: any) => {
    e && e.stopPropagation && e.stopPropagation()
    aliyunBoardRef.current.setToolType(tool)
    setShowlaserPenSetting(false)
  }

  const changeColor = (color: string, e?: any) => {
    e && e.stopPropagation && e.stopPropagation()
    aliyunBoardRef.current.setCurrentBrush({ stroke: color })
    setShowcolorSetting(false)
  }

  const changeSelectStyle = usePersistFn((style: string = '', e?: any) => {
    e && e.stopPropagation && e.stopPropagation()
    const currentToolName = aliyunBoardRef.current.currentToolName
    if (currentToolName === 'pointer' || currentToolName === 'multiSelect') {
      if (!aliyunBoardRef.current.stage.selection.shapes[0]) {
        message.info('请先选中文本再尝试修改格式')
        return
      }
      const selectTextStyle = aliyunBoardRef.current.stage.selection.shapes[0].textStyle
      switch (style) {
        case 'fontWeight':
          const fontWeight = selectTextStyle.fontWeight === 'bold' ? 'normal' : 'bold'
          aliyunBoardRef.current.updateSelectedTextStyle({ ...selectTextStyle, fontWeight })
          break
        case 'fontStyle':
          const fontStyle = selectTextStyle.fontStyle === 'italic' ? 'normal' : 'italic'
          aliyunBoardRef.current.updateSelectedTextStyle({ ...selectTextStyle, fontStyle })
          break
        case 'textDecoration':
          const textDecoration = selectTextStyle.textDecoration === 'underline' ? 'none' : 'underline'
          aliyunBoardRef.current.updateSelectedTextStyle({ ...selectTextStyle, textDecoration })
          break
        default:
          break
      }
      setShowtextSetting(false)
    } else if (currentToolName === 'text') {
      aliyunBoardRef.current.setCurrentBrush({ fontSize: fontsize * 12 })
    } else {
      message.info('请先选中字体或使用文本工具')
    }
  })

  const clearAllSettingState = () => {
    showtextSetting && setShowtextSetting(false)
    showpenSetting && setShowpenSetting(false)
    showlaserPenSetting && setShowlaserPenSetting(false)
    showcolorSetting && setShowcolorSetting(false)
  }

  const pageInputHandler = (e: any) => {
    if (e.target.value === '' || /^\d+$/.test(e.target.value)) setInputPage(e.target.value)
  }

  const changPageHandler = () => {
    if (inputPage === page) return
    if (inputPage <= 0 || !inputPage || inputPage > maxPage.current) {
      message.error('页码输入有误')
      setInputPage(page)
      return
    }
    doGotoScene(inputPage)
  }

  const clickPreviewHandler = (num: number) => {
    if (num === page) return
    doGotoScene(num)
  }

  const aliyunBoardReadyHandler = usePersistFn(() => {
    // 设置默认数据
    maxPage.current = aliyunBoardRef.current.getScenesCount()
    setPage(aliyunBoardRef.current.getCurrentSceneIndex() + 1)
    setInputPage(aliyunBoardRef.current.getCurrentSceneIndex() + 1)
    // 不显示工具栏
    if (!room.isOwner) {
      aliyunBoardRef.current.setReadOnly(true)
      aliyunBoardRef.current.setToolType('pointer')
    }
  })

  const doGotoScene = async (to: number) => {
    to = Number(to)
    // 先切页面后获取
    setPage(to)
    setInputPage(to)
    aliyunBoardRef.current.gotoScene(to - 1)
  }

  const doSetScale = (num: number, isTemp?: boolean) => {
    aliyunBoardRef.current.setScale(num)
    if (!isTemp) {
      scale.current = num
    }
  }

  const aliyunBoardFitHandler = usePersistFn(() => {
    // 设置缩放
    if (whiteBoardWrapperRef.current) {
      const wrapperDiv = whiteBoardWrapperRef.current as HTMLDivElement
      const width = wrapperDiv.clientWidth
      const height = wrapperDiv.clientHeight
      if (width / height > wbDefaultWidth / wbDefaultHeight) {
        // 屏幕比较宽，height为长边顶头
        doSetScale(height / wbDefaultHeight)
      } else {
        doSetScale(width / wbDefaultWidth)
      }
    }
  })

  const convertDoneHandler = usePersistFn((isSuccess: boolean) => {
    if (!isSuccess) {
      message.error('文档转码失败')
      // setPercent(100)
      const updateFile = [...uploadFileList]
      updateFile[0].failure = true
      updateFile[0].finished = finishedStatus.FileTranscodeFailure
      setUploadFileList([...updateFile])
      fileList.current.pop()
    }
  })

  const insertPPTHandler = usePersistFn(async () => {
    try {
      console.log(status.currentDocId)
      if (!status.currentDocId) return
      const initPage = page
      setIsLoading(true)
      await doGotoScene(maxPage.current)
      const docInfo = await window.docService.getUrlList(status.currentDocId)
      const { urlList, docName } = docInfo
      const index = maxPage.current
      // 添加白板，并设置该页背景
      for (let i in urlList) {
        await aliyunBoardRef.current.addScene()
        await aliyunBoardRef.current.addBackgroundImage(urlList[i])
      }
      await doGotoScene(0)
      maxPage.current += urlList.length
      // 上报页码映射
      let pageList: whiteBoardPageInfo[]
      window.wbService
        .reportWhiteboardPageOperate('add', status.currentDocId, index, urlList.length, 'page')
        .then((d: any) => {
          pageList = d.pageList
          return Promise.all([
            generateDocIdsToGetAllDocs(d.pageList),
            generateDocIdsToGetAllDocs(d.intraPageResourceList),
          ])
        })
        .then(async (res: any) => {
          const [docsInfo, intraDocsInfo] = res
          setAllIntraDocsInfo(intraDocsInfo)
          allDocsInfo.current = docsInfo || []
          setGroupList(generateGroupList(pageList, docsInfo))
          setPreviewList(generatePreviewList(pageList, docsInfo))
          doGotoScene(index + 1)
          message.success('文档上传成功')
          await doGotoScene(initPage)
          setIsLoading(false)
          const updateFile = [...uploadFileList]
          updateFile[0].percent = 100
          updateFile[0].finished = finishedStatus.FileInsertFinish
          setUploadFileList([...updateFile])
          // setPercent(100)
          setTimeout(() => {
            // setShowProgess(false)
            // setPercent(0)
            updateFile[0].showProgess = false
            setUploadFileList([...updateFile])
          }, 3000)
        })
    } catch (err) {
      console.error(err)
      setIsLoading(false)
      fileList.current.pop()
    }
  })

  const setGroupExpand = (index: number) => {
    const newGroupList = groupList.slice(0)
    newGroupList[index].expand = !newGroupList[index].expand
    setGroupList(newGroupList)
  }

  const popoverContent = (content: string) => <span>{content}</span>

  const progressHandler = usePersistFn((progress: number) => {
    const updateFile = [...uploadFileList]
    if (progress === 1 && isConvert.current) {
      // 当上传OK时，等转码，将进度条设到99
      // setPercent(99)
      updateFile[0].percent = 99
      setUploadFileList([...updateFile])
      return
    }
    // setPercent(Math.round(progress * 10000) / 100)
    updateFile[0].percent = Math.round(progress * 10000) / 100
    setUploadFileList([...updateFile])
  })

  const getAllDocsInfo = (docIds: string[]) => {
    if (docIds && docIds.length > 0) return Promise.all(docIds.map((docId) => window.docService.getUrlList(docId)))
    return Promise.resolve()
  }

  const generateDocIdsToGetAllDocs = async (pageList: any) => {
    // 分割出docId列表
    let docIds: string[] = []
    if (pageList && pageList.length > 0) {
      docIds = pageList.reduce((cur: string[], acc: any) => {
        if (cur.includes(acc.group) || acc.group.length <= 6) return cur
        cur.push(acc.group)
        return cur
      }, [])
    }
    // 获取所有doc信息
    return await getAllDocsInfo(docIds)
  }

  const generateGroupList = (pageInfos: whiteBoardPageInfo[], docsInfo: any[]) => {
    const list = []
    for (let i in pageInfos) {
      if (list.length === 0 || list[list.length - 1].group !== pageInfos[i].group) {
        // 列表为空 或 最后一项的group是否还是同一个
        list.push({
          groupName:
            pageInfos[i].group.length > 6
              ? docsInfo.find((item: any) => item.docId === pageInfos[i].group).docName
              : 'wb',
          group: pageInfos[i].group,
          startPage: pageInfos[i].whiteboardPage,
          expand: true,
          groupSize: 1,
        })
      } else {
        // 计算出group包含的图片数量，渲染用
        list[list.length - 1].groupSize += 1
      }
    }
    return list
  }

  const generatePreviewList = (pageInfos: whiteBoardPageInfo[], docsInfo: any[]) => {
    const list = []
    for (let i = 0; i < pageInfos.length; i++) {
      // 当lenth>6时一定是docId
      if (pageInfos[i].group.length <= 6)
        list.push('https://img.alicdn.com/imgextra/i1/O1CN017mbF0i1fJB2tuCb8J_!!6000000003985-2-tps-933-694.png')
      else {
        const docInfo = docsInfo.find((item: any) => pageInfos[i].group === item.docId)
        list.push(docInfo.urlList[pageInfos[i].groupPage - 1])
      }
    }
    return list
  }

  const confirmDeleteWhiteBoard = (e: any, delPage: number) => {
    e.stopPropagation()
    confirm({
      title: '您确定要删除当前页面吗?',
      icon: <ExclamationCircleOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk() {
        if (maxPage.current === 1) {
          message.info('当前为最后一页, 无法删除')
          return
        }
        window.wbService
          .deleteWhiteboardPage(delPage)
          .then(() => {
            let pageList: whiteBoardPageInfo[]
            window.wbService
              .getWhiteboardPageInfo()
              .then((d: any) => {
                pageList = d.pageList
                return Promise.all([
                  generateDocIdsToGetAllDocs(d.pageList),
                  generateDocIdsToGetAllDocs(d.intraPageResourceList),
                ])
              })
              .then((res: any) => {
                aliyunBoardRef.current.removeSceneByIndex(delPage - 1)
                if (maxPage.current === page) {
                  setPage(page - 1)
                  setInputPage(inputPage - 1)
                }
                maxPage.current -= 1
                const [docsInfo, intraDocsInfo] = res
                setAllIntraDocsInfo(intraDocsInfo)
                allDocsInfo.current = docsInfo || []
                setGroupList(generateGroupList(pageList, docsInfo))
                setPreviewList(generatePreviewList(pageList, docsInfo))
                message.info('当前页面成功删除')
              })
          })
          .catch((err: any) => {
            message.error('当前页面删除失败请稍后重试')
            console.log(err)
          })
      },
      onCancel() {},
    })
  }

  const confirmDeleteWholeFile = (e: any, group: any) => {
    e.stopPropagation()
    confirm({
      title: `您确定要删除课件${group.groupName}吗?`,
      icon: <ExclamationCircleOutlined />,
      okText: '确定',
      cancelText: '取消',
      async onOk() {
        if (groupList.length === 1) {
          aliyunBoardRef.current.gotoScene(group.groupSize - 1)
          aliyunBoardRef.current.addScene()
          await window.wbService.reportWhiteboardPageOperate('add', 'wb', group.groupSize, 1, 'page')
          maxPage.current += 1
          setPage(1)
          setInputPage(1)
        }
        window.wbService
          .deleteWhiteboardDoc(group.group)
          .then(() => {
            let pageList: whiteBoardPageInfo[]
            window.wbService
              .getWhiteboardPageInfo()
              .then((d: any) => {
                pageList = d.pageList
                return Promise.all([
                  generateDocIdsToGetAllDocs(d.pageList),
                  generateDocIdsToGetAllDocs(d.intraPageResourceList),
                ])
              })
              .then((res: any) => {
                for (let i = 0; i < group.groupSize; i++) {
                  aliyunBoardRef.current.removeSceneByIndex(group.startPage - 1)
                }
                maxPage.current -= group.groupSize
                const [docsInfo, intraDocsInfo] = res
                setAllIntraDocsInfo(intraDocsInfo)
                allDocsInfo.current = docsInfo || []
                setGroupList(generateGroupList(pageList, docsInfo))
                setPreviewList(generatePreviewList(pageList, docsInfo))
                message.info(`课件${group.groupName}成功删除`)
                fileList.current.map((value, index) => {
                  if (value === group.groupName) {
                    fileList.current.splice(index, 1)
                  }
                })
              })
          })
          .catch((err: any) => {
            message.error('当前页面删除失败请稍后重试')
            console.log(err)
          })
      },
      onCancel() {},
    })
  }

  const closeShowProgess = () => {
    const updateFile = [...uploadFileList]
    updateFile[0].showProgess = false
    setUploadFileList([...updateFile])
  }

  const setWbTransformScale = (scale?: number) => {
    const dom = document.querySelector('.aliyunboard-canvas-inner')
    if (!dom) return
    if (!scale) {
      // 如果不传scale，则根据白板宽度计算scale
      const boardWidth = dom.clientWidth
      ;(dom as HTMLDivElement).style.transform = `scale(${300 / boardWidth})`
    } else {
      ;(dom as HTMLDivElement).style.transform = `scale(${scale})`
    }
  }

  const leftToolRender = (tools: whiteBoardTool[][]) => {
    return (
      <div className={`${styles['left-toolbar']} ${styles['toolbar']}`}>
        {tools.map((group, gIndex) => (
          <div className={styles['tool-group']} key={gIndex}>
            {group.map((item, index) => {
              let dom
              switch (item.type) {
                case 'icon':
                  dom = (
                    <Popover content={popoverContent(item.label)} placement="right" trigger="hover" key={index}>
                      <div
                        className={`${styles.tool} ${item.label === currentTool ? styles['tool-active'] : ''}`}
                        onClick={() => {
                          clearAllSettingState()
                          item.callback && item.callback()
                        }}
                      >
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref={item.icon}></use>
                        </svg>
                      </div>
                    </Popover>
                  )
                  break
                case 'withsetting':
                  dom = (
                    <div key={index}>
                      <div
                        className={`${styles.tool} ${item.label === currentTool ? styles['tool-active'] : ''}`}
                        ref={item.ref}
                        onClick={() => {
                          clearAllSettingState()
                          item.callback && item.callback()
                        }}
                        // onMouseEnter={() => {
                        //   item.showing && item.showing(true)
                        // }}
                        // onMouseLeave={() => {
                        //   item.showing && item.showing(false)
                        // }}
                      >
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref={item.icon}></use>
                        </svg>
                        <div className={styles.subscript}></div>
                        <div className={styles.connect}></div>
                        {item.state && item.showSetting && item.showSetting(item)}
                      </div>
                    </div>
                  )
                  break
                case 'upload':
                  dom = (
                    <Popover content={popoverContent(item.label)} placement="right" trigger="hover" key={index}>
                      <div className={`${styles.tool} ${item.label === currentTool ? styles['tool-active'] : ''}`}>
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref={item.icon}></use>
                        </svg>
                        <input
                          type="file"
                          className={styles['tool-inputfile']}
                          onChange={item.handler}
                          title=""
                          accept={item.typeList || ''}
                          ref={inputRef}
                        />
                      </div>
                    </Popover>
                  )
                  break
                default:
                  dom = (
                    <Popover content={popoverContent(item.label)} placement="right" trigger="hover" key={index}>
                      <div key={index}></div>
                    </Popover>
                  )
              }
              return dom
            })}
          </div>
        ))}
      </div>
    )
  }

  const bottomToolRender = (tools: whiteBoardTool[][]) => {
    return (
      <div className={`${styles['bottom-toolbar']} ${styles['toolbar']}`}>
        {tools.map((group, groupIndex) => (
          <div className={styles['tool-group']} key={groupIndex}>
            {group.map((item, index) => {
              let dom
              switch (item.type) {
                case 'icon':
                  dom = (
                    <div
                      className={`${styles.tool} ${item.disable ? styles['tool-disable'] : ''}`}
                      key={index}
                      title={item.label}
                      onClick={() => item.callback && item.callback()}
                    >
                      <svg className="icon" aria-hidden="true">
                        <use xlinkHref={item.icon}></use>
                      </svg>
                    </div>
                  )
                  break
                case 'page':
                  dom = (
                    <div className={`${styles.tool} ${styles['page-tool']}`} key={index}>
                      <input
                        type="text"
                        value={inputPage}
                        onChange={pageInputHandler}
                        onBlur={changPageHandler}
                        onKeyUp={(e) => {
                          e.key === 'Enter' && changPageHandler()
                        }}
                      />{' '}
                      / {maxPage.current}
                    </div>
                  )
                  break
                default:
                  dom = <div key={index}></div>
              }
              return (
                <Popover content={popoverContent(item.label)} placement="top" trigger="hover" key={index}>
                  {dom}
                </Popover>
              )
            })}
          </div>
        ))}
      </div>
    )
  }

  useClickAway(() => {
    showpenSetting && setShowpenSetting(false)
  }, [scrollAxis, toolPenRef])

  useClickAway(() => {
    showtextSetting && setShowtextSetting(false)
  }, [scrollAxis, toolTextRef])

  useClickAway(() => {
    showlaserPenSetting && setShowlaserPenSetting(false)
  }, [scrollAxis, toolLaserRef])

  useClickAway(() => {
    showcolorSetting && setShowcolorSetting(false)
  }, [scrollAxis, toolColorRef])

  useEffect(() => {
    emitter.on('insertPPT', insertPPTHandler)
    emitter.on('convertDone', convertDoneHandler)
    window.docService.on(EventNameEnum.DocUploadProcess, progressHandler)
    let pageList: any
    window.wbService
      .getWhiteboardPageInfo()
      .then((d: any) => {
        pageList = d.pageList
        return Promise.all([
          generateDocIdsToGetAllDocs(d.pageList),
          generateDocIdsToGetAllDocs(d.intraPageResourceList),
        ])
      })
      .then((res: any) => {
        const [docsInfo, intraDocsInfo] = res
        setAllIntraDocsInfo(intraDocsInfo)
        allDocsInfo.current = docsInfo || []
        const initGroupList = generateGroupList(pageList, docsInfo)
        setGroupList(initGroupList)
        setPreviewList(generatePreviewList(pageList, docsInfo))
        initGroupList.forEach((item) => {
          fileList.current.push(item.groupName)
        })
        aliyunBoardRef.current = new AliyunBoard(aliyunBoardConfig)
        window.aliyunBoard = aliyunBoardRef.current
        aliyunBoardRef.current.on('ALIYUNBOARD_READY', aliyunBoardReadyHandler)
        aliyunBoardRef.current.on('ALIYUNBOARD_FIT', aliyunBoardFitHandler)
        // 初始化数据结束后再渲染白板ui
        setLoaded(true)
      })
    return () => {
      emitter.remove('insertPPT', insertPPTHandler)
      emitter.remove('convertDone', convertDoneHandler)
    }
  }, [])

  useEffect(() => {
    if (loaded) {
      if (status.viewMode === 'whiteBoard') {
        setWbTransformScale(1)
      } else {
        setWbTransformScale()
      }
    }
  }, [status.viewMode])

  useEffect(() => {
    if (loaded && room.isOwner) {
      if (boardType === 'pure') {
        aliyunBoardRef.current.setReadOnly(true)
      } else {
        aliyunBoardRef.current.setReadOnly(false)
      }
    }
  }, [boardType])

  return (
    <div
      className={`${styles['white-board-container']} ${
        linking && status.viewMode === 'whiteBoard' ? styles.linking : ''
      }`}
    >
      {isLoading ? (
        <div className={styles['white-board-loading']}>
          <div className={styles['white-board-loading-text']}>文档载入中...</div>
        </div>
      ) : (
        ''
      )}
      <div className={styles['white-board']} ref={whiteBoardWrapperRef}>
        {loaded ? <Canvas model={aliyunBoardRef.current} style={{ height: '100%', width: '100%' }} /> : ''}
      </div>
      {boardType === 'full' && room.isOwner && (
        <div className={`${styles['sidebar']} ${!slidebarExpand ? styles['hide-slide'] : ''}`}>
          <div className={styles['expander']} onClick={() => setSlidebarExpand(!slidebarExpand)}>
            {slidebarExpand ? (
              <svg aria-hidden="true" className="icon">
                <use xlinkHref="#icon-xiangyoufanyesvg"></use>
              </svg>
            ) : (
              <svg aria-hidden="true" className="icon">
                <use xlinkHref="#icon-xiangzuofanye"></use>
              </svg>
            )}
          </div>
          <div className={styles['group-container']}>
            {groupList.map((group: any, gIndex) => {
              return (
                <div className={styles['thumbnail-group']} key={gIndex}>
                  <div
                    className={styles['thumbnail-group-title']}
                    onClick={() => setGroupExpand(gIndex)}
                    onMouseEnter={() => setShowDelButton(gIndex)}
                    onMouseLeave={() => setShowDelButton(-1)}
                  >
                    {showDelButton === gIndex ? (
                      group.groupName === 'wb' ? (
                        ''
                      ) : (
                        <svg
                          className={`${styles['thumbnail-title-del']} icon`}
                          aria-hidden="true"
                          onClick={(e) => {
                            confirmDeleteWholeFile(e, group)
                          }}
                        >
                          <use xlinkHref="#icon-shanchu1"></use>
                        </svg>
                      )
                    ) : (
                      ''
                    )}
                    <div className={styles['thumbnail-group-title-text']}>
                      {group.groupName === 'wb' ? '白板' : group.groupName}
                    </div>
                    <svg className={`${group.expand ? styles['expand'] : styles['fold']} icon`} aria-hidden="true">
                      <use xlinkHref="#icon-ic_toolbar_arrow"></use>
                    </svg>
                  </div>
                  {group.expand && (
                    <div className={styles['thumbnail-container']}>
                      {new Array(group.groupSize).fill('1').map((item, index) => (
                        <div
                          className={`${styles['thumbnail']} ${
                            page === group.startPage + index ? styles['thumbnail-active'] : ''
                          }`}
                          key={index}
                          onClick={() => clickPreviewHandler(group.startPage + index)}
                        >
                          <div
                            className={`${styles['thumbnail-bar']} ${
                              page === group.startPage + index ? styles['thumbnail-bar-active'] : ''
                            }`}
                          >
                            <div className={styles['thumbnail-num']}>{addZero(index + 1)}</div>
                            {page === group.startPage + index ? (
                              <div
                                className={styles['thumbnail-del']}
                                onClick={(e) => {
                                  confirmDeleteWhiteBoard(e, group.startPage + index)
                                }}
                              >
                                <svg className="icon" aria-hidden="true">
                                  <use xlinkHref="#icon-shanchu1"></use>
                                </svg>
                              </div>
                            ) : (
                              ''
                            )}
                          </div>
                          <img src={previewList[group.startPage + index - 1]} />
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </div>
      )}
      {boardType === 'full' && room.isOwner && leftToolRender(leftTools)}
      {boardType === 'full' &&
        !room.isRecorder &&
        (room.isOwner ? bottomToolRender(bottomTools) : bottomToolRender(studentBottomTools))}
      <ShowUpload uploadlist={uploadFileList} closeShowProgess={closeShowProgess} />
    </div>
  )
}

export default connect(({ room, status }: { room: RoomModelState; status: StatusModelState }) => ({
  room,
  status,
}))(WhiteBoard)
