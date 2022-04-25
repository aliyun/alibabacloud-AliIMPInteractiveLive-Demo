export const getAuthStatus = (): boolean => {
  const token = window.localStorage.getItem('token');
  if (token) {
    try {
      const { accessTokenExpiredTime } = JSON.parse(token);
      const authTimeStamp = window.localStorage.getItem('authTimeStamp');
      if (!accessTokenExpiredTime || !authTimeStamp) return false; // token是否保存正确
      if (+new Date() - Number(authTimeStamp) > accessTokenExpiredTime)
        return false; // token是否过期
      return true;
    } catch (err) {
      return false; // token是否保存正确
    }
  }
  return false; // 是否保存token
};
