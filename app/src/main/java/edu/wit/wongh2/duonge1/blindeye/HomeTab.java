package edu.wit.wongh2.duonge1.blindeye;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HomeTab extends Fragment {

    // "radar" objects
    private CircularProgressDrawable circle;
    private ImageView ivDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_home, container, false);

        ivDrawable = (ImageView) v.findViewById(R.id.sensorView);

        circle = new CircularProgressDrawable.Builder()
                .setRingWidth(getResources().getDimensionPixelSize(R.dimen.drawable_ring_size))
                .setOutlineColor(getResources().getColor(android.R.color.darker_gray))
                .setCenterColor(getResources().getColor(android.R.color.holo_blue_dark))
                .create();

        ivDrawable.setImageDrawable(circle);

        progressCircleAnimation().start();

        return v;
    }

    private Animator progressCircleAnimation() {
        AnimatorSet animation = new AnimatorSet();

        final Animator innerCircleAnimation = ObjectAnimator.ofFloat(circle, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 1f);
        innerCircleAnimation.setDuration(3600);
        Animator innerCircleAnimationEnd = ObjectAnimator.ofFloat(circle, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 1f, 0f);
        innerCircleAnimationEnd.setDuration(3600);

        innerCircleAnimationEnd.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                circle.setIndeterminate(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //indeterminateAnimation.end();
                //circle.setIndeterminate(false);
                //circle.setProgress(0);
                progressCircleAnimation().start();
            }
        });

        animation.playSequentially(innerCircleAnimation, innerCircleAnimationEnd);

        return animation;
    }


}