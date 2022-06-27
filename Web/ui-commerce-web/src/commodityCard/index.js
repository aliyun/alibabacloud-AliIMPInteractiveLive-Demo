import React from 'react'
import "./commodityCard.less"
class CommodityCard extends React.Component {

    render() {
        let { goodImg } = this.props
        console.log(goodImg, '===goodImg===');
        return (
            <a target="_blank" href='https://detail.tmall.com/item.htm?id=618015229132&ali_refid=a3_430673_1006:1235070200:N:emtiAWsF8+zhhxaiwzc0Aw==:2f95831c03d6b91a126e2580213d8328&ali_trackid=1_2f95831c03d6b91a126e2580213d8328&spm=a2e0b.20350158.31919782.2' className="commodity-info">
                <img alt="" src={goodImg.goods_image_url}></img>
            </a>
        )
    }
}
export default CommodityCard