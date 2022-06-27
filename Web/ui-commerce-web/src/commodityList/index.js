import React from 'react'
import "./commodityList.less"
class CommodityList extends React.Component {
    state = {
        showList: false,
        commodityList: [
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子设计感小众学院风显瘦',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            },
            {
                img: 'https://img2.baidu.com/it/u=3314321215,452626843&fm=253&fmt=auto&app=138&f=JPEG?w=350&h=449',
                title: '2022年夏季新款polo领长裙冷淡风女装高级感初恋茶歇法式连衣裙子',
                price: '￥138',
            }
        ]
    }
    closeList() {
        this.props.closeList()
    }
    render() {
        let { commodityList } = this.state
        return (
            <div className="commodity-list">
                <div className="list-top">
                    <div style={{ fontSize: "12px", color: "rgba(0,0,0,0.8)" }}>商品数量：{commodityList.length}</div>
                    <div className="close-list" onClick={() => { this.closeList() }}></div>
                </div>
                <div className="list-content">
                    {
                        commodityList.map((item, index) => {
                            return <div className="commodity--list-info" key={index}>
                                <div className="left">
                                    <img alt="商品图" src={item.img}></img>
                                </div>
                                <div className="right">
                                    <div className="commodity-title">
                                        {item.title}
                                    </div>
                                    <div className="commodity-price">
                                        <span>{item.price}</span>
                                        <div>
                                            <a target="_blank" href='https://detail.tmall.com/item.htm?id=618015229132&ali_refid=a3_430673_1006:1235070200:N:emtiAWsF8+zhhxaiwzc0Aw==:2f95831c03d6b91a126e2580213d8328&ali_trackid=1_2f95831c03d6b91a126e2580213d8328&spm=a2e0b.20350158.31919782.2'>马上抢</a></div>
                                    </div>
                                </div>
                            </div>
                        })
                    }
                </div>
            </div>
        )
    }
}
export default CommodityList