import { FC } from 'react'
import { message } from 'antd'

import styles from './index.less'

interface WhiteBoardProps {
  uploadlist: any
  closeShowProgess: Function
}
interface uploadObj {
  fileName: string
  showProgess: boolean
  percent: number
  failure: boolean
  finished: string
}

const ShowUpload: FC<WhiteBoardProps> = ({ uploadlist, closeShowProgess }) => {
  const closeLabel = async (finishedState: string) => {
    switch (finishedState) {
      case 'file-upload-start':
        try {
          await window.docService.abortUpload()
          closeShowProgess()
        } catch (err) {
          console.log(err)
          message.error('取消上传失败')
        }
        break
      case 'file-insert-finish':
        closeShowProgess()
        break
      default:
        break
    }
  }

  return (
    <div className={styles['showupload']}>
      {uploadlist.map((item: uploadObj, index: any) => {
        return item.showProgess ? (
          <div className={styles['showupload-item']} key={index}>
            <div className={styles['showupload-icon']}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_dialog_kejian_grey"></use>
              </svg>
            </div>
            <div className={styles['showupload-state']}>
              <div className={styles['showupload-state-title']}>{item.fileName}</div>
              <div className={styles['showupload-state-speed']}>{!item.failure ? item.percent + '%' : '失败'}</div>
              <div className={styles['showupload-state-reload']}>
                {!item.failure ? (
                  ''
                ) : (
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-ic_kejian_shuaxin"></use>
                  </svg>
                )}
              </div>
            </div>
            <div
              className={styles['showupload-close']}
              onClick={() => {
                closeLabel(item.finished)
              }}
            >
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_kejian_close"></use>
              </svg>
            </div>
          </div>
        ) : (
          ''
        )
      })}
    </div>
  )
}

export default ShowUpload
