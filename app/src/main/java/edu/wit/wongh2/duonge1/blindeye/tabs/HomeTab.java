package edu.wit.wongh2.duonge1.blindeye.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.fanrunqi.waveprogress.WaveProgressView;
import edu.wit.wongh2.duonge1.blindeye.R;

public class HomeTab extends Fragment {

    // "radar" objects
    private WaveProgressView wpv;

    private TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_home, container, false);

        tv = (TextView) v.findViewById(R.id.distanceView);
        wpv = (WaveProgressView) v.findViewById(R.id.waveProgressbar);
        wpv.setWaveColor("#5b9ef4");

        return v;
    }

    public void setText(String text) {
        tv.setText(text+" cm");
    }

    public WaveProgressView getWaveProgressView() {
        return wpv;
    }

}