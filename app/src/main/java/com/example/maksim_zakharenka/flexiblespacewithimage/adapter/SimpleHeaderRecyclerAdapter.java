package com.example.maksim_zakharenka.flexiblespacewithimage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SimpleHeaderRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final LayoutInflater mInflater;
    private final List<String> mItems;
    private final View mHeaderView;

    public SimpleHeaderRecyclerAdapter(final Context context, final List<String> items, final View headerView) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
        mHeaderView = headerView;
    }

    @Override
    public int getItemCount() {
        if (mHeaderView == null) {
            return mItems.size();
        } else {
            return mItems.size() + 1;
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        } else {
            return new ItemViewHolder(mInflater.inflate(android.R.layout.simple_list_item_1, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).textView.setText(mItems.get(position - 1));
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(final View view) {
            super(view);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ItemViewHolder(final View view) {
            super(view);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }
    }
}
