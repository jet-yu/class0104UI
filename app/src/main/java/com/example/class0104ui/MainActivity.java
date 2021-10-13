package com.example.class0104ui;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.class0104ui.view.TaskClearDrawable;

public class MainActivity extends AppCompatActivity {


    private ImageView mImageView;
    private TaskClearDrawable mTaskClearDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mImageView = findViewById(R.id.imageView);
        mTaskClearDrawable = new TaskClearDrawable(this, Utils.dp2px(300), Utils.dp2px(300));
        mImageView.setImageDrawable(mTaskClearDrawable);


        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.i("Zero", "mTaskClearDrawable = " + mTaskClearDrawable.isRunning());
                if (false == mTaskClearDrawable.isRunning()) {
                    mTaskClearDrawable.start();
                }
            }
        });

    }

    private void start() {
        ValueAnimator valueAnimator1 = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator1.setDuration(1000);
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator2.setDuration(1000);
        ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator3.setDuration(1000);

        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.e("valueAnimator1", String.valueOf(valueAnimator.getAnimatedValue()));
            }
        });

        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.e("valueAnimator2", String.valueOf(valueAnimator.getAnimatedValue()));

            }
        });

        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.e("valueAnimator3", String.valueOf(valueAnimator.getAnimatedValue()));

            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(valueAnimator1, valueAnimator2, valueAnimator3);
        animatorSet.start();
    }
}