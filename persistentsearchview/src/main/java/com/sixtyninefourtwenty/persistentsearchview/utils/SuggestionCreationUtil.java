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

package com.sixtyninefourtwenty.persistentsearchview.utils;

import androidx.annotation.NonNull;

import com.sixtyninefourtwenty.persistentsearchview.model.Suggestion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class responsible for wrapping raw suggestions into
 * instances of the {@link Suggestion} class.
 */
public final class SuggestionCreationUtil {


    private SuggestionCreationUtil() {}


    /**
     * Wraps raw suggestions into a list of {@link Suggestion} objects with
     * the type set to {@link Suggestion.Type#RECENT_SEARCH}.
     *
     * @param rawSuggestions The suggestions to wrap
     *
     * @return A list of {@link Suggestion} objects
     */
    @NonNull
    public static List<Suggestion> asRecentSearchSuggestions(@NonNull List<String> rawSuggestions) {
        return fromRawSuggestions(Suggestion.Type.RECENT_SEARCH, rawSuggestions);
    }


    /**
     * Wraps raw suggestions into a list of {@link Suggestion} objects with
     * the type set to {@link Suggestion.Type#REGULAR_SEARCH}.
     *
     * @param rawSuggestions The suggestions to wrap
     *
     * @return A list of {@link Suggestion} objects
     */
    @NonNull
    public static List<Suggestion> asRegularSearchSuggestions(@NonNull List<String> rawSuggestions) {
        return fromRawSuggestions(Suggestion.Type.REGULAR_SEARCH, rawSuggestions);
    }


    /**
     * Wraps raw suggestions into a list of {@link Suggestion} objects with
     * the type set to passed one.
     *
     * @param suggestionType The suggestion type
     * @param rawSuggestions The suggestions to wrap
     *
     * @return A list of {@link Suggestion} objects
     */
    @NonNull
    private static List<Suggestion> fromRawSuggestions(@NonNull Suggestion.Type suggestionType, @NonNull List<String> rawSuggestions) {
        Preconditions.nonNull(suggestionType);
        Preconditions.nonNull(rawSuggestions);

        return rawSuggestions.stream()
                .map(rawSuggestion -> new Suggestion(suggestionType, rawSuggestion))
                .collect(Collectors.toList());
    }


}
