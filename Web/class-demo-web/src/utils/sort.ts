export const sort = (list: any, key: any) => {
  const compareStatus = (key: any) => {
    return (x: any, y: any) => {
      const value1 = x[key];
      const value2 = y[key];
      return value2 - value1;
    };
  };
  list.sort(compareStatus(key)).sort((x: any, y: any) => {
    return Object.keys(y).length - Object.keys(x).length;
  });
  return list;
};
