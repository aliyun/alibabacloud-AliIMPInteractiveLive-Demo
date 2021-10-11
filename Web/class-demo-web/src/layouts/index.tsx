import Header from '@/components/Header';
import './index.less';

export default function (props: any) {
  if (props.location.pathname === '/login') {
    return <>{props.children}</>;
  }
  const roomId = props.location.query.roomId || 666666;
  const role = props.location.pathname.substr(1) || 'teacher';
  return (
    <>
      <Header roomId={roomId} role={role} />
      {props.children}
    </>
  );
}
