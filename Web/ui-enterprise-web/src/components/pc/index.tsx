import { useState } from 'react';
import styles from './pc.less';
import { BasicMap, formatDate } from '@/utils';
import { Input, Button, Modal, message } from 'antd';
import { useLatest } from 'ahooks';

const { TextArea } = Input;

interface IProps {
  state: BasicMap<any>;
  chatMainEl: any;
  likeCount: number;
  isLiving: boolean;
  isPlayback: boolean;
  isStopped: boolean;
  isMuteAll: boolean;
  isMuteSelf: boolean;
  sendComment: (e: any) => Promise<void>;
  messageValue: string;
  setMessageValue: (val: string) => void;
  refreshPlayer: () => void;
  likeClickHandler: () => void;
  animeContainerEl: any;
  preStartTime: number;
  isNotStart: boolean;
  isOnError: boolean;
}

export default function pc(props: IProps) {
  const [showScheduleModel, setShowScheduleModel] = useState(false);
  const [mobile, setMobile] = useState('');
  const [isScheduled, setIsScheduled] = useState(false);
  const mobileRef = useLatest(mobile);
  const mobileOnChange = (e: any) => {
    setMobile(e.target.value.replace(/[^\d]/g, ''));
  };
  const confirmSchedule = () => {
    const reg = /^1[3-9]\d{9}$/;
    if (!reg.test(mobileRef.current)) {
      message.error('请输入正确手机号');
      return;
    }
    message.success('预约成功');
    setIsScheduled(true);
    setShowScheduleModel(false);
  };
  return (
    <div className={styles['room-page']}>
      <div className={styles.container}>
        <div className={styles.player_container}>
          <div className={styles.player_header}>
            <div className={styles.live_info}>
              <div className={styles.avatar}>
                <img
                  src={
                    props.state.roomDetail.extension &&
                    props.state.roomDetail.extension.anchorAvatarURL
                      ? props.state.roomDetail.extension.anchorAvatarURL
                      : '//img.alicdn.com/imgextra/i1/O1CN01CQ4ImC1uxDtOFDJPQ_!!6000000006103-2-tps-112-112.png'
                  }
                />
              </div>
              <div className={styles.info_main}>
                <div className={styles.title}>
                  {props.state.roomDetail.title || '直播间'}
                </div>
                <div className={styles.data}>
                  <div className={styles.data_item}>
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref="#icon-ic_header_guankan"></use>
                    </svg>
                    {props.state.roomDetail.pv || 0}
                  </div>
                  <div className={styles.data_item}>
                    <svg
                      className="icon"
                      aria-hidden="true"
                      style={{ fontSize: '20px' }}
                    >
                      <use xlinkHref="#icon-ic_header_dianzan"></use>
                    </svg>
                    {props.likeCount}
                  </div>
                  <div
                    className={`${styles.data_item} share`}
                    style={{ cursor: 'pointer' }}
                    data-clipboard-text={window.location.href}
                  >
                    <svg
                      className="icon"
                      aria-hidden="true"
                      style={{ fontSize: '20px' }}
                    >
                      <use xlinkHref="#icon-ic_header_fenxiang"></use>
                    </svg>
                    分享
                  </div>
                </div>
              </div>
            </div>
            {/* <div className={styles.player_header_custom}>
              <span>自定义拓展区域</span>
            </div> */}
          </div>
          <div className={styles.player_main}>
            {!props.isLiving && !props.isStopped && !props.isOnError && (
              <div className={styles.nolive}>
                {props.isNotStart && (
                  <div className={styles.schedule_container}>
                    <h1>直播未开始</h1>
                    {props.preStartTime > 0 && (
                      <p>
                        将于
                        {formatDate(
                          new Date(props.preStartTime),
                          'yyyy-MM-dd hh:mm',
                        )}
                        开播
                      </p>
                    )}
                    {isScheduled ? (
                      <Button
                        style={{
                          background: '#cbcbcb',
                          borderColor: '#cbcbcb',
                          color: '#fff',
                        }}
                        size="large"
                      >
                        已经预约
                      </Button>
                    ) : (
                      <Button
                        style={{
                          background: 'rgba(0,188,212,1)',
                          borderColor: 'rgba(0,188,212,1)',
                          color: '#fff',
                        }}
                        size="large"
                        onClick={() => setShowScheduleModel(true)}
                      >
                        立即预约
                      </Button>
                    )}
                  </div>
                )}
              </div>
            )}
            {props.isOnError && (
              <div className={styles.refresh}>
                <Button
                  style={{
                    background: 'rgba(255,68,44,1)',
                    borderColor: 'rgba(255,68,44,1)',
                    color: '#fff',
                    borderRadius: '20px',
                  }}
                  size="middle"
                  onClick={props.refreshPlayer}
                >
                  刷新
                </Button>
              </div>
            )}
            {props.isStopped && (
              <div className={styles.finished}>
                <p>直播已结束</p>
              </div>
            )}
            <div
              id="player"
              className={`prism-player ${
                !props.isLiving && !props.isStopped ? 'hidden' : ''
              } ${styles.player}`}
            ></div>
          </div>
          <div className={styles.player_footer}>
            {/* <div className={styles.player_footer_custom}>
              <span>自定义拓展区域</span>
            </div> */}
          </div>
        </div>
        <div className={styles.chat_container}>
          {/* <div className={styles.chat_header}>
            <div className={styles.chat_header_custom}>
              <span>自定义拓展区域</span>
            </div>
          </div> */}
          <div className={styles.chat_main} ref={props.chatMainEl}>
            <div
              className={`${styles['chat-item']} ${styles['chat-item-notice']}`}
            >
              欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。
            </div>
            {props.state.messageArray.map((data: any, index: number) => (
              <div
                className={`${styles['chat-item']} ${
                  data.nickname ? '' : styles['chat-item-notice']
                }`}
                key={index}
              >
                <span className={styles.emphasize}>
                  {data.nickname ? data.nickname + '：' : ''}
                </span>
                <span dangerouslySetInnerHTML={{ __html: data.content }}></span>
              </div>
            ))}
          </div>
          <div className={styles.chat_footer}>
            <TextArea
              className={styles.chat_textarea}
              autoSize={true}
              placeholder={
                !props.isMuteAll && !props.isMuteSelf
                  ? '和主播说点什么...'
                  : `${
                      props.isMuteAll
                        ? '主播已开启全员禁言...'
                        : '您已被禁言...'
                    }`
              }
              disabled={props.isMuteAll || props.isMuteSelf}
              onKeyDown={props.sendComment}
              value={props.messageValue}
              onChange={(e) => props.setMessageValue(e.target.value)}
            ></TextArea>
            <div className={styles.like_btn} onClick={props.likeClickHandler}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_dialog_dianzan1"></use>
              </svg>
              <div
                className={styles['like-anime-container']}
                ref={props.animeContainerEl}
              ></div>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.liveinfo_container}>
        <div className={styles.liveinfo_title}>直播信息</div>
        <div className={styles.liveinfo_detail}>
          <img
            src="https://img.alicdn.com/imgextra/i4/O1CN010sO3ud1ftollFOsuW_!!6000000004065-2-tps-1391-1180.png"
            alt="info"
          />
        </div>
      </div>
      <Modal
        title="立即预约"
        visible={showScheduleModel}
        onOk={confirmSchedule}
        onCancel={() => setShowScheduleModel(false)}
        className={`${styles.modal} pcmodal`}
        okText="提交"
        cancelText="取消"
      >
        <input
          type="text"
          value={mobile}
          onChange={mobileOnChange}
          placeholder="请填写手机号"
        />
        <p>开播前10分钟通过短信提醒</p>
      </Modal>
    </div>
  );
}
