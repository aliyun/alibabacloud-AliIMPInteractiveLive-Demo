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
