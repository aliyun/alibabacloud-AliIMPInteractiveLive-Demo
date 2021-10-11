import { formatDate } from '@/utils/utils';
import SignUtils from '@/utils/SignUtil';
import roomEngineConfig from '@/constants/config';
import qs from 'qs';

const getSignHeader = (query: any, path: string, signSecret: string) => {
  // 构造queryString
  const queryString = qs.stringify(query);
  const headers = {
    // 如果验签，属性顺序需与服务端一致
    'a-app-id': 'imp-room',
    'a-signature-method': SignUtils.SIGNATURE_METHOD,
    'a-signature-nonce': Math.floor(Math.random() * 10000).toString(),
    'a-signature-version': SignUtils.SIGNATURE_VERSION,
    'a-timestamp': formatDate(new Date(), 'yyyy-MM-ddThh:mm:ssZ'),
  };
  // 构造header queryString
  const headerString = qs.stringify(headers);
  // 构造signString
  const signString = SignUtils.buildSignString(
    'POST',
    `https://***.***.com/${path}`,
    queryString,
    headerString,
  );
  // 生成签名
  const signature = SignUtils.generateSignature(signString, signSecret + '&');
  return {
    headers: {
      ...headers,
      'a-signature': signature,
    },
    queryString,
  };
};

export default (path: any, params: any) => {
  const { signSecret } = roomEngineConfig;
  const origin = window.location.href.includes('alicdn.com')
    ? 'https://***.***.com'
    : window.location.origin;
  const { headers, queryString } = getSignHeader(
    {
      ...params,
    },
    path,
    signSecret,
  );
  return fetch(`${origin}/${path}?${queryString}`, {
    method: 'POST',
    headers,
  })
    .then((res) => res.json())
    .catch((err) => {
      console.error(err);
    });
};
