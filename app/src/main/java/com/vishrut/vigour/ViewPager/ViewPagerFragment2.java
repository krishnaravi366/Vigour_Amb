package com.vishrut.vigour.ViewPager;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vishrut.vigour.R;

public class ViewPagerFragment2 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_view_pager_fragment2, container, false);
        ImageView image = (ImageView) view.findViewById(R.id.viewpagerimage2);
        Picasso.with(getContext()).load(R.drawable.viewpager2).into(image);
        return view;
    }
}
