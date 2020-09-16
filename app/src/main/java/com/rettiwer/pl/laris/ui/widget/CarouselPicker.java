package com.rettiwer.pl.laris.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rettiwer.pl.laris.R;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CarouselPicker extends ViewPager {
    private float divisor = (float) 1.5;

    public CarouselPicker(Context context) {
        this(context, null);
    }

    public CarouselPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setPageTransformer(false, new CustomPageTransformer(getContext()));
        this.setClipChildren(false);
        this.setFadingEdgeLength(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) height = h;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        setPageMargin((int) (-w / divisor));
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        this.setOffscreenPageLimit(adapter.getCount());
    }

    public void changeItemsColor(String color) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = findViewWithTag(i);
            ImageView iv = view.findViewById(R.id.iv);

            Drawable wrapDrawable = DrawableCompat.wrap(iv.getDrawable());
            DrawableCompat.setTint(wrapDrawable, Color.parseColor(color));
            iv.setImageDrawable(wrapDrawable);
        }
    }

    public static class CarouselViewAdapter extends PagerAdapter {
        List<DrawableItem> items;
        Context context;
        int drawable;
        String defaultColor;

        public CarouselViewAdapter(Context context, List<DrawableItem> items, int drawable, String defaultColor) {
            this.context = context;
            this.drawable = drawable;
            this.items = items;
            if (this.drawable == 0) {
                this.drawable = R.layout.icon_picker_item;
            }
            this.defaultColor = defaultColor;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public List<DrawableItem> getItems() {
            return items;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(context).inflate(this.drawable, null);
            ImageView iv = view.findViewById(R.id.iv);

            DrawableItem drawableItem = items.get(position);
            Drawable wrapDrawable = DrawableCompat.wrap(drawableItem.getDrawable());
            DrawableCompat.setTint(wrapDrawable, Color.parseColor(defaultColor));
            iv.setImageDrawable(wrapDrawable);

            view.setTag(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }

    public static class DrawableItem {
        private Drawable drawable;
        private String iconName;

        public DrawableItem(Drawable drawable, String iconName) {
            this.drawable = drawable;
            this.iconName = iconName;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public String getIconName() {
            return iconName;
        }
    }


}