package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allynav.shape.R;

/**
 * 根据 Android DrawableState 切换 TextView 文本内容。
 *
 * <p>支持默认、按下、checked、禁用、聚焦和 selected 文本。未配置某个状态时回退到
 * 默认文本。通过 Java 调用 setText 会更新默认文本，内部切换状态文本时使用 applying
 * 标记防止递归覆盖默认值。</p>
 */
public final class TextStateDelegate {

    /** 状态解析顺序；禁用状态在 resolveStateText 中优先匹配。 */
    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    /** 目标控件、默认文本和各状态可选文本。 */
    private final TextView mView;
    private CharSequence mDefaultText;
    private CharSequence mPressedText;
    private CharSequence mCheckedText;
    private CharSequence mDisabledText;
    private CharSequence mFocusedText;
    private CharSequence mSelectedText;
    /** 内部 setText 重入保护，避免状态文本被记录成新的默认文本。 */
    private boolean mApplyingState;

    public TextStateDelegate(@NonNull TextView view, @Nullable AttributeSet attrs) {
        mView = view;
        mDefaultText = view.getText();
        if (attrs == null) {
            return;
        }
        TypedArray array = view.getContext().obtainStyledAttributes(attrs, R.styleable.ShapeTextState);
        mPressedText = array.getText(R.styleable.ShapeTextState_shape_textPressed);
        mCheckedText = array.getText(R.styleable.ShapeTextState_shape_textChecked);
        mDisabledText = array.getText(R.styleable.ShapeTextState_shape_textDisabled);
        mFocusedText = array.getText(R.styleable.ShapeTextState_shape_textFocused);
        mSelectedText = array.getText(R.styleable.ShapeTextState_shape_textSelected);
        array.recycle();
    }

    public void onTextSet(@Nullable CharSequence text) {
        if (!mApplyingState) {
            mDefaultText = text;
            refresh();
        }
    }

    public void refresh() {
        // 只在目标文本变化时调用 setText，减少无效布局和 TextWatcher 回调。
        CharSequence stateText = resolveStateText(mView.getDrawableState());
        CharSequence targetText = stateText != null ? stateText : mDefaultText;
        if (TextUtils.equals(mView.getText(), targetText)) {
            return;
        }
        mApplyingState = true;
        try {
            mView.setText(targetText);
        } finally {
            mApplyingState = false;
        }
    }

    @Nullable
    private CharSequence resolveStateText(int[] stateSet) {
        // disabled 优先，随后处理 pressed、checked、focused 和 selected。
        if (mDisabledText != null && StateSet.stateSetMatches(STATE_DISABLED, stateSet)) {
            return mDisabledText;
        }
        if (mPressedText != null && StateSet.stateSetMatches(STATE_PRESSED, stateSet)) {
            return mPressedText;
        }
        if (mCheckedText != null && StateSet.stateSetMatches(STATE_CHECKED, stateSet)) {
            return mCheckedText;
        }
        if (mFocusedText != null && StateSet.stateSetMatches(STATE_FOCUSED, stateSet)) {
            return mFocusedText;
        }
        if (mSelectedText != null && StateSet.stateSetMatches(STATE_SELECTED, stateSet)) {
            return mSelectedText;
        }
        return null;
    }

    public TextStateDelegate setPressedText(@Nullable CharSequence text) {
        mPressedText = text;
        refresh();
        return this;
    }

    public TextStateDelegate setCheckedText(@Nullable CharSequence text) {
        mCheckedText = text;
        refresh();
        return this;
    }

    public TextStateDelegate setDisabledText(@Nullable CharSequence text) {
        mDisabledText = text;
        refresh();
        return this;
    }

    public TextStateDelegate setFocusedText(@Nullable CharSequence text) {
        mFocusedText = text;
        refresh();
        return this;
    }

    public TextStateDelegate setSelectedText(@Nullable CharSequence text) {
        mSelectedText = text;
        refresh();
        return this;
    }
}
