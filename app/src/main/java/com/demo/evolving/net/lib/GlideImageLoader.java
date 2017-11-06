package com.demo.evolving.net.lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.imagepicker.loader.ImageLoader;
import com.lzy.ninegrid.NineGridView;

import java.io.File;

public class GlideImageLoader implements ImageLoader, NineGridView.ImageLoader {
    @Override
    public void onDisplayImage(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url)
                .placeholder(R.drawable.ic_default_color)
                .error(R.drawable.ic_default_color)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    @Override
    public Bitmap getCacheImage(String url) {
        return null;
    }

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        Glide.with(activity).load(new File(path))
                .placeholder(R.drawable.ic_default_color)
                .error(R.drawable.ic_default_color)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
    }
}
