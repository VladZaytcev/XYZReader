package com.example.xyzreader.ui.list.adapter;

import android.support.v7.widget.RecyclerView;

import com.example.xyzreader.databinding.ListItemArticleBinding;

class ViewHolder extends RecyclerView.ViewHolder {
    final ListItemArticleBinding binding;

    ViewHolder(ListItemArticleBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
