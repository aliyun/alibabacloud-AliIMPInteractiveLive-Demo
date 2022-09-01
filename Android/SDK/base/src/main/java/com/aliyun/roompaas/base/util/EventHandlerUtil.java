package com.aliyun.roompaas.base.util;

import com.aliyun.roompaas.base.exposable.PluginService;

import java.lang.ref.Reference;

/**
 * Created by KyleCe on 2021/9/15
 */
public class EventHandlerUtil {

    public static <EH, PS extends PluginService<EH>> void addEventHandler(Reference<PS> ref, EH eh) {
        PS ps = Utils.getRef(ref);
        if (ps == null || eh == null) {
            return;
        }

        ps.addEventHandler(eh);
    }

    public static <EH, PS extends PluginService<EH>> void removeEventHandler(Reference<PS> ref, EH eh) {
        PS ps = Utils.getRef(ref);
        if (ps == null || eh == null) {
            return;
        }

        ps.removeEventHandler(eh);
    }

}
