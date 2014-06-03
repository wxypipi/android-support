/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v17.leanback.R;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Collection;

/**
 * DetailsOverviewRowPresenter renders {@link DetailsOverviewRow} to display an
 * overview of an item. Typically this row will be the first row in a fragment
 * such as {@link android.support.v17.leanback.app.DetailsFragment
 * DetailsFragment}.
 *
 * <p>The detailed description is rendered using a {@link Presenter}.
 */
public class DetailsOverviewRowPresenter extends RowPresenter {

    private static final String TAG = "DetailsOverviewRowPresenter";
    private static final boolean DEBUG = false;

    private static final int MORE_ACTIONS_FADE_MS = 100;

    public static class ViewHolder extends RowPresenter.ViewHolder {
        final ImageView mImageView;
        final FrameLayout mDetailsDescriptionFrame;
        final HorizontalGridView mActionsRow;
        final View mMoreActionsView;
        Presenter.ViewHolder mDetailsDescriptionViewHolder;
        int mNumItems;
        boolean mShowMoreRight;
        boolean mShowMoreLeft;
        boolean mShowMoreActions;

        void bind(ItemBridgeAdapter bridgeAdapter) {
            mNumItems = bridgeAdapter.getItemCount();
            bridgeAdapter.setAdapterListener(mAdapterListener);

            mMoreActionsView.setAlpha(0f);
            mShowMoreRight = false;
            mShowMoreActions = false;

            mShowMoreLeft = true;
            showMoreLeft(false);
        }

        final ItemBridgeAdapter.AdapterListener mAdapterListener =
                new ItemBridgeAdapter.AdapterListener() {

            @Override
            public void onAttachedToWindow(ItemBridgeAdapter.ViewHolder viewHolder) {
                checkFirstAndLastPosition(false);
            }
            @Override
            public void onDetachedFromWindow(ItemBridgeAdapter.ViewHolder viewHolder) {
                checkFirstAndLastPosition(false);
            }
        };

        final RecyclerView.OnScrollListener mScrollListener =
                new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(int newState) {
            }
            @Override
            public void onScrolled(int dx, int dy) {
                checkFirstAndLastPosition(true);
            }
        };

        private int getViewCenter(View view) {
            return (view.getRight() - view.getLeft()) / 2;
        }

        private void checkFirstAndLastPosition(boolean fromScroll) {
            RecyclerView.ViewHolder viewHolder;

            viewHolder = mActionsRow.findViewHolderForPosition(mNumItems - 1);
            boolean showRight = (viewHolder == null ||
                    viewHolder.itemView.getRight() > mActionsRow.getWidth());

            boolean showMore = (viewHolder == null ||
                    getViewCenter(viewHolder.itemView) > mActionsRow.getWidth());

            viewHolder = mActionsRow.findViewHolderForPosition(0);
            boolean showLeft = (viewHolder == null || viewHolder.itemView.getLeft() < 0);

            if (DEBUG) Log.v(TAG, "checkFirstAndLast fromScroll " + fromScroll +
                    " showRight " + showRight + " showLeft " + showLeft);

            showMoreActions(showMore);
            showMoreRight(showRight);
            showMoreLeft(showLeft);
        }

        private void showMoreLeft(boolean show) {
            if (show != mShowMoreLeft) {
                mActionsRow.setFadingLeftEdge(show);
                mShowMoreLeft = show;
            }
        }

        private void showMoreRight(boolean show) {
            if (show != mShowMoreRight) {
                mActionsRow.setFadingRightEdge(show);
                mShowMoreRight = show;
            }
        }

        private void showMoreActions(boolean show) {
            if (show != mShowMoreActions) {
                mMoreActionsView.animate().alpha(show ? 1f : 0).setDuration(
                        MORE_ACTIONS_FADE_MS).start();
                mShowMoreActions = show;
            }
        }

        public ViewHolder(View rootView) {
            super(rootView);
            mImageView = (ImageView) rootView.findViewById(R.id.details_overview_image);
            mDetailsDescriptionFrame =
                    (FrameLayout) rootView.findViewById(R.id.details_overview_description);
            mActionsRow =
                    (HorizontalGridView) rootView.findViewById(R.id.details_overview_actions);
            mActionsRow.setOnScrollListener(mScrollListener);

            final int fadeLength = rootView.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_overview_actions_fade_size);
            mActionsRow.setFadingRightEdgeLength(fadeLength);
            mActionsRow.setFadingRightEdgeOffset(-fadeLength);
            mActionsRow.setFadingLeftEdgeLength(fadeLength);
            mActionsRow.setFadingLeftEdgeOffset(-fadeLength);

            mMoreActionsView = rootView.findViewById(R.id.details_overview_actions_more);
        }
    }

    private final Presenter mDetailsPresenter;
    private final ActionPresenterSelector mActionPresenterSelector;
    private final ItemBridgeAdapter mActionBridgeAdapter;
    private int mBackgroundColor = Color.TRANSPARENT;
    private boolean mBackgroundColorSet;
    private boolean mIsStyleLarge = true;

    /**
     * Constructor that uses the given {@link Presenter} to render the detailed
     * description for the row.
     */
    public DetailsOverviewRowPresenter(Presenter detailsPresenter) {
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        mDetailsPresenter = detailsPresenter;
        mActionPresenterSelector = new ActionPresenterSelector();
        mActionBridgeAdapter = new ItemBridgeAdapter();
    }

    /**
     * Sets the listener for action click events.
     */
    public void setOnActionClickedListener(OnActionClickedListener listener) {
        mActionPresenterSelector.setOnActionClickedListener(listener);
    }

    /**
     * Gets the listener for action click events.
     */
    public OnActionClickedListener getOnActionClickedListener() {
        return mActionPresenterSelector.getOnActionClickedListener();
    }

    /**
     * Sets the background color.  If not set a default from the theme will be used.
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundColorSet = true;
    }

    /**
     * Returns the background color.  If no background color was set, returns transparent.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Sets the layout style to be large or small.
     * The default is large.
     */
    public void setStyleLarge(boolean large) {
        mIsStyleLarge = large;
    }

    /**
     * Returns true if the layout style is large.
     */
    public boolean isStyleLarge() {
        return mIsStyleLarge;
    }

    private int getDefaultBackgroundColor(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.defaultBrandColor, outValue, true);
        return context.getResources().getColor(outValue.resourceId);
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.lb_details_overview, parent, false);
        ViewHolder vh = new ViewHolder(v);

        vh.mDetailsDescriptionViewHolder =
            mDetailsPresenter.onCreateViewHolder(vh.mDetailsDescriptionFrame);
        vh.mDetailsDescriptionFrame.addView(vh.mDetailsDescriptionViewHolder.view);

        initDetailsOverview(v.findViewById(R.id.details_overview));

        return vh;
    }

    private void initDetailsOverview(View view) {
        int resId = mIsStyleLarge ? R.dimen.lb_details_overview_height_large :
            R.dimen.lb_details_overview_height_small;

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = view.getResources().getDimensionPixelSize(resId);
        view.setLayoutParams(lp);

        view.setBackgroundColor(mBackgroundColorSet ?
                mBackgroundColor : getDefaultBackgroundColor(view.getContext()));

    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);

        DetailsOverviewRow row = (DetailsOverviewRow) item;
        ViewHolder vh = (ViewHolder) holder;
        if (row.getImageDrawable() != null) {
            vh.mImageView.setImageDrawable(row.getImageDrawable());
        }
        if (vh.mDetailsDescriptionViewHolder == null) {
        }
        mDetailsPresenter.onBindViewHolder(vh.mDetailsDescriptionViewHolder, row);

        mActionBridgeAdapter.clear();
        ArrayObjectAdapter aoa = new ArrayObjectAdapter(mActionPresenterSelector);
        aoa.addAll(0, (Collection)row.getActions());

        mActionBridgeAdapter.setAdapter(aoa);
        vh.mActionsRow.setAdapter(mActionBridgeAdapter);

        vh.bind(mActionBridgeAdapter);
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder holder) {
        super.onUnbindRowViewHolder(holder);

        ViewHolder vh = (ViewHolder) holder;
        if (vh.mDetailsDescriptionViewHolder != null) {
            mDetailsPresenter.onUnbindViewHolder(vh.mDetailsDescriptionViewHolder);
        }

        vh.mActionsRow.setAdapter(null);
    }
}
