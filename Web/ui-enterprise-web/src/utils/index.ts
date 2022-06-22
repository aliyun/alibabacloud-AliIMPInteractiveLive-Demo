export interface BasicMap<U> {
  [index: string]: U;
}

// 创建一个dom元素
export const createDom = (type = 'div', options: any, content = '') => {
  let dom = document.createElement(type);
  if (
    options &&
    options.toString() === '[object Object]' &&
    JSON.stringify(options) !== '{}'
  ) {
    Object.keys(options).forEach((item) => {
      dom.setAttribute(item, options[item]);
    });
  }
  if (content) dom.append(content);
  return dom;
};

// 获取一个max和min之间的随机数
export const randomNum = (max: number, min: number) => {
  return Math.floor(Math.random() * (max - min + 1) + min);
};

// 格式化输出时间
export const formatDate = (date: Date, fmt: string): string => {
  let o: BasicMap<number> = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds(),
    'q+': Math.floor((date.getMonth() + 3) / 3),
    S: date.getMilliseconds(),
  };
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(
      RegExp.$1,
      (date.getFullYear() + '').substr(4 - RegExp.$1.length),
    );
  }
  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1
          ? '' + o[k]
          : ('00' + o[k]).substr(('' + o[k]).length),
      );
    }
  }
  return fmt;
};

// 判断当前在哪个platform
export const UA = (() => {
  const ua = navigator.userAgent;
  const isAndroid = /(?:Android)/.test(ua);
  const isFireFox = /(?:Firefox)/.test(ua);
  const isPad =
    /(?:iPad|PlayBook)/.test(ua) ||
    (isAndroid && !/(?:Mobile)/.test(ua)) ||
    (isFireFox && /(?:Tablet)/.test(ua));
  const isiPad =
    /(?:iPad)/.test(ua) ||
    (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
  const isiPhone = /(?:iPhone)/.test(ua) && !isPad;
  const isPC = !isiPhone && !isAndroid && !isPad && !isiPad;
  return {
    isPad,
    isiPhone,
    isAndroid,
    isPC,
    isiPad,
  };
})();

// 测试用，点击6次切到pre
let preCount = 0;
export const setPre = () => {
  preCount += 1;
  if (preCount > 6) {
    window.localStorage.getItem('pre')
      ? window.localStorage.removeItem('pre')
      : window.localStorage.setItem('pre', '1');
    window.location.reload();
  }
};

// 把search分割成对象
export const splitSearch = (search: string): BasicMap<any> => {
  const result: BasicMap<string> = {};
  try {
    search
      .split('?')[1]
      .split('&')
      .forEach((item: string) => {
        const itemSplit = item.split('=');
        result[itemSplit[0]] = itemSplit[1];
      });
    return result;
  } catch (err) {
    return {};
  }
};

export const getUrlWithString = (str: string) => {
  const reg = new RegExp(
    '(https?|http|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]',
  );
  if (reg.test(str)) {
    const match = str.match(reg);
    let newStr;
    if (match && match[0]) newStr = match[0];
    return str.replace(
      reg,
      `<a href="${newStr}" target="_blank">${newStr}</a>`,
    );
  }
  return str;
};
