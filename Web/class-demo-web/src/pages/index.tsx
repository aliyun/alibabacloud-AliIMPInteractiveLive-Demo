import styles from './index.less';
import { history } from 'umi';

export default function IndexPage() {
  history.push('/login');
  return <div></div>;
}
