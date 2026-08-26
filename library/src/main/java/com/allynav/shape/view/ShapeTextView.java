package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.widget.AppCompatTextView;
import com.allynav.shape.R;
import com.allynav.shape.builder.CompoundDrawableTintBuilder;
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
 * 屏幕可见性维护仅供系统 Marquee 使用的内部 selected 状态；业务 selected 状态仍由调用方
 * 的 setSelected() 独立控制。</p>
 */
public class ShapeTextView extends AppCompatTextView implements
        IGetShapeDrawableBuilder, IGetTextColorBuilder, IGetTextStateDelegate,
        MarqueeTextDelegate.SelectionHost {

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
    private final CompoundDrawableTintBuilder mCompoundDrawableTintBuilder;
    private final AdaptiveTextDelegate mAdaptiveTextDelegate;
    private final AutoFitTextDelegate mAutoFitTextDelegate;
    private final MarqueeTextDelegate mMarqueeTextDelegate;
    /** 业务代码设置的真实 selected 状态，不受系统跑马灯内部状态影响。 */
    private boolean mSemanticSelected;
    /** 系统 Marquee 为启动滚动临时使用的 selected 状态。 */
    private boolean mMarqueeSelected;
    /**
     * 是否允许父 LinearLayout 使用当前文字基线参与横向子控件定位。
     *
     * <p>AutoFit 可能在控件由 GONE 恢复为 VISIBLE 时选出不同字号。若横向 LinearLayout
     * 使用 TextView 的 baseline 对齐子控件，字号对应的 ascent/descent 变化会使整个固定
     * 高度按钮向下移动，甚至把底部描边移出父容器。默认在 AutoFit 开启时关闭基线输出，
     * 普通 ShapeTextView 则保留系统行为；调用方可通过 XML 或 Java 显式覆盖。</p>
     */
    private boolean mTextBaselineEnabled;
    /**
     * 调用方是否通过 XML 或 Java 明确指定过基线策略。
     *
     * <p>未指定时，基线开关会跟随 AutoFit 动态变化；一旦显式指定，就不再由 AutoFit
     * 覆盖，保证调用方对表单基线布局拥有最终控制权。</p>
     */
    private boolean mTextBaselineConfigured;

    public ShapeTextView(Context context) {
        this(context, null);
    }

    public ShapeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public ShapeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 保存 XML 或父类初始化阶段设置的真实选中状态，后续与 Marquee 状态分开维护。
        mSemanticSelected = super.isSelected();

        // 所有委托先复制 XML 配置，再统一回收 TypedArray 并应用初始外观。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeTextView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        mTextColorBuilder = new TextColorBuilder(this, typedArray, STYLEABLE);
        mTextStateDelegate = new TextStateDelegate(this, attrs);
        mCompoundDrawableTintBuilder = new CompoundDrawableTintBuilder(this, attrs);
        mAdaptiveTextDelegate = new AdaptiveTextDelegate(this, typedArray);
        mAutoFitTextDelegate = new AutoFitTextDelegate(this, typedArray);
        mMarqueeTextDelegate = new MarqueeTextDelegate(this, typedArray, this);
        // 未显式配置时仅对 AutoFit 控件关闭基线。这样按钮栏无需逐个给父 LinearLayout
        // 添加 baselineAligned=false，同时不改变普通文本在表单中的原生基线对齐能力。
        mTextBaselineConfigured = typedArray.hasValue(
                R.styleable.ShapeTextView_shape_textBaselineEnabled);
        mTextBaselineEnabled = mTextBaselineConfigured
                ? typedArray.getBoolean(
                        R.styleable.ShapeTextView_shape_textBaselineEnabled, true)
                : !mAutoFitTextDelegate.isEnabled();
        // 跑马灯会修改 singleLine/ellipsize，必须在保存完其他文字基准配置后初始化。
        mMarqueeTextDelegate.initialize();
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
        mTextColorBuilder.intoTextColor();
        mTextStateDelegate.refresh();
        if (mCompoundDrawableTintBuilder.hasCustomTint()) {
            // XML 至少配置一个 tint 状态时才接管 compound drawable，未配置时保持原生行为。
            mCompoundDrawableTintBuilder.intoTint();
        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mTextColorBuilder == null) {
            return;
        }
        mTextColorBuilder.setTextColor(color);
    }

    /**
     * 设置业务层的真实选中状态。
     *
     * <p>系统跑马灯也需要 selected=true，但这个内部状态不应让
     * {@code shape_textSelectedColor} 等业务状态属性提前生效。因此这里记录业务状态，
     * 再把“业务选中或跑马灯选中”的结果交给 View 内部，以保留 Android Marquee 的启动条件。</p>
     */
    @Override
    public void setSelected(boolean selected) {
        mSemanticSelected = selected;
        super.setSelected(selected || mMarqueeSelected);
        // 当 Marquee 已经使底层 selected=true 时，业务状态变化不会触发 View 的内部标志变化，
        // 仍需主动刷新 drawable state，才能让 selected 颜色/背景立即跟随业务状态变化。
        refreshDrawableState();
    }

    /**
     * 过滤仅由跑马灯产生的 selected drawable state。
     *
     * <p>TextView.isSelected() 仍然可以看到内部 selected=true，所以原生 Marquee 能继续
     * 滚动；这里仅从状态数组中移除该内部状态，避免文本颜色、Shape 背景、复合图片 tint
     * 和状态文本把“正在滚动”误判为“业务选中”。真实业务选中时 mSemanticSelected=true，
     * selected state 会原样保留。</p>
     */
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace);
        if (!mMarqueeSelected || mSemanticSelected) {
            return drawableState;
        }

        // 原数组可能带有供子类追加状态的尾部空间，压缩时保留数组长度并把空位放到末尾。
        int writeIndex = 0;
        for (int state : drawableState) {
            if (state != android.R.attr.state_selected) {
                drawableState[writeIndex++] = state;
            }
        }
        while (writeIndex < drawableState.length) {
            drawableState[writeIndex++] = 0;
        }
        return drawableState;
    }

    /** 返回业务层的真实 selected 状态，供跑马灯委托保存原始配置。 */
    @Override
    public boolean isSemanticSelected() {
        return mSemanticSelected;
    }

    /** 恢复业务层 selected 状态，统一经过 ShapeTextView 的状态刷新逻辑。 */
    @Override
    public void setSemanticSelected(boolean selected) {
        setSelected(selected);
    }

    /** 返回系统跑马灯当前是否使用了内部 selected 状态。 */
    @Override
    public boolean isMarqueeSelected() {
        return mMarqueeSelected;
    }

    /**
     * 设置系统跑马灯内部 selected 状态。
     *
     * <p>底层 View 仍接收业务状态与内部状态的并集，确保 TextView 的 Marquee 逻辑不变；
     * onCreateDrawableState() 会根据 mSemanticSelected 决定是否对外暴露 selected。</p>
     */
    @Override
    public void setMarqueeSelected(boolean selected) {
        mMarqueeSelected = selected;
        super.setSelected(mSemanticSelected || selected);
        refreshDrawableState();
    }

    /**
     * 强制重建系统 Marquee 的 selected 触发沿。
     *
     * <p>业务选中状态可能本来就是 true，此时不能通过普通的内部状态 setter 观察到
     * false -> true。这里暂时直接清除底层 View 的 selected，再恢复业务与 Marquee 状态
     * 的并集；mSemanticSelected 始终不变，因此调用方的真实选中意图不会丢失。</p>
     */
    @Override
    public void restartMarqueeSelection() {
        mMarqueeSelected = false;
        super.setSelected(false);
        refreshDrawableState();
        mMarqueeSelected = true;
        super.setSelected(true);
        refreshDrawableState();
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
        if (mMarqueeTextDelegate != null && !isMarqueeApplyingConfiguration()) {
            // 新文本会替换 TextView 内部 Layout；即使 selected 仍为 true，也需要重启滚动。
            mMarqueeTextDelegate.onContentMetricsChanged();
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
        if (mMarqueeTextDelegate != null && !isMarqueeApplyingConfiguration()) {
            // AutoFit 通过本方法应用最终字号，字号变化后系统 Marquee 必须重新建立。
            mMarqueeTextDelegate.onContentMetricsChanged();
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
        if (mMarqueeTextDelegate != null) {
            mMarqueeTextDelegate.onContentMetricsChanged();
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
        if (mMarqueeTextDelegate != null) {
            mMarqueeTextDelegate.onContentMetricsChanged();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 固定高度适配上轮产生的临时行距/行数必须在系统测量前恢复。
        if (mAdaptiveTextDelegate != null) {
            mAdaptiveTextDelegate.beforeMeasure();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mAutoFitTextDelegate != null
                && mAutoFitTextDelegate.fitAfterMeasure(getMeasuredWidth())) {
            // AutoFit 已根据第一次测得的正文宽度选出最终字号。必须在本次测量调用内立即
            // 复测，让父容器第一次布局时就获得最终高度和 baseline；否则横向 LinearLayout
            // 会在下一轮布局中按新基线移动子控件，表现为控件显示后突然向下偏移。
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 返回给父布局的文字基线位置。
     *
     * <p>返回 -1 是 Android View 约定的“该控件没有可用于对齐的 baseline”。横向
     * LinearLayout 收到 -1 后会按自身 gravity 正常放置固定高度按钮，不会再根据 AutoFit
     * 前后变化的字体基线修正子控件纵向坐标。该方法只影响父布局定位，不影响控件内部
     * gravity、文字绘制位置、跑马灯或背景描边。</p>
     */
    @Override
    public int getBaseline() {
        return mTextBaselineEnabled ? super.getBaseline() : -1;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 动态显示后的首次布局完成时，跑马灯才能取得有效尺寸和屏幕坐标。
        mMarqueeTextDelegate.onLayout();
        // AutoFit 已在 onMeasure 内完成并复测，此处只处理依赖最终 Layout 的高度适配。
        mAdaptiveTextDelegate.afterLayout();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // AutoFit 在每次 onMeasure 中直接比较正文可用宽度，无需在布局结束后再请求一轮
        // 测量；这里仅通知跑马灯内部 Layout 已随最终宽高变化，需要按稳定尺寸重启。
        if (mMarqueeTextDelegate != null && (width != oldWidth || height != oldHeight)) {
            // GONE -> VISIBLE 或父容器重新排版后，最终宽高变化也会使内部 Layout 失效。
            mMarqueeTextDelegate.onContentMetricsChanged();
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
        if (mCompoundDrawableTintBuilder != null) {
            mCompoundDrawableTintBuilder.onDrawableStateChanged(getDrawableState());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 先让跑马灯委托基于本轮最新 Layout 重建 selected 状态，再进入 TextView 原生绘制；
        // Android TextView 会在父类 onDraw() 中真正启动和绘制 Marquee。
        mMarqueeTextDelegate.onDraw();
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

    /** 返回 drawableStart/Top/End/Bottom 共用的状态 tint 构建器。 */
    public CompoundDrawableTintBuilder getCompoundDrawableTintBuilder() {
        return mCompoundDrawableTintBuilder;
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
        if (!mTextBaselineConfigured) {
            // 自动策略下，运行时开启 AutoFit 也要同步退出父容器基线对齐；关闭后恢复
            // TextView 原生 baseline，行为与 XML 初始化保持一致。
            updateTextBaselineEnabled(!enabled);
        }
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

    /** 返回当前是否允许父布局使用 ShapeTextView 的文字基线。 */
    public boolean isTextBaselineEnabled() {
        return mTextBaselineEnabled;
    }

    /**
     * 设置是否向父布局提供文字基线。
     *
     * <p>状态变化后调用 requestLayout，使已经完成布局的父 LinearLayout 立即重新计算
     * 子控件纵向位置；不需要调用方手动操作父容器。</p>
     */
    public void setTextBaselineEnabled(boolean enabled) {
        mTextBaselineConfigured = true;
        updateTextBaselineEnabled(enabled);
    }

    /** 应用基线状态并仅在状态真正变化时请求父容器重新布局。 */
    private void updateTextBaselineEnabled(boolean enabled) {
        if (mTextBaselineEnabled != enabled) {
            mTextBaselineEnabled = enabled;
            requestLayout();
        }
    }
}
