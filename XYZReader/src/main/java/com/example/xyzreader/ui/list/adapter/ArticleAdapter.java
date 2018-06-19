package com.example.xyzreader.ui.list.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.databinding.ListItemArticleBinding;
import com.example.xyzreader.ui.list.ArticleListActivity;
import com.example.xyzreader.ui.list.adapter.ViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = ArticleAdapter.class.toString();
    private ArticleListActivity mArticleListActivity;
    private final Cursor mCursor;
    private ListItemArticleBinding mItemBinding;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
    // Use default locale format
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

   public ArticleAdapter(ArticleListActivity articleListActivity, Cursor cursor) {
        mArticleListActivity = articleListActivity;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_article,
                        parent, false);
        final ViewHolder vh = new ViewHolder(mItemBinding);
        mItemBinding.getRoot().setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    mArticleListActivity,
                    new Pair<>(mItemBinding.thumbnail,
                            mArticleListActivity.getResources().getString(R.string.transition_image)),
                    new Pair<>(mItemBinding.articleTitle,
                            mArticleListActivity.getResources().getString(R.string.transition_title)));
            mArticleListActivity.startActivity(intent, options.toBundle());
        });
        return vh;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        mItemBinding.articleTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            mItemBinding.articleSubtitle.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            mItemBinding.articleSubtitle.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        Picasso.with(mArticleListActivity)
                .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .placeholder(R.drawable.empty_detail)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        ConstraintSet set = new ConstraintSet();
                        set.clone(mItemBinding.constraintContainer);
                        set.setDimensionRatio(mItemBinding.thumbnail.getId(), String.format(Locale.getDefault(), "%d:%d", width, height));
                        set.applyTo(mItemBinding.constraintContainer);
                        mItemBinding.mainContainer.setVisibility(View.VISIBLE);
                        mItemBinding.thumbnail.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
