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
  if (window.engineAuthed) {
    return <div style={{ height: '100%' }}>{props.children}</div>;
  } else if (isLogin) {
    return <Redirect to={`/doLogin?from=${props.match.path}${qs}`} />;
  } else {
    return <Redirect to="/" />;
  }
};
