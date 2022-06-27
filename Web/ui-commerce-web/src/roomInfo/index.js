import './roomInfo.less';
import { DownOutlined } from '@ant-design/icons';
import { message } from 'antd';
import React from "react"
class RoomInfo extends React.Component {
    state = {
        showButton: true
    }
    subscribeClickHandler() {
        this.setState({
            showButton: false
        });
        message.success({
            duration: 3,
            content: '关注成功',
            className: 'custom-class',
            style: {
                marginTop: '45vh',
            },
        })
    }
    render() {
        let { roomDetail } = this.props
        return (
            <div className="top">
                <div className="room-info-container">
                    <div className="top">
                        <div className="room-info-container">
                            <div className="room-info">
                                <div className="avatar"
                                    style={{
                                        backgroundImage: `url('${'https://img.alicdn.com/imgextra/i4/O1CN01BQZKz41EGtPZp3U5P_!!6000000000325-2-tps-1160-1108.png'
                                            }')`,
                                    }}
                                >
                                </div>
                                <div className="info">
                                    <div className="title">
                                        {
                                            (roomDetail.currentRoomDetail && roomDetail.currentRoomDetail.title.length) > 10 ?
                                                <div className='animation-title'>{roomDetail.currentRoomDetail ? roomDetail.currentRoomDetail.title : roomDetail.title}</div> :
                                                <span>{roomDetail.currentRoomDetail ? roomDetail.currentRoomDetail.title : roomDetail.title}</span>
                                        }
                                    </div>
                                    <div className="data">
                                        {roomDetail.currentRoomDetail ? roomDetail.currentRoomDetail.pv : roomDetail.onlineCount}观看
                                    </div>
                                </div>
                                {
                                    <div className="subscribe" onClick={() => this.subscribeClickHandler()} style={{ display: this.state.showButton ? 'block' : 'none' }}>
                                    </div>
                                }
                            </div>
                            <div className="room-notice">
                                <div className="notice">
                                    <i className="icon"></i>
                                    <span>公告</span>
                                    <i className="expend"><DownOutlined /></i>
                                </div>
                                <div className="rank">
                                    <div className="hot"></div>
                                    <div className="rank-text">这里是榜单名称99+名</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}
export default RoomInfo;