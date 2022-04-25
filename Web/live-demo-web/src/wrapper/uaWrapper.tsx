import { Redirect, IRouteComponentProps } from 'umi';
import { UA } from '../utils';

const pathReg = /^\/m\b|^\/m\/\w+/;

export default (props: IRouteComponentProps) => {
  const path = props.location.pathname || '';
  const isMatchPC = !pathReg.test(path);
  console.log(path, isMatchPC, UA.isPC);
  if ((UA.isPC && isMatchPC) || (!UA.isPC && !isMatchPC)) {
    return props.children;
  } else {
    const newPath =
      (UA.isPC
        ? props.route.path?.replace(pathReg, '')
        : `/m${props.route.path}`) || '';
    return <Redirect to={newPath + props.location.search} />;
  }
};
