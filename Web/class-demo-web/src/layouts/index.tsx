import { IRouteComponentProps } from 'umi';
import Header from '@/components/header';
import styles from './index.less';
import Student from '@/pages/student';

export default function Layout({ children, location }: IRouteComponentProps) {
  const from = location.pathname.includes('teacher') ? 'teacher' : 'student';
  return (
    <div className={styles['app-layout']}>
      <Header from={from} />
      {children}
    </div>
  );
}
