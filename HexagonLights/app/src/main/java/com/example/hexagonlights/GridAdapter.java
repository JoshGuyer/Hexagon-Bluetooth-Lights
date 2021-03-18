package com.example.hexagonlights;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.HexagonHolder> {

    int red, green, blue;
    private Context context;
    private OnNoteListener mOnNoteListener;
    public GridAdapter(Context context, OnNoteListener onNoteListener) {
        this.context = context;
        this.mOnNoteListener = onNoteListener;
    }
    @Override
    public GridAdapter.HexagonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hexa_tv, parent, false);
        return new HexagonHolder(view, mOnNoteListener);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(GridAdapter.HexagonHolder holder, int position) {
        int pos = position + 1;
        int topMargin = pxFromDp(holder.imageView.getContext(), -17);
        int bottomMargin = pxFromDp(holder.imageView.getContext(), 17);
        int leftMargin = pxFromDp(holder.imageView.getContext(), 50); //3 times of 17
        int negLeftMargin = pxFromDp(holder.imageView.getContext(), -50);
        GridLayoutManager.LayoutParams param = (GridLayoutManager.LayoutParams) holder.imageView.getLayoutParams();
        if(position == 0) {
            //3
            //param.setMargins(leftMargin+leftMargin+leftMargin,0,0,0);
            //6
            param.setMargins(leftMargin-35,0,0,bottomMargin);
        }
        if(position == 1) {
            //3
            //param.setMargins(leftMargin+leftMargin,topMargin,0, bottomMargin);
            //6
            param.setMargins(10,0,0, bottomMargin);
        }
        if(position == 2) {
            //3
            //param.setMargins(negLeftMargin-55,topMargin,0,bottomMargin);
            //6
            param.setMargins(negLeftMargin+50,0,0,bottomMargin);
        }
        if(position == 3) {
            param.setMargins(leftMargin+leftMargin-35,topMargin*2,0,bottomMargin);
        }
        if(position == 4) {
            param.setMargins(leftMargin+10,topMargin*2,0,bottomMargin);
        }
        if(position == 5) {
            param.setMargins(leftMargin-75,topMargin*2,0,bottomMargin);
        }
        holder.imageView.setLayoutParams(param);

    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    class HexagonHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        OnNoteListener onNoteListener;

        @SuppressLint("ResourceAsColor")
        HexagonHolder(View v, OnNoteListener onNoteListener) {
            super(v);
            this.onNoteListener = onNoteListener;
            v.setOnClickListener(this);
            imageView = v.findViewById(R.id.tv_1);

        }

        @Override
        public void onClick(View view) {
            onNoteListener.onNoteClick(getAdapterPosition());
            Log.d("rgb", red + " " + green + " " + blue);
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(red,green,blue)));
        }
    }
    public void setColor(int r, int g, int b) {
        this.red = r;
        this.green = g;
        this.blue = b;
    }
    private int pxFromDp(final Context context, final float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}