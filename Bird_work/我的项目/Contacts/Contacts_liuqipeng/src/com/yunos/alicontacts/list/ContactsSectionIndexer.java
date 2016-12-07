/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yunos.alicontacts.list;

import android.text.TextUtils;
import android.widget.SectionIndexer;

import java.util.Arrays;

/**
 * A section indexer that is configured with precomputed section titles and
 * their respective counts.
 */
public class ContactsSectionIndexer implements SectionIndexer {

    private String[] mSections;
    private int[] mPositions;
    private int mCount;
    private static final String BLANK_HEADER_STRING = "#";

    /**
     * Constructor.
     *
     * @param sections a non-null array
     * @param counts a non-null array of the same size as <code>sections</code>
     */
    public ContactsSectionIndexer(String[] sections, int[] counts) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        }

        if (sections.length != counts.length) {
            throw new IllegalArgumentException(
                    "The sections and counts arrays must have the same length");
        }

        // TODO process sections/counts based on current locale and/or specific section titles

        String[] sections1 = null;
        int[] counts1 = null;

        if(sections.length > 1) {
            if(TextUtils.isEmpty(sections[0]) && sections[1].equals(BLANK_HEADER_STRING)) {
                sections1 = new String[sections.length - 1];
                counts1 = new int[counts.length - 1];

                sections1[0] = BLANK_HEADER_STRING;
                counts1[0] = counts[0] + counts[1];

                for(int i = 2; i < sections.length; i++) {
                    sections1[i - 1] = sections[i];
                    counts1[i - 1] = counts[i];
                }
            } else {
                sections1 = sections;
                counts1 = counts;
            }
        } else {
            sections1 = sections;
            counts1 = counts;
        }

        this.mSections = sections1;
        mPositions = new int[counts1.length];

        int position = 0;
        for (int i = 0; i < counts1.length; i++) {
            if (TextUtils.isEmpty(mSections[i]) || " ".equals(mSections[i])) {
                mSections[i] = /*BLANK_HEADER_STRING*/ " "; // fix "#" character sort issue in contacts list.
            } else if (!mSections[i].equals(BLANK_HEADER_STRING)) {
                mSections[i] = mSections[i].trim();
            }

            mPositions[i] = position;
            position += counts1[i];
        }
        mCount = position;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mSections.length) {
            return -1;
        }

        return mPositions[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }

        int index = Arrays.binarySearch(mPositions, position);

        /*
         * Consider this example: section positions are 0, 3, 5; the supplied
         * position is 4. The section corresponding to position 4 starts at
         * position 3, so the expected return value is 1. Binary search will not
         * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
         * To get from that number to the expected value of 1 we need to negate
         * and subtract 2.
         */
        return index >= 0 ? index : -index - 2;
    }

}
