package com.allynav.shape.config;

import com.allynav.shape.other.TextStateDelegate;

/**
 * 对外暴露 {@link TextStateDelegate} 的能力接口。
 *
 * <p>用于动态设置按下、checked、禁用、聚焦和 selected 状态文本。委托的 setter
 * 会立即刷新目标控件，不需要再调用 into 方法。</p>
 */
public interface IGetTextStateDelegate {

    TextStateDelegate getTextStateDelegate();
}
