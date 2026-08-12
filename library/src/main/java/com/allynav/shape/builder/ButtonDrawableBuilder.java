package com.allynav.shape.builder;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.CompoundButton;
import androidx.core.widget.CompoundButtonCompat;
import com.allynav.shape.R;
import com.allynav.shape.config.ICompoundButtonStyleable;

/**
 * CompoundButton 按钮图标构建器。
 *
 * <p>负责读取 CheckBox、RadioButton 的默认、按下、选中、禁用、聚焦和 selected
 * 状态图标，并组装成 {@link StateListDrawable}。未配置某个状态时会自然回退到默认
 * 图标，因此调用方可以只覆盖需要的状态。</p>
 *
 * <p>Java 动态设置完成后必须调用 {@link #intoButtonDrawable()}，才会把最新状态列表
 * 应用到目标控件。</p>
 */
public final class ButtonDrawableBuilder {

    /** 接收最终按钮图标状态列表的目标控件。 */
    private final CompoundButton mCompoundButton;

    /** 各 Android DrawableState 对应的图标；null 表示未配置该状态。 */
    private Drawable mButtonDrawable;
    private Drawable mButtonPressedDrawable;
    private Drawable mButtonCheckedDrawable;
    private Drawable mButtonDisabledDrawable;
    private Drawable mButtonFocusedDrawable;
    private Drawable mButtonSelectedDrawable;

    public ButtonDrawableBuilder(CompoundButton compoundButton, TypedArray typedArray, ICompoundButtonStyleable styleable) {
        mCompoundButton = compoundButton;

        // 占位资源表示继续使用控件当前的 android:button，而不是把图标清空。
        if (typedArray.hasValue(styleable.getButtonDrawableStyleable())) {
            if (typedArray.getResourceId(styleable.getButtonDrawableStyleable(), 0) != R.drawable.shape_view_placeholder) {
                mButtonDrawable = typedArray.getDrawable(styleable.getButtonDrawableStyleable());
            } else {
                mButtonDrawable = CompoundButtonCompat.getButtonDrawable(mCompoundButton);
            }
        } else {
            mButtonDrawable = null;
            mCompoundButton.setButtonDrawable(null);
        }

        if (typedArray.hasValue(styleable.getButtonPressedDrawableStyleable())) {
            mButtonPressedDrawable = typedArray.getDrawable(styleable.getButtonPressedDrawableStyleable());
        }

        if (typedArray.hasValue(styleable.getButtonCheckedDrawableStyleable())) {
            mButtonCheckedDrawable = typedArray.getDrawable(styleable.getButtonCheckedDrawableStyleable());
        }

        if (typedArray.hasValue(styleable.getButtonDisabledDrawableStyleable())) {
            mButtonDisabledDrawable = typedArray.getDrawable(styleable.getButtonDisabledDrawableStyleable());
        }

        if (typedArray.hasValue(styleable.getButtonFocusedDrawableStyleable())) {
            mButtonFocusedDrawable = typedArray.getDrawable(styleable.getButtonFocusedDrawableStyleable());
        }

        if (typedArray.hasValue(styleable.getButtonSelectedDrawableStyleable())) {
            mButtonSelectedDrawable = typedArray.getDrawable(styleable.getButtonSelectedDrawableStyleable());
        }
    }

    public ButtonDrawableBuilder setButtonDrawable(Drawable drawable) {
        if (mButtonPressedDrawable == mButtonDrawable) {
            mButtonPressedDrawable = drawable;
        }
        if (mButtonCheckedDrawable == mButtonDrawable) {
            mButtonCheckedDrawable = drawable;
        }
        if (mButtonDisabledDrawable == mButtonDrawable) {
            mButtonDisabledDrawable = drawable;
        }
        if (mButtonFocusedDrawable == mButtonDrawable) {
            mButtonFocusedDrawable = drawable;
        }
        if (mButtonSelectedDrawable == mButtonDrawable) {
            mButtonSelectedDrawable = drawable;
        }
        mButtonDrawable = drawable;
        return this;
    }

    public Drawable getButtonDrawable() {
        return mButtonDrawable;
    }

    public ButtonDrawableBuilder setButtonPressedDrawable(Drawable drawable) {
        mButtonPressedDrawable = drawable;
        return this;
    }

    public Drawable getButtonPressedDrawable() {
        return mButtonPressedDrawable;
    }

    public ButtonDrawableBuilder setButtonCheckedDrawable(Drawable drawable) {
        mButtonCheckedDrawable = drawable;
        return this;
    }

    public Drawable getButtonCheckedDrawable() {
        return mButtonCheckedDrawable;
    }

    public ButtonDrawableBuilder setButtonDisabledDrawable(Drawable drawable) {
        mButtonDisabledDrawable = drawable;
        return this;
    }

    public Drawable getButtonDisabledDrawable() {
        return mButtonDisabledDrawable;
    }

    public ButtonDrawableBuilder setButtonFocusedDrawable(Drawable drawable) {
        mButtonFocusedDrawable = drawable;
        return this;
    }

    public Drawable getButtonFocusedDrawable() {
        return mButtonFocusedDrawable;
    }

    public ButtonDrawableBuilder setButtonSelectedDrawable(Drawable drawable) {
        mButtonSelectedDrawable = drawable;
        return this;
    }

    public Drawable getButtonSelectedDrawable() {
        return mButtonSelectedDrawable;
    }

    public void intoButtonDrawable() {
        if (mButtonDrawable == null) {
            return;
        }

        if (mButtonPressedDrawable == null &&
                mButtonCheckedDrawable == null &&
                mButtonDisabledDrawable == null &&
                mButtonFocusedDrawable == null &&
                mButtonSelectedDrawable == null) {
            mCompoundButton.setButtonDrawable(mButtonDrawable);
            return;
        }

        // 特殊状态从高优先级到低优先级添加，默认项始终放在最后兜底。
        StateListDrawable drawable = new StateListDrawable();
        if (mButtonPressedDrawable != null) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, mButtonPressedDrawable);
        }
        if (mButtonCheckedDrawable != null) {
            drawable.addState(new int[]{android.R.attr.state_checked}, mButtonCheckedDrawable);
        }
        if (mButtonDisabledDrawable != null) {
            drawable.addState(new int[]{-android.R.attr.state_enabled}, mButtonDisabledDrawable);
        }
        if (mButtonFocusedDrawable != null) {
            drawable.addState(new int[]{android.R.attr.state_focused}, mButtonFocusedDrawable);
        }
        if (mButtonSelectedDrawable != null) {
            drawable.addState(new int[]{android.R.attr.state_selected}, mButtonSelectedDrawable);
        }
        drawable.addState(new int[]{}, mButtonDrawable);
        mCompoundButton.setButtonDrawable(drawable);
    }
}
