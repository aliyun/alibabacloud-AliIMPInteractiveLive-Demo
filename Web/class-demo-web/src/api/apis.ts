import http from './index';

export const getToken = (params: any) => {
  const path = 'api/login/getToken';
  return http(path, params);
};

export const createRoom = (params: any) => {
  const path = 'api/login/createRoom';
  return http(path, params);
};

export const startLesson = (params: any) => {
  const path = 'api/room/startLesson';
  return http(path, params);
};

export const endLesson = (params: any) => {
  const path = 'api/room/endLesson';
  return http(path, params);
};

export const startRecording = (params: any) => {
  const path = 'api/room/startRecording';
  return http(path, params);
};

export const listRecords = (params: any) => {
  const path = 'api/room/listRecords';
  return http(path, params);
};

export const getLessonStartTime = (params: any) => {
  const path = 'api/room/getLessonStartTime';
  return http(path, params);
};
