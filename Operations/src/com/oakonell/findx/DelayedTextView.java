package com.oakonell.findx;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class DelayedTextView extends TextView {

    public DelayedTextView(Context context) {
        super(context);
    }

    public DelayedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Handler handler = new Handler();
    String pendingText;
    Runnable pendingRunnable;

    public void writeText(String text) {
        if (pendingRunnable != null) {
            if (pendingText.equals(text)) {
                return;
            }
            handler.removeCallbacks(pendingRunnable);
            pendingRunnable = null;
            pendingText = null;
        }
        setText(text);
    }

    public void writeWithDelay(final SoundInfo delayInfo, final TextViewInfo info) {
        if (pendingRunnable != null) {
            if (pendingText.equals(info.text)) {
                return;
            }
            handler.removeCallbacks(pendingRunnable);
            pendingRunnable = null;
            pendingText = null;
        }
        final DelayIter iter = new DelayIter();
        pendingText = info.text;
        pendingRunnable = new Runnable() {
            @Override
            public void run() {
                iter.count++;
                if (iter.count <= info.text.length()) {
                    setText(info.text.substring(0, iter.count));
                    handler.postDelayed(this, 10);
                } else {
                    if (info.next != null) {
                        info.next.textView.writeWithDelay(delayInfo, info.next);
                    } else {
                        delayInfo.soundManager.stopSound(delayInfo.streamId);
                        if (info.animationFinished != null) {
                            info.animationFinished.run();
                        }
                    }
                    pendingRunnable = null;
                    pendingText = null;
                }
            }

        };
        handler.postDelayed(pendingRunnable, 10);
    }

    private static class DelayIter {
        int count;
    }

    public static class TextViewInfo {
        Runnable animationFinished;
        DelayedTextView textView;
        String text;
        TextViewInfo next;
    }

    public static class SoundInfo {
        SoundManager soundManager;
        int streamId;
    }

}
