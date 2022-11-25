package com.codingcosmos.movieland.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class YTPlayerBugFixRecyclerView extends RecyclerView {
    private int touchSlop;
    private List<MotionEvent> touchEventTrack = new ArrayList<>();

    public YTPlayerBugFixRecyclerView(@NonNull Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public YTPlayerBugFixRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public YTPlayerBugFixRecyclerView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Only intercept vertical scroll
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchEventTrack.clear();
                touchEventTrack.add(MotionEvent.obtain(ev));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                touchEventTrack.add(MotionEvent.obtain(ev));
                break;
            }
            case MotionEvent.ACTION_UP: {
                touchEventTrack.add(MotionEvent.obtain(ev));
                if (isClick(ev)) {
                    for (int i = 0; i < getChildCount(); i++) {
                        View view = getChildAt(i);
                        if (!isInView(ev, view)) {
                            continue;
                        }
                        boolean handle = false;
                        for (MotionEvent event : touchEventTrack) {
                            handle = dispatchTransformedTouchEvent(event, view);
                            if (!handle) {
                                break;
                            }
                        }
                        if (handle) {
                            MotionEvent cancelEvent = MotionEvent.obtain(ev);
                            cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                            return onTouchEvent(cancelEvent);
                        }
                    }
                }
                break;
            }
        }
        return onTouchEvent(ev);
    }

    private boolean isInView(MotionEvent ev, View view) {
        Rect rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect.contains((int) ev.getX(), (int) ev.getY());
    }

    private boolean isClick(MotionEvent ev) {
        MotionEvent downEvent = this.touchEventTrack.get(0);
        return Math.abs(downEvent.getX() - ev.getX()) < touchSlop && Math.abs(downEvent.getY() - ev.getY()) < touchSlop;
    }


    private boolean dispatchTransformedTouchEvent(MotionEvent event, View child) {
        float offsetX = getScrollX() - child.getLeft();
        float offsetY = getScaleY() - child.getTop();
        event.offsetLocation(offsetX, offsetY);
        boolean handle = child.dispatchTouchEvent(event);
        if (!handle) {
            event.offsetLocation(-offsetX, -offsetY);
        }
        return handle;
    }
}
