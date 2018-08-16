package com.websistems.wallpaperhub.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.websistems.wallpaperhub.R;
import com.websistems.wallpaperhub.models.Category;
import com.websistems.wallpaperhub.models.Wallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder> {

    private Context context;
    private List<Wallpaper> wallpaperList;

    public WallpapersAdapter(Context context, List<Wallpaper> wallpaperList) {
        this.context = context;
        this.wallpaperList = wallpaperList;
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_wallpapers, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        Wallpaper w = wallpaperList.get(position);

        Glide.with(context).load(w.url).into(holder.imageView);
        holder.textView.setText(w.title);

        if (w.isFavourite){
            holder.checkBoxFav.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public class WallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

        private ImageView imageView;
        private TextView textView;

        private CheckBox checkBoxFav;
        private ImageButton btnShare, btnDownload;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.text_view_title);

            checkBoxFav = itemView.findViewById(R.id.cbxFav);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDownload = itemView.findViewById(R.id.btnDownload);

            checkBoxFav.setOnCheckedChangeListener(this);
            btnShare.setOnClickListener(this);
            btnDownload.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnShare:
                    shareWallpaper(wallpaperList.get(getAdapterPosition()));
                    break;

                case R.id.btnDownload:
                    downloadWallpaper(wallpaperList.get(getAdapterPosition()));
                    break;
            }
        }

        private void shareWallpaper(Wallpaper w) {
            ((Activity) context).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(context).asBitmap().load(w.url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    ((Activity) context).findViewById(R.id.progressbar).setVisibility(View.GONE);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource));

                    context.startActivity(Intent.createChooser(intent, "Wallpaper Hub"));
                }
            });
        }

        private Uri getLocalBitmapUri(Bitmap bmp){
            Uri bmpUri = null;

            try {
                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "wallpaper_hub_" + System.currentTimeMillis() + ".png");
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmpUri = Uri.fromFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmpUri;
        }

        private void downloadWallpaper(final Wallpaper wallpaper){
            ((Activity) context).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            Glide.with(context).asBitmap().load(wallpaper.url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    ((Activity) context).findViewById(R.id.progressbar).setVisibility(View.GONE);

                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    Uri uri = saveWallpaperANdGetUri(resource, wallpaper.id);

                    if (uri != null){
                        intent.setDataAndType(uri, "image/*");
                        context.startActivity(Intent.createChooser(intent, "Wallpaper Hub"));
                    }
                }
            });
        }

        private Uri saveWallpaperANdGetUri(Bitmap bitmap, String id){
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);

                    context.startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                return null;
            }

            File folder = new File(Environment.getExternalStorageDirectory().toString() + "/wallpapers_hub");
            folder.mkdirs();

            File file = new File(folder, id + ".jpg");
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                return Uri.fromFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null){
                Toast.makeText(context, "Please, login first...", Toast.LENGTH_LONG).show();
                buttonView.setChecked(false);
                return;
            }

            int position = getAdapterPosition();
            Wallpaper w = wallpaperList.get(position);

            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(w.category);

            if (isChecked){
                dbFavs.child(w.id).setValue(w);
            } else {
                dbFavs.child(w.id).setValue(null);
            }
        }
    }
}
