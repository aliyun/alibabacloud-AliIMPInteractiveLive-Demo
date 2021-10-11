const parseNum = (m: Number) => {
  return m < 10 ? '0' + m : m;
};
export const getTime = (time: any) => {
  const y = time.getFullYear();
  const m = time.getMonth() + 1;
  const d = time.getDate();
  const h = time.getHours();
  const mm = time.getMinutes();
  return (
    y +
    '-' +
    parseNum(m) +
    '-' +
    parseNum(d) +
    ' ' +
    parseNum(h) +
    ':' +
    parseNum(mm)
  );
};

export const calcTime = (time: any) => {
  const timeSecond = Math.floor(time / 1000);
  const min = Math.floor(timeSecond / 60);
  const second = timeSecond % 60;
  return parseNum(min) + ':' + parseNum(second);
};
