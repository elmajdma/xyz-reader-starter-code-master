package com.example.xyzreader.ui;


import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleLoader.Query;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleDetailedFragmentEnhanced extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>{
  private static final String TAG = "ArticleDetailedFragmen";
  private View mRootView;
  private long mItemId;
  private Cursor mCursor;
  public static final String ARG_ITEM_ID = "item_id";
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
  private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
  private SimpleDateFormat outputFormat = new SimpleDateFormat();

  public ArticleDetailedFragmentEnhanced() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItemId = getArguments().getLong(ARG_ITEM_ID);
    }

  }
  public static ArticleDetailedFragmentEnhanced newInstance(long itemId) {
    Bundle arguments = new Bundle();
    arguments.putLong(ARG_ITEM_ID, itemId);
    ArticleDetailedFragmentEnhanced fragment = new ArticleDetailedFragmentEnhanced();
    fragment.setArguments(arguments);
    return fragment;
  }
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
    // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
    // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
    // we do this in onActivityCreated.
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      mRootView=inflater.inflate(R.layout.fragment_article_detail_enhanced, container, false);
    bindViews();
      return  mRootView;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (!isAdded()) {
      if (data != null) {
        data.close();
      }
      return;
    }

    mCursor = data;
    if (mCursor != null && !mCursor.moveToFirst()) {
      Log.e(TAG, "Error reading item detail cursor");
      mCursor.close();
      mCursor = null;
    }

    bindViews();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mCursor = null;
    bindViews();

  }

  private void bindViews() {
    if (mRootView == null) {
      return;
    }

    TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
    TextView bylineView = (TextView) mRootView.findViewById(R.id.article_date);
    TextView authorView = (TextView) mRootView.findViewById(R.id.article_subtitle);
    ImageView mPhotoView=(ImageView) mRootView.findViewById(R.id.photo);
    bylineView.setMovementMethod(new LinkMovementMethod());
    TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
    if (mCursor != null) {

      mRootView.setVisibility(View.VISIBLE);
      mRootView.animate().alpha(1);
      titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
      authorView.setText(mCursor.getString(Query.AUTHOR));
      Date publishedDate = parsePublishedDate();
      if (!publishedDate.before(START_OF_EPOCH.getTime())) {

        bylineView.setText(Html.fromHtml(
            DateUtils.getRelativeTimeSpanString(
                publishedDate.getTime(),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString()));

      } else {
        // If date is before 1902, just show the string
        bylineView.setText(Html.fromHtml(
            outputFormat.format(publishedDate)));

      }
      bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
      Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(mPhotoView);

    } else {
      mRootView.setVisibility(View.GONE);
      Toast.makeText(getActivity(),"here",Toast.LENGTH_LONG).show();
      titleView.setText("N/A");
      bylineView.setText("N/A" );
      bodyView.setText("N/A");
    }
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

}
