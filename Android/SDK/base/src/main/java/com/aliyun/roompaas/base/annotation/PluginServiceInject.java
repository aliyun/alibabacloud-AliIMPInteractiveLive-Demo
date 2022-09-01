package com.aliyun.roompaas.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插件服务注解<hr/>
 * SDK在初始化时会对加入该注解的服务进行动态加载<br/>
 *
 * @author puke
 * @version 2021/6/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginServiceInject {
}
