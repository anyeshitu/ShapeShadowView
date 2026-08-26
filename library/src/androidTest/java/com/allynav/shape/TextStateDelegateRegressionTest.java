package com.allynav.shape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.text.Editable;
import android.view.inputmethod.EditorInfo;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.allynav.shape.view.ShapeEditText;
import com.allynav.shape.view.ShapeTextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TextStateDelegate 与 ShapeEditText 的回归测试。
 *
 * <p>这些用例使用真实 Android 控件，而不是普通 JVM 中无法完整模拟 TextView 的假对象，
 * 用 Editable.replace 复现键盘输入、删除和粘贴最终都会走到的 onTextChanged 回调。每次
 * 修改文本后再主动刷新状态，等价于失焦、按下或禁用等 DrawableState 变化触发的刷新，
 * 可以直接验证委托是否保存了最新业务文本。</p>
 */
@RunWith(AndroidJUnit4.class)
public final class TextStateDelegateRegressionTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
    }

    /** 验证初始值 2.0 被用户改为 0.8 后，失焦刷新不会恢复旧值。 */
    @Test
    public void userInputSurvivesFocusLossRefresh() {
        ShapeEditText editText = newShapeEditText();
        editText.setText("2.0");
        replaceEditableText(editText, "0.8");

        editText.clearFocus();
        editText.getTextStateDelegate().refresh();

        assertEquals("0.8", editText.getText().toString());
    }

    /** 验证用户删除全部文本后，状态刷新仍保留空文本，而不是恢复初始化值。 */
    @Test
    public void deletedTextSurvivesStateRefresh() {
        ShapeEditText editText = newShapeEditText();
        editText.setText("2.0");
        Editable editable = editText.getText();
        editable.delete(0, editable.length());

        editText.getTextStateDelegate().refresh();

        assertEquals("", editText.getText().toString());
    }

    /** 验证通过 Editable.replace 模拟粘贴后的文本，失焦刷新仍保留粘贴结果。 */
    @Test
    public void pastedTextSurvivesFocusLossRefresh() {
        ShapeEditText editText = newShapeEditText();
        editText.setText("2.0");
        replaceEditableText(editText, "粘贴后的新文本");

        editText.clearFocus();
        editText.getTextStateDelegate().refresh();

        assertEquals("粘贴后的新文本", editText.getText().toString());
    }

    /** 验证 ShapeTextView 原有的按下状态文本仍能在状态进入和退出时正确切换。 */
    @Test
    public void shapeTextViewStateTextRemainsFunctional() {
        ShapeTextView textView = new ShapeTextView(mContext);
        textView.setText("普通文本");
        textView.getTextStateDelegate().setPressedText("按下文本");

        textView.setPressed(true);
        assertEquals("按下文本", textView.getText().toString());

        textView.setPressed(false);
        assertEquals("普通文本", textView.getText().toString());
    }

    /** 验证 ShapeEditText 的禁用状态文本不会污染用户文本，恢复可用后仍回到默认值。 */
    @Test
    public void shapeEditTextStateTextDoesNotPolluteDefaultText() {
        ShapeEditText editText = newShapeEditText();
        editText.setText("2.0");
        editText.getTextStateDelegate().setDisabledText("输入框不可用");

        editText.setEnabled(false);
        assertEquals("输入框不可用", editText.getText().toString());

        editText.setEnabled(true);
        assertEquals("2.0", editText.getText().toString());
    }

    /** 验证状态文本已存在时，业务再次 setText 也会替换默认文本并在恢复时生效。 */
    @Test
    public void programmaticSetTextUpdatesDefaultText() {
        ShapeEditText editText = newShapeEditText();
        editText.setText("2.0");
        editText.getTextStateDelegate().setDisabledText("输入框不可用");
        editText.setText("0.8");

        editText.setEnabled(false);
        editText.setEnabled(true);

        assertEquals("0.8", editText.getText().toString());
    }

    /** 验证 closeKeyboardEnable 的公开开关与原有输入法动作配置仍然存在。 */
    @Test
    public void closeKeyboardFeatureRemainsEnabledByPublicApi() {
        ShapeEditText editText = newShapeEditText();
        editText.setCloseKeyboardEnabled(true);

        assertTrue(editText.isCloseKeyboardEnabled());
        assertEquals(EditorInfo.IME_ACTION_DONE,
                editText.getImeOptions() & EditorInfo.IME_MASK_ACTION);

        editText.setCloseKeyboardEnabled(false);
        assertFalse(editText.isCloseKeyboardEnabled());
    }

    private ShapeEditText newShapeEditText() {
        return new ShapeEditText(mContext);
    }

    /**
     * 使用 Editable 原地替换文本，模拟输入法在 EditText 中执行的编辑操作。
     * 该路径不会调用 ShapeEditText.setText，因此正好覆盖本次修复的缺口。
     */
    private static void replaceEditableText(ShapeEditText editText, String text) {
        Editable editable = editText.getText();
        editable.replace(0, editable.length(), text);
    }
}
