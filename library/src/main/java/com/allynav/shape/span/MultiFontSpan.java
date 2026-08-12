package com.allynav.shape.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import androidx.annotation.NonNull;
import com.allynav.shape.config.ITextViewAttribute;
import java.util.Arrays;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2022/05/04
 *    desc   : 在同一文字范围叠加多个 ReplacementSpan 的组合容器
 *
 * <p>测量阶段取所有子 Span 返回宽度的最大值，绘制阶段按传入顺序逐个执行，适合把
 * 描边、渐变等效果叠加在同一段文本上。调用方应保证各 Span 使用兼容字体度量。</p>
 */
public class MultiFontSpan extends AlignmentReplacementSpan {

    /** 测量的文本宽度 */
    private float mMeasureTextWidth;

    /** 保留调用顺序，后加入的 Span 会绘制在先加入效果之上。 */
    private final List<ReplacementSpan> mReplacementSpans;

    public MultiFontSpan(ITextViewAttribute textViewAttribute, ReplacementSpan... replacementSpans) {
        super(textViewAttribute);
        mReplacementSpans = Arrays.asList(replacementSpans);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        for (ReplacementSpan replacementSpan : mReplacementSpans) {
            int size = replacementSpan.getSize(paint, text, start, end, fm);
            mMeasureTextWidth = Math.max(mMeasureTextWidth, size);
        }
        return (int) mMeasureTextWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        for (ReplacementSpan replacementSpan : mReplacementSpans) {
            replacementSpan.draw(canvas, text, start, end, x, top, y, bottom, paint);
        }
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        super.updateMeasureState(p);
        for (ReplacementSpan replacementSpan : mReplacementSpans) {
            replacementSpan.updateMeasureState(p);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        for (ReplacementSpan replacementSpan : mReplacementSpans) {
            replacementSpan.updateDrawState(ds);
        }
    }
}
