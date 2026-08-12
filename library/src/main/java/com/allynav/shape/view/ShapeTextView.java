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
 * ShapeView 风格的增强 TextView。
 *
 * <p>除 Shape 背景、状态文字色、渐变、描边和状态文本外，还集成三项默认关闭的文字
 * 能力：固定高度自适应、二分查找自动字号和系统跑马灯。三项能力通过独立委托实现，
 * 避免继承多个第三方 TextView 时产生测量与生命周期冲突。</p>
 *
 * <p>执行顺序为：测量前恢复自适应基准值；正常测量/布局；布局后先自动调整字号；字号
 * 稳定后再压缩行间距或减少行数。跑马灯启用时强制单行，并根据控件及父容器的最终
 * 屏幕可见性维护 selected 状态。</p>
 */
public class ShapeTextView extends AppCompatTextView implements
        IGetShapeDrawableBuilder, IGetTextColorBuilder, IGetTextStateDelegate {

    /** 固定高度自适应模式常量，值与 attrs.xml 中枚举保持一致。 */
    public static final int ADAPTIVE_MODE_REDUCE_LINES =
            AdaptiveTextDelegate.MODE_REDUCE_LINES;
    public static final int ADAPTIVE_MODE_REDUCE_LINE_SPACING =
            AdaptiveTextDelegate.MODE_REDUCE_LINE_SPACING;
    public static final int ADAPTIVE_MODE_REDUCE_LINE_SPACING_THEN_LINES =
            AdaptiveTextDelegate.MODE_REDUCE_LINE_SPACING_THEN_LINES;

    private static final ShapeTextViewStyleable STYLEABLE = new ShapeTextViewStyleable();

    /** 外观、状态文本和三项文字行为分别由独立对象管理。 */
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

        // 所有委托先复制 XML 配置，再统一回收 TypedArray 并应用初始外观。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeTextView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        mTextColorBuilder = new TextColorBuilder(this, typedArray, STYLEABLE);
        mTextStateDelegate = new TextStateDelegate(this, attrs);
        mAdaptiveTextDelegate = new AdaptiveTextDelegate(this, typedArray);
        mAutoFitTextDelegate = new AutoFitTextDelegate(this, typedArray);
        mMarqueeTextDelegate = new MarqueeTextDelegate(this, typedArray);
        // 跑马灯会修改 singleLine/ellipsize，必须在保存完其他文字基准配置后初始化。
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
        // 描边开启时把文本包装成 Span；状态委托只记录调用方原始文本。
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
        // 文本变化会影响换行、高度和字号，通知两个自适应委托重新计算。
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
        // 外部修改 maxLines 时更新基准；委托内部临时修改由 applying 标记过滤。
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
        // 行距既影响固定高度适配，也影响自动字号的 StaticLayout 测量。
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.onLineSpacingChanged(add, multiplier);
        }
        if (mAutoFitTextDelegate != null && !isAdaptiveApplying()) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        // 跑马灯开启期间始终保持 MARQUEE，防止样式或业务代码改回 END。
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
        // 保存调用方字号作为自动字号关闭后的恢复值，并触发排版重新计算。
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
        // 固定高度适配上轮产生的临时行距/行数必须在系统测量前恢复。
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.beforeMeasure();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 动态显示后的首次布局完成时，跑马灯才能取得有效尺寸和屏幕坐标。
        mMarqueeTextDelegate.onLayout();
        // 自动字号发生变化会请求下一轮布局，本轮不再继续压缩行距或行数。
        if (mAutoFitTextDelegate.fitAfterLayout()) {
            return;
        }
        mAdaptiveTextDelegate.afterLayout();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // 只有宽度变化会改变文字换行和单行可用空间。
        if (mAutoFitTextDelegate != null && width != oldWidth) {
            mAutoFitTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    protected void onVisibilityChanged(android.view.View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mMarqueeTextDelegate != null) {
            // 普通可见性回调可能早于父容器布局完成，仅用于停止或安排延迟刷新。
            mMarqueeTextDelegate.onVisibilityChanged(
                    isShown() && getWindowVisibility() == VISIBLE);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mMarqueeTextDelegate != null) {
            // Activity、Dialog 或窗口前后台切换时同步停止或恢复跑马灯。
            mMarqueeTextDelegate.onVisibilityChanged(
                    visibility == VISIBLE && isShown());
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (mMarqueeTextDelegate != null) {
            // 聚合状态包含所有父容器，负责处理父布局 GONE -> VISIBLE 的最终结果。
            mMarqueeTextDelegate.onVisibilityAggregated(isVisible);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 附着后开始监听全局布局和滚动，首次刷新会延迟到坐标稳定后执行。
        mMarqueeTextDelegate.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        // 必须先取消委托中的任务和监听器，再执行父类的窗口分离逻辑。
        mMarqueeTextDelegate.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        // pressed/checked/disabled 等状态变化时同步可选状态文本。
        if (mTextStateDelegate != null) {
            mTextStateDelegate.refresh();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 渐变依赖最终尺寸，先更新 Paint Shader，再执行 TextView 原始绘制。
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
        // 跑马灯切换会改变单行和 maxLines，两个自适应委托需要重新建立测量基准。
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
