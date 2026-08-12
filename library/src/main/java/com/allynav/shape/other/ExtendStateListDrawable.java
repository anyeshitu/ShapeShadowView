package com.allynav.shape.other;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import java.util.HashMap;

/**
 * 可按语义存取状态 Drawable 的 StateListDrawable。
 *
 * <p>系统 StateListDrawable 只能追加状态，无法直接取回默认或某个状态 Drawable。
 * 本类在添加状态时同步保存引用，供 ShapeDrawableBuilder 动态重建背景时提取默认项。
 * 状态匹配顺序仍由 addState 调用顺序决定。</p>
 */
public class ExtendStateListDrawable extends StateListDrawable {

   private static final int[] STATE_DEFAULT = new int[]{};
   private static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
   private static final int[] STATE_CHECKED = new int[]{android.R.attr.state_checked};
   private static final int[] STATE_DISABLED = new int[]{-android.R.attr.state_enabled};
   private static final int[] STATE_FOCUSED = new int[]{android.R.attr.state_focused};
   private static final int[] STATE_SELECTED = new int[]{android.R.attr.state_selected};

   /** 使用内部固定状态数组作为 key，因此按数组引用即可稳定取回对应 Drawable。 */
   private final HashMap<int[], Drawable> mDrawableMap = new HashMap<>();

   @Override
   public void addState(int[] stateSet, Drawable drawable) {
      // 先交给系统建立状态表，再保存非空 Drawable 供语义 getter 使用。
      super.addState(stateSet, drawable);
      if (drawable == null) {
         return;
      }
      mDrawableMap.put(stateSet, drawable);
   }

   public void setDefaultDrawable(Drawable drawable) {
      addState(STATE_DEFAULT, drawable);
   }

   public Drawable getDefaultDrawable() {
      return mDrawableMap.get(STATE_DEFAULT);
   }

   public void setPressedDrawable(Drawable drawable) {
      addState(STATE_PRESSED, drawable);
   }

   public Drawable getPressedDrawable() {
      return mDrawableMap.get(STATE_PRESSED);
   }

   public void setCheckDrawable(Drawable drawable) {
      addState(STATE_CHECKED, drawable);
   }

   public Drawable getCheckDrawable() {
      return mDrawableMap.get(STATE_CHECKED);
   }

   public void setDisabledDrawable(Drawable drawable) {
      addState(STATE_DISABLED, drawable);
   }

   public Drawable getDisabledDrawable() {
      return mDrawableMap.get(STATE_DISABLED);
   }

   public void setFocusedDrawable(Drawable drawable) {
      addState(STATE_FOCUSED, drawable);
   }

   public Drawable getFocusedDrawable() {
      return mDrawableMap.get(STATE_FOCUSED);
   }

   public void setSelectDrawable(Drawable drawable) {
      addState(STATE_SELECTED, drawable);
   }

   public Drawable getSelectDrawable() {
      return mDrawableMap.get(STATE_SELECTED);
   }
}
