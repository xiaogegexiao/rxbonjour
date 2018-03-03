package com.xiao.rxbonjour.common;


import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;

public interface OnSubscribeEvent<T> extends ObservableOnSubscribe<T> {
    Action onCompleted();
}
