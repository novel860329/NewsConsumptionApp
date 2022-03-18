package com.example.accessibility_detect;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.accessibility_detect.questions.fragments.ChoosePictureFragment;

import java.util.ArrayList;

import static com.example.accessibility_detect.questions.fragments.ChoosePictureFragment.name;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<Boolean> mCheck = new ArrayList<>();
    private int type;
    private SharedPreferences pref;
    private Context mContext;
    private String picture_time;
    private int lastposition;

    public RecyclerViewAdapter(Context Context, ArrayList<String> mImages, ArrayList<Boolean> mCheck, int type) {
        Log.d(TAG, "RecyclerViewAdapter");
        this.mImages = mImages;
        this.mCheck = mCheck;
        this.mContext = Context;
        this.type = type;
        pref = mContext.getSharedPreferences("test",Context.MODE_PRIVATE);
    }
    @Override
    public int getItemViewType(int position) {
        return type;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        Log.d(TAG, "ViewGroup:"+parent);
        if(viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
            ViewHolder holder = new ViewHolder(view, viewType);
            holder.image = (ImageView) view.findViewById(R.id.image);
            holder.checker = (ImageView) view.findViewById(R.id.check);
            return holder;
        }
        else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
            ViewHolder holder = new ViewHolder(view, viewType);
            holder.image = (ImageView) view.findViewById(R.id.image);
            holder.checker = (ImageView) view.findViewById(R.id.check);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder called");

        if(holder.Holderid == 1) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(mImages.get(position))
                    .into(holder.image);

            if (mCheck.get(position))
                holder.checker.setVisibility(View.VISIBLE);
            else
                holder.checker.setVisibility(View.INVISIBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //lastposition = pref.getInt("position", 0);
                    pref.edit().putString("ChooseImage", mImages.get(position)).apply();
                    Log.d(TAG, "OnClick: clicked on an image: " + mImages.get(position));
                    Log.d(TAG, "Position: " + position + " " + lastposition);

                    try {
                        String[] mImages_split = mImages.get(position).split("/|-|\\.");
                        picture_time = mImages_split[mImages_split.length - 6] + ":" + mImages_split[mImages_split.length - 5]
                                + ":" + mImages_split[mImages_split.length - 4];
                        name.setText(picture_time);
                        Log.d(TAG, "time: " + picture_time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //CSVHelper.storeToCSV("RecycleView.csv","Position: " + position + " " + lastposition);
                    //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
                    mCheck.set(lastposition, false);
                    mCheck.set(position, true);
                    Glide.with(mContext)
                            .asBitmap()
                            .load(mImages.get(position))
                            .into(ChoosePictureFragment.imageShow);
                    lastposition = position;
//                pref.edit().putInt("position", position).apply();
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 刷新操作
//
//                    }
//                });
                    notifyDataSetChanged();
                }
            });
        }
        else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(mImages.get(position))
                    .into(holder.image);

            if (mCheck.get(position))
                holder.checker.setVisibility(View.VISIBLE);
            else
                holder.checker.setVisibility(View.INVISIBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //lastposition = pref.getInt("position", 0);
                    pref.edit().putString("ChooseImage", mImages.get(position)).apply();
                    Log.d(TAG, "OnClick: clicked on an image: " + mImages.get(position));
                    Log.d(TAG, "Position: " + position + " " + lastposition);

                    try {
                        String[] mImages_split = mImages.get(position).split("/|-|\\.");
                        picture_time = mImages_split[mImages_split.length - 6] + ":" + mImages_split[mImages_split.length - 5]
                                + ":" + mImages_split[mImages_split.length - 4];
                        name.setText(picture_time);
                        Log.d(TAG, "time: " + picture_time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //CSVHelper.storeToCSV("RecycleView.csv","Position: " + position + " " + lastposition);
                    //Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();
                    mCheck.set(lastposition, false);
                    mCheck.set(position, true);
                    Glide.with(mContext)
                            .asBitmap()
                            .load(mImages.get(position))
                            .into(ChoosePicture.imageShow);
                    lastposition = position;
//                pref.edit().putInt("position", position).apply();
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 刷新操作
//
//                    }
//                });
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount= " + mImages.size());
        return mImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        ImageView checker;
        int Holderid;
        //        TextView name;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            Holderid = viewType;
//            image = (ImageView)itemView.findViewById(R.id.image);
//            checker = (ImageView)itemView.findViewById(R.id.check);
        }
    }

}