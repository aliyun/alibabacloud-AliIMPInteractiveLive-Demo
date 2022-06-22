import styles from './mobile.less';
import { BasicMap, formatDate } from '@/utils';
import { useRef, useState } from 'react';
import { Tabs, Dialog, Toast } from 'antd-mobile';
import { ArrowsAltOutlined, ReloadOutlined } from '@ant-design/icons';
import { useLatest } from 'ahooks';

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
  likeClickHandler: () => void;
  refreshPlayer: () => void;
  animeContainerEl: any;
  preStartTime: number;
  isNotStart: boolean;
  isOnError: boolean;
  // only mobile
  noticeClickHandler: () => void;
  showNotice: boolean;
  needUserClick: boolean;
  userStartPlay: () => void;
  chatGoBottom: () => void;
  requestFullScreen: () => void;
  anchorNick: string;
  anchorInfo: {
    anchorAvatarURL: string;
    anchorIntroduction: string;
    liveIntroduction: string;
  };
}

const tabItems = [
  { key: 'chat', title: '互动消息' },
  { key: 'info', title: '直播信息' },
  { key: 'custom1', title: '自定义项1' },
  { key: 'custom2', title: '自定义项2' },
  { key: 'custom3', title: '自定义项3' },
];

export default function Mobile(props: IProps) {
  const [showScheduleDialog, setShowScheduleDialog] = useState(false);
  const [mobile, setMobile] = useState('');
  const [activeIndex, setActiveIndex] = useState(0);
  const [isScheduled, setIsScheduled] = useState(false);
  const mobileRef = useLatest(mobile);
  const confirmSchedule = () => {
    const reg = /^1[3-9]\d{9}$/;
    if (!reg.test(mobileRef.current)) {
      Toast.show({
        content: '请输入正确手机号',
      });
      return;
    }
    Toast.show({
      content: '预约成功',
    });
    setIsScheduled(true);
    setShowScheduleDialog(false);
  };
  const mobileOnChange = (e: any) => {
    setMobile(e.target.value.replace(/[^\d]/g, ''));
  };

  return (
    <div className={styles['room-page']}>
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
              </div>
            )}
          </div>
        )}
        {props.isOnError && (
          <div className={styles.refresh}>
            <ReloadOutlined onClick={props.refreshPlayer} />
            <p>加载失败，请点击刷新</p>
          </div>
        )}
        {props.isStopped && (
          <div className={styles.finished}>
            <p>直播已结束</p>
          </div>
        )}
        <ArrowsAltOutlined
          className={styles.fullscreen_btn}
          onClick={props.requestFullScreen}
        />
        <div className={styles.anchor_info}>
          <div
            className={styles.avatar}
            style={{
              backgroundImage: `url(${props.anchorInfo.anchorAvatarURL})`,
            }}
          ></div>
          <div className={styles.anchorInfo_main}>
            <div className={styles.nick}>{props.anchorNick}</div>
            <div className={styles.view}>{props.state.roomDetail.pv} 观看</div>
          </div>
        </div>
        <div
          id="player"
          className={`prism-player ${
            !props.isLiving && !props.isPlayback ? 'hidden' : ''
          } ${styles.player}`}
        ></div>
      </div>
      <Tabs
        activeKey={tabItems[activeIndex].key}
        onChange={(key) => {
          const index = tabItems.findIndex((item) => item.key === key);
          setActiveIndex(index);
          console.log(key);
          if (key === 'chat') {
            setTimeout(() => {
              props.chatGoBottom();
            }, 10);
          }
        }}
        style={{
          '--title-font-size': '14px',
          '--active-line-color': '#00bdd4',
          '--active-line-height': '3px',
          '--active-title-color': '#000',
          '--fixed-active-line-width': '40px',
          color: '#969799',
        }}
      >
        {tabItems.map((item) => (
          <Tabs.Tab title={item.title} key={item.key} />
        ))}
      </Tabs>
      {tabItems[activeIndex].key === 'chat' && (
        <div className={styles.tab_content}>
          <div className={styles.chat_main} ref={props.chatMainEl}>
            {/* <div
              className={`${styles['chat-item']} ${styles['chat-item-notice']}`}
            >
              欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。
            </div> */}
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
          <div className={styles.tab_footer}>
            <div className={styles.operations}>
              <form
                action=""
                className={styles['chat-input-form']}
                onSubmit={(e: any) => e.preventDefault()}
              >
                <input
                  type="text"
                  className={styles['chat-input']}
                  placeholder={
                    !props.isMuteAll && !props.isMuteSelf
                      ? '说些什么吧...'
                      : `${
                          props.isMuteAll
                            ? '主播已开启全员禁言...'
                            : '您已被禁言...'
                        }`
                  }
                  disabled={
                    props.isMuteAll || props.isMuteSelf || props.isNotStart
                  }
                  onKeyDown={props.sendComment}
                  value={props.messageValue}
                  onChange={(e) => props.setMessageValue(e.target.value)}
                />
              </form>
              <div
                className={`${styles['operation-btn']} share`}
                data-clipboard-text={
                  props.isNotStart ? '' : window.location.href
                }
              >
                {props.isNotStart ? (
                  <img src="https://img.alicdn.com/imgextra/i1/O1CN01lFZDm71Yng9HQy0nJ_!!6000000003104-2-tps-76-76.png" />
                ) : (
                  <img src="https://img.alicdn.com/imgextra/i2/O1CN01xRWdjz1oWlNIGIjoa_!!6000000005233-2-tps-76-76.png" />
                )}
              </div>
              <div
                className={styles['operation-btn']}
                onClick={props.likeClickHandler}
              >
                {props.isNotStart ? (
                  <img src="https://img.alicdn.com/imgextra/i1/O1CN015931o428Ei4sR2Vga_!!6000000007901-2-tps-76-76.png" />
                ) : (
                  <img src="https://img.alicdn.com/imgextra/i3/O1CN01TnIfk01ejuaSNjRi6_!!6000000003908-2-tps-76-76.png" />
                )}
                <div
                  className={styles['like-anime-container']}
                  ref={props.animeContainerEl}
                ></div>
                {props.likeCount > 0 && (
                  <div className={styles.like_count}>{props.likeCount}</div>
                )}
              </div>
              <div className={`${styles['operation-btn']}`}>
                {props.isNotStart ? (
                  <img src="https://img.alicdn.com/imgextra/i4/O1CN01PVnfa8219we2xFaAD_!!6000000006943-2-tps-76-76.png" />
                ) : (
                  <img src="https://img.alicdn.com/imgextra/i4/O1CN01oPOrFl1V8VhXiKjkt_!!6000000002608-2-tps-76-76.png" />
                )}
              </div>
            </div>
          </div>
        </div>
      )}
      {tabItems[activeIndex].key === 'info' && (
        <div className={styles.tab_content}>
          <div
            className={`${styles.info_main} ${
              props.isNotStart ? '' : styles.hide_footer
            }`}
          >
            <div className={styles.tab_info_item}>
              <div className={styles.title}>主播介绍</div>
              <div className={styles.tab_info_main}>
                <div
                  className={styles.avatar}
                  style={{
                    backgroundImage: `url(${props.anchorInfo.anchorAvatarURL})`,
                  }}
                ></div>
                <div className={styles.anchorInfo_main}>
                  <div className={styles.nick}>{props.anchorNick}</div>
                  <div className={styles.intro}>
                    {props.anchorInfo.anchorIntroduction}
                  </div>
                </div>
              </div>
            </div>
            <div className={styles.tab_info_item}>
              <div className={styles.title}>直播简介</div>
              <div className={styles.live_info}>
                {props.anchorInfo.liveIntroduction}
              </div>
            </div>
            <img
              src="https://img.alicdn.com/imgextra/i2/O1CN01go7Cjf1HGNtBB3tva_!!6000000000730-2-tps-686-1200.png"
              alt="info"
            />
          </div>
          {props.isNotStart && (
            <div className={styles.tab_footer}>
              <div className={styles.operations}>
                <div
                  className={`${styles['operation-btn']} share`}
                  data-clipboard-text={window.location.href}
                >
                  <img src="https://img.alicdn.com/imgextra/i2/O1CN01xRWdjz1oWlNIGIjoa_!!6000000005233-2-tps-76-76.png" />
                </div>
                {isScheduled ? (
                  <div
                    className={`${styles.full_btn} ${styles.full_btn_disabled}`}
                  >
                    已经预约
                  </div>
                ) : (
                  <div
                    className={styles.full_btn}
                    onClick={() => setShowScheduleDialog(true)}
                  >
                    立即预约
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
      {tabItems[activeIndex].key === 'custom1' && (
        <div className={styles.tab_content}>自定义项1</div>
      )}
      {tabItems[activeIndex].key === 'custom2' && (
        <div className={styles.tab_content}>自定义项2</div>
      )}
      {tabItems[activeIndex].key === 'custom3' && (
        <div className={styles.tab_content}>自定义项3</div>
      )}
      <div className={styles.safe_zone}></div>
      <Dialog
        visible={showScheduleDialog}
        bodyStyle={{
          width: '75vw',
        }}
        content={
          <>
            <div className={styles.schedule_title}>立即预约</div>
            <input
              type="text"
              className={styles.schedule_input}
              placeholder="请输入手机号"
              value={mobile}
              onChange={mobileOnChange}
            />
            <div className={styles.schedule_subintro}>
              开播前10分钟通过短信提醒
            </div>
          </>
        }
        actions={[
          [
            {
              key: 'cancel',
              text: '取消',
              style: {
                color: '#000',
              },
              onClick: () => setShowScheduleDialog(false),
            },
            {
              key: 'submit',
              text: '提交',
              style: {
                color: '#00BCD4',
              },
              onClick: () => confirmSchedule(),
            },
          ],
        ]}
      />
    </div>
  );
}
