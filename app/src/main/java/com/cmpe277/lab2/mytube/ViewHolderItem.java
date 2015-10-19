package com.cmpe277.lab2.mytube;

import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by knbarve on 10/18/15.
 */
public  class ViewHolderItem {
    public ImageView thumbnail;
    public TextView title;
    public TextView viewCounts;
    public TextView publishDate;
    public ImageButton favoriteBtn;
    public CheckBox favoriteBox;

    public ViewHolderItem(ImageView thumbnail, TextView title, TextView viewCounts, TextView publishDate, ImageButton favoriteButton) {
        this.thumbnail = thumbnail;
        this.title = title;
        this.viewCounts = viewCounts;
        this.publishDate = publishDate;
        this.favoriteBtn = favoriteButton;
    }

    public ViewHolderItem() {

    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ImageView thumbnail) {
        this.thumbnail = thumbnail;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getViewCounts() {
        return viewCounts;
    }

    public void setViewCounts(TextView viewCounts) {
        this.viewCounts = viewCounts;
    }

    public TextView getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(TextView publishDate) {
        this.publishDate = publishDate;
    }

    public ImageButton getFavoriteButton() {
        return favoriteBtn;
    }

    public void setFavoriteButton(ImageButton favoriteButton) {
        this.favoriteBtn = favoriteButton;
    }
}
