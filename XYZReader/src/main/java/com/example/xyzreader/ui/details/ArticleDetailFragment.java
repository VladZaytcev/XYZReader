package com.example.xyzreader.ui.details;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.list.ArticleListActivity;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment  {
    public static final String ARG_ITEM_ID = "item_id";
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(String text) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_ID, text);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        if(getArguments()!=null && getArguments().containsKey(ARG_ITEM_ID)){
            String bodyText=getArguments().getString(ARG_ITEM_ID);

            TextView bodyView =  rootView.findViewById(R.id.article_body);
            bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
            bodyView.setText(Html.fromHtml(bodyText));
        }

        return rootView;
    }
}
