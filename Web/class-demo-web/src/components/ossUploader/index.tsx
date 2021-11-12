import { FC, useEffect, useState, useRef } from 'react';
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  connect,
  Dispatch,
} from 'umi';
import { Upload, Button, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import OSS from 'ali-oss';
import { useMount } from 'ahooks';
import styles from './index.less';

interface PageProps {
  room: RoomModelState;
  status: StatusModelState;
  user: UserModelState;
  dispatch: Dispatch;
}

const OssUploader: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const handleUpload = (options: any) => {
    console.log(options);
    const { file, onSuccess, onError, onProgress } = options;
    const avaliableTypeList = ['ppt', 'pptx', 'pdf', 'png', 'jpg'];
    const fileInfo = file.name.split('.');
    if (fileInfo.length < 2) {
      message.error('文件缺少后缀名');
      return false;
    }
    const fileName = fileInfo[0];
    const fileType = fileInfo[fileInfo.length - 1];
    if (!avaliableTypeList.includes(fileType)) {
      message.error('文件格式不正确');
      return false;
    }
    let docId: string;
    let aliOss;
    window.wbService
      .createDoc(fileName, fileType)
      .then((res: any) => {
        // 先create doc，获取oss bucket
        aliOss = new OSS({
          region: 'oss-cn-shanghai',
          accessKeyId: res.credentials.accessKeyId,
          accessKeySecret: res.credentials.accessKeySecret,
          stsToken: res.credentials.securityToken,
          refreshSTSTokenInterval: 300000,
          bucket: res.ossBucket,
        });
        docId = res.docId;
        window.wbService.reportUploadStatus(docId, 0); // 上传中
        return aliOss.multipartUpload(res.ossObject, file, {
          // 调用multipartUpload
          progress: (progress: number, checkpoint: any) => {
            onProgress({ percent: progress * 100 });
          },
        });
      })
      .then(() => {
        onSuccess();
        window.wbService.reportUploadStatus(docId, 1); // 上传成功
        window.wbService
          .createDocConversionTask(docId, 'jpg', fileName)
          .then((d: any) => {
            console.log(d);
            dispatch({
              type: 'status/setCurrentDocId',
              payload: d.targetDocId,
            });
          }); // 转码
        dispatch({
          type: 'status/setIsDocConverting',
          payload: true,
        });
      })
      .catch((err: any) => {
        console.error(err);
        message.error('上传失败');
        window.wbService.reportUploadStatus(docId, 2); // 上传失败
        onError();
      });
  };
  return (
    <Upload customRequest={handleUpload}>
      <Button icon={<UploadOutlined />}>上传课件</Button>
      {status.docConverting && <div>转码中</div>}
    </Upload>
  );
};

export default connect(
  ({
    room,
    status,
    user,
  }: {
    room: RoomModelState;
    status: StatusModelState;
    user: UserModelState;
  }) => ({
    room,
    status,
    user,
  }),
)(OssUploader);
