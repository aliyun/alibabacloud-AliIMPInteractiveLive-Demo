//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alibaba.dingpaas.live;

public final class ArtcInfoModel {
    public String artcUrl = "";
    public String artcH5Url = "";

    public ArtcInfoModel(String artcUrl, String artcH5Url) {
        this.artcUrl = artcUrl;
        this.artcH5Url = artcH5Url;
    }

    public ArtcInfoModel() {
    }

    public String getArtcUrl() {
        return this.artcUrl;
    }

    public String getArtcH5Url() {
        return this.artcH5Url;
    }

    public String toString() {
        return "ArtcInfoModel{artcUrl=" + this.artcUrl + ",artcH5Url=" + this.artcH5Url + "}";
    }
}

