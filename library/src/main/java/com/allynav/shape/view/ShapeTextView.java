package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.widget.AppCompatTextView;
import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.builder.TextColorBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.config.IGetTextColorBuilder;
import com.allynav.shape.config.IGetTextStateDelegate;
import com.allynav.shape.other.AdaptiveTextDelegate;
import com.allynav.shape.other.AutoFitTextDelegate;
import com.allynav.shape.other.MarqueeTextDelegate;
import com.allynav.shape.other.TextStateDelegate;
import com.allynav.shape.styleable.ShapeTextViewStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 TextView
 */
public class ShapeTextView extends AppCompatTextView implements
        IGetShapeDrawableBuilder, IGetTextColorBuilder, IGetTextStateDelegate {

    public static final int ADAPTIVE_MODE_REDUCE_LINES =
            AdaptiveTextDelegate.MODE_REDUCE_LINES;
    public static final int ADAPTIVE_MODE_REDUCE_LINE_SPACING =
            AdaptiveTextDelegate.MODE_REDUCE_LINE_SPACING;
    public static final int ADAPTIVE_MODE_REDUCE_LINE_SPACING_THEN_LINES =
            AdaptiveTextDelegate.MODE_REDUCE_LINE_SPACING_THEN_LINES;

    private static final ShapeTextViewStyleable STYLEABLE = new ShapeTextViewStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final TextColorBuilder mTextColorBuilder;
    private final TextStateDelegate mTextStateDelegate;
    private final AdaptiveTextDelegate mAdaptiveTextDelegate;
    private final AutoFitTextDelegate mAutoFitTextDelegate;
    private final MarqueeTextDelegate mMarqueeTextDelegate;

    public ShapeTextView(Context context) {
        this(context, null);
    }

    public ShapeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public ShapeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeTextView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        mTextColorBuilder = new TextColorBuilder(this, typedArray, STYLEABLE);
        mTextStateDelegate = new TextStateDelegate(this, attrs);
        mAdaptiveTextDelegate = new AdaptiveTextDelegate(this, typedArray);
        mAutoFitTextDelegate = new AutoFitTextDelegate(this, typedArray);
        mMarqueeTextDelegate = new MarqueeTextDelegate(this, typedArray);
        mMarqueeTextDelegate.initialize();
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
        mTextColorBuilder.intoTextColor();
        mTextStateDelegate.refresh();
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mTextColorBuilder == null) {
            return;
        }
        mTextColorBuilder.setTextColor(color);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mTextColorBuilder != null && mTextColorBuilder.isTextStrokeColorEnable()) {
            super.setText(mTextColorBuilder.buildStrokeFontSpannable(text), BufferType.SPANNABLE);
        } else {
            super.setText(text, type);
        }
        if (mTextStateDelegate != null) {
            mTextStateDelegate.onTextSet(text);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onContentMetricsChanged();
        }
        if (mAutoFitTextDelegate != null
                && !isAdaptiveApplying()
                && !isMarqueeApplyingConfiguration()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        if (mAdaptiveTextDelegate != null && !isMarqueeApplyingConfiguration()) {
            mAdaptiveTextDelegate.onMaxLinesChanged(maxLines);
        }
        if (mAutoFitTextDelegate != null
                && !isAdaptiveApplying()
                && !isMarqueeApplyingConfiguration()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        if (mAdaptiveTextDelegate != null && !isMarqueeApplyingConfiguration()) {
            mAdaptiveTextDelegate.onMaxLinesChanged(lines);
        }
        if (mAutoFitTextDelegate != null
                && !isAdaptiveApplying()
                && !isMarqueeApplyingConfiguration()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setLineSpacing(float add, float multiplier) {
        super.setLineSpacing(add, multiplier);
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onLineSpacingChanged(add, multiplier);
        }
        if (mAutoFitTextDelegate != null && !isAdaptiveApplying()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        if (mMarqueeTextDelegate != null
                && mMarqueeTextDelegate.isEnabled()
                && !mMarqueeTextDelegate.isApplyingConfiguration()) {
            super.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            return;
        }
        super.setEllipsize(where);
    }

    private boolean isMarqueeApplyingConfiguration() {
        return mMarqueeTextDelegate != null
                && mMarqueeTextDelegate.isApplyingConfiguration();
    }

    private boolean isAdaptiveApplying() {
        return mAdaptiveTextDelegate != null && mAdaptiveTextDelegate.isApplying();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onContentMetricsChanged();
        }
        if (mAutoFitTextDelegate != null) {
            mAutoFitTextDelegate.onTextSizeChanged(unit, size);
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onContentMetricsChanged();
        }
        if (mAutoFitTextDelegate != null && !isAdaptiveApplying()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onContentMetricsChanged();
        }
        if (mAutoFitTextDelegate != null) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.beforeMeasure();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mAutoFitTextDelegate.fitAfterLayout()) {
            return;
        }
        mAdaptiveTextDelegate.afterLayout();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (mAutoFitTextDelegate != null && width != oldWidth) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    protected void onVisibilityChanged(android.view.View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mMarqueeTextDelegate != null) {
            mMarqueeTextDelegate.onVisibilityChanged(
                    isShown() && getWindowVisibility() == VISIBLE);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mMarqueeTextDelegate != null) {
            mMarqueeTextDelegate.onVisibilityChanged(
                    visibility == VISIBLE && isShown());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMarqueeTextDelegate.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mMarqueeTextDelegate.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTextStateDelegate != null) {
            mTextStateDelegate.refresh();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTextColorBuilder.onDraw(this, canvas, getPaint());
        super.onDraw(canvas);
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }

    @Override
    public TextColorBuilder getTextColorBuilder() {
        return mTextColorBuilder;
    }

    @Override
    public TextStateDelegate getTextStateDelegate() {
        return mTextStateDelegate;
    }

    public boolean isAdaptiveTextEnabled() {
        return mAdaptiveTextDelegate.isEnabled();
    }

    public void setAdaptiveTextEnabled(boolean enabled) {
        mAdaptiveTextDelegate.setEnabled(enabled);
    }

    public int getAdaptiveTextMode() {
        return mAdaptiveTextDelegate.getMode();
    }

    public void setAdaptiveTextMode(int mode) {
        mAdaptiveTextDelegate.setMode(mode);
    }

    public int getAdaptiveMinLines() {
        return mAdaptiveTextDelegate.getMinLines();
    }

    public void setAdaptiveMinLines(int minLines) {
        mAdaptiveTextDelegate.setMinLines(minLines);
    }

    public void setAdaptiveMinLineSpacingExtra(float minLineSpacingExtra) {
        mAdaptiveTextDelegate.setMinLineSpacingExtra(minLineSpacingExtra);
    }

    public void clearAdaptiveMinLineSpacingExtra() {
        mAdaptiveTextDelegate.clearMinLineSpacingExtra();
    }

    public boolean isAutoFitTextEnabled() {
        return mAutoFitTextDelegate.isEnabled();
    }

    public void setAutoFitTextEnabled(boolean enabled) {
        mAutoFitTextDelegate.setEnabled(enabled);
    }

    public boolean isEnableFit() {
        return isAutoFitTextEnabled();
    }

    public void enableFit() {
        setAutoFitTextEnabled(true);
    }

    public void setEnableFit(boolean enabled) {
        setAutoFitTextEnabled(enabled);
    }

    public float getAutoFitMinTextSize() {
        return mAutoFitTextDelegate.getMinTextSize();
    }

    public void setAutoFitMinTextSize(float sizeInSp) {
        setAutoFitMinTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp);
    }

    public void setAutoFitMinTextSize(int unit, float size) {
        mAutoFitTextDelegate.setMinTextSize(unit, size);
    }

    public float getMinTextSize() {
        return getAutoFitMinTextSize();
    }

    public void setMinTextSize(float sizeInSp) {
        setAutoFitMinTextSize(sizeInSp);
    }

    public void setMinTextSize(int unit, float size) {
        setAutoFitMinTextSize(unit, size);
    }

    public float getAutoFitMaxTextSize() {
        return mAutoFitTextDelegate.getMaxTextSize();
    }

    public void setAutoFitMaxTextSize(float sizeInSp) {
        setAutoFitMaxTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp);
    }

    public void setAutoFitMaxTextSize(int unit, float size) {
        mAutoFitTextDelegate.setMaxTextSize(unit, size);
    }

    public float getMaxTextSize() {
        return getAutoFitMaxTextSize();
    }

    public void setMaxTextSize(float sizeInSp) {
        setAutoFitMaxTextSize(sizeInSp);
    }

    public void setMaxTextSize(int unit, float size) {
        setAutoFitMaxTextSize(unit, size);
    }

    public float getAutoFitPrecision() {
        return mAutoFitTextDelegate.getPrecision();
    }

    public void setAutoFitPrecision(float precision) {
        mAutoFitTextDelegate.setPrecision(precision);
    }

    public float getPrecision() {
        return getAutoFitPrecision();
    }

    public void setPrecision(float precision) {
        setAutoFitPrecision(precision);
    }

    public boolean isMarqueeEnabled() {
        return mMarqueeTextDelegate.isEnabled();
    }

    public void setMarqueeEnabled(boolean enabled) {
        mMarqueeTextDelegate.setEnabled(enabled);
        mAdaptiveTextDelegate.onContentMetricsChanged();
        mAutoFitTextDelegate.onContentMetricsChanged();
    }

    public int getShapeMarqueeRepeatLimit() {
        return mMarqueeTextDelegate.getRepeatLimit();
    }

    public void setShapeMarqueeRepeatLimit(int repeatLimit) {
        mMarqueeTextDelegate.setRepeatLimit(repeatLimit);
    }

    public boolean isMarqueeRequireFullyVisible() {
        return mMarqueeTextDelegate.isRequireFullyVisible();
    }

    public void setMarqueeRequireFullyVisible(boolean requireFullyVisible) {
        mMarqueeTextDelegate.setRequireFullyVisible(requireFullyVisible);
    }
}
