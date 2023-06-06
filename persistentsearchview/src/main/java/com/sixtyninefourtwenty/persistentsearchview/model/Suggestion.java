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

package com.sixtyninefourtwenty.persistentsearchview.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * A model class representing a suggestion.
 */
public class Suggestion implements Serializable {

    public enum Type {
        RECENT_SEARCH, REGULAR_SEARCH
    }

    private final long id;
    @NonNull
    private final Type type;
    @NonNull
    private final String text;

    public Suggestion(long id, @NonNull String text) {
        this(id, Type.REGULAR_SEARCH, text);
    }

    public Suggestion(@NonNull Type type, @NonNull String text) {
        this(-1, type, text);
    }

    public Suggestion(long id, @NonNull Type type, @NonNull String text) {
        this.id = id;
        this.type = type;
        this.text = text;
    }

    /**
     * Gets the id of the suggestion.
     *
     * @return The suggestion's id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Checks whether this suggestion has a valid ID (bigger than 0).
     *
     * @return true if valid; false otherwise
     */
    public boolean hasValidId() {
        return (this.id > 0);
    }

    /**
     * Gets the type of the suggestion.
     *
     * @return The suggestion's type
     */
    @NonNull
    public Type getType() {
        return this.type;
    }

    /**
     * Gets the text of the suggestion.
     *
     * @return The suggestion's text
     */
    @NonNull
    public String getText() {
        return this.text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Suggestion that)) return false;
        return id == that.id && type == that.type && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, text);
    }
}
