import { BasicMap } from '@/utils/utils';
export const initHashList = (
  userList: any,
  ownerId: any,
  confUserList?: any,
  applyList?: any,
) => {
  const studentList = hashList(userList);
  if (confUserList) {
    confUserList.forEach((data: any) => {
      if (!studentList || !studentList[data.userId]) return;
      studentList[data.userId].status = data.status;
      studentList[data.userId].cameraStatus = data.cameraStatus;
      studentList[data.userId].micphoneStatus = data.micphoneStatus;
    });
  }
  if (applyList) {
    applyList.forEach((data: any) => {
      if (!studentList || !studentList[data.userId]) return;
      studentList[data.userId].status = 7;
    });
  }
  delete studentList[ownerId];
  return studentList;
};

export const hashList = (userList: any) => {
  const hashList: BasicMap<any> = {};
  const assist = <any[]>[];
  for (let i = 0; i < userList.length; i++) {
    if (exist(userList[i].userId, assist)) continue;
    assist.push(userList[i].userId);
    hashList[userList[i].userId] = userList[i];
  }
  return hashList;
};

export const exist = (item: any, list: any) => {
  for (let i = 1; i < list.length; i++) {
    if (item === list[i]) return true;
  }
  return false;
};
