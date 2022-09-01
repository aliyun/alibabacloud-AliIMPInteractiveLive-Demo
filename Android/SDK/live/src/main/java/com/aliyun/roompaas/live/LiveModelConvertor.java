package com.aliyun.roompaas.live;

import android.net.Uri;

import com.alibaba.dingpaas.easyutils.Easyutils;
import com.alibaba.dingpaas.easyutils.EasyutilsEncryptDecryptDataResult;
import com.alibaba.dingpaas.live.ArtcInfo;
import com.alibaba.dingpaas.live.ArtcInfoModel;
import com.alibaba.dingpaas.live.GetLiveDetailRsp;
import com.alibaba.dingpaas.live.Live;
import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.live.LiveInfo;
import com.alibaba.dingpaas.live.PlayUrl;
import com.alibaba.dingpaas.live.PlayUrlModel;
import com.aliyun.roompaas.base.BaseSettings;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.HexUtil;
import com.aliyun.roompaas.base.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author puke
 * @version 2022/4/19
 */
class LiveModelConvertor {
    private static final String TAG = "LiveModelConvertor";
    private static final String ENCRYPT_VIDEO_PAAS_PRINT = "vpaasPrint";

    static LiveDetail convertLiveDetail(GetLiveDetailRsp detailRsp) {
        LiveDetail liveDetail = new LiveDetail();

        Live live = detailRsp.live;
        if (live != null) {
            LiveInfo liveInfo = new LiveInfo();
            liveInfo.anchorId = live.anchorId;
            liveInfo.liveId = live.uuid;
            liveInfo.title = live.title;
            liveInfo.playUrl = live.playUrl;
            liveInfo.createDate = live.createDate;
            liveInfo.endDate = live.endDate;
            liveInfo.preStartDate = live.preStartDate;
            liveInfo.preEndDate = live.preEndDate;
            liveInfo.duration = live.duration;
            liveInfo.pushUrl = isEncrypt(live.pushUrl) ? decryptPushUrl(live.pushUrl) : live.pushUrl;
            liveInfo.liveUrl = live.liveUrl;
            liveInfo.status = live.status;
            liveInfo.introduction = live.introduction;
            liveInfo.codeLevel = live.codeLevel;
            liveInfo.playUrlList = convertPlayUrlList(live.playUrlList);
            liveInfo.hlsUrl = live.hlsUrl;
            liveInfo.artcInfo = convertArtcInfo(live.artcInfo);
            liveInfo.coverUrl = live.coverUrl;
            liveInfo.userDefineField = live.userDefineField;
            liveInfo.roomId = live.roomId;
            liveInfo.enableLinkMic = live.enableLinkMic;

            liveDetail.liveInfo = liveInfo;
        }

        return liveDetail;
    }

    private static ArrayList<PlayUrlModel> convertPlayUrlList(ArrayList<PlayUrl> playUrlList) {
        if (playUrlList == null) {
            return null;
        }

        ArrayList<PlayUrlModel> models = new ArrayList<>();
        for (PlayUrl playUrl : playUrlList) {
            PlayUrlModel model = new PlayUrlModel();
            model.codeLevel = playUrl.codeLevel;
            model.flvUrl = playUrl.flvUrl;
            model.hlsUrl = playUrl.hlsUrl;
            model.rtmpUrl = playUrl.rtmpUrl;
            models.add(model);
        }
        return models;
    }

    private static ArtcInfoModel convertArtcInfo(ArtcInfo artcInfo) {
        if (artcInfo == null) {
            return null;
        }

        ArtcInfoModel model = new ArtcInfoModel();
        model.artcUrl = artcInfo.artcUrl;
        model.artcH5Url = artcInfo.artcH5Url;
        return model;
    }

    private static boolean isEncrypt(String pushUrl) {
        return Utils.isNotEmpty(pushUrl) && pushUrl.contains(ENCRYPT_VIDEO_PAAS_PRINT);
    }

    private static String decryptPushUrl(String pushUrl) {
        try {
            Uri uri = Uri.parse(pushUrl);
            String password = uri.getQueryParameter(ENCRYPT_VIDEO_PAAS_PRINT);
            String hexIn = uri.getLastPathSegment();
            byte[] passwordByte = password.getBytes(StandardCharsets.UTF_8);
            byte[] saltByte = BaseSettings.getAppId().getBytes(StandardCharsets.UTF_8);
            byte[] inDataByte = HexUtil.hex2ByteArray(hexIn);
            EasyutilsEncryptDecryptDataResult result = Easyutils.decryptData(passwordByte, saltByte, inDataByte);
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme(uri.getScheme()).authority(uri.getAuthority());
            List<String> pathSegments = uri.getPathSegments();
            int length = uri.getPathSegments().size();
            for (int i = 0; i < length - 1; i++) {
                uriBuilder.appendPath(pathSegments.get(i));
            }
            pushUrl = String.format("%s/%s", uriBuilder.build().toString(), new String(result.data, StandardCharsets.UTF_8));
        } catch (Exception e) {
            Logger.e(TAG, "decryptPushUrl error " + e.getMessage());
        }

        return pushUrl;
    }
}
