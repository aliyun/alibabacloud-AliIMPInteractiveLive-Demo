export const isWeiXin = () => {
  let ua = window.navigator.userAgent.toLowerCase()
  return /microMessenger/ig.test(ua)
}

export const isQQ = () => {
  let ua = window.navigator.userAgent.toLowerCase()
  /* eslint eqeqeq: off */
  let isqq = ua.match(/ qq/i) == ' qq'
  /* eslint eqeqeq: off */
  if (ua.match(/mqqbrowser/i) == 'mqqbrowser') {
    isqq = false
  }
  return isqq
}

export const isIos = () => {
  return /iPad|iPhone|iPod/g.test(navigator.userAgent)
}

export const isAndroid = () => {
  return /android/gi.test(navigator.userAgent)
}

export const isTel = (phone = '') => {
  return /^1[3-9]\d{9}$/g.test(phone)
}

export const isUrl = (str = '') => {
  return /^((ht|f)tps?):\/\/[\w-]+(\.[\w-]+)+([\w\-.,@?^=%&:/~+#]*[\w\-@?^=%&/~+#])?$/.test(str)
}

export function isFunction (value) {
  return ({}).toString.call(value) === '[object Function]'
}

export const formatDate = (date, fmt) => {
  if (isNaN(date)) {
    date = date.replace(/-/g, '/')
  }
  if (parseInt(date).toString().length <= 10) {
    date = parseInt(date) * 1000
  }
  date = new Date(date)

  let o = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds(),
    'q+': Math.floor((date.getMonth() + 3) / 3),
    'S': date.getMilliseconds()
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
    }
  }
  return fmt
}

export const throttle = (callback, threshold) => {
  if (!isFunction(callback)) return
  let last, timer
  threshold = threshold || 250
  return function () {
    const context = this
    let args = arguments
    let now = +new Date()
    if (last && now < last + threshold) {
      clearTimeout(timer)
      timer = setTimeout(function () {
        callback.apply(context, args)
      }, threshold)
    } else {
      last = now
      callback.apply(context, args)
    }
  }
}

export const inArray = (str = '', array = []) => {
  let length = array.length
  for (let i = 0; i < length; i++) {
    if (str === array[i]) {
      return true
    }
  }
  return false
}

// 判断当前在哪个platform
export const UA = (() => {
  const ua = navigator.userAgent
  const isAndroid = /(?:Android)/.test(ua)
  const isFireFox = /(?:Firefox)/.test(ua)
  const isPad
    = /(?:iPad|PlayBook)/.test(ua) || (isAndroid && !/(?:Mobile)/.test(ua)) || (isFireFox && /(?:Tablet)/.test(ua))
  const isiPad = /(?:iPad)/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)
  const isiPhone = /(?:iPhone)/.test(ua) && !isPad
  const isPC = !isiPhone && !isAndroid && !isPad && !isiPad
  const isMac = /(?:Mac OS)/.test(ua)
  const isWin = /(?:Windows)/i.test(ua)
  return {
    isPad,
    isiPhone,
    isAndroid,
    isPC,
    isiPad,
    isMac,
    isWin,
  }
})()
