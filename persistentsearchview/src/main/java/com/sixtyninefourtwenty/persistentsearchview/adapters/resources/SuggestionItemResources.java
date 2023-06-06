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

package com.sixtyninefourtwenty.persistentsearchview.adapters.resources;

import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.sixtyninefourtwenty.persistentsearchview.utils.Preconditions;

/**
 * Resources for suggestion items.
 */
public class SuggestionItemResources {

    @ColorInt
    private int iconColor;
    @ColorInt
    private int recentSearchIconColor;
    @ColorInt
    private int searchSuggestionIconColor;
    @ColorInt
    private int textColor;
    @ColorInt
    private int selectedTextColor;

    @NonNull
    private String currentQuery;

    @NonNull
    private Typeface typeface;


    public SuggestionItemResources() {
        iconColor = Color.BLACK;
        recentSearchIconColor = Color.BLACK;
        searchSuggestionIconColor = Color.BLACK;
        textColor = Color.BLACK;
        selectedTextColor = Color.BLACK;
        currentQuery = "";
        typeface = Typeface.DEFAULT;
    }


    /**
     * Sets the color of the icon.
     *
     * @param iconColor The color to set
     *
     * @return this
     */
    public SuggestionItemResources setIconColor(@ColorInt int iconColor) {
        this.iconColor = iconColor;
        return this;
    }


    /**
     * Gets the color of the icon.
     *
     * @return The icon's color
     */
    @ColorInt
    public int getIconColor() {
        return iconColor;
    }


    /**
     * Sets the color of the recent search icon.
     *
     * @param recentSearchIconColor The color to set
     *
     * @return this
     */
    public SuggestionItemResources setRecentSearchIconColor(@ColorInt int recentSearchIconColor) {
        this.recentSearchIconColor = recentSearchIconColor;
        return this;
    }


    /**
     * Gets the color of the recent search icon.
     *
     * @return The recent search icon's color
     */
    @ColorInt
    public int getRecentSearchIconColor() {
        return recentSearchIconColor;
    }


    /**
     * Sets the color of the search suggestion icon.
     *
     * @param searchSuggestionIconColor The color to set
     *
     * @return this
     */
    public SuggestionItemResources setSearchSuggestionIconColor(@ColorInt int searchSuggestionIconColor) {
        this.searchSuggestionIconColor = searchSuggestionIconColor;
        return this;
    }


    /**
     * Gets the color of the search suggestion icon.
     *
     * @return The search suggestion icon's color
     */
    @ColorInt
    public int getSearchSuggestionIconColor() {
        return searchSuggestionIconColor;
    }


    /**
     * Sets the color of the text.
     *
     * @param textColor The color to set
     *
     * @return this
     */
    public SuggestionItemResources setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }


    /**
     * Gets the color of the text.
     *
     * @return The text's color
     */
    @ColorInt
    public int getTextColor() {
        return textColor;
    }


    /**
     * Sets the color of the selected text.
     *
     * @param selectedTextColor The color to set
     *
     * @return this
     */
    public SuggestionItemResources setSelectedTextColor(@ColorInt int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        return this;
    }


    /**
     * Gets the color of the selected text.
     *
     * @return The selected text's color
     */
    @ColorInt
    public int getSelectedTextColor() {
        return selectedTextColor;
    }


    /**
     * Sets the current query.
     *
     * @param currentQuery The current query to set
     *
     * @return this
     */
    public SuggestionItemResources setCurrentQuery(@NonNull String currentQuery) {
        Preconditions.nonNull(currentQuery);

        this.currentQuery = currentQuery;

        return this;
    }


    /**
     * Gets the current query.
     *
     * @return The v query
     */
    @NonNull
    public String getCurrentQuery() {
        return currentQuery;
    }


    /**
     * Sets the typeface of the text.
     *
     * @param typeface The typeface to set
     *
     * @return this
     */
    public SuggestionItemResources setTypeface(@NonNull Typeface typeface) {
        Preconditions.nonNull(typeface);

        this.typeface = typeface;

        return this;
    }


    /**
     * Gets the typeface of the text.
     *
     * @return The text's typeface
     */
    @NonNull
    public Typeface getTypeface() {
        return typeface;
    }


}
