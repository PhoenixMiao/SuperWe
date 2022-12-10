package com.example.superwe.toast;

import android.view.View;

/**
 *    desc   : {@link View.OnClickListener} 包装类
 */
@SuppressWarnings("rawtypes")
final class ViewClickWrapper implements View.OnClickListener {

    private final XToast<?> mToast;
    private final XToast.OnClickListener mListener;

    ViewClickWrapper(XToast<?> toast, XToast.OnClickListener listener) {
        mToast = toast;
        mListener = listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onClick(View view) {
        if (mListener == null) {
            return;
        }
        mListener.onClick(mToast, view);
    }
}