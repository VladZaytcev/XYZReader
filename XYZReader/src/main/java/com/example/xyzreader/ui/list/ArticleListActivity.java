package com.example.xyzreader.ui.list;

import android.support.v4.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.databinding.ActivityArticleListBinding;
import com.example.xyzreader.databinding.ListItemArticleBinding;
import com.example.xyzreader.ui.details.ArticleDetailActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private ActivityArticleListBinding mListBinding;

    private static final String TAG = ArticleListActivity.class.toString();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        mListBinding = DataBindingUtil.setContentView(this, R.layout.activity_article_list);

        mListBinding.swipeRefreshLayout.setOnRefreshListener(this);

        getSupportLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        updateRefreshingUI(true);
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                updateRefreshingUI(intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false));
            }
        }
    };

    private void updateRefreshingUI(boolean isRefreshing) {
        mListBinding.swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ArticleAdapter adapter = new ArticleAdapter(cursor);
        adapter.setHasStableIds(true);
        mListBinding.recyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mListBinding.recyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListBinding.recyclerView.setAdapter(null);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private class ArticleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        private ListItemArticleBinding mItemBinding;

        ArticleAdapter(Cursor cursor) {
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
            mItemBinding.getRoot().setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                    ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())))));
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

            Picasso.with(ArticleListActivity.this)
                    .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    //.placeholder(getResources().getDrawable(R.drawable.ic_placeholder))
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

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final ListItemArticleBinding binding;

        ViewHolder(ListItemArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
