package com.nezihyilmaz.coloredtabs;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;

class Tab {

    private AppCompatImageView tabView;
    private Drawable icon;
    private int color;
    private String text;

    Tab(AppCompatImageView tabView, Drawable icon, int color, String text) {
        this.tabView = tabView;
        this.icon = icon;
        this.color = color;
        this.text = text;
    }

    public AppCompatImageView getTabView() {
        return tabView;
    }

    public void setTabView(AppCompatImageView tabView) {
        this.tabView = tabView;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}