import React from 'react'
import './popup.less'
class Popup extends React.Component {
    closeCard() {
        this.props.closeCard(false)
    }
    render() {
        let { dataItem, dataTitle } = this.props
        return <div className="fu-box">
            <div className="fu-title">{dataTitle}</div>
            <div className="fu-content">
                {dataItem.map((item, index) => {
                    return <div className='content-item' key={index}>
                        <div className="fu-img">
                            <img alt="" src={item.img}></img>
                        </div>
                        <span>{item.title}</span>
                    </div>
                })}
            </div>
            <div className="fu-close" onClick={() => { this.closeCard() }}>取消</div>
        </div>
    }
}
export default Popup