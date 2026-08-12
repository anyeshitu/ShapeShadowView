package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.allynav.shape.R;

/**
 * 管理 ShapeTextView 的系统跑马灯配置和可见性状态。
 *
 * <p>Android 原生跑马灯要求 TextView 同时满足单行、MARQUEE 和 selected 状态。
 * 本委托负责应用这些条件，并在控件或父容器发生 GONE/VISIBLE、窗口切换、滚动和
 * 重新布局后，根据控件在屏幕中的实际可见区域维护 selected 状态。</p>
 *
 * <p>关闭功能时会恢复控件开启跑马灯前的行数、ellipsize 和 selected 配置，避免
 * 动态开关跑马灯后永久改变调用方原有的 TextView 行为。</p>
 */
public final class MarqueeTextDelegate {

    /** 跑马灯所属的 TextView，不持有 Context 等额外对象。 */
    private final TextView mTextView;
    /** 复用可见区域对象，避免布局或滚动过程中反复创建 Rect。 */
    private final Rect mVisibleRect = new Rect();
    /**
     * 延迟执行最终可见性检查。执行前先清除排队标记，允许检查过程中产生的新事件
     * 安排下一次刷新。
     */
    private final Runnable mRefreshRunnable = () -> {
        mRefreshPosted = false;
        refreshSelectedState();
    };
    /** 滚动后重新判断控件是否仍位于屏幕可见区域。 */
    private final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener =
            this::scheduleRefresh;
    /** 全局布局变化后重新判断尺寸、位置和父容器可见性。 */
    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener =
            this::scheduleRefresh;

    /** 是否启用 ShapeTextView 跑马灯能力。 */
    private boolean mEnabled;
    /** 系统跑马灯重复次数，-1 表示无限循环。 */
    private int mRepeatLimit;
    /** 是否要求控件完整显示在屏幕中才允许滚动。 */
    private boolean mRequireFullyVisible;

    /** 以下字段保存启用跑马灯前的 TextView 配置，用于动态关闭时完整恢复。 */
    private boolean mOriginalSingleLine;
    private int mOriginalMaxLines;
    private TextUtils.TruncateAt mOriginalEllipsize;
    private boolean mOriginalSelected;

    /** 是否已经保存原始配置并应用过跑马灯配置。 */
    private boolean mConfigurationApplied;
    /** 是否已经注册 ViewTreeObserver 监听器。 */
    private boolean mObserving;
    /** 防止应用配置时 ShapeTextView 的重写方法把 MARQUEE 再次拦截。 */
    private boolean mApplyingConfiguration;
    /** 是否已有延迟刷新任务，连续事件只保留一个任务。 */
    private boolean mRefreshPosted;
    /**
     * 控件从隐藏状态恢复后是否需要重启跑马灯。仅把 selected 重复设置为 true 不一定
     * 会重建系统 Marquee，因此恢复显示时需要主动产生一次 false -> true 状态变化。
     */
    private boolean mRestartPending = true;
    /** 保存注册监听器时使用的实例，确保从同一个 ViewTreeObserver 中移除监听器。 */
    private ViewTreeObserver mObserver;

    public MarqueeTextDelegate(TextView textView, TypedArray typedArray) {
        mTextView = textView;
        mEnabled = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_marqueeEnable, false);
        mRepeatLimit = typedArray.getInt(
                R.styleable.ShapeTextView_shape_marqueeRepeatLimit, -1);
        mRequireFullyVisible = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_marqueeRequireFullyVisible, true);
    }

    public void initialize() {
        // 构造完成后统一应用 XML 配置，避免初始化逻辑散落在 ShapeTextView 中。
        applyConfiguration();
    }

    public boolean isApplyingConfiguration() {
        return mApplyingConfiguration;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        applyConfiguration();
    }

    public int getRepeatLimit() {
        return mRepeatLimit;
    }

    public void setRepeatLimit(int repeatLimit) {
        mRepeatLimit = repeatLimit;
        if (mEnabled) {
            // 已启用时立即同步到系统 TextView，无需等待下一次布局。
            mTextView.setMarqueeRepeatLimit(repeatLimit);
        }
    }

    public boolean isRequireFullyVisible() {
        return mRequireFullyVisible;
    }

    public void setRequireFullyVisible(boolean requireFullyVisible) {
        mRequireFullyVisible = requireFullyVisible;
        // 可见性规则变化后重新计算 selected 状态。
        scheduleRefresh();
    }

    /**
     * 接收控件自身、祖先或窗口可见性变化后的初步结果。
     *
     * <p>这里不能只依赖传入结果直接启动跑马灯，因为 GONE -> VISIBLE 后布局和全局坐标
     * 可能尚未稳定；显示时统一延迟到 {@link #refreshSelectedState()} 做最终判断。</p>
     */
    public void onVisibilityChanged(boolean isVisible) {
        if (!mEnabled) {
            return;
        }
        if (!isVisible) {
            cancelRefresh();
            mTextView.setSelected(false);
            mRestartPending = true;
            return;
        }
        scheduleRefresh();
    }

    /**
     * 接收 View.onVisibilityAggregated 的最终聚合可见性。
     *
     * <p>该回调同时考虑控件本身及全部父容器，专门覆盖父容器由 GONE 恢复为 VISIBLE
     * 时子控件没有重新附着窗口的场景。</p>
     */
    public void onVisibilityAggregated(boolean isVisible) {
        if (!mEnabled) {
            return;
        }
        if (!isVisible) {
            cancelRefresh();
            mTextView.setSelected(false);
            mRestartPending = true;
            return;
        }
        // 父容器从 GONE 恢复后，以聚合可见状态重新启动跑马灯。
        scheduleRefresh();
    }

    /** 布局完成后重新检查尺寸和屏幕位置，覆盖控件动态显示后的首次有效布局。 */
    public void onLayout() {
        scheduleRefresh();
    }

    /** 控件附着窗口后注册位置监听，并安排首次可见性检查。 */
    public void onAttachedToWindow() {
        startObserving();
        scheduleRefresh();
    }

    /** 控件离开窗口时取消任务和监听器，防止持有失效的 ViewTreeObserver。 */
    public void onDetachedFromWindow() {
        cancelRefresh();
        stopObserving();
        if (mEnabled) {
            mTextView.setSelected(false);
            mRestartPending = true;
        }
    }

    /**
     * 根据开关应用或恢复 TextView 配置。
     *
     * <p>首次开启时只保存一次原始配置；后续重复设置属性不会覆盖备份。关闭后恢复
     * 备份并清除标记，下一次开启会重新读取调用方当时的最新配置。</p>
     */
    private void applyConfiguration() {
        cancelRefresh();
        mApplyingConfiguration = true;
        try {
            if (!mEnabled) {
                stopObserving();
                if (!mConfigurationApplied) {
                    return;
                }
                if (mOriginalSingleLine) {
                    mTextView.setSingleLine(true);
                } else {
                    mTextView.setSingleLine(false);
                    mTextView.setMaxLines(mOriginalMaxLines);
                }
                mTextView.setEllipsize(mOriginalEllipsize);
                mTextView.setSelected(mOriginalSelected);
                mConfigurationApplied = false;
                return;
            }
            if (!mConfigurationApplied) {
                // 必须在 setSingleLine 和 setEllipsize 之前保存调用方原始配置。
                mOriginalSingleLine = mTextView.getMaxLines() == 1;
                mOriginalMaxLines = mTextView.getMaxLines();
                mOriginalEllipsize = mTextView.getEllipsize();
                mOriginalSelected = mTextView.isSelected();
                mConfigurationApplied = true;
            }
            mTextView.setSingleLine(true);
            mTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mTextView.setMarqueeRepeatLimit(mRepeatLimit);
            mRestartPending = true;
            startObserving();
            scheduleRefresh();
        } finally {
            mApplyingConfiguration = false;
        }
    }

    /**
     * 合并短时间内连续发生的布局、滚动和可见性事件。
     *
     * <p>任务排队后不再反复 remove/post，否则持续布局的页面会一直重置 300ms，导致
     * selected 永远没有机会变成 true。</p>
     */
    private void scheduleRefresh() {
        if (!mEnabled || mRefreshPosted) {
            return;
        }
        // 合并连续布局和滚动事件，但不重复计时，避免刷新任务一直被推迟。
        mRefreshPosted = true;
        mTextView.postDelayed(mRefreshRunnable, 300L);
    }

    /** 取消尚未执行的刷新任务，并同步清除排队标记。 */
    private void cancelRefresh() {
        mTextView.removeCallbacks(mRefreshRunnable);
        mRefreshPosted = false;
    }

    /**
     * 在布局稳定后计算实际屏幕可见性，并据此启停系统跑马灯。
     *
     * <p>getGlobalVisibleRect 会同时考虑父容器裁剪和窗口边界；开启完整可见限制时，
     * 再比较可见矩形与控件尺寸。保留 1px 容差用于规避坐标取整误差。</p>
     */
    private void refreshSelectedState() {
        if (!mEnabled || !mTextView.isAttachedToWindow()
                || mTextView.getVisibility() != View.VISIBLE) {
            mTextView.setSelected(false);
            mRestartPending = true;
            return;
        }

        boolean visible = mTextView.getGlobalVisibleRect(mVisibleRect);
        if (visible && mRequireFullyVisible) {
            visible = mVisibleRect.width() >= mTextView.getWidth() - 1
                    && mVisibleRect.height() >= mTextView.getHeight() - 1;
        }
        if (!visible) {
            mTextView.setSelected(false);
            mRestartPending = true;
            return;
        }
        if (mRestartPending) {
            // 控件从隐藏恢复后强制产生状态变化，确保系统重新启动跑马灯。
            mTextView.setSelected(false);
            mTextView.setSelected(true);
            mRestartPending = false;
            return;
        }
        mTextView.setSelected(true);
    }

    /** 注册全局布局和滚动监听，同一次附着周期内只注册一次。 */
    private void startObserving() {
        if (!mEnabled || mObserving || !mTextView.isAttachedToWindow()) {
            return;
        }
        mObserver = mTextView.getViewTreeObserver();
        mObserver.addOnScrollChangedListener(mScrollChangedListener);
        mObserver.addOnGlobalLayoutListener(mGlobalLayoutListener);
        mObserving = true;
    }

    /** 从原 ViewTreeObserver 移除监听器，并释放观察状态。 */
    private void stopObserving() {
        if (!mObserving) {
            return;
        }
        if (mObserver != null && mObserver.isAlive()) {
            mObserver.removeOnScrollChangedListener(mScrollChangedListener);
            mObserver.removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        mObserver = null;
        mObserving = false;
    }
}
