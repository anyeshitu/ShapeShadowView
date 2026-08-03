package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allynav.shape.R;

/** Applies optional text content for standard Android drawable states. */
public final class TextStateDelegate {

    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    private final TextView mView;
    private CharSequence mDefaultText;
    private CharSequence mPressedText;
    private CharSequence mCheckedText;
    private CharSequence mDisabledText;
    private CharSequence mFocusedText;
    private CharSequence mSelectedText;
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
