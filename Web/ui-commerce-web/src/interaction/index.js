import React from 'react'
import '../styles/anime.less';
import './interaction.less'
import CommodityCard from '../commodityCard'
import CommodityList from '../commodityList'
import Popup from '../popup'
import { message } from 'antd'
import { createDom, randomNum } from '../utils'
let likeBubbleCount = 0;
class Interaction extends React.Component {
  constructor() {
    super()
    this.animeContainerEl = React.createRef()
  }
  state = {
    showTime: '',
    showList: false,
    userTime: '',
    isPlus: false,
    dialog: false,
    textPending: false,
    keyFlag: false,
    keyTimer: 0,
    userTimes: 0,
    textPendTimer: 0,
    isConcerned: true,
    showShare: false,
    showCard: false,
    messageValue: '',
    shareItem: [{
      img: 'https://img.alicdn.com/imgextra/i3/O1CN01VECQTR24HDmiEVM72_!!6000000007365-2-tps-96-96.png',
      title: '微信好友'
    },
    {
      img: 'https://img.alicdn.com/imgextra/i3/O1CN01ihDHOP1jRCU4cVQhX_!!6000000004544-2-tps-96-96.png',
      title: '朋友圈'
    },
    {
      img: 'https://img.alicdn.com/imgextra/i2/O1CN01moim5d25M5bw602FP_!!6000000007511-2-tps-96-96.png',
      title: '微博'
    },
    {
      img: 'https://img.alicdn.com/imgextra/i4/O1CN01PDCN0B1LVHEFIlKho_!!6000000001304-2-tps-96-96.png',
      title: 'QQ好友'
    },
    ],
    interaction: [{
      img: 'https://img.alicdn.com/imgextra/i3/O1CN01EJHtWo1LJpKNcVBBZ_!!6000000001279-2-tps-96-96.png',
      title: '送礼物'
    },
    {
      img: 'https://img.alicdn.com/imgextra/i2/O1CN01Z0gO9u1k3DMJzu73X_!!6000000004627-2-tps-96-96.png',
      title: '订阅列表'
    },],
  }
  closeList = () => {
    this.setState({
      showList: false
    })
  }
  showList = () => {
    this.setState({
      showList: true
    })
  }
  touchInputHandler() {
  }
  setMessageValue = (e) => {

    if (e.target.value.length > this.props.chartInfo[1].sendCommentMaxLength) {
      message.error(`最多输入${this.props.chartInfo[1].sendCommentMaxLength}字符`)
      return
    }
    this.setState({
      messageValue: e.target.value
    })
  };
  likeClickHandler() {
    const animeRdm = randomNum(this.props.maxAnameCount, 1);
    const bubble = createDom('div', {
      class: `bubble anime-${animeRdm}`,
      id: `bubble-${likeBubbleCount}`,
    });
    let nowCount = likeBubbleCount;
    likeBubbleCount++;
    this.animeContainerEl.current.append(bubble);
    setTimeout(() => {
      this.animeContainerEl.current.removeChild(
        document.querySelector(`#bubble-${nowCount}`),
      );
    }, 1500);
    this.props.setLikeCounts(this.props.likeCount + 1);
    window.chatService.sendLike();
  };
  shareClickHandler() {
    this.setState({
      showShare: true,
      showCard: true
    })
  };
  interactionClickHandler() {
    this.setState({
      showShare: false,
      showCard: true
    })
  };
  async plusOne(e, item) {
    console.log('加1', e, item, this);
    const msg = item.content
    if (!msg) return
    try {
      await window.chatService.sendComment(msg)
      this.state.textPendTimer = setTimeout(() => {
        this.setState({
          textPending: false
        })
      },
        this.props.chartInfo[1].sendCommentInterval)
      const messageItem = {
        nickname: this.props.nickname,
        content: msg,
        level: this.props.userId.length === 1 ? 1 : (this.props.userId.length < 5 ? 2 : 3),
        isMe: true,
        isPlus: true,
        color: '#' + Math.floor(Math.random() * (2 << 23)).toString(16)
      }
      this.props.addMsgItem(messageItem)
    } catch (err) {
      clearTimeout(this.state.textPendTimer)
      this.setState({
        textPending: false
      })
      this.setState({
        messageValue: msg
      })
    } finally {
    }
  };
  concerned() {
    console.log('concerned');
  };
  closeCard(val) {
    this.setState({
      showShare: val,
      showCard: val
    })
  }
  async sendComment() {
    if (!this.state.messageValue) return
    const msg = this.state.messageValue.trim()
    if (!msg) return
    this.setState({
      textPending: true,
      messageValue: ''
    })
    try {
      await window.chatService.sendComment(msg)
      this.state.textPendTimer = setTimeout(() => {
        this.setState({
          textPending: false
        })
      },
        this.props.chartInfo[1].sendCommentInterval)
      let list = this.props.messageArray.map(item => {
        return item.content
      })
      let contentList = []
      for (var i = 0; i < list.length; i++) {
        if (list[i] === list[i + 1] || list[i] === msg) {
          contentList.push(list[i]);
        }
      }
      const messageItem = {
        nickname: this.props.nickname,
        content: msg,
        level: this.props.userId.length === 1 ? 1 : (this.props.userId.length < 5 ? 2 : 3),
        isMe: true,
        isPlus: contentList.includes(msg),
        color: '#' + Math.floor(Math.random() * (2 << 23)).toString(16)
      }
      this.props.addMsgItem(messageItem)
    } catch (err) {
      clearTimeout(this.state.textPendTimer)
      this.setState({
        textPending: false
      })
      this.setState({
        messageValue: msg
      })
    } finally {
    }
  }
  showGoodCard = (e) => {
    clearTimeout(this.state.showTime)
    this.setState({
      showTime: setTimeout(() => {
        this.props.hideGoodCard()
      }, this.props.goodShowTime * 1000)
    })
  }
  keydownHandler = (e) => {
    if (e.keyCode !== 13) return
    e.preventDefault()
    if (this.state.keyFlag) return
    this.setState({
      keyFlag: true
    })
    clearTimeout(this.state.keyTimer)
    this.setState({
      keyTimer: setTimeout(() => {
        this.setState({
          keyFlag: false
        })
      }, 500)
    })
    this.sendComment()
  }
  render() {
    const { messageValue, shareItem, showShare, showCard, interaction, dialog, showList } = this.state
    const { messageArray, userStatus, goodImg, goodShowTime, likeCount } = this.props
    console.log(userStatus, '===userStatus===');
    return (
      <div className="interaction">
        {dialog && (<div className="dialog-box">
          <div className="content-text">开启直播小窗口边看边买吧~</div>
          <div className="btns">
            <div className="close-dialog" onClick={() => { this.setState({ dialog: false }) }}>关闭</div>
            <div className="query-dialog" onClick={() => { this.setState({ dialog: false }) }}>开启</div>
          </div>
        </div>)}
        <div style={{ display: (userStatus.content) ? "block" : "none" }}>
          <div className={userStatus.level === 1 ? 'user-status-one' : (userStatus.level === 2 ? 'user-status-two' : 'user-status-three')}>
            <span className={userStatus.level === 1 ? 'fan-one' : (userStatus.level === 2 ? 'fan-two' : 'fan-three')}></span>
            <span className="emphasize" >{userStatus.nickname}</span>
            <span style={{ fontSize: "0.8rem", lineHeight: '1rem' }}>{userStatus.content}</span>
          </div>
        </div>
        <div className="chat-window">
          {
            messageArray.length > 0 && messageArray.map((item, index) => {
              return <div className="chat-item" key={index}>
                <div className='item-content' >
                  <span className={item.level === 1 ? 'fan-one' : (item.level === 2 ? 'fan-two' : 'fan-three')}></span>
                  <span className="emphasize" style={{ color: item.color }}>{item.nickname}</span>
                  <span style={{ fontSize: "0.8rem", lineHeight: '0.8rem' }}>{item.content}</span>
                </div>
                {
                  item.isPlus && (<div onClick={(e) => { this.plusOne(e, item) }} className="plus-one">+1</div>)
                }
                {
                  item.isConcerned && (<div onClick={() => { this.concerned() }} className="concerned">我也关注</div>)
                }
              </div>
            })
          }
          <div className="chat">
            欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。
          </div>
        </div>
        {
          <div onLoad={this.showGoodCard} style={{ display: (goodImg.goods_image_url) ? "block" : "none" }}><CommodityCard goodImg={goodImg} goodShowTime={goodShowTime}></CommodityCard>
          </div>
        }
        <div className="operations">
          <div className="commodity" onClick={() => { this.showList() }}></div>
          <form
            action=""
            className="chat-input-form"
            onSubmit={(e) => e.preventDefault()}
          >
            <input
              type="text"
              className="chat-input"
              value={messageValue}
              placeholder="说点什么..."
              onKeyDown={this.keydownHandler}
              onChange={this.setMessageValue}
              onTouchStart={() => this.touchInputHandler()}
            />
          </form>
          <div
            className="operation-beauty" onClick={() => this.interactionClickHandler()}>
          </div>
          <div
            className="operation-more" onClick={() => { this.setState({ dialog: true }) }}>
          </div>
          <div
            className="operation-share" onClick={() => this.shareClickHandler()}>
          </div>
          <div
            className="operation-like" onClick={() => this.likeClickHandler()}>
            <div className="count" style={{ display: likeCount > 0 ? 'block' : 'none' }}>{likeCount}</div>
            <div
              className='like-anime-container'
              ref={this.animeContainerEl}
            ></div>
          </div>
        </div>
        {
          showCard && (<Popup dataItem={showShare ? shareItem : interaction} dataTitle={showShare ? '立即分享给好友' : '主播互动'} closeCard={() => { this.closeCard() }}></Popup>)
        }
        {
          showList && <CommodityList closeList={this.closeList}></CommodityList>
        }
      </div>
    )
  }
}
export default Interaction