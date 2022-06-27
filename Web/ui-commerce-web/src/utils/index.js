// 获取一个max和min之间的随机数
export const randomNum = (max, min) => {
  return Math.floor(Math.random() * (max - min + 1) + min);
};

// 创建一个dom元素
export const createDom = (type = 'div', options, content = '') => {
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

// 把search分割成对象
export const splitSearch = (search) => {
  const result = {};
  try {
    search
      .split('?')[1]
      .split('&')
      .forEach((item) => {
        const itemSplit = item.split('=');
        result[itemSplit[0]] = itemSplit[1];
      });
    return result;
  } catch (err) {
    return {};
  }
};

export const getUrlWithString = (str) => {
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

export const replaceHttps = (url) => {
  if (!url || typeof url !== 'string') return;
  return url.replace(/^http:\/\//i, 'https://');
};

export const changeFavicon = (url) => {
  const link = document.createElement('link');
  const oldLink = document.querySelector('link[rel="shortcut icon"]');
  console.log(url, oldLink);
  if (oldLink) document.head.removeChild(oldLink);
  link.href =
    url || 'https://img.alicdn.com/tfs/TB1_ZXuNcfpK1RjSZFOXXa6nFXa-32-32.ico';
  link.rel = 'shotrcut icon';
  link.id = 'favicon';
  document.head.appendChild(link);
};
