package com.allynav.shape.other;

import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

/**
 * 管理 ShapeEditText 的可选输入框增强行为。
 *
 * <p>开启后提供三项能力：输入法点击完成/前往按钮或硬件回车时收起键盘；获得焦点时
 * 延迟全选文本，方便用户直接覆盖输入；失去焦点时隐藏光标并把选择范围收回到起点。
 * 所有行为都由控件生命周期回调驱动，不通过 {@code setOnEditorActionListener} 或
 * {@code setOnFocusChangeListener} 注册监听器，因此业务代码后续设置监听器不会覆盖
 * 组件自己的行为。</p>
 *
 * <p>委托默认关闭。关闭时不会修改输入法选项、光标可见性、选择范围或键盘状态；运行时
 * 关闭功能时也会恢复创建委托时保存的原始输入法选项和光标状态。</p>
 */
public final class CloseKeyboardEditTextDelegate {

    /** 被增强的输入框。 */
    private final EditText mEditText;
    /** 创建委托时的输入法选项，关闭功能后恢复。 */
    private final int mOriginalImeOptions;
    /** 创建委托时的光标可见性，关闭功能后恢复。 */
    private final boolean mOriginalCursorVisible;
    /** 聚焦后延迟执行全选，等待系统完成焦点和输入连接切换。 */
    private final Runnable mSelectAllRunnable;

    /** 是否启用收键盘、聚焦全选和失焦隐藏光标能力。 */
    private boolean mEnabled;

    public CloseKeyboardEditTextDelegate(@NonNull EditText editText, boolean enabled) {
        mEditText = editText;
        mOriginalImeOptions = editText.getImeOptions();
        mOriginalCursorVisible = editText.isCursorVisible();
        mSelectAllRunnable = () -> {
            // 输入框可能在 post 执行前已经失焦或被关闭，必须再次判断，避免错误全选新控件文本。
            if (mEnabled && mEditText.isFocused()) {
                mEditText.selectAll();
            }
        };
        mEnabled = enabled;
        if (mEnabled) {
            applyEnabledState();
        }
    }

    /** 返回当前是否启用输入框增强行为。 */
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * 开启或关闭输入框增强行为。
     *
     * <p>运行时开启会把输入法动作设置为 DONE，并立即按当前焦点状态应用光标和选中文本
     * 规则；关闭会取消待执行的全选任务并恢复构造时的输入法选项与光标状态。</p>
     */
    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (mEnabled) {
            applyEnabledState();
        } else {
            mEditText.removeCallbacks(mSelectAllRunnable);
            mEditText.setImeOptions(mOriginalImeOptions);
            mEditText.setCursorVisible(mOriginalCursorVisible);
        }
    }

    /**
     * 接收输入法动作回调。
     *
     * <p>不同输入法对“完成”按钮返回的 action 可能不同，因此同时兼容 DONE、GO、NONE
     * 和 UNSPECIFIED。真正的默认处理仍先交给父类，确保 EditText 原有行为和业务监听器
     * 正常执行；随后再执行组件自己的收键盘逻辑。</p>
     */
    public void onEditorAction(int actionCode) {
        if (!mEnabled) {
            return;
        }
        int normalizedAction = actionCode & EditorInfo.IME_MASK_ACTION;
        boolean isCompletionAction = normalizedAction == EditorInfo.IME_ACTION_DONE
                || normalizedAction == EditorInfo.IME_ACTION_GO
                || normalizedAction == EditorInfo.IME_ACTION_UNSPECIFIED
                || normalizedAction == EditorInfo.IME_ACTION_NONE;
        if (isCompletionAction) {
            closeKeyboard();
        }
    }

    /**
     * 接收硬件键盘或输入法转发的回车抬起事件。
     *
     * <p>部分输入法不会进入带 actionCode 的 onEditorAction，而是把完成按钮转成回车
     * 事件；这里补充处理普通 Enter 和数字键盘 Enter，避免设备差异导致功能失效。</p>
     */
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (!mEnabled || event.isCanceled() || !event.hasNoModifiers()) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            closeKeyboard();
            return true;
        }
        return false;
    }

    /** 接收输入框焦点变化，应用聚焦全选或失焦隐藏光标规则。 */
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (!mEnabled) {
            return;
        }
        if (focused) {
            mEditText.setCursorVisible(true);
            mEditText.removeCallbacks(mSelectAllRunnable);
            // 等待系统完成焦点分发后再全选，否则部分 ROM 会在输入连接更新时清掉选择范围。
            mEditText.post(mSelectAllRunnable);
        } else {
            mEditText.removeCallbacks(mSelectAllRunnable);
            mEditText.setCursorVisible(false);
            // 失焦后不保留选区，避免重新聚焦时出现残留高亮或选择句柄。
            mEditText.setSelection(0, 0);
        }
    }

    /**
     * 处理控件离开窗口的生命周期事件。
     *
     * <p>聚焦时的全选是通过 {@link View#post(Runnable)} 延迟执行的。如果控件在任务执行前
     * 被从窗口移除，例如列表复用、Fragment 销毁或页面切换，必须主动移除这个任务，避免
     * 延迟回调继续访问已经失效的控件状态。</p>
     */
    public void onDetachedFromWindow() {
        mEditText.removeCallbacks(mSelectAllRunnable);
    }

    /**
     * 主动收起键盘并转移焦点。
     *
     * <p>优先请求可触摸聚焦的父 View 接管焦点；如果父布局不能接管，则清除当前焦点。
     * 最后通过 InputMethodManager 隐藏软键盘，覆盖“清除焦点但键盘仍短暂保留”的设备差异。</p>
     */
    public void closeKeyboard() {
        View parent = mEditText.getParent() instanceof View
                ? (View) mEditText.getParent() : null;
        boolean focusTransferred = parent != null
                && parent.isFocusableInTouchMode()
                && parent.requestFocus();
        if (!focusTransferred) {
            // 父布局可能虽然支持触摸聚焦，但当前状态拒绝接管焦点，此时仍要清除输入框焦点。
            mEditText.clearFocus();
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mEditText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    /** 应用开启状态下的输入法选项和当前焦点规则。 */
    private void applyEnabledState() {
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        onFocusChanged(mEditText.isFocused(), 0, null);
    }
}
