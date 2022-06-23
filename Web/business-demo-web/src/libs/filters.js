import { formatDate } from '@/libs/utils'

const filters = {
  filter: (value, key, props) => {
    let propList = props ? [value, ...props] : [value]
    if ((key && value) || value === 0) return filters[key].apply(this, propList)
    else return value || '-'
  },

  formatDate: (date = '2018-08-08 00:00:00', format) => {
    return formatDate(date, format)
  },

  mobile: mobile => {
    mobile = mobile + ''
    return `${mobile.substr(0, 3)}****${mobile.substring(7)}`
  },

  week: value => {
    const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return week[value]
  },

  thousand: value => {
    return `${value / 1000}k`
  },

  status: value => {
    const statusList = ['未开始', '直播中', '已结束']
    return statusList[value] || '-'
  },

  number: value => {
    return value === 0 ? 0 : value
  }
}

export default filters
