import { useEffect } from 'react';
import { IRouteComponentProps } from 'umi';
import styles from './index.less';
import { doLogin } from '../biz/doLogin';

export default function IndexPage(props: IRouteComponentProps) {
  const { from } = props.location.query;
  const query = props.location.query;
  let qs = '';
  if (Object.keys(query).length > 0) {
    qs = Object.keys(query)
      .map((item: string) => {
        if (item === 'from') return '';
        return `${item}=${query[item]}`;
      })
      .join('&');
  }
  useEffect(() => {
    const nickname = window.localStorage.getItem('nickname');
    if (!nickname) {
      props.history.replace('/login');
      return;
    }
    doLogin(nickname)
      .then(() => {
        props.history.replace(
          `${from ? from : '/roomList'}${qs ? `?${qs}` : ''}`,
        );
      })
      .catch((err) => {
        props.history.replace('/');
      });
  }, []);
  return <div className={styles['do-login']}>登录中</div>;
}
