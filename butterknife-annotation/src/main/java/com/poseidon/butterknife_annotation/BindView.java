package com.poseidon.butterknife_annotation;

import androidx.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 编译时注解，源码中存在
@Retention(RetentionPolicy.SOURCE)
// 应用在成员变量上
@Target(ElementType.FIELD)
public @interface BindView {
    // 返回值必须是资源id
    @IdRes int value();
}