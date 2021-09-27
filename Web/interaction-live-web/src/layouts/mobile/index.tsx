import { IRouteComponentProps } from 'umi';
import styles from './index.less';

export default function Layout({ children }: IRouteComponentProps) {
  return (
    <div className={styles['app-layout']}>
      {children}
    </div>
  );
}
