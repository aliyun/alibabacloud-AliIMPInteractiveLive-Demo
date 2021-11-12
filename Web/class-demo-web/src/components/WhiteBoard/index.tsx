import { FC, useEffect, useState, useRef, useCallback } from 'react'
import { RoomModelState, StatusModelState, connect, Dispatch } from 'umi'
import { addZero, BasicMap } from '@/utils'
import { message, Popover } from 'antd'
import { usePersistFn, useMount } from 'ahooks'
import Emitter from '@/utils/emitter'
import styles from './index.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  dispatch: Dispatch
  boardType: 'pure' | 'full'
}

interface whiteBoardTool {
  label: string
  type: string
  icon?: string
  callback: () => any
  disable?: boolean
}

interface whiteBoardPageInfo {
  whiteboardPage: number
  groupPage: number
  group: string
}

const { Canvas, AliyunBoard } = window.aliyunBoardSDK

const WhiteBoard: FC<PageProps> = ({ room, status, dispatch, boardType }) => {
  const [page, setPage] = useState(1)
  const [inputPage, setInputPage] = useState(1)
  const [maxPage, setMaxPage] = useState(1)
  const [scale, setScale] = useState(1)
  const [currentTool, setCurrentTool] = useState('画笔')
  const [previewList, setPreviewList] = useState<string[]>([])
  const [groupList, setGroupList] = useState<any[]>([])
  const [showSidebar, setShowSidebar] = useState(boardType === 'full') // 是否显示sidebar，负责切换视图时直接隐藏
  const [slidebarExpand, setSlidebarExpand] = useState(true) // 是否将sidebar展开，负责白板视图时收起与放开

  const getDocumentData = async () => {
    return (await window.wbService.getWbToken(room.docKey)).documentAccessInfo
  }

  const aliyunBoardConfig = {
    getDocumentData,
    docKey: room.docKey,
    syncModel: 1,
    maxSceneCount: 100000,
    minScale: 0.1,
    maxScale: 3,
    fitMode: 1,
    forceSync: true,
    pointerMultiSelect: false,
    enableGasture: true,
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

  const aliyunBoardRef = useRef(new AliyunBoard(aliyunBoardConfig))

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
      type: 'icon',
      icon: '#icon-huabi',
      callback: () => {
        setCurrentTool('画笔')
        aliyunBoardRef.current.setToolType('pen')
      },
    },
    {
      label: '文本',
      type: 'icon',
      icon: '#icon-wenben1',
      callback: () => {
        setCurrentTool('文本')
        aliyunBoardRef.current.setToolType('text')
      },
    },
    {
      label: '激光笔',
      type: 'icon',
      icon: '#icon-jiguangbi',
      callback: () => {
        setCurrentTool('激光笔')
        aliyunBoardRef.current.setToolType('laserPen')
      },
    },
    {
      label: '箭头',
      type: 'icon',
      icon: '#icon-jiantou',
      callback: () => {
        setCurrentTool('箭头')
        aliyunBoardRef.current.setToolType('straight')
      },
    },
    {
      label: '颜料桶',
      type: 'icon',
      icon: '#icon-yanse',
      callback: () => message.info('暂不支持'),
    },
  ]

  const fileTools: whiteBoardTool[] = [
    {
      label: '插入图片',
      type: 'icon',
      icon: '#icon-tupian',
      callback: () => message.info('暂不支持'),
    },
    {
      label: '上传PPT/课件',
      type: 'icon',
      icon: '#icon-shangchuan1',
      callback: () => {
        dispatch({
          type: 'status/setShowPPTUploader',
          payload: !status.showPPTUploader,
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
    {
      label: '全屏',
      type: 'icon',
      icon: '#icon-kuangxuan2',
      disable: true,
      callback: () => message.info('暂不支持'),
    },
  ]

  const scaleTools: whiteBoardTool[] = [
    {
      label: '放大',
      type: 'icon',
      icon: '#icon-fangda1',
      disable: scale >= aliyunBoardConfig.maxScale - 0.1, // 白板的bug，实际上将要达到maxScale的时候就报错了
      callback: () => {
        if (scale >= aliyunBoardConfig.maxScale - 0.1) return
        doSetScale(scale + 0.1)
      },
    },
    {
      label: '缩小',
      type: 'icon',
      icon: '#icon-suoxiao2',
      disable: scale <= aliyunBoardConfig.minScale,
      callback: () => {
        if (scale <= aliyunBoardConfig.minScale) return
        doSetScale(scale - 0.1)
      },
    },
    {
      label: '截屏',
      type: 'icon',
      icon: '#icon-jieping',
      disable: true,
      callback: () => message.info('暂不支持'),
    },
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
      disable: page === maxPage,
      callback: () => {
        if (page === maxPage) return
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
        setTimeout(
          ((prePage) => {
            return () => {
              Promise.all([
                aliyunBoardRef.current.getPreviewData(prePage - 1),
                aliyunBoardRef.current.getPreviewData(prePage),
              ]).then((imgList: HTMLImageElement[]) => {
                console.log(imgList)
                const list = previewList.slice(0)
                list.splice(prePage - 1, 0, imgList[0].src)
                list[prePage] = imgList[1].src
                setPreviewList(list)
              })
            }
          })(page),
          500,
        )
        // 上报页码映射
        window.wbService.reportWhiteboardPageOperate('add', 'wb', page, 1).then((d: any) => {
          setGroupList(generateGroupList(d.pageList))
        })
        setMaxPage(maxPage + 1)
        setPage(page + 1)
        setInputPage(Number(inputPage) + 1)
      },
    },
  ]

  const leftTools = [paintTools, fileTools, clearTools]
  const bottomTools = [zTools1, zTools2, scaleTools, pageTools, fileTools2]

  const pageInputHandler = (e: any) => {
    if (e.target.value === '' || /^\d+$/.test(e.target.value)) setInputPage(e.target.value)
  }

  const changPageHandler = () => {
    if (inputPage === page) return
    if (inputPage <= 0 || !inputPage || inputPage > maxPage) {
      message.error('页码输入有误')
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
    setMaxPage(aliyunBoardRef.current.getScenesCount())
    setPage(aliyunBoardRef.current.getCurrentSceneIndex() + 1)
    setInputPage(aliyunBoardRef.current.getCurrentSceneIndex() + 1)
    aliyunBoardRef.current.getPreviewData().then((previewList: HTMLImageElement[]) => {
      setPreviewList(previewList.map((item: HTMLImageElement) => item.src))
    })
    if (boardType === 'pure') {
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
    // 需要在切换页面之后重新获取图片
    let item = await aliyunBoardRef.current.getPreviewData(page - 1)
    setPreviewList(
      previewList.map((preview, index) => {
        return index === page - 1 ? item.src : preview
      }),
    )
  }

  const doSetScale = (num: number) => {
    aliyunBoardRef.current.setScale(num)
    setScale(num)
  }

  const aliyunBoardFitHandler = usePersistFn(() => {
    doSetScale(1)
  })

  const insertPPTHandler = usePersistFn(async () => {
    try {
      await doGotoScene(maxPage)
      const docInfo = await window.wbService.getDoc(status.currentDocId)
      const { urlList, docName } = docInfo
      const index = maxPage
      let pptPreviewList = previewList.slice(0)
      for (let i in urlList) {
        await aliyunBoardRef.current.addScene()
        await aliyunBoardRef.current.addBackgroundImage(urlList[i])
        pptPreviewList.push(urlList[i])
      }
      aliyunBoardRef.current.gotoScene(index)
      setPage(index + 1)
      setMaxPage(maxPage + urlList.length)
      setPreviewList(pptPreviewList)
      // 上报页码映射
      window.wbService.reportWhiteboardPageOperate('add', docName, index, urlList.length).then((d: any) => {
        setGroupList(generateGroupList(d.pageList))
      })
    } catch (err) {
      console.error(err)
    }
  })

  const generateGroupList = (pageList: whiteBoardPageInfo[]) => {
    const list = []
    for (let i in pageList) {
      if (list.length === 0 || list[list.length - 1].groupName !== pageList[i].group) {
        list.push({
          groupName: pageList[i].group,
          startPage: pageList[i].whiteboardPage,
          expand: true,
          groupSize: 1,
        })
      } else {
        list[list.length - 1].groupSize += 1
      }
    }
    return list
  }

  const setGroupExpand = (index: number) => {
    const newGroupList = groupList.slice(0)
    newGroupList[index].expand = !newGroupList[index].expand
    setGroupList(newGroupList)
  }

  const popoverContent = (content: string) => <span>{content}</span>

  useMount(() => {
    emitter.on('insertPPT', insertPPTHandler)
    window.wbService.getWhiteboardPageInfo().then((d: any) => {
      setGroupList(generateGroupList(d.pageList))
    })
  })

  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      doSetScale(1)
    } else {
      doSetScale(0.3)
    }
  }, [status.viewMode])

  useEffect(() => {
    if (boardType === 'pure') {
      aliyunBoardRef.current.setReadOnly(true)
      setShowSidebar(false)
    } else {
      aliyunBoardRef.current.setReadOnly(false)
      setShowSidebar(true)
    }
  }, [boardType])

  aliyunBoardRef.current.on('ALIYUNBOARD_READY', aliyunBoardReadyHandler)
  aliyunBoardRef.current.on('ALIYUNBOARD_FIT', aliyunBoardFitHandler)

  return (
    <div className={styles['white-board-container']}>
      <div className={styles['white-board']}>
        <Canvas model={aliyunBoardRef.current} style={{ height: '100%', width: '100%' }} />
      </div>
      {showSidebar && (
        <div className={`${styles['side-bar']} ${!slidebarExpand ? styles['hide-slide'] : ''}`}>
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
          {groupList.map((group: any, gIndex) => (
            <div className={styles['thumbnail-group']} key={gIndex}>
              <div className={styles['thumbnail-group-title']} onClick={() => setGroupExpand(gIndex)}>
                {group.groupName === 'wb' ? '白板' : group.groupName}
                <svg className={`${group.expand ? styles['expand'] : ''} icon`} aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_arrow_noc_copy"></use>
                </svg>
              </div>
              {group.expand && (
                <div className={styles['thumbnail-container']}>
                  {new Array(group.groupSize).fill('1').map((item, index) => (
                    <div
                      className={`${styles['thumbnail']} ${
                        page === group.startPage + index ? styles['thumbnail-active'] : ''
                      }`}
                      data-index={addZero(index + 1)}
                      key={index}
                      onClick={() => clickPreviewHandler(group.startPage + index)}
                    >
                      <img src={previewList[group.startPage + index - 1]} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
      {boardType === 'full' && (
        <div className={`${styles['left-toolbar']} ${styles['toolbar']}`}>
          {leftTools.map((group, gIndex) => (
            <div className={styles['tool-group']} key={gIndex}>
              {group.map((item, index) => (
                <Popover content={popoverContent(item.label)} placement="right" trigger="hover" key={index}>
                  <div
                    className={`${styles.tool} ${item.label === currentTool ? styles['tool-active'] : ''}`}
                    onClick={() => item.callback()}
                  >
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                  </div>
                </Popover>
              ))}
            </div>
          ))}
        </div>
      )}
      {boardType === 'full' && (
        <div className={`${styles['bottom-toolbar']} ${styles['toolbar']}`}>
          {bottomTools.map((group, groupIndex) => (
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
                        onClick={() => item.callback()}
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
                        / {maxPage}
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
      )}
    </div>
  )
}

export default connect(({ room, status }: { room: RoomModelState; status: StatusModelState }) => ({
  room,
  status,
}))(WhiteBoard)
