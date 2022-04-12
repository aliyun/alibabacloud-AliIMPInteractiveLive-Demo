import { Redirect, IRouteComponentProps } from 'umi';
import { getAuthStatus } from '../biz/getAuthStatus';

export default (props: IRouteComponentProps) => {
  const isLogin = getAuthStatus();
  const query = props.history.location.query || {};
  let qs = '';
  if (Object.keys(query).length > 0) {
    qs =
      '&' +
      Object.keys(query)
        .map((item: string) => `${item}=${query[item]}`)
        .join('&');
  }
  if (window.roomEngine && window.roomEngine.isLogined) {
    return props.children;
  } else if (isLogin) {
    return <Redirect to={`/doLogin?from=${props.match.path}${qs}`} />;
  } else {
    return <Redirect to="/" />;
  }
};
