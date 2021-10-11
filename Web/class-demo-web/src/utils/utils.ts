export interface BasicMap<U> {
  [index: string]: U;
}

export const formatDate = (date: Date, fmt: string) => {
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

export const throttle = (fn: any, time: any) => {
  let timer = null;
  if (!timer) {
    timer = setTimeout(() => {
      fn();
      timer = null;
    }, time);
  }
};
