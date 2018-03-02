package com.xiao.rxbonjour.model;

import android.support.annotation.IntDef;

public interface NsdStatus {

    int ADDED = 0;
    int REMOVED = 1;
    @IntDef({ADDED, REMOVED})
    @interface STATUS{}

    boolean isAdded();
}
