#pragma once
#include <string>
#include <vector>
#include <map>
class SignUtil
{

public:
  /*
  * ǩ����������HeaderKey
  */
  const static std::string kAppId;
  const static std::string kSignature; // ǩ�������
  const static std::string kSignatureMethod;
  const static std::string kTimestamp;
  const static std::string kSignatureVersion;
  const static std::string kSignatureNonce;

  // ǩ���㷨�̶�ֵ
  const static std::string kSignatureMethodValue;

  // ǩ���㷨�汾�̶�ֵ
  const static std::string kSignatureVersionValue;

  // ǩ���㷨����
  const static std::string kAlgorithmName;
  const static std::string kEncoding;
  const static std::string kUrlEncoding;

  /**
   * ��ȡǩ��
   *
   * @param signSecret ǩ����Կ
   * @param method     {@link }�������ִ�Сд
   * @param path       ʾ����/api/service��������"/"��ͷ
   * @param params     �������
   * @param headers    ����ͷ������ǩ����������ȡ����ͷ����header ��ȫСд
   *                     ҵ�������
   *                      ��a-token��
   *                     ����������
   *                      ��a-app-id��
   *                      ��a-signature��
   *                      ��a-signature-method��
   *                      ��a-timestamp��
   *                      ��a-signature-version��
   *                      ��a-signature-nonce��
   * @return sign      ǩ��
   */
  static std::string GetSign(
    std::string signSecret, 
    std::string method, 
    std::string path, 
    std::map<std::string, std::string> params,
    std::map<std::string, std::string> headers);

  static std::string GetUTCTimeStampFormatString();

  static std::string GenerateNonce();
private:
  /**
 * ����������󣬽��й淶�����ã���ϳ������ַ���
 * @param queryParamsMap   �����������
 * @return �淶���������ַ���
 */
  static std::string CanonicalizedQuery(std::map<std::string, std::string> query_params_map);

  /**
   * URL����
   * ʹ��UTF-8�ַ������� RFC3986 ���������������Ͳ���ȡֵ
   */
  static std::string PercentEncode(std::string value);

  /***
   * ����ǩ���ַ���
   * @param method       HTTP����ķ���
   * @param urlPath      HTTP�����·��
   * @param queryString  �淶���������ַ���
   * @param headerString �淶����ͷ���ַ���
   * @return ǩ���ַ���
   */
  static std::string BuildSignString(std::string method, std::string urlPath, std::string queryString, std::string headerString);

  /***
   * ����ǩ��
   * @param stringToSign ǩ���ַ���
   * @param signSecret   ǩ����Կ
   * @return ����õ���ǩ��
   */
  static std::string Sign(std::string string_to_sign, std::string sign_secret);

  const static std::vector<std::string> processed_headers;
};

