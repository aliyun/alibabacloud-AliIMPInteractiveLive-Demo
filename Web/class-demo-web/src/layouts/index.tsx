import { IRouteComponentProps } from 'umi';
import Header from '@/components/header';
import styles from './index.less';
import Student from '@/pages/student';

export default function Layout({ children, location }: IRouteComponentProps) {
  const isPre = window.localStorage.getItem('pre');
  const from = location.pathname.includes('teacher') ? 'teacher' : 'student';
  return (
    <div className={styles['app-layout']}>
      {isPre && <div className={styles.ispre}>预发</div>}
      <Header from={from} />
      {children}
    </div>
  );
}
