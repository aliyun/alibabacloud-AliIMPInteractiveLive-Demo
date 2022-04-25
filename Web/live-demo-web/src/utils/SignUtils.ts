import CryptoES from 'crypto-es';

// 对称加密算法，可以验证secret来源
// 实际情况中按照自己服务端的情况配置，或者选择不实现
// 也可以替换成自己的加密算法，比如非对称加密
const SignUtils = {
  SIGNATURE_METHOD: 'HMAC-SHA1',
  SIGNATURE_VERSION: '1.0',

  /***
   * 构造签名字符串
   * @param method       HTTP请求的方法
   * @param urlPath      HTTP请求的路径
   * @param queryString  规范化的请求字符串
   * @param headerString 规范化的头部字符串
   * @return 签名字符串
   */
  buildSignString(
    method: string,
    urlPath: string,
    queryString: string,
    headerString: string,
  ): string {
    return `${method}+${SignUtils.percentEncode(
      urlPath,
    )}+${SignUtils.percentEncode(queryString)}+${SignUtils.percentEncode(
      headerString,
    )}`;
  },

  /***
   * UTF-8百分号编码
   * @param str buildSignString生成的
   * @return 签名字符串
   */
  percentEncode(str: string): string {
    return encodeURIComponent(str)
      .replace('+', '%20')
      .replace('*', '%2A')
      .replace('%7E', '~');
  },

  /***
   * 生成签名
   * @param stringToSign buildSignString生成的
   * @param signSecret 密钥
   * @return 签名字符串
   */
  generateSignature(stringToSign: string, signSecret: string): string {
    return SignUtils.percentEncode(
      CryptoES.enc.Base64.stringify(
        CryptoES.HmacSHA1(stringToSign, signSecret),
      ),
    );
  },
};

export default SignUtils;
