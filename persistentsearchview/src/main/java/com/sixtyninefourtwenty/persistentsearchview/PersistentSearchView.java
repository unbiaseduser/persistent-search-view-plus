/*
 * Copyright 2017 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sixtyninefourtwenty.persistentsearchview;

import static com.sixtyninefourtwenty.persistentsearchview.utils.Utils.TOOLBAR_TITLE_TYPEFACE;
import static com.sixtyninefourtwenty.persistentsearchview.utils.Utils.adjustColorAlpha;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.getAnimationMarker;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.getVisibilityMarker;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.isVisible;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.makeGone;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.makeInvisible;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.makeVisible;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.setAnimationMarker;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.setScale;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.setVisibilityMarker;
import static com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils.updateHeight;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sixtyninefourtwenty.persistentsearchview.adapters.SuggestionsRecyclerViewAdapter;
import com.sixtyninefourtwenty.persistentsearchview.adapters.resources.SuggestionItemResources;
import com.sixtyninefourtwenty.persistentsearchview.animations.BackgroundDimmingAnimation;
import com.sixtyninefourtwenty.persistentsearchview.listeners.AnimatorListenerAdapter;
import com.sixtyninefourtwenty.persistentsearchview.listeners.AnimatorListenerDecorator;
import com.sixtyninefourtwenty.persistentsearchview.listeners.OnSearchConfirmedListener;
import com.sixtyninefourtwenty.persistentsearchview.listeners.OnSearchQueryChangeListener;
import com.sixtyninefourtwenty.persistentsearchview.listeners.OnSuggestionChangeListener;
import com.sixtyninefourtwenty.persistentsearchview.listeners.QueryListener;
import com.sixtyninefourtwenty.persistentsearchview.model.Suggestion;
import com.sixtyninefourtwenty.persistentsearchview.utils.AnimationType;
import com.sixtyninefourtwenty.persistentsearchview.utils.KeyboardManagingUtil;
import com.sixtyninefourtwenty.persistentsearchview.utils.Preconditions;
import com.sixtyninefourtwenty.persistentsearchview.utils.StateUtils;
import com.sixtyninefourtwenty.persistentsearchview.utils.Utils;
import com.sixtyninefourtwenty.persistentsearchview.utils.ViewUtils;
import com.sixtyninefourtwenty.persistentsearchview.utils.VoiceRecognitionDelegate;
import com.sixtyninefourtwenty.persistentsearchview.widgets.AdvancedEditText;
import com.sixtyninefourtwenty.persistentsearchview.databinding.ViewPersistentSearchBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A search view designed to simplify the process of implementing
 * search-related functionality that also comes with a large set
 * of useful features.
 */
@SuppressWarnings("unused")
public class PersistentSearchView extends FrameLayout {


    private static final int ANIMATION_DURATION_BUTTON_SCALING = 100;
    private static final int BACKGROUND_ANIMATION_MIN_DURATION = 150;
    private static final int BACKGROUND_ANIMATION_MAX_DURATION = 250;

    private static final float DEFAULT_DIM_AMOUNT = 0.5f;

    private static final Interpolator BUTTON_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator SUGGESTIONS_CONTAINER_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();


    private boolean isDismissibleOnTouchOutside;
    private boolean isProgressBarEnabled;
    private boolean isVoiceInputButtonEnabled;
    private boolean isClearInputButtonEnabled;
    private boolean areSuggestionsDisabled;
    private boolean isSpeechRecognitionAvailable;
    private boolean shouldDimBehind;
    private boolean shouldNotifyAboutQueryChange;

    private int queryInputHintColor;
    private int queryInputTextColor;
    private int inputBarIconColor;
    private int dividerColor;
    private int progressBarColor;
    private int suggestionIconColor;
    private int recentSearchIconColor;
    private int searchSuggestionIconColor;
    private int suggestionTextColor;
    private int suggestionSelectedTextColor;
    private int cardBackgroundColor;
    private int backgroundDimColor;

    private int suggestionItemHeight;
    private int cardCornerRadius;
    private int cardElevation;

    private float dimAmount;

    private Drawable leftButtonDrawable;
    private Drawable rightButtonDrawable;
    private Drawable clearInputButtonDrawable;
    private Drawable voiceInputButtonDrawable;

    private String queryInputHint;

    private Typeface queryTextTypeface;
    private Typeface suggestionTextTypeface;

    private State state;

    private VoiceRecognitionDelegate voiceRecognitionDelegate;

    private List<Suggestion> suggestionItems;

    private SuggestionsRecyclerViewAdapter adapter;

    private ViewPersistentSearchBinding binding;
    private View dividerView;
    private ImageView leftBtnIv;
    private ImageView rightBtnIv;
    private ImageView clearInputBtnIv;
    private ImageView voiceInputBtnIv;
    private ProgressBar progressBar;
    private AdvancedEditText inputEt;
    private MaterialCardView cardView;
    private FrameLayout leftContainerFl;
    private FrameLayout inputButtonsContainerFl;
    private FrameLayout rightButtonContainerFl;
    private LinearLayout suggestionsContainerLL;
    private RecyclerView suggestionsRecyclerView;

    private ValueAnimator suggestionsContainerAnimator;
    private BackgroundDimmingAnimation backgroundEnterAnimation;
    private BackgroundDimmingAnimation backgroundExitAnimation;

    private OnSearchQueryChangeListener onSearchQueryChangeListener;
    private OnSuggestionChangeListener onSuggestionChangeListener;
    private OnSearchConfirmedListener onSearchConfirmedListener;
    private OnClickListener onLeftBtnClickListener;
    private OnClickListener onClearInputBtnClickListener;

    private Runnable exitAnimationEndAction;


    private enum State {

        EXPANDED,
        COLLAPSED

    }


    public PersistentSearchView(Context context) {
        super(context);
        init(null);
    }


    public PersistentSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public PersistentSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }


    @SuppressWarnings("NewApi")
    public PersistentSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    @SuppressWarnings("NewApi")
    private void init(AttributeSet attrs) {
        binding = ViewPersistentSearchBinding.inflate(LayoutInflater.from(getContext()), this);

        initDefaults();
        initResources(attrs);
        initMainContainer();
        initQueryInputBar();
        initSuggestionsContainer();

        collapse(false);
        drawOnTopOfAllOtherViews();
    }


    private void initDefaults() {
        dimAmount = DEFAULT_DIM_AMOUNT;
        suggestionItems = new ArrayList<>();

        initDefaultColors();
        initDefaultDimensions();
        initDefaultDrawables();
        initDefaultStrings();
        initDefaultTypefaces();
        initDefaultAnimations();
        initDefaultFlags();
    }


    private void initDefaultColors() {
        backgroundDimColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_background_dim_color);

        queryInputHintColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_query_input_hint_color);
        queryInputTextColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_query_input_text_color);

        inputBarIconColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_query_input_bar_icon_color);
        dividerColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_divider_color);
        progressBarColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_progress_bar_color);

        suggestionIconColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_suggestion_item_icon_color);
        recentSearchIconColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_suggestion_item_recent_search_icon_color);
        searchSuggestionIconColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_suggestion_item_search_suggestion_icon_color);
        suggestionTextColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_suggestion_item_text_color);
        suggestionSelectedTextColor = ContextCompat.getColor(getContext(), R.color.persistent_search_view_suggestion_item_selected_text_color);
    }


    private void initDefaultDimensions() {
        final Resources resources = getResources();

        suggestionItemHeight = resources.getDimensionPixelSize(R.dimen.persistent_search_view_item_height);
        cardCornerRadius = resources.getDimensionPixelSize(R.dimen.persistent_search_view_card_view_corner_radius);
        cardElevation = resources.getDimensionPixelSize(R.dimen.persistent_search_view_card_view_elevation);
    }


    private void initDefaultDrawables() {
        leftButtonDrawable = ContextCompat.getDrawable(getContext(), R.drawable.baseline_arrow_back_24);
        clearInputButtonDrawable = ContextCompat.getDrawable(getContext(), R.drawable.baseline_close_24);
        voiceInputButtonDrawable = ContextCompat.getDrawable(getContext(), R.drawable.baseline_mic_24);
    }


    private void initDefaultStrings() {
        queryInputHint = getResources().getString(R.string.persistent_search_view_query_input_hint);
    }


    private void initDefaultTypefaces() {
        queryTextTypeface = TOOLBAR_TITLE_TYPEFACE;
        suggestionTextTypeface = Typeface.DEFAULT;
    }


    private void initDefaultAnimations() {
        backgroundEnterAnimation = new BackgroundDimmingAnimation(
            this,
            backgroundDimColor,
            0f,
            dimAmount
        );

        backgroundExitAnimation = new BackgroundDimmingAnimation(
            this,
            backgroundDimColor,
            dimAmount,
            0f
        );
    }


    private void initDefaultFlags() {
        isSpeechRecognitionAvailable = Utils.isSpeechRecognitionAvailable(getContext());
        isProgressBarEnabled = true;
        isVoiceInputButtonEnabled = true;
        isClearInputButtonEnabled = true;
        isDismissibleOnTouchOutside = true;
        areSuggestionsDisabled = false;
        shouldDimBehind = true;
        shouldNotifyAboutQueryChange = true;
    }


    private void initResources(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        final TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.PersistentSearchView);

        initBooleanResources(attributes);
        initColorResources(attributes);
        initDimensionResources(attributes);
        initDrawableResources(attributes);
        initStringResources(attributes);

        attributes.recycle();
    }


    private void initBooleanResources(TypedArray attributes) {
        isDismissibleOnTouchOutside = attributes.getBoolean(R.styleable.PersistentSearchView_psv_isDismissableOnTouchOutside, isDismissibleOnTouchOutside);
        isProgressBarEnabled = attributes.getBoolean(R.styleable.PersistentSearchView_psv_isProgressBarEnabled, isProgressBarEnabled);
        isVoiceInputButtonEnabled = attributes.getBoolean(R.styleable.PersistentSearchView_psv_isVoiceInputButtonEnabled, isVoiceInputButtonEnabled);
        isClearInputButtonEnabled = attributes.getBoolean(R.styleable.PersistentSearchView_psv_isClearInputButtonEnabled, isClearInputButtonEnabled);
        areSuggestionsDisabled = attributes.getBoolean(R.styleable.PersistentSearchView_psv_areSuggestionsDisabled, areSuggestionsDisabled);
        shouldDimBehind = attributes.getBoolean(R.styleable.PersistentSearchView_psv_shouldDimBehind, shouldDimBehind);
    }


    private void initColorResources(TypedArray attributes) {
        backgroundDimColor = attributes.getColor(R.styleable.PersistentSearchView_psv_dimColor, backgroundDimColor);

        queryInputHintColor = attributes.getColor(R.styleable.PersistentSearchView_psv_queryInputHintColor, queryInputHintColor);
        queryInputTextColor = attributes.getColor(R.styleable.PersistentSearchView_psv_queryInputTextColor, queryInputTextColor);

        inputBarIconColor = attributes.getColor(R.styleable.PersistentSearchView_psv_queryInputBarIconColor, inputBarIconColor);
        dividerColor = attributes.getColor(R.styleable.PersistentSearchView_psv_dividerColor, dividerColor);
        progressBarColor = attributes.getColor(R.styleable.PersistentSearchView_psv_progressBarColor, progressBarColor);

        suggestionIconColor = attributes.getColor(R.styleable.PersistentSearchView_psv_suggestionIconColor, suggestionIconColor);
        recentSearchIconColor = attributes.getColor(R.styleable.PersistentSearchView_psv_suggestionRecentSearchIconColor, recentSearchIconColor);
        searchSuggestionIconColor = attributes.getColor(R.styleable.PersistentSearchView_psv_suggestionSearchSuggestionIconColor, searchSuggestionIconColor);
        suggestionTextColor = attributes.getColor(R.styleable.PersistentSearchView_psv_suggestionTextColor, suggestionTextColor);
        suggestionSelectedTextColor = attributes.getColor(R.styleable.PersistentSearchView_psv_suggestionSelectedTextColor, suggestionSelectedTextColor);

        cardBackgroundColor = attributes.getColor(R.styleable.PersistentSearchView_psv_cardBackgroundColor, 0);
    }


    private void initDimensionResources(TypedArray attributes) {
        dimAmount = attributes.getFloat(R.styleable.PersistentSearchView_psv_dimAmount, dimAmount);
        cardCornerRadius = attributes.getDimensionPixelSize(R.styleable.PersistentSearchView_psv_cardCornerRadius, cardCornerRadius);
        cardElevation = attributes.getDimensionPixelSize(R.styleable.PersistentSearchView_psv_cardElevation, cardElevation);
    }


    private void initDrawableResources(TypedArray attributes) {
        if(attributes.hasValue(R.styleable.PersistentSearchView_psv_leftButtonDrawable)) {
            leftButtonDrawable = attributes.getDrawable(R.styleable.PersistentSearchView_psv_leftButtonDrawable);
        }

        if(attributes.hasValue(R.styleable.PersistentSearchView_psv_rightButtonDrawable)) {
            rightButtonDrawable = attributes.getDrawable(R.styleable.PersistentSearchView_psv_rightButtonDrawable);
        }

        if(attributes.hasValue(R.styleable.PersistentSearchView_psv_clearInputButtonDrawable)) {
            clearInputButtonDrawable = attributes.getDrawable(R.styleable.PersistentSearchView_psv_clearInputButtonDrawable);
        }

        if(attributes.hasValue(R.styleable.PersistentSearchView_psv_voiceInputButtonDrawable)) {
            voiceInputButtonDrawable = attributes.getDrawable(R.styleable.PersistentSearchView_psv_voiceInputButtonDrawable);
        }
    }


    private void initStringResources(TypedArray attributes) {
        if(attributes.hasValue(R.styleable.PersistentSearchView_psv_queryInputHint)) {
            queryInputHint = attributes.getString(R.styleable.PersistentSearchView_psv_queryInputHint);
        }
    }


    private void initMainContainer() {
        cardView = binding.cardView;
        if (cardBackgroundColor != 0) {
            setCardBackgroundColor(cardBackgroundColor);
        }
        setCardCornerRadius(cardCornerRadius);
        setCardElevation(cardElevation);
        setOnClickListener(mOnParentOutsideClickListener);
    }


    private void initQueryInputBar() {
        initQueryInputEditText();
        initQueryInputBarProgressBar();
        initQueryInputBarLeftButton();
        iniQueryInputBarButtonsContainers();

        setQueryInputBarIconColor(inputBarIconColor);
    }


    private void initQueryInputEditText() {
        inputEt = binding.inputEt;
        setQueryInputHint(queryInputHint);
        setQueryInputHintColor(queryInputHintColor);
        setQueryInputTextColor(queryInputTextColor);
        setQueryTextTypeface(queryTextTypeface);
        inputEt.setOnEditorActionListener(mInternalEditorActionListener);
        inputEt.addTextChangedListener(mQueryListener);
        inputEt.setTouchEventInterceptor(mInputEditTextTouchEventInterceptor);
    }


    private void initQueryInputBarProgressBar() {
        progressBar = binding.progressBar;
        setProgressBarColor(progressBarColor);
    }


    private void initQueryInputBarLeftButton() {
        leftContainerFl = binding.leftContainerFl;

        leftBtnIv = binding.leftBtnIv;
        setLeftButtonDrawable(leftButtonDrawable);
        leftBtnIv.setOnClickListener(mOnLeftButtonClickListener);
    }


    private void iniQueryInputBarButtonsContainers() {
        inputButtonsContainerFl = binding.inputBtnsContainerFl;

        initQueryInputBarRightButton();
        initQueryInputBarClearInputButton();
        initQueryInputBarVoiceInputButton();
    }


    private void initQueryInputBarRightButton() {
        rightButtonContainerFl = binding.rightBtnContainerFl;

        rightBtnIv = binding.rightBtnIv;
        setRightButtonDrawable(rightButtonDrawable);
    }


    private void initQueryInputBarClearInputButton() {
        clearInputBtnIv = binding.clearInputBtnIv;
        setClearInputButtonDrawable(clearInputButtonDrawable);
        updateClearInputButtonState();
        clearInputBtnIv.setOnClickListener(mOnClearInputButtonClickListener);
    }


    private void initQueryInputBarVoiceInputButton() {
        voiceInputBtnIv = binding.voiceInputBtnIv;
        setVoiceInputButtonDrawable(voiceInputButtonDrawable);
        updateVoiceInputButtonState();
        voiceInputBtnIv.setOnClickListener(mOnVoiceInputButtonClickListener);
    }


    private void initSuggestionsContainer() {
        dividerView = binding.divider;
        setDividerColor(dividerColor);

        suggestionsContainerLL = binding.suggestionsContainerLl;

        initSuggestionsRecyclerView();
    }


    private void initSuggestionsRecyclerView() {
        suggestionsRecyclerView = binding.suggestionsRecyclerView;
        Utils.disableRecyclerViewAnimations(suggestionsRecyclerView);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionsRecyclerView.addOnScrollListener(mSuggestionsRecyclerViewScrollListener);

        initSuggestionsAdapter();
    }


    private void initSuggestionsAdapter() {
        adapter = new SuggestionsRecyclerViewAdapter(
                new SuggestionItemResources(),
                onSuggestionClickListener,
                onRemoveButtonClickListener
        );
        setRecentSearchIconColor(recentSearchIconColor);
        setSearchSuggestionIconColor(searchSuggestionIconColor);
        setSuggestionIconColor(suggestionIconColor);
        setSuggestionTextColor(suggestionTextColor);
        setSuggestionSelectedTextColor(suggestionSelectedTextColor);
        setSuggestionTextTypeface(suggestionTextTypeface);
        setAdapterQuery(getInputQuery());

        suggestionsRecyclerView.setAdapter(adapter);
    }


    private void drawOnTopOfAllOtherViews() {
        setTranslationZ(999);
    }


    private void updateLeftContainerVisibility() {
        if(ViewUtils.isVisible(leftBtnIv) || isProgressBarEnabled) {
            ViewUtils.makeVisible(leftContainerFl);
        } else {
            ViewUtils.makeGone(leftContainerFl);
        }
    }


    private void updateVoiceInputButtonState() {
        if(isVoiceInputEnabled()) {
            final boolean isInputQueryEmpty = isInputQueryEmpty();

            ViewUtils.setScale(voiceInputBtnIv, (isInputQueryEmpty ? 1f : 0f));
            ViewUtils.setVisibility(voiceInputBtnIv, (isInputQueryEmpty ? View.VISIBLE : View.GONE));
        } else {
            ViewUtils.setVisibility(voiceInputBtnIv, View.GONE);
        }

        updateInputButtonsContainerVisibility();
    }


    private void updateClearInputButtonState() {
        if(isClearInputButtonEnabled) {
            final boolean isInputQueryEmpty = isInputQueryEmpty();

            ViewUtils.setScale(clearInputBtnIv, (isInputQueryEmpty ? 0f : 1f));
            ViewUtils.setVisibility(clearInputBtnIv, (isInputQueryEmpty ? View.GONE : View.VISIBLE));
        } else {
            ViewUtils.setVisibility(clearInputBtnIv, View.GONE);
        }

        updateInputButtonsContainerVisibility();
    }


    private void updateInputButtonsContainerVisibility() {
        if(isVoiceInputEnabled() || isClearInputButtonEnabled) {
            ViewUtils.makeVisible(inputButtonsContainerFl);
        } else {
            ViewUtils.makeGone(inputButtonsContainerFl);
        }
    }


    private void showClearInputButtonWithVoiceInputButton(boolean animate) {
        if(isClearInputButtonVisible()) {
            hideVoiceInputButton(false);
        } else {
            if(isVoiceInputEnabled() && isVoiceInputButtonVisible()) {
                if(animate) {
                    hideVoiceInputButton(
                        true,
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnded(Animator animation) {
                                showClearInputButton(true);
                            }
                        }
                    );
                } else {
                    hideVoiceInputButton(false);
                    showClearInputButton(false);
                }
            } else {
                showClearInputButton(animate);
            }
        }
    }


    private void showClearInputButton() {
        showClearInputButton(true);
    }


    private void showClearInputButton(boolean animate) {
        showClearInputButton(animate, null);
    }


    private void showClearInputButton(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isClearInputButtonEnabled ||
            getVisibilityMarker(clearInputBtnIv) ||
            (animate && AnimationType.ENTER.equals(getAnimationMarker(clearInputBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(clearInputBtnIv);
        makeVisible(clearInputBtnIv);
        setVisibilityMarker(clearInputBtnIv, true);

        if(animate) {
            clearInputBtnIv.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(clearInputBtnIv, AnimationType.ENTER);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        setAnimationMarker(clearInputBtnIv, AnimationType.NO_ANIMATION);
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(clearInputBtnIv, 1f);
            setAnimationMarker(clearInputBtnIv, AnimationType.NO_ANIMATION);
        }
    }


    private void hideClearInputButtonWithVoiceInputButton(boolean animate) {
        if(isVoiceInputEnabled()) {
            if(animate) {
                hideClearInputButton(
                    true,
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnded(Animator animation) {
                            showVoiceInputButton(true);
                        }
                    }
                );
            } else {
                hideClearInputButton(false);
                showVoiceInputButton(false);
            }
        } else {
            hideClearInputButton(animate);
        }
    }


    private void hideClearInputButton() {
        hideClearInputButton(true);
    }


    private void hideClearInputButton(boolean animate) {
        hideClearInputButton(animate, null);
    }


    private void hideClearInputButton(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isClearInputButtonEnabled ||
            !getVisibilityMarker(clearInputBtnIv) ||
            (animate && AnimationType.EXIT.equals(getAnimationMarker(clearInputBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(clearInputBtnIv);
        setVisibilityMarker(clearInputBtnIv, false);

        if(animate) {
            clearInputBtnIv.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(clearInputBtnIv, AnimationType.EXIT);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        makeGone(clearInputBtnIv);
                        setAnimationMarker(clearInputBtnIv, AnimationType.NO_ANIMATION);
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(clearInputBtnIv, 0f);
            makeGone(clearInputBtnIv);
            setAnimationMarker(clearInputBtnIv, AnimationType.NO_ANIMATION);
        }
    }


    private boolean isClearInputButtonVisible() {
        return isVisible(clearInputBtnIv);
    }


    private void showVoiceInputButton() {
        showVoiceInputButton(true);
    }


    private void showVoiceInputButton(boolean animate) {
        showVoiceInputButton(animate, null);
    }


    private void showVoiceInputButton(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isVoiceInputEnabled() ||
            getVisibilityMarker(voiceInputBtnIv) ||
            (animate && AnimationType.ENTER.equals(getAnimationMarker(voiceInputBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(voiceInputBtnIv);
        makeVisible(voiceInputBtnIv);
        setVisibilityMarker(voiceInputBtnIv, true);

        if(animate) {
            voiceInputBtnIv.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(voiceInputBtnIv, AnimationType.ENTER);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        setAnimationMarker(voiceInputBtnIv, AnimationType.NO_ANIMATION);
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(voiceInputBtnIv, 1f);
            setAnimationMarker(voiceInputBtnIv, AnimationType.NO_ANIMATION);
        }
    }


    private void hideVoiceInputButton() {
        hideVoiceInputButton(true);
    }


    private void hideVoiceInputButton(boolean animate) {
        hideVoiceInputButton(animate, null);
    }


    private void hideVoiceInputButton(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isVoiceInputEnabled() ||
            !getVisibilityMarker(voiceInputBtnIv) ||
            (animate && AnimationType.EXIT.equals(getAnimationMarker(voiceInputBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(voiceInputBtnIv);
        setVisibilityMarker(voiceInputBtnIv, false);

        if(animate) {
            voiceInputBtnIv.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(voiceInputBtnIv, AnimationType.EXIT);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        makeGone(voiceInputBtnIv);
                        setAnimationMarker(voiceInputBtnIv, AnimationType.NO_ANIMATION);
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(voiceInputBtnIv, 0f);
            makeGone(voiceInputBtnIv);
            setAnimationMarker(voiceInputBtnIv, AnimationType.NO_ANIMATION);
        }
    }


    private boolean isVoiceInputButtonVisible() {
        return isVisible(voiceInputBtnIv);
    }


    /**
     * Shows the left button by animating it.
     */
    public final void showLeftButton() {
        showLeftButton(true);
    }


    /**
     * Shows the left button by either animating it or not.
     *
     * @param animate Whether to use animation when showing the left button
     */
    public final void showLeftButton(boolean animate) {
        showLeftButtonInternal(animate);
    }


    /**
     * Shows the left button by animating it and hides the progress bar if shown.
     */
    public final void showLeftButtonWithProgressBar() {
        showLeftButtonWithProgressBar(true);
    }


    /**
     * Shows the left button by either animating it or not and hides the progress bar if shown.
     *
     * @param animate Whether to use animation when showing the left button and hiding the progress bar
     */
    public final void showLeftButtonWithProgressBar(boolean animate) {
        hideProgressBarWithLeftButton(animate);
    }


    /**
     * Hides the right button by animating it.
     */
    public final void hideLeftButton() {
        hideLeftButton(true);
    }


    /**
     * Hides the right button by either animating it or not.
     *
     * @param animate Whether to use animation when showing the left button
     */
    public final void hideLeftButton(boolean animate) {
        hideLeftButtonInternal(animate);
    }


    /**
     * Hides the left button by animating it and hides the progress bar if shown.
     */
    public final void hideLeftButtonWithProgressBar() {
        hideLeftButtonWithProgressBar(true);
    }


    /**
     * Hides the left button by either animating it or not and shows the progress bar if shown.
     *
     * @param animate Whether to use animation when hiding the left button and showing the progress bar
     */
    public final void hideLeftButtonWithProgressBar(boolean animate) {
        showProgressBarWithLeftButton(animate);
    }


    private void showLeftButtonInternal(boolean animate) {
        showLeftButtonInternal(animate, null);
    }


    private void showLeftButtonInternal(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(getVisibilityMarker(leftBtnIv) ||
            (animate && AnimationType.ENTER.equals(getAnimationMarker(leftBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(leftBtnIv);
        makeVisible(leftBtnIv);
        setVisibilityMarker(leftBtnIv, true);

        if(animate) {
            leftBtnIv.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(leftBtnIv, AnimationType.ENTER);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        setAnimationMarker(leftBtnIv, AnimationType.NO_ANIMATION);
                        updateLeftContainerVisibility();
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(leftBtnIv, 1f);
            setAnimationMarker(leftBtnIv, AnimationType.NO_ANIMATION);
            updateLeftContainerVisibility();
        }
    }


    private void hideLeftButtonInternal(boolean animate) {
        hideLeftButtonInternal(animate, null);
    }


    private void hideLeftButtonInternal(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!getVisibilityMarker(leftBtnIv) ||
            (animate && AnimationType.EXIT.equals(getAnimationMarker(leftBtnIv)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(leftBtnIv);
        setVisibilityMarker(leftBtnIv, false);

        if(animate) {
            leftBtnIv.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(leftBtnIv, AnimationType.EXIT);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        makeGone(leftBtnIv);
                        setAnimationMarker(leftBtnIv, AnimationType.NO_ANIMATION);
                        updateLeftContainerVisibility();
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(leftBtnIv, 0f);
            makeGone(leftBtnIv);
            setAnimationMarker(leftBtnIv, AnimationType.NO_ANIMATION);
            updateLeftContainerVisibility();
        }
    }


    /**
     * Checks whether the left button is visible (i.e., its visibility flags is {@link View#VISIBLE}).
     *
     * @return true if visible; false otherwise
     */
    public final boolean isLeftButtonVisible() {
        return isVisible(leftBtnIv);
    }


    /**
     * Shows the right button (i.e., change its visibility flags to {@link View#VISIBLE}.
     */
    public final void showRightButton() {
        makeVisible(rightButtonContainerFl);
    }


    /**
     * Hides the right button (i.e., change its visibility flags to {@link View#GONE}.
     */
    public final void hideRightButton() {
        makeGone(rightButtonContainerFl);
    }


    /**
     * Shows the progress bar by animating it.
     */
    public final void showProgressBar() {
        showProgressBar(true);
    }


    /**
     * Shows the progress bar by either animating it or not.
     *
     * @param animate Whether to use animation when showing the progress bar
     */
    public final void showProgressBar(boolean animate) {
        showProgressBarInternal(animate);
    }


    /**
     * Hides the progress bar by animating it.
     */
    public final void hideProgressBar() {
        hideProgressBar(true);
    }


    /**
     * Hides the progress bar by either animating it or not.
     *
     * @param animate Whether to use animation when hiding the progress bar
     */
    public final void hideProgressBar(boolean animate) {
        hideProgressBarInternal(animate);
    }


    /**
     * Shows the progress bar by animating it and hides the left button if shown.
     */
    public final void showProgressBarWithLeftButton() {
        showProgressBarWithLeftButton(true);
    }


    /**
     * Shows the progress bar by either animating it or not and hides the left button if shown.
     *
     * @param animate Whether to use animation when showing the progress bar and hiding the left button
     */
    public final void showProgressBarWithLeftButton(boolean animate) {
        if(!isLeftButtonVisible()) {
            showProgressBarInternal(animate);
        } else {
            if(animate) {
                hideLeftButtonInternal(
                    true,
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnded(Animator animation) {
                            showProgressBarInternal(true);
                        }
                    }
                );
            } else {
                hideLeftButtonInternal(false);
                showProgressBarInternal(false);
            }
        }
    }


    /**
     * Hides the progress bar by animating it and shows the left button if hidden.
     */
    public final void hideProgressBarWithLeftButton() {
        hideProgressBarWithLeftButton(true);
    }


    /**
     * Hides the progress bar by either animating it or not and shows the left button if hidden.
     *
     * @param animate Whether to use animation for hiding the progress bar and showing the left button
     */
    public final void hideProgressBarWithLeftButton(boolean animate) {
        if(!isProgressBarVisible()) {
            showLeftButtonInternal(animate);
        } else {
            if(animate) {
                hideProgressBarInternal(
                    true,
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnded(Animator animation) {
                            showLeftButtonInternal(true);
                        }
                    }
                );
            } else {
                hideProgressBarInternal(false);
                showLeftButtonInternal(false);
            }
        }
    }


    private void showProgressBarInternal(boolean animate) {
        showProgressBarInternal(animate, null);
    }


    private void showProgressBarInternal(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isProgressBarEnabled ||
            getVisibilityMarker(progressBar) ||
            (animate && AnimationType.ENTER.equals(getAnimationMarker(progressBar)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(progressBar);
        makeVisible(progressBar);
        setVisibilityMarker(progressBar, true);

        if(animate) {
            progressBar.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(progressBar, AnimationType.ENTER);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        setAnimationMarker(progressBar, AnimationType.NO_ANIMATION);
                        updateLeftContainerVisibility();
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(progressBar, 1f);
            setAnimationMarker(progressBar, AnimationType.NO_ANIMATION);
            updateLeftContainerVisibility();
        }
    }


    private void hideProgressBarInternal(boolean animate) {
        hideProgressBarInternal(animate, null);
    }


    private void hideProgressBarInternal(boolean animate, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if(!isProgressBarEnabled ||
            !getVisibilityMarker(progressBar) ||
            (animate && AnimationType.EXIT.equals(getAnimationMarker(progressBar)))) {
            return;
        }

        ViewUtils.cancelAllAnimations(progressBar);
        setVisibilityMarker(progressBar, false);

        if(animate) {
            progressBar.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setListener(new AnimatorListenerDecorator(animatorListenerAdapter) {

                    @Override
                    public void onAnimationStarted(Animator animation) {
                        super.onAnimationStarted(animation);
                        setAnimationMarker(progressBar, AnimationType.EXIT);
                    }

                    @Override
                    public void onAnimationEnded(Animator animation) {
                        super.onAnimationEnded(animation);
                        makeGone(progressBar);
                        setAnimationMarker(progressBar, AnimationType.NO_ANIMATION);
                        updateLeftContainerVisibility();
                    }

                })
                .setInterpolator(BUTTON_ANIMATION_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION_BUTTON_SCALING)
                .start();
        } else {
            setScale(progressBar, 0f);
            makeGone(progressBar);
            setAnimationMarker(progressBar, AnimationType.NO_ANIMATION);
            updateLeftContainerVisibility();
        }
    }


    /**
     * Checks whether the progress bar is visible (i.e., its visibility flags is {@link View#VISIBLE}.
     *
     * @return true if visible; false otherwise
     */
    public final boolean isProgressBarVisible() {
        return isVisible(progressBar);
    }


    private void showKeyboard() {
        inputEt.requestFocus();
        KeyboardManagingUtil.showKeyboard(inputEt);
    }


    private void hideKeyboard() {
        inputEt.clearFocus();
        KeyboardManagingUtil.hideKeyboard(inputEt);
    }


    /**
     * Confirms the search action by invoking the editor action listener.
     */
    public final void confirmSearchAction() {
        if(mInternalEditorActionListener != null) {
            mInternalEditorActionListener.onEditorAction(
                inputEt,
                EditorInfo.IME_ACTION_SEARCH,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SEARCH)
            );
        }
    }


    private void postExitAnimationEndActionEvent(long delay) {
        cancelExitAnimationEndActionEvent();

        exitAnimationEndAction = () -> {
            requestLayout();
            exitAnimationEndAction = null;
        };

        postDelayed(exitAnimationEndAction, delay);
    }


    private void cancelExitAnimationEndActionEvent() {
        if(exitAnimationEndAction != null) {
            removeCallbacks(exitAnimationEndAction);
            exitAnimationEndAction = null;
        }
    }


    /**
     * Expands the search view, i.e. changes its state to {@link State#EXPANDED}, by animating it.
     */
    public final void expand() {
        expand(true);
    }


    /**
     * Expands the search view, i.e. changes its state to {@link State#EXPANDED}, by either animating
     * it or not.
     *
     * @param animate Whether to animate the expansion or not
     */
    public final void expand(boolean animate) {
        if(isExpanded()) {
            return;
        }

        setEnabled(true);
        setClickable(true);
        setState(State.EXPANDED);

        inputEt.setEnabled(true);
        inputEt.setSelection(inputEt.length());

        showKeyboard();
        cancelExitAnimationEndActionEvent();
        updateBackground(state, animate);

        if(!areSuggestionsDisabled()) {
            makeVisible(suggestionsContainerLL);
            suggestionsContainerLL.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getSuggestionsContainerMaxHeight(), MeasureSpec.AT_MOST)
            );

            makeVisible(dividerView);
            updateSuggestionsContainerHeight(
                state,
                0,
                suggestionsContainerLL.getMeasuredHeight(),
                animate
            );
        }

        requestLayout();
    }


    /**
     * Collapses the search view, i.e. changes its state to {@link State#COLLAPSED}, by animating it.
     */
    public final void collapse() {
        collapse(true);
    }


    /**
     * Collapses the search view, i.e. changes its state to {@link State#COLLAPSED}, by either animating
     * it or not.
     *
     * @param animate Whether to animate the collapse or not
     */
    public final void collapse(boolean animate) {
        if(!isExpanded() || (animate && isExitAnimationRunning())) {
            return;
        }

        setEnabled(false);
        setClickable(false);
        setState(State.COLLAPSED);

        inputEt.setEnabled(false);

        hideKeyboard();
        cancelExitAnimationEndActionEvent();

        final long duration = getSuggestionsContainerAnimationDuration(suggestionsContainerLL.getMeasuredHeight(), 0);

        updateBackgroundWithAnimation(state, duration);

        if(!areSuggestionsDisabled()) {
            makeInvisible(dividerView);
            updateSuggestionsContainerHeightWithAnimation(
                state,
                suggestionsContainerLL.getMeasuredHeight(),
                0,
                duration
            );
        }

        if(animate) {
            postExitAnimationEndActionEvent(duration);
        } else {
            requestLayout();
        }
    }


    private void updateBackgroundWithAnimation(State state) {
        updateBackgroundWithAnimation(state, BACKGROUND_ANIMATION_MAX_DURATION);
    }


    private void updateBackgroundWithAnimation(State state, long duration) {
        updateBackground(state, duration, true);
    }


    private void updateBackground(State state, boolean animate) {
        updateBackground(state, BACKGROUND_ANIMATION_MAX_DURATION, animate);
    }


    private void updateBackground(State state,
                                  long duration,
                                  boolean animate) {
        if(!shouldDimBehind) {
            setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        if(State.EXPANDED.equals(state)) {
            if(animate) {
                backgroundEnterAnimation.setDuration(duration).start();
            } else {
                setBackgroundColor(adjustColorAlpha(backgroundDimColor, dimAmount));
            }
        } else {
            if(animate) {
                backgroundExitAnimation.setDuration(duration).start();
            } else {
                setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }


    private void updateSuggestionsContainerHeightWithAnimation(
        final State state,
        final int fromHeight,
        final int toHeight,
        final long duration
    ) {
        updateSuggestionsContainerHeight(
            state,
            fromHeight,
            toHeight,
            duration,
            true
        );
    }


    private void updateSuggestionsContainerHeight(
        final State state,
        final int fromHeight,
        final int toHeight,
        final boolean animate
    ) {
        updateSuggestionsContainerHeight(
            state,
            fromHeight,
            toHeight,
            BACKGROUND_ANIMATION_MAX_DURATION,
            animate
        );
    }


    private void updateSuggestionsContainerHeight(
        final State state,
        final int fromHeight,
        final int toHeight,
        final long duration,
        final boolean animate
    ) {
        cancelSuggestionsContainerAnimation();
        updateDividerVisibility();

        if(animate && (fromHeight != toHeight)) {
            suggestionsContainerAnimator = ValueAnimator.ofInt(fromHeight, toHeight);
            suggestionsContainerAnimator.addUpdateListener(animation -> {
                final int newHeight = (Integer) animation.getAnimatedValue();
                updateHeight(suggestionsContainerLL, newHeight);
            });
            suggestionsContainerAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnded(Animator animation) {
                    if(State.COLLAPSED.equals(state)) {
                        suggestionsContainerLL.setVisibility(View.GONE);
                    }
                }
            });
            suggestionsContainerAnimator.setInterpolator(SUGGESTIONS_CONTAINER_ANIMATION_INTERPOLATOR);
            suggestionsContainerAnimator.setDuration(duration);
            suggestionsContainerAnimator.start();
        } else {
            updateHeight(suggestionsContainerLL, toHeight);

            if(State.COLLAPSED.equals(state)) {
                suggestionsContainerLL.setVisibility(View.GONE);
            }
        }
    }


    private void cancelSuggestionsContainerAnimation() {
        if(suggestionsContainerAnimator != null) {
            suggestionsContainerAnimator.cancel();
        }
    }


    private void updateDividerVisibility() {
        if(suggestionItems.isEmpty()) {
            makeGone(dividerView);
        } else {
            makeVisible(dividerView);
        }
    }


    private void cancelAllAnimations() {
        cancelSuggestionsContainerAnimation();

        backgroundEnterAnimation.stop();
        backgroundExitAnimation.stop();
    }


    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        cancelExitAnimationEndActionEvent();
        cancelAllAnimations();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        final int[] measuredSize;

        if(isExpanded() || isExitAnimationRunning()) {
            measuredSize = Utils.getScreenSize(getContext());
        } else {
            measuredSize = calculateTheUsedSpace(widthMeasureSpec);
        }

        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }


    private int[] calculateTheUsedSpace(int widthMeasureSpec) {
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measuredHeight = (cardView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());

        return new int[] { measuredWidth, measuredHeight };
    }


    private void setState(State state) {
        this.state = state;
    }


    private void setAdapterQuery(String query) {
        adapter.setResources(getAdapterResources().setCurrentQuery(query));
    }


    private SuggestionItemResources getAdapterResources() {
        return adapter.getResources();
    }


    /**
     * Sets the suggestions items for this search view.
     *
     * @param suggestions The suggestions to set
     */
    public final void setSuggestions(@NonNull List<? extends Suggestion> suggestions) {
        setSuggestions(suggestions, true);
    }


    /**
     * Sets the suggestion items for this search view.
     *
     * @param suggestions The suggestions to set
     * @param expandIfNecessary Whether to expand the search view if it's in a collapsed state
     */
    @SuppressWarnings("unchecked")
    public final void setSuggestions(@NonNull List<? extends Suggestion> suggestions, boolean expandIfNecessary) {
        Preconditions.nonNull(suggestions);

        if(isExpanded()) {
            final int currentHeight = suggestionsContainerLL.getMeasuredHeight();

            adapter.submitList(suggestionItems = (List<Suggestion>) suggestions, () -> {
                remeasureSuggestionsContainer();
                updateSuggestionsContainerHeightWithAnimation(
                        state,
                        currentHeight,
                        suggestionsContainerLL.getMeasuredHeight(),
                        getSuggestionsContainerAnimationDuration(currentHeight, suggestionsContainerLL.getMeasuredHeight())
                );
            });
        } else {
            adapter.submitList(suggestionItems = (List<Suggestion>) suggestions, () -> {
                if(expandIfNecessary) {
                    expand();
                }
            });
        }
    }


    private void remeasureSuggestionsContainer() {
        suggestionsContainerLL.measure(
            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(getSuggestionsContainerMaxHeight(), MeasureSpec.AT_MOST)
        );
    }


    /**
     * Sets the color of the query input hint.
     *
     * @param color The color to set
     */
    public final void setQueryInputHintColor(@ColorInt int color) {
        queryInputHintColor = color;

        inputEt.setHintTextColor(color);
    }


    /**
     * Sets the color of the query input text.
     *
     * @param textColor The color to set
     */
    public final void setQueryInputTextColor(@ColorInt int textColor) {
        queryInputTextColor = textColor;

        inputEt.setTextColor(textColor);
    }


    /**
     * Sets the gravity of the query input view.
     *
     * @param gravity The gravity to set
     */
    public final void setQueryInputGravity(int gravity) {
        inputEt.setGravity(gravity);
    }


    /**
     * Sets the color of the query input bar icons.
     *
     * @param color The color to set
     */
    public final void setQueryInputBarIconColor(@ColorInt int color) {
        inputBarIconColor = color;

        setLeftButtonDrawable(Utils.getColoredDrawable(leftButtonDrawable, color));
        setRightButtonDrawable(Utils.getColoredDrawable(rightButtonDrawable, color));
        setClearInputButtonDrawable(Utils.getColoredDrawable(clearInputButtonDrawable, color));
        setVoiceInputButtonDrawable(Utils.getColoredDrawable(voiceInputButtonDrawable, color));
    }


    /**
     * Sets the color of the divider.
     *
     * @param color The color to set
     */
    public final void setDividerColor(@ColorInt int color) {
        dividerColor = color;

        dividerView.setBackgroundColor(color);
    }


    /**
     * Sets the color of the progress bar.
     *
     * @param color The color to set
     */
    public final void setProgressBarColor(@ColorInt int color) {
        progressBarColor = color;

        Utils.setProgressBarColor(progressBar, color);
    }


    /**
     * Sets the color of the suggestion icon.
     *
     * @param color The color to set
     */
    public final void setSuggestionIconColor(@ColorInt int color) {
        suggestionIconColor = color;

        adapter.setResources(getAdapterResources().setIconColor(color));
    }


    /**
     * Sets the color of the recent search icon.
     *
     * @param color The color to set
     */
    public final void setRecentSearchIconColor(@ColorInt int color) {
        recentSearchIconColor = color;

        adapter.setResources(getAdapterResources().setRecentSearchIconColor(color));
    }


    /**
     * Sets the color of the search suggestion icon.
     *
     * @param color The color to set
     */
    public final void setSearchSuggestionIconColor(@ColorInt int color) {
        searchSuggestionIconColor = color;

        adapter.setResources(getAdapterResources().setSearchSuggestionIconColor(color));
    }


    /**
     * Sets the color of the suggestion text.
     *
     * @param color The color to set
     */
    public final void setSuggestionTextColor(@ColorInt int color) {
        suggestionTextColor = color;

        adapter.setResources(getAdapterResources().setTextColor(color));
    }


    /**
     * Sets the color of the selected suggestion text.
     *
     * @param color The color to set
     */
    public final void setSuggestionSelectedTextColor(@ColorInt int color) {
        suggestionSelectedTextColor = color;

        adapter.setResources(getAdapterResources().setSelectedTextColor(color));
    }


    /**
     * Sets the color of the card background.
     *
     * @param color The color to set
     */
    public final void setCardBackgroundColor(@ColorInt int color) {
        cardBackgroundColor = color;

        cardView.setCardBackgroundColor(color);
    }


    /**
     * Sets the color of the background dim.
     *
     * @param color The color to set
     */
    public final void setBackgroundDimColor(@ColorInt int color) {
        backgroundDimColor = color;

        backgroundEnterAnimation.setDimColor(color);
        backgroundExitAnimation.setDimColor(color);
    }


    /**
     * Sets the radius of the card corner.
     *
     * @param cornerRadius The corner radius to set
     */
    public final void setCardCornerRadius(int cornerRadius) {
        cardCornerRadius = cornerRadius;

        cardView.setRadius(cardCornerRadius);
    }


    /**
     * Sets the elevation of the card.
     *
     * @param cardElevation The elevation to set
     */
    public final void setCardElevation(int cardElevation) {
        this.cardElevation = cardElevation;

        cardView.setCardElevation(cardElevation);
        cardView.setMaxCardElevation(cardElevation);
    }


    /**
     * Sets the amount of the background dim.
     *
     * @param dimAmount The dim amount to set
     */
    public final void setBackgroundDimAmount(float dimAmount) {
        this.dimAmount = dimAmount;

        backgroundEnterAnimation.setToAlpha(dimAmount);
        backgroundExitAnimation.setFromAlpha(dimAmount);
    }


    /**
     * Sets the drawable of the left button.
     *
     * @param drawableResId The resource ID of the drawable
     */
    public final void setLeftButtonDrawable(@DrawableRes int drawableResId) {
        setLeftButtonDrawable(
            Utils.getColoredDrawable(
                getContext(),
                drawableResId,
                inputBarIconColor
            )
        );
    }


    /**
     * Sets the drawable of the left button.
     *
     * @param drawable The drawable to set
     */
    public final void setLeftButtonDrawable(Drawable drawable) {
        leftButtonDrawable = drawable;

        leftBtnIv.setImageDrawable(drawable);
    }


    /**
     * Sets the drawable of the right button.
     *
     * @param drawableResId The resource ID of the drawable
     */
    public final void setRightButtonDrawable(@DrawableRes int drawableResId) {
        setRightButtonDrawable(
            Utils.getColoredDrawable(
                getContext(),
                drawableResId,
                inputBarIconColor
            )
        );
    }


    /**
     * Sets the drawable of the right button.
     *
     * @param drawable The drawable to set
     */
    public final void setRightButtonDrawable(Drawable drawable) {
        rightButtonDrawable = drawable;

        rightBtnIv.setImageDrawable(drawable);
        if (drawable == null) {
            hideRightButton();
        } else {
            showRightButton();
        }
    }


    /**
     * Sets the drawable of the clear input button.
     *
     * @param drawableResId The resource ID of the drawable
     */
    public final void setClearInputButtonDrawable(@DrawableRes int drawableResId) {
        setClearInputButtonDrawable(
            Utils.getColoredDrawable(
                getContext(),
                drawableResId,
                inputBarIconColor
            )
        );
    }


    /**
     * Sets the drawable of the clear input button.
     *
     * @param drawable The drawable to set
     */
    public final void setClearInputButtonDrawable(Drawable drawable) {
        clearInputButtonDrawable = drawable;

        clearInputBtnIv.setImageDrawable(drawable);
    }


    /**
     * Sets the drawable of the voice input button.
     *
     * @param drawableResId The resource ID of the drawable
     */
    public final void setVoiceInputButtonDrawable(@DrawableRes int drawableResId) {
        setVoiceInputButtonDrawable(
            Utils.getColoredDrawable(
                getContext(),
                drawableResId,
                inputBarIconColor
            )
        );
    }


    /**
     * Sets the drawable of the voice input button.
     *
     * @param drawable The drawable to set
     */
    public void setVoiceInputButtonDrawable(Drawable drawable) {
        voiceInputButtonDrawable = drawable;

        voiceInputBtnIv.setImageDrawable(drawable);
    }


    /**
     * Sets the input query.
     *
     * @param query The query to set
     */
    public final void setInputQuery(@NonNull String query) {
        setInputQueryInternal(query, true);
    }


    private final void setInputQueryInternal(@NonNull String query, boolean notifyAboutQueryChange) {
        Preconditions.nonNull(query);

        shouldNotifyAboutQueryChange = notifyAboutQueryChange;

        inputEt.setText(query);
        inputEt.setSelection(inputEt.length());

        shouldNotifyAboutQueryChange = true;
    }


    /**
     * Sets the hint of the query input.
     *
     * @param hint The hint to set
     */
    public final void setQueryInputHint(@NonNull String hint) {
        Preconditions.nonNull(hint);

        queryInputHint = hint;

        inputEt.setHint(hint);
    }


    /**
     * Sets the typeface of the query text.
     *
     * @param typeface The typeface to set
     */
    public final void setQueryTextTypeface(@NonNull Typeface typeface) {
        Preconditions.nonNull(typeface);

        queryTextTypeface = typeface;

        inputEt.setTypeface(typeface);
    }


    /**
     * Sets the typeface of the suggestion text.
     *
     * @param typeface The typeface to set
     */
    public final void setSuggestionTextTypeface(@NonNull Typeface typeface) {
        Preconditions.nonNull(typeface);

        suggestionTextTypeface = typeface;

        adapter.setResources(getAdapterResources().setTypeface(typeface));
    }


    /**
     * Sets the delegate for voice recognition.
     *
     * @param delegate The delegate to set
     */
    public final void setVoiceRecognitionDelegate(VoiceRecognitionDelegate delegate) {
        voiceRecognitionDelegate = delegate;
    }


    /**
     * Sets the listener to invoke when a search has been confirmed.
     *
     * @param onSearchConfirmedListener The listener to set
     */
    public final void setOnSearchConfirmedListener(OnSearchConfirmedListener onSearchConfirmedListener) {
        this.onSearchConfirmedListener = onSearchConfirmedListener;
    }


    /**
     * Sets the listener to invoke when a search query has been changed.
     *
     * @param onSearchQueryChangeListener The listener to set
     */
    public final void setOnSearchQueryChangeListener(OnSearchQueryChangeListener onSearchQueryChangeListener) {
        this.onSearchQueryChangeListener = onSearchQueryChangeListener;
    }


    /**
     * Sets the listener to invoke when a suggestion has been changed.
     *
     * @param onSuggestionChangeListener The listener to set
     */
    public final void setOnSuggestionChangeListener(OnSuggestionChangeListener onSuggestionChangeListener) {
        this.onSuggestionChangeListener = onSuggestionChangeListener;
    }


    /**
     * Sets the listener to invoke when a left button is clicked.
     *
     * @param listener The listener to set
     */
    public final void setOnLeftBtnClickListener(OnClickListener listener) {
        onLeftBtnClickListener = listener;
    }


    /**
     * Sets the listener to invoke when a clear button is clicked.
     *
     * @param listener The listener to set
     */
    public final void setOnClearInputBtnClickListener(OnClickListener listener) {
        onClearInputBtnClickListener = listener;
    }


    /**
     * Sets the listener to invoke when a right button is clicked.
     *
     * @param listener The listener to set
     */
    public final void setOnRightBtnClickListener(OnClickListener listener) {
        rightBtnIv.setOnClickListener(listener);
    }


    /**
     * Sets a flag indicating whether it is possible to dismiss this search view
     * (i.e. convert from {@link State#EXPANDED to {@link State#COLLAPSED} state}
     * when a user clicks on the outside area.
     *
     * @param dismissOnTouchOutside Whether this search view is dismissible when
     * a user clicks on the outside area.
     */
    public final void setDismissOnTouchOutside(boolean dismissOnTouchOutside) {
        isDismissibleOnTouchOutside = dismissOnTouchOutside;
    }


    /**
     * Checks whether this search view is dismissible when a user clicks on
     * the outside area.
     *
     * @return true if dismissible; false otherwise
     */
    public final boolean isDismissibleOnTouchOutside() {
        return isDismissibleOnTouchOutside;
    }


    /**
     * Sets a flag indicating whether the progress bar should be enabled or not.
     * This flag will basically allow the progress bar to be shown and hidden
     * by calling the appropriate methods {@link PersistentSearchView#showProgressBar()}
     * and {@link PersistentSearchView#hideProgressBar()}.
     *
     * @param isEnabled true if enabled; false otherwise
     */
    public final void setProgressBarEnabled(boolean isEnabled) {
        isProgressBarEnabled = isEnabled;

        updateLeftContainerVisibility();
    }


    /**
     * Checks whether the progress bar is enabled (whether it is allowed to be
     * shown and hidden).
     *
     * @return true if enabled; false otherwise
     */
    public final boolean isProgressBarEnabled() {
        return isProgressBarEnabled;
    }


    /**
     * Sets a flag indicating whether the voice input button should be enabled by
     * the search view.
     *
     * Whether or not the voice input button will be shown depends largely on
     * {@link PersistentSearchView#isVoiceInputEnabled()} method's return value.
     * This method is solely responsible for setting a hint whether the voice input
     * button should be enabled at all, but for it to be visible the device should have
     * some kind of speech recognition support. If it does not or the value passed into
     * this method is false, then the button won't be visible. If the device does have
     * speech recognition support and the value passed into this method is true, then
     * the button will be visible.
     *
     * @param isEnabled true if should be enabled; false otherwise
     */
    public final void setVoiceInputButtonEnabled(boolean isEnabled) {
        isVoiceInputButtonEnabled = isEnabled;

        updateVoiceInputButtonState();
    }


    /**
     * Checks whether the voice input button is enabled.
     *
     * @return true if enabled; false otherwise
     */
    public final boolean isVoiceInputButtonEnabled() {
        return isVoiceInputButtonEnabled;
    }


    /**
     * Checks whether the voice input is enabled.
     * <p>
     * For the voice input to be enabled, the voice input button should be enabled and
     * the device should have some kind of speech recognition support.
     *
     * @return true if enabled; false otherwise
     */
    public final boolean isVoiceInputEnabled() {
        return (isVoiceInputButtonEnabled() && isSpeechRecognitionAvailable);
    }


    /**
     * Sets a flag indicating whether the clear input button should be enabled
     * by the search view.
     *
     * @param isEnabled true if should be enabled; false otherwise
     */
    public final void setClearInputButtonEnabled(boolean isEnabled) {
        isClearInputButtonEnabled = isEnabled;

        updateClearInputButtonState();
    }


    /**
     * Checks whether the clear input button is enabled.
     *
     * @return true if enabled; false otherwise
     */
    public final boolean isClearInputButtonEnabled() {
        return isClearInputButtonEnabled;
    }


    /**
     * Sets whether it is possible to show suggestions or not.
     *
     * @param areSuggestionsDisabled Whether it is possible to show suggestions or not
     */
    public final void setSuggestionsDisabled(boolean areSuggestionsDisabled) {
        this.areSuggestionsDisabled = areSuggestionsDisabled;
    }


    /**
     * Checks whether it is possible to show suggestions or not.
     *
     * @return true if possible; false otherwise
     */
    public final boolean areSuggestionsDisabled() {
        return areSuggestionsDisabled;
    }


    /**
     * Sets whether it is possible to dim background.
     *
     * @param dimBackground Whether it is possible to dim background
     */
    public final void setDimBackground(boolean dimBackground) {
        shouldDimBehind = dimBackground;
    }


    private boolean isExitAnimationRunning() {
        return (exitAnimationEndAction != null);
    }


    /**
     * Checks whether this search view is in {@link State#EXPANDED} state.
     *
     * @return true if expanded; false otherwise
     */
    public final boolean isExpanded() {
        return State.EXPANDED.equals(state);
    }


    /**
     * Checks whether the input query is empty.
     *
     * @return true if empty; false otherwise
     */
    public final boolean isInputQueryEmpty() {
        return TextUtils.isEmpty(inputEt.getText().toString());
    }


    /**
     * Checks whether the input is currently focused.
     *
     * @return true if focused; false otherwise
     */
    public final boolean isInputFocused() {
        return inputEt.hasFocus();
    }


    /**
     * Retrieves the input query.
     *
     * @return The input query
     */
    public final String getInputQuery() {
        return inputEt.getText().toString();
    }


    private int getSuggestionsContainerMaxHeight() {
        final int maxRawHeight = (Utils.getScreenSize(getContext())[1] / 2);
        final int maxItemCount = (maxRawHeight / suggestionItemHeight);
        final int maxAllowedItemCount = 8;

        return ((maxItemCount > maxAllowedItemCount) ? (maxAllowedItemCount * suggestionItemHeight) : (maxItemCount * suggestionItemHeight));
    }


    private long getSuggestionsContainerAnimationDuration(final long fromHeight, final long toHeight) {
        final int maxHeight = getSuggestionsContainerMaxHeight();
        return (long) Math.max(BACKGROUND_ANIMATION_MIN_DURATION, ((Math.abs(toHeight - fromHeight) * 1f / maxHeight) * BACKGROUND_ANIMATION_MAX_DURATION));
    }


    private final OnClickListener mOnParentOutsideClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if(isDismissibleOnTouchOutside) {
                collapse();
            }
        }

    };

    private final OnClickListener mOnLeftButtonClickListener = v -> {
        if(isExpanded()) {
            collapse();
        } else if(onLeftBtnClickListener != null) {
            onLeftBtnClickListener.onClick(v);
        }
    };

    private final OnClickListener mOnClearInputButtonClickListener = v -> {
        expand();
        setInputQuery("");

        if(onClearInputBtnClickListener != null) {
            onClearInputBtnClickListener.onClick(v);
        }
    };

    private final OnClickListener mOnVoiceInputButtonClickListener = v -> {
        expand();

        if (voiceRecognitionDelegate != null) {
            voiceRecognitionDelegate.openSpeechRecognizer();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final OnTouchListener mInputEditTextTouchEventInterceptor = (view, event) -> {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            expand();
        }

        return !isExpanded();
    };

    private final TextView.OnEditorActionListener mInternalEditorActionListener = (view, actionId, event) -> {
        if((actionId == EditorInfo.IME_ACTION_SEARCH) && (onSearchConfirmedListener != null)) {
            onSearchConfirmedListener.onSearchConfirmed(this, getInputQuery());
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            onSearchConfirmedListener.onSearchConfirmed(this, getInputQuery());
        }
        return true;
    };

    private final QueryListener mQueryListener = new QueryListener() {

        @Override
        public void onQueryChanged(String oldQuery, String newQuery) {
            setAdapterQuery(newQuery);

            if((onSearchQueryChangeListener != null) && shouldNotifyAboutQueryChange) {
                onSearchQueryChangeListener.onSearchQueryChanged(PersistentSearchView.this, oldQuery, newQuery);
            }
        }

        @Override
        public void onQueryEntered(String query) {
            if(!TextUtils.isEmpty(query)) {
                showClearInputButtonWithVoiceInputButton(true);
            }
        }

        @Override
        public void onQueryRemoved() {
            hideClearInputButtonWithVoiceInputButton(true);
        }

    };

    private final Consumer<Suggestion> onSuggestionClickListener = suggestion -> {
        if (onSuggestionChangeListener != null) {
            onSuggestionChangeListener.onSuggestionPicked(suggestion);
        }

        setInputQueryInternal(suggestion.getText(), false);
        collapse();
    };

    private final Consumer<Suggestion> onRemoveButtonClickListener = suggestion -> {
        final int currentHeight = suggestionsContainerLL.getMeasuredHeight();

        adapter.deleteItem(suggestion, () -> {
            remeasureSuggestionsContainer();
            updateSuggestionsContainerHeightWithAnimation(
                    state,
                    currentHeight,
                    suggestionsContainerLL.getMeasuredHeight(),
                    getSuggestionsContainerAnimationDuration(
                            currentHeight,
                            suggestionsContainerLL.getMeasuredHeight()
                    )
            );

            if (onSuggestionChangeListener != null) {
                onSuggestionChangeListener.onSuggestionRemoved(suggestion);
            }
        });
    };

    private final RecyclerView.OnScrollListener mSuggestionsRecyclerViewScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if((newState == RecyclerView.SCROLL_STATE_DRAGGING) && !suggestionItems.isEmpty()) {
                hideKeyboard();
            }
        }

    };


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(StateUtils.fetchParentState(state));

        final SavedState savedState = (SavedState) state;

        setQueryInputHintColor(savedState.queryInputHintColor);
        setQueryInputTextColor(savedState.queryInputTextColor);
        setQueryInputBarIconColor(savedState.inputBarIconColor);
        setDividerColor(savedState.dividerColor);
        setProgressBarColor(savedState.progressBarColor);
        setSuggestionIconColor(savedState.suggestionIconColor);
        setRecentSearchIconColor(savedState.recentSearchIconColor);
        setSearchSuggestionIconColor(savedState.searchSuggestionIconColor);
        setSuggestionTextColor(savedState.suggestionTextColor);
        setSuggestionSelectedTextColor(savedState.suggestionSelectedTextColor);
        setCardBackgroundColor(savedState.cardBackgroundColor);
        setBackgroundDimColor(savedState.backgroundDimColor);
        setCardElevation(savedState.cardElevation);
        setCardCornerRadius(savedState.cardCornerRadius);
        setBackgroundDimAmount(savedState.dimAmount);
        setInputQueryInternal(savedState.query, false);
        setQueryInputHint(savedState.inputHint);
        setDismissOnTouchOutside(savedState.isDismissibleOnTouchOutside);
        setProgressBarEnabled(savedState.isProgressBarEnabled);
        setVoiceInputButtonEnabled(savedState.isVoiceInputButtonEnabled);
        setClearInputButtonEnabled(savedState.isClearInputButtonEnabled);
        setSuggestionsDisabled(savedState.areSuggestionsDisabled);
        setDimBackground(savedState.shouldDimBehind);

        if(State.EXPANDED.equals(savedState.state)) {
            expand(false);
        } else {
            collapse(false);
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);

        savedState.queryInputHintColor = queryInputHintColor;
        savedState.queryInputTextColor = queryInputTextColor;
        savedState.inputBarIconColor = inputBarIconColor;
        savedState.dividerColor = dividerColor;
        savedState.progressBarColor = progressBarColor;
        savedState.suggestionIconColor = suggestionIconColor;
        savedState.recentSearchIconColor = recentSearchIconColor;
        savedState.searchSuggestionIconColor = searchSuggestionIconColor;
        savedState.suggestionTextColor = suggestionTextColor;
        savedState.suggestionSelectedTextColor = suggestionSelectedTextColor;
        savedState.cardBackgroundColor = cardBackgroundColor;
        savedState.backgroundDimColor = backgroundDimColor;
        savedState.cardElevation = cardElevation;
        savedState.cardCornerRadius = cardCornerRadius;
        savedState.dimAmount = dimAmount;
        savedState.query = getInputQuery();
        savedState.inputHint = inputEt.getHint().toString();
        savedState.state = state;
        savedState.isDismissibleOnTouchOutside = isDismissibleOnTouchOutside;
        savedState.isProgressBarEnabled = isProgressBarEnabled;
        savedState.isVoiceInputButtonEnabled = isVoiceInputButtonEnabled;
        savedState.isClearInputButtonEnabled = isClearInputButtonEnabled;
        savedState.areSuggestionsDisabled = areSuggestionsDisabled;
        savedState.shouldDimBehind = shouldDimBehind;

        return savedState;
    }


    private static class SavedState extends BaseSavedState {

        private static final String KEY_QUERY_INPUT_HINT_COLOR = "query_input_hint_color";
        private static final String KEY_QUERY_INPUT_TEXT_COLOR = "query_input_text_color";
        private static final String KEY_INPUT_BAR_ICON_COLOR = "input_bar_icon_color";
        private static final String KEY_DIVIDER_COLOR = "divider_color";
        private static final String KEY_PROGRESS_BAR_COLOR = "progress_bar_color";
        private static final String KEY_SUGGESTION_ICON_COLOR = "suggestion_icon_color";
        private static final String KEY_RECENT_SEARCH_ICON_COLOR = "recent_search_icon_color";
        private static final String KEY_SEARCH_SUGGESTION_ICON_COLOR = "search_suggestion_icon_color";
        private static final String KEY_SUGGESTION_TEXT_COLOR = "suggestion_text_color";
        private static final String KEY_SUGGESTION_SELECTED_TEXT_COLOR = "suggestion_selected_text_color";
        private static final String KEY_CARD_BACKGROUND_COLOR = "card_background_color";
        private static final String KEY_BACKGROUND_DIM_COLOR = "background_dim_color";
        private static final String KEY_CARD_CORNER_RADIUS = "card_corner_radius";
        private static final String KEY_CARD_ELEVATION = "card_elevation";
        private static final String KEY_DIM_AMOUNT = "dim_amount";
        private static final String KEY_QUERY = "query";
        private static final String KEY_INPUT_HINT = "input_hint";
        private static final String KEY_STATE = "state";
        private static final String KEY_IS_DISMISSIBLE_ON_TOUCH_OUTSIDE = "is_dismissible_on_touch_outside";
        private static final String KEY_IS_PROGRESS_BAR_ENABLED = "is_progress_bar_enabled";
        private static final String KEY_IS_VOICE_INPUT_BUTTON_ENABLED = "is_voice_input_button_enabled";
        private static final String KEY_IS_CLEAR_INPUT_BUTTON_ENABLED = "is_clear_input_button_enabled";
        private static final String KEY_ARE_SUGGESTIONS_DISABLED = "are_suggestions_disabled";
        private static final String KEY_SHOULD_DIM_BEHIND = "should_dim_behind";

        private int queryInputHintColor;
        private int queryInputTextColor;
        private int inputBarIconColor;
        private int dividerColor;
        private int progressBarColor;
        private int suggestionIconColor;
        private int recentSearchIconColor;
        private int searchSuggestionIconColor;
        private int suggestionTextColor;
        private int suggestionSelectedTextColor;
        private int cardBackgroundColor;
        private int backgroundDimColor;

        private int cardCornerRadius;
        private int cardElevation;

        private float dimAmount;

        private String query;
        private String inputHint;

        private State state;

        private boolean isDismissibleOnTouchOutside;
        private boolean isProgressBarEnabled;
        private boolean isVoiceInputButtonEnabled;
        private boolean isClearInputButtonEnabled;
        private boolean areSuggestionsDisabled;
        private boolean shouldDimBehind;


        private SavedState(Parcelable superState) {
            super(superState);
        }


        private SavedState(Parcel parcel) {
            super(parcel);

            final Bundle bundle = parcel.readBundle(getClass().getClassLoader());
            this.queryInputHintColor = bundle.getInt(KEY_QUERY_INPUT_HINT_COLOR);
            this.queryInputTextColor = bundle.getInt(KEY_QUERY_INPUT_TEXT_COLOR);
            this.inputBarIconColor = bundle.getInt(KEY_INPUT_BAR_ICON_COLOR);
            this.dividerColor = bundle.getInt(KEY_DIVIDER_COLOR);
            this.progressBarColor = bundle.getInt(KEY_PROGRESS_BAR_COLOR);
            this.suggestionIconColor = bundle.getInt(KEY_SUGGESTION_ICON_COLOR);
            this.recentSearchIconColor = bundle.getInt(KEY_RECENT_SEARCH_ICON_COLOR);
            this.searchSuggestionIconColor = bundle.getInt(KEY_SEARCH_SUGGESTION_ICON_COLOR);
            this.suggestionTextColor = bundle.getInt(KEY_SUGGESTION_TEXT_COLOR);
            this.suggestionSelectedTextColor = bundle.getInt(KEY_SUGGESTION_SELECTED_TEXT_COLOR);
            this.cardBackgroundColor = bundle.getInt(KEY_CARD_BACKGROUND_COLOR);
            this.backgroundDimColor = bundle.getInt(KEY_BACKGROUND_DIM_COLOR);
            this.cardCornerRadius = bundle.getInt(KEY_CARD_CORNER_RADIUS);
            this.cardElevation = bundle.getInt(KEY_CARD_ELEVATION);
            this.dimAmount = bundle.getFloat(KEY_DIM_AMOUNT, DEFAULT_DIM_AMOUNT);
            this.query = bundle.getString(KEY_QUERY);
            this.inputHint = bundle.getString(KEY_INPUT_HINT);
            this.state = (State) bundle.getSerializable(KEY_STATE);
            this.isDismissibleOnTouchOutside = bundle.getBoolean(KEY_IS_DISMISSIBLE_ON_TOUCH_OUTSIDE, true);
            this.isProgressBarEnabled = bundle.getBoolean(KEY_IS_PROGRESS_BAR_ENABLED, true);
            this.isVoiceInputButtonEnabled = bundle.getBoolean(KEY_IS_VOICE_INPUT_BUTTON_ENABLED, true);
            this.isClearInputButtonEnabled = bundle.getBoolean(KEY_IS_CLEAR_INPUT_BUTTON_ENABLED, true);
            this.areSuggestionsDisabled = bundle.getBoolean(KEY_ARE_SUGGESTIONS_DISABLED, false);
            this.shouldDimBehind = bundle.getBoolean(KEY_SHOULD_DIM_BEHIND, true);
        }


        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);

            final Bundle bundle = new Bundle();
            bundle.putInt(KEY_QUERY_INPUT_HINT_COLOR, this.queryInputHintColor);
            bundle.putInt(KEY_QUERY_INPUT_TEXT_COLOR, this.queryInputTextColor);
            bundle.putInt(KEY_INPUT_BAR_ICON_COLOR, this.inputBarIconColor);
            bundle.putInt(KEY_DIVIDER_COLOR, this.dividerColor);
            bundle.putInt(KEY_PROGRESS_BAR_COLOR, this.progressBarColor);
            bundle.putInt(KEY_SUGGESTION_ICON_COLOR, this.suggestionIconColor);
            bundle.putInt(KEY_RECENT_SEARCH_ICON_COLOR, this.recentSearchIconColor);
            bundle.putInt(KEY_SEARCH_SUGGESTION_ICON_COLOR, this.searchSuggestionIconColor);
            bundle.putInt(KEY_SUGGESTION_TEXT_COLOR, this.suggestionTextColor);
            bundle.putInt(KEY_SUGGESTION_SELECTED_TEXT_COLOR, this.suggestionSelectedTextColor);
            bundle.putInt(KEY_CARD_BACKGROUND_COLOR, this.cardBackgroundColor);
            bundle.putInt(KEY_BACKGROUND_DIM_COLOR, this.backgroundDimColor);
            bundle.putInt(KEY_CARD_CORNER_RADIUS, this.cardCornerRadius);
            bundle.putInt(KEY_CARD_ELEVATION, this.cardElevation);
            bundle.putFloat(KEY_DIM_AMOUNT, this.dimAmount);
            bundle.putString(KEY_QUERY, this.query);
            bundle.putString(KEY_INPUT_HINT, this.inputHint);
            bundle.putSerializable(KEY_STATE, this.state);
            bundle.putBoolean(KEY_IS_DISMISSIBLE_ON_TOUCH_OUTSIDE, this.isDismissibleOnTouchOutside);
            bundle.putBoolean(KEY_IS_PROGRESS_BAR_ENABLED, this.isProgressBarEnabled);
            bundle.putBoolean(KEY_IS_VOICE_INPUT_BUTTON_ENABLED, this.isVoiceInputButtonEnabled);
            bundle.putBoolean(KEY_IS_CLEAR_INPUT_BUTTON_ENABLED, this.isClearInputButtonEnabled);
            bundle.putBoolean(KEY_ARE_SUGGESTIONS_DISABLED, this.areSuggestionsDisabled);
            bundle.putBoolean(KEY_SHOULD_DIM_BEHIND, this.shouldDimBehind);

            parcel.writeBundle(bundle);
        }


        public static final Creator<SavedState> CREATOR = new Creator<>() {

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };


    }


}
