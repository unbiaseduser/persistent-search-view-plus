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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sixtyninefourtwenty.persistentsearchview.PersistentSearchView;

import java.util.List;

/**
 * A utility class used for delegating voice recognition related
 * work to.
 */
public class VoiceRecognitionDelegate {


    @NonNull
    private PersistentSearchView searchView;

    private AppCompatActivity supportActivity;

    private androidx.fragment.app.Fragment supportFragment;

    private final ActivityResultLauncher<Intent> voiceInput;
    private final ActivityResultCallback<ActivityResult> internalCallback = result -> {
        final Intent data = result.getData();
        if (result.getResultCode() == Activity.RESULT_OK && data != null) {
            final List<String> receivedData = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if((receivedData != null) && (receivedData.size() > 0)) {
                searchView.setInputQuery(receivedData.get(0));
                searchView.confirmSearchAction();
            }
        }
    };


    public VoiceRecognitionDelegate(@NonNull AppCompatActivity activity, @NonNull PersistentSearchView searchView) {
        Preconditions.nonNull(activity);

        this.supportActivity = activity;
        this.searchView = searchView;
        voiceInput = supportActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), internalCallback);
    }


    public VoiceRecognitionDelegate(@NonNull androidx.fragment.app.Fragment fragment, @NonNull PersistentSearchView searchView) {
        Preconditions.nonNull(fragment);

        this.supportFragment = fragment;
        this.searchView = searchView;
        voiceInput = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), internalCallback);
    }


    /**
     * Tries to open the speech recognizer on user's device.
     */
    public final void openSpeechRecognizer() {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Utils.getLocale(getContext()));

        voiceInput.launch(intent);
    }


    private Context getContext() {
        if(supportActivity != null) {
            return supportActivity;
        } else if(supportFragment != null) {
            return supportFragment.requireContext();
        } else {
            throw new IllegalStateException("Could not get context in VoiceRecognitionDelegate.");
        }
    }


}
