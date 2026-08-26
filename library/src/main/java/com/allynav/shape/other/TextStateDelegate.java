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
    /**
     * 内部 setText 重入保护。
     *
     * <p>状态切换需要暂时把状态文本显示到目标控件，这次 setText 同样会触发
     * ShapeEditText.onTextChanged。标记可以让 onTextSet 和 onEditableTextChanged 都跳过
     * 这次内部写入，保证默认文本始终代表业务文本而不是状态展示文本。</p>
     */
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

    /**
     * 接收 TextView 内部 Editable 的实时变化，并把它作为下一次状态恢复的默认文本。
     *
     * <p>EditText 通过键盘输入、删除或粘贴时，通常只修改当前的 Editable，不会调用
     * 控件重写的 {@code setText()}。如果这里只依赖 {@link #onTextSet(CharSequence)}，
     * 委托就会一直保存初始化时的旧值，失焦触发 {@link #refresh()} 时便会把旧文本写回。
     * 该方法由 ShapeEditText 的 {@code onTextChanged()} 调用，因此覆盖所有由 Editable
     * 触发的内容变化，同时不主动刷新状态文本，避免用户编辑过程中被状态文本打断。</p>
     *
     * <p>状态文本切换本身也会调用 {@code setText()}，但 {@link #mApplyingState} 在这段
     * 调用链内保持为 {@code true}。忽略这类回调可以防止“按下文本”“禁用文本”等展示值
     * 覆盖真正的默认文本，也避免状态刷新出现递归。</p>
     *
     * @param text 当前控件内部的最新文本；允许为空，和 TextView 的文本契约保持一致
     */
    public void onEditableTextChanged(@Nullable CharSequence text) {
        if (!mApplyingState) {
            mDefaultText = text;
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
