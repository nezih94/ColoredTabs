package com.nezihyilmaz.coloredtabs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class ColorTabLayout extends FrameLayout {

    private Context context;

    private TabSelectionListener tabSelectionListener;

    private LinearLayout linearLayout;
    private List<Tab> tabs;
    private int initialSelection=0;
    private int selectedViewPosition=-1;
    private int savedViewPosition=-1;

    private TextView tabTextView;

    private int[] textWidth;

    private float indicatorX;
    private float indicatorWidth;
    private RectF indicatorBounds=new RectF();
    private Paint indicatorPaint = new Paint();


    public ColorTabLayout(@NonNull Context context) {
        super(context);
        init(context,null,0,0);
    }

    public ColorTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0,0);
    }

    public ColorTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr,0);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs,defStyleAttr,defStyleRes);
    }

    private void init(Context context , AttributeSet attrs, int defStyleAttr, int defStyleRes){

        Log.d("init","true");
        this.context=context;

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,R.styleable.ColorTabLayout,defStyleAttr,defStyleRes);
        try {

            int tabXMLsource=typedArray.getResourceId(R.styleable.ColorTabLayout_nyTabs_xmlSource,-1);
            XmlParser xmlParser = new XmlParser(context,tabXMLsource);
            tabs=xmlParser.parseTabsXML();

            setUpTextView();
            String typeFacePath=typedArray.getString(R.styleable.ColorTabLayout_nyTabs_typeFace);
            setTypeFace(typeFacePath);

            initialSelection=typedArray.getInteger(R.styleable.ColorTabLayout_nyTabs_initialSelection,0);

        }finally {
            typedArray.recycle();
        }


        linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setUpTabViews();

        setWillNotDraw(false);
        indicatorPaint.setAntiAlias(true);

        final ViewTreeObserver viewTreeObserver=getViewTreeObserver();

        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int tabToSelect=0;

                if (savedViewPosition>=0){
                    tabToSelect=savedViewPosition;
                }
                else {
                    tabToSelect=initialSelection;
                }

                selectTab(tabToSelect,false);

            }
        });

    }

    public void setListener(TabSelectionListener listener){
        tabSelectionListener=listener;
    }


    private void setUpTabViews(){

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        linearLayoutParams.gravity=Gravity.CENTER_VERTICAL;

        int padding =(int) pxFromDp(8);

        TypedValue typedValue=new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);

        for(Tab tab : tabs){

            AppCompatImageView imageView = new AppCompatImageView(context);

            imageView.setColorFilter(tab.getColor());
            imageView.setPadding(padding,padding,padding,padding);
            imageView.setImageDrawable(tab.getIcon());
            imageView.setClickable(true);
            imageView.setBackgroundResource(typedValue.resourceId);
            imageView.setOnClickListener(clickListener);

            linearLayout.addView(imageView,linearLayoutParams);
            tab.setTabView(imageView);
        }
    }


    private void setUpTextView(){

        LayoutParams layoutParams=new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity= Gravity.CENTER_VERTICAL;
        tabTextView=new TextView(context);
        tabTextView.setTextColor(Color.WHITE);
        tabTextView.setGravity(Gravity.START);


        addView(tabTextView,layoutParams);


        textWidth=new int[tabs.size()];

        TextPaint textPaint=tabTextView.getPaint();

        for (int i =0 ; i<tabs.size() ; i++){

            textWidth[i]= (int) textPaint.measureText(tabs.get(i).getText());
        }

    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            for (int i =0 ; i<tabs.size() ; i++){
                Tab tab=tabs.get(i);
                if (view==tab.getTabView()){
                   selectTab(i,true);
                }
            }

        }
    };


    public void selectTab(int position, boolean animate){

        int color=0;
        String text=null;

        if (selectedViewPosition>=0) {
            Tab deSelectedTab = tabs.get(selectedViewPosition);
            AppCompatImageView deSelectedView=deSelectedTab.getTabView();
            animateDeSelectedViewColor(deSelectedView,deSelectedTab.getColor(),animate);
        }

        Tab selectedTab=tabs.get(position);

        AppCompatImageView view=selectedTab.getTabView();
        color=selectedTab.getColor();
        text=selectedTab.getText();

        animateSelectedViewColor(view,color,animate);

        float separatorWidth=textWidth[position];
        float separatorOffsetUnit = separatorWidth / (tabs.size()-1);
        float separatorX=view.getRight() - (separatorOffsetUnit*(position+1));

        float indicatorPadding=getWidth()/12;
        float indicatorX= separatorX - ( (separatorX / (position+1) )) ;
        float indicatorWidth=view.getWidth()+separatorWidth;
        float indicatorExtraX=0;
        float indicatorExtraWidth=0;

        if (position==0){
            indicatorExtraX=-view.getWidth();
            indicatorExtraWidth=view.getWidth();
        }

        else if (position==tabs.size()-1){
            indicatorExtraWidth=view.getWidth();
        }

        float textOffset=(tabs.size()/separatorWidth)*(getWidth()/tabs.size()+1);

        animateIndicator(indicatorX+indicatorExtraX,indicatorWidth+indicatorExtraWidth,color,animate);

        rePositionIcons(position,separatorX,separatorWidth,animate);
        animateText(separatorX-textOffset,separatorWidth,text,animate);

        if (tabSelectionListener!=null) {
            tabSelectionListener.onTabSelected(position);
        }

        selectedViewPosition=position;

    }


    private void animateIndicator(float separatorX , float separatorWidth ,int color , boolean animate){

        if(animate){
            ValueAnimator indicatorXanimator = ValueAnimator.ofFloat(indicatorX,separatorX);
            indicatorXanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    indicatorX=(float)valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });

            ValueAnimator indicatorWidthAnimator = ValueAnimator.ofFloat(indicatorWidth,separatorWidth);
            indicatorWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    indicatorWidth=(float)valueAnimator.getAnimatedValue();
                }
            });

            ValueAnimator indicatorColorAnimator=ValueAnimator.ofInt(indicatorPaint.getColor(),color);
            indicatorColorAnimator.setEvaluator(new ArgbEvaluator());
            indicatorColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    indicatorPaint.setColor((int)valueAnimator.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet=new AnimatorSet();
            animatorSet.playTogether(indicatorXanimator,indicatorWidthAnimator,indicatorColorAnimator);
            animatorSet.setDuration(225);
            animatorSet.start();
        }
        else{
            indicatorX=separatorX;
            indicatorWidth=separatorWidth;
            indicatorPaint.setColor(color);
            invalidate();
        }
    }



    private void animateText(final float newX , final float newWidth , final String text , boolean animate){

        if (animate) {
            tabTextView.animate()
                    .translationXBy(-tabTextView.getWidth())
                    .alpha(0f)
                    .setDuration(150)
                    .setListener(new Animator.AnimatorListener() {

                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {

                            tabTextView.setText(text);
                            tabTextView.setX(newX+tabTextView.getWidth());
                            tabTextView.animate()
                                    .x(tabTextView.getX()-tabTextView.getWidth())
                                    .alpha(1f)
                                    .setDuration(120)
                                    .setListener(null)
                                    .start();

                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }).start();
        }
        else{
            tabTextView.setText(text);
            tabTextView.setX(newX);
        }

    }


    private void rePositionIcons(int clickedPosition, float separatorX , float separatorWidth ,boolean animate) {


        float leftBarPositionUnit =( separatorX / (clickedPosition+1) );

        for (int i =0 ; i<=clickedPosition ; i ++){

            View tab = tabs.get(i).getTabView();
            float translateX= ( (leftBarPositionUnit* (i+1)) )  - (leftBarPositionUnit*1.1f);
            if (animate) {
                tab.animate().x(translateX).start();
            }
            else{
                tab.setX(translateX);
            }
        }

        int rightBarTabCount=(tabs.size()-1)-clickedPosition;

        if (rightBarTabCount>0){

            float rigthBarWidth=getWidth() - (separatorX+separatorWidth);
            float rightBarPositionUnit = rigthBarWidth / rightBarTabCount;

            for (int i=clickedPosition+1 ; i<tabs.size() ; i++){

                View tab=tabs.get(i).getTabView();
                int tabPos =i-clickedPosition;
                float translateX = ( separatorX+separatorWidth ) + (rightBarPositionUnit*tabPos) - tab.getWidth()*0.9f;
                if (animate) {
                    tab.animate().x(translateX).start();
                }
                else{
                    tab.setX(translateX);
                }
            }
        }
    }

    private void animateSelectedViewColor(final AppCompatImageView view , int color , boolean animate){

        if (animate) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(color, Color.WHITE);
            valueAnimator.setEvaluator(new ArgbEvaluator());
            valueAnimator.setDuration(200);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    view.setColorFilter((int) valueAnimator.getAnimatedValue());
                }
            });
            valueAnimator.start();
        } else {
            view.setColorFilter(Color.WHITE);
        }

    }

    private void animateDeSelectedViewColor(final AppCompatImageView view , int color, boolean animate){

        if (animate) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(Color.WHITE, color);
            valueAnimator.setEvaluator(new ArgbEvaluator());
            valueAnimator.setDuration(200);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    view.setColorFilter((int) valueAnimator.getAnimatedValue());
                }
            });
            valueAnimator.start();
        }
        else{
            view.setColorFilter(color);
        }
    }

    public void setTypeFace(String typeFacePath){

        if (typeFacePath==null){
            typeFacePath="OpenSans-Semibold.ttf";
        }

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+typeFacePath);

        if (tabTextView!=null){
            tabTextView.setTypeface(tf);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        indicatorBounds.set(indicatorX,getTop()+(getHeight()/7),indicatorX+indicatorWidth,getBottom()-(getHeight()/7));
        canvas.drawRoundRect(indicatorBounds,100,100,indicatorPaint);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        DisplayMetrics displayMetrics=Resources.getSystem().getDisplayMetrics();

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true);

        int desiredWidth = displayMetrics.widthPixels;
        int desiredHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());


        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = desiredWidth;
        } else {
            width = desiredWidth;
        }


        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = desiredHeight;
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }*/

    @Override
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("currentPosition", selectedViewPosition); // ... save stuff
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            int tabPosition = bundle.getInt("currentPosition");
            state = bundle.getParcelable("superState");

            Log.d("saved",String.valueOf(tabPosition));
            selectTab(tabPosition,false);

        }
        super.onRestoreInstanceState(state);
    }

    private float dpFromPx( final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    private float pxFromDp( final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
