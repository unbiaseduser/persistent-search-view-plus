package com.sixtyninefourtwenty.persistentsearchview.adapters;

import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.makeGone;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.makeVisible;

import android.annotation.SuppressLint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sixtyninefourtwenty.persistentsearchview.R;
import com.sixtyninefourtwenty.persistentsearchview.adapters.resources.SuggestionItemResources;
import com.sixtyninefourtwenty.persistentsearchview.model.Suggestion;
import com.sixtyninefourtwenty.persistentsearchview.utils.Utils;
import com.sixtyninefourtwenty.persistentsearchview.databinding.ViewPersistentSearchSuggestionItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class SuggestionsRecyclerViewAdapter extends ListAdapter<Suggestion, SuggestionsRecyclerViewAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Suggestion> SUGGESTION_ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Suggestion oldItem, @NonNull Suggestion newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Suggestion oldItem, @NonNull Suggestion newItem) {
            return oldItem.getText().equals(newItem.getText());
        }
    };

    @NonNull
    private SuggestionItemResources resources;
    @NonNull
    private final Consumer<Suggestion> onItemClickListener;
    @NonNull
    private final Consumer<Suggestion> onItemRemoveButtonClickListener;

    public SuggestionsRecyclerViewAdapter(@NonNull SuggestionItemResources resources,
                                          @NonNull Consumer<Suggestion> onItemClickListener,
                                          @NonNull Consumer<Suggestion> onItemRemoveButtonClickListener) {
        super(SUGGESTION_ITEM_CALLBACK);
        this.resources = resources;
        this.onItemClickListener = onItemClickListener;
        this.onItemRemoveButtonClickListener = onItemRemoveButtonClickListener;
    }

    public void deleteItem(@NonNull Suggestion item, @NonNull Runnable commitCallback) {
        List<Suggestion> current = getCurrentList();
        final int itemIndex = current.indexOf(item);
        if (itemIndex != -1) {
            List<Suggestion> newList = new ArrayList<>(current);
            newList.remove(item);
            submitList(newList, commitCallback);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setResources(@NonNull SuggestionItemResources resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    @NonNull
    public SuggestionItemResources getResources() {
        return resources;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final var binding = ViewPersistentSearchSuggestionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding,
                pos -> onItemClickListener.accept(getItem(pos)),
                pos -> onItemRemoveButtonClickListener.accept(getItem(pos)));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindSuggestion(getItem(position), resources);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTv;
        private final ImageView iconIv;
        private final ImageView removeBtnIv;

        public ViewHolder(ViewPersistentSearchSuggestionItemBinding binding,
                          IntConsumer onItemClickListener,
                          IntConsumer onItemRemoveButtonClickListener) {
            super(binding.getRoot());
            itemView.setOnClickListener(v -> onItemClickListener.accept(getAdapterPosition()));
            textTv = binding.textTv;
            iconIv = binding.iconIv;
            removeBtnIv = binding.removeBtnIv;
            removeBtnIv.setOnClickListener(v -> onItemRemoveButtonClickListener.accept(getAdapterPosition()));
        }

        public void bindSuggestion(@NonNull Suggestion suggestion, @NonNull SuggestionItemResources resources) {
            final boolean isRecentSearchSuggestion = suggestion.getType() == Suggestion.Type.RECENT_SEARCH;
            bindText(suggestion, resources);
            bindIcon(isRecentSearchSuggestion, resources);
            bindButton(isRecentSearchSuggestion, resources);
        }

        private void bindText(@NonNull Suggestion suggestion, @NonNull SuggestionItemResources resources) {
            textTv.setTextColor(resources.getTextColor());
            final String text = suggestion.getText();
            final int startIndex = text.toLowerCase().indexOf(resources.getCurrentQuery().toLowerCase());
            final int endIndex = Math.min(resources.getCurrentQuery().length(), text.length());
            final boolean isCurrentQueryValid = !TextUtils.isEmpty(resources.getCurrentQuery());
            final boolean isStartIndexValid = (startIndex != -1);
            final boolean isEndIndexValid = (startIndex <= endIndex);

            if (isCurrentQueryValid && isStartIndexValid && isEndIndexValid) {
                final SpannableString spannableString = new SpannableString(text);
                spannableString.setSpan(
                        new ForegroundColorSpan(resources.getSelectedTextColor()),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                textTv.setText(spannableString);
            } else {
                textTv.setText(text);
            }
        }

        private void bindIcon(boolean isRecentSearchSuggestion, SuggestionItemResources resources) {
            iconIv.setImageDrawable(
                    Utils.getColoredDrawable(
                            iconIv.getContext(),
                            (isRecentSearchSuggestion ? R.drawable.baseline_history_24 : R.drawable.baseline_search_24),
                            (isRecentSearchSuggestion ? resources.getRecentSearchIconColor() : resources.getSearchSuggestionIconColor())
                    )
            );
        }

        private void bindButton(boolean isRecentSearchSuggestion, SuggestionItemResources resources) {
            if(isRecentSearchSuggestion) {
                removeBtnIv.setImageDrawable(
                        Utils.getColoredDrawable(
                                removeBtnIv.getContext(),
                                R.drawable.baseline_close_24,
                                resources.getIconColor()
                        )
                );
                makeVisible(removeBtnIv);
            } else {
                makeGone(removeBtnIv);
            }
        }
    }
}
