/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.yunos.alicontacts.editor;

import android.content.Context;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.yunos.alicontacts.R;
import com.yunos.alicontacts.editor.Editor.EditorListener;
import com.yunos.alicontacts.model.RawContactModifier;
import com.yunos.alicontacts.model.RawContactDelta;
import com.yunos.alicontacts.model.RawContactDelta.ValuesDelta;
import com.yunos.alicontacts.model.account.AccountType.EditField;
import com.yunos.alicontacts.model.dataitem.DataKind;

import java.util.ArrayList;

/**
 * Custom view for an entire section of data as segmented by
 * {@link DataKind} around a {@link Data#MIMETYPE}. This view shows a
 * section header and a trigger for adding new {@link Data} rows.
 */
public class KindSectionView extends LinearLayout implements EditorListener {
    //private static final String TAG = "KindSectionView";

    //private TextView mTitle;
    private ViewGroup mEditors;
//    private View mAddFieldFooter;
    private String mTitleString;

    private DataKind mKind;
    private RawContactDelta mState;
    private boolean mReadOnly;

    private ViewIdGenerator mViewIdGenerator;

    private LayoutInflater mInflater;

    private Context mContext;

    private final ArrayList<Runnable> mRunWhenWindowFocused = new ArrayList<Runnable>(1);

    public KindSectionView(Context context) {
        this(context, null);
        mContext = context;
    }

    public KindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mEditors != null) {
            int childCount = mEditors.getChildCount();
            for (int i = 0; i < childCount; i++) {
                mEditors.getChildAt(i).setEnabled(enabled);
            }
        }

       /* if (enabled && !mReadOnly) {
            mAddFieldFooter.setVisibility(View.VISIBLE);
        } else {
            mAddFieldFooter.setVisibility(View.GONE);
        }
        //    added by xiaodong.lxd
        mAddFieldFooter.setVisibility(View.GONE);*/
    }

    public boolean isReadOnly() {
        return mReadOnly;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //mTitle = (TextView) findViewById(R.id.kind_title);
        mEditors = (ViewGroup) findViewById(R.id.kind_editors);
       /* mAddFieldFooter = findViewById(R.id.add_field_footer);
        mAddFieldFooter.setVisibility(View.GONE);
        mAddFieldFooter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup click listener to add an empty field when the footer is clicked.
                mAddFieldFooter.setVisibility(View.GONE);
                addItem();
            }
        });*/
    }

    @Override
    public void onDeleteRequested(Editor editor) {
        // If there is only 1 editor in the section, then don't allow the user to delete it.
        // Just clear the fields in the editor.
//        Log.d(TAG, "sxsexe--->onDeleteRequested  mimeType " + mKind.mimeType + " mimeCount " + mState.getMimeEntriesCount(mKind.mimeType, true) );
        if (getEditorCount() == 1) {
            editor.clearAllFields();
        } else {
            //boolean isFirst = isFirstEditor(editor);
            // Otherwise it's okay to delete this {@link Editor}

            //View view = mEditors.getChildAt(1);//get next
            editor.deleteEditor();

            //added by xiaodong.lxd
            /*if(isFirst) {
                if(view != null && view instanceof Editor) {
                    LabeledEditorView labeledEditorView = (LabeledEditorView)view;
                    labeledEditorView.setKindTypeImageVisible(true, mKind.mimeType);
                }
            }*/
        }
    }

    /*private boolean isFirstEditor(Editor editor) {
        View firstView = mEditors.getChildAt(0);
        return firstView == editor;
    }*/

    @Override
    public void onRequest(int request) {
        // If a field has become empty or non-empty, then check if another row
        // can be added dynamically.

        if (request == FIELD_TURNED_NON_EMPTY) {
            //modified by xiaodong.lxd
//            updateAddFooterVisible(true);
            int count = mEditors.getChildCount();
            if(count > 0) {
                View lastView = mEditors.getChildAt(count - 1);
                if(!((Editor)lastView).isEmpty()) {
                    addItem(false);
                }
            }

        } /*else if(request == FIELD_TURNED_EMPTY) {
            //TODO deleteItem();
        }*/
    }

    public void setState(DataKind kind, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        mKind = kind;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;

        setId(mViewIdGenerator.getId(state, kind, null, ViewIdGenerator.NO_VIEW_INDEX));

        // TODO: handle resources from remote packages
        mTitleString = (kind.titleRes == -1 || kind.titleRes == 0)
                ? ""
                : getResources().getString(kind.titleRes);
//        if(mTitle != null) {
//            mTitle.setText(mTitleString);
//        }

//        Log.d(TAG, "sxsexe---> setState mimeType " + kind.mimeType);
        rebuildFromState();
        //updateAddFooterVisible(false);
        updateSectionVisible();
    }

    public String getTitle() {
        return mTitleString;
    }

    public void setTitleVisible(boolean visible) {
        findViewById(R.id.kind_title_layout).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Build editors for all current {@link #mState} rows.
     */
    public void rebuildFromState() {
        // Remove any existing editors
        mEditors.removeAllViews();

        // Check if we are displaying anything here
        boolean hasEntries = mState.hasMimeEntries(mKind.mimeType);
//        Log.d("KindSectionView", "sxsexe--->rebuildFromState  hasEntries " + hasEntries + " mKind.mimeType " + mKind.mimeType);

        if (hasEntries) {
            int index = 0;
            for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
                // Skip entries that aren't visible
                if (!entry.isVisible()) continue;
                if (isEmptyNoop(entry)) continue;

                //modified by xiaodong.lxd
                //View view = createEditorView(entry);
                createEditorView(entry);
                //LabeledEditorView labeledEditorView = (LabeledEditorView)view;
                //labeledEditorView.setKindTypeImageVisible(index++ == 0, mKind.mimeType);
                index++;
            }
            //added by xiaodong.lxd
            if(index >= 1) {
                ArrayList<ValuesDelta> entries = mState.getMimeEntries(mKind.mimeType);
                if(entries != null && !entries.isEmpty() && entries.size() >= 1) {
                    ValuesDelta entry = entries.get(entries.size() - 1);
                    EditField field = mKind.fieldList.get(0);
                    String value = entry.getAsString(field.column);
                    if(!TextUtils.isEmpty(value) && mKind.typeOverallMax != 1) {
                        addItem(false);
                    }
                }
            }
        }
    }

    /**
     * Creates an EditorView for the given entry. This function must be used while constructing
     * the views corresponding to the the object-model. The resulting EditorView is also added
     * to the end of mEditors
     */
    private View createEditorView(ValuesDelta entry) {
        final View view;
//        Log.d(TAG, "sxsexe---->createEditorView mimeType" + mKind.mimeType);
        try {
            view = mInflater.inflate(mKind.editorLayoutResourceId, mEditors, false);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot allocate editor with layout resource ID " +
                    mKind.editorLayoutResourceId + " for MIME type " + mKind.mimeType +
                    " with error " + e.toString());
        }

        view.setEnabled(isEnabled());

        if (view instanceof Editor) {
            Editor editor = (Editor) view;
            editor.setDeletable(true);
            editor.setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
            editor.setEditorListener(this);
        }
//        Log.d(TAG, "sxsexe---->createEditorView mEditors.addView view " + view.getClass().getName());
        mEditors.addView(view);
        return view;
    }

    /**
     * Tests whether the given item has no changes (so it exists in the database) but is empty
     */
    private boolean isEmptyNoop(ValuesDelta item) {
        if (!item.isNoop()) return false;
        final int fieldCount = mKind.fieldList.size();
        for (int i = 0; i < fieldCount; i++) {
            final String column = mKind.fieldList.get(i).column;
            final String value = item.getAsString(column);
            if (!TextUtils.isEmpty(value)) return false;
        }
        return true;
    }

    private void updateSectionVisible() {
        setVisibility(getEditorCount() != 0 ? VISIBLE : GONE);
    }

    protected void updateAddFooterVisible(boolean animate) {/*
        if (!mReadOnly && (mKind.typeOverallMax != 1)) {
            // First determine whether there are any existing empty editors.
            updateEmptyEditors();
            // If there are no existing empty editors and it's possible to add
            // another field, then make the "add footer" field visible.
            if (!hasEmptyEditor() && RawContactModifier.canInsert(mState, mKind)) {
                if (animate) {
                    EditorAnimator.getInstance().showAddFieldFooter(mAddFieldFooter);
                } else {
                    mAddFieldFooter.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
        if (animate) {
            EditorAnimator.getInstance().hideAddFieldFooter(mAddFieldFooter);
        } else {
            mAddFieldFooter.setVisibility(View.GONE);
        }

        //added by xiaodong.lxd
        mAddFieldFooter.setVisibility(View.GONE);
    */}

    /**
     * Updates the editors being displayed to the user removing extra empty
     * {@link Editor}s, so there is only max 1 empty {@link Editor} view at a time.
     */
    /*private void updateEmptyEditors() {
        List<View> emptyEditors = getEmptyEditors();

        // If there is more than 1 empty editor, then remove it from the list of editors.
        if (emptyEditors.size() > 1) {
            for (View emptyEditorView : emptyEditors) {
                // If no child {@link View}s are being focused on within
                // this {@link View}, then remove this empty editor.
                if (emptyEditorView.findFocus() == null) {
                    mEditors.removeView(emptyEditorView);
                }
            }
        }
    }*/

    /**
     * Returns a list of empty editor views in this section.
     */
    /*private List<View> getEmptyEditors() {
        List<View> emptyEditorViews = new ArrayList<View>();
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            View view = mEditors.getChildAt(i);
            if (((Editor) view).isEmpty()) {
                emptyEditorViews.add(view);
            }
        }
        return emptyEditorViews;
    }*/

    /**
     * Returns true if one of the editors has all of its fields empty, or false
     * otherwise.
     */
    /*private boolean hasEmptyEditor() {
        return getEmptyEditors().size() > 0;
    }*/

    /**
     * Returns true if all editors are empty.
     */
    public boolean isEmpty() {
        for (int i = 0; i < mEditors.getChildCount(); i++) {
            View view = mEditors.getChildAt(i);
            if (!((Editor) view).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extends superclass implementation to also run tasks
     * enqueued by {@link #runWhenWindowFocused}.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            for (Runnable r: mRunWhenWindowFocused) {
                r.run();
            }
            mRunWhenWindowFocused.clear();
        }
    }

    /**
     * Depending on whether we are in the currently-focused window, either run
     * the argument immediately, or stash it until our window becomes focused.
     */
    private void runWhenWindowFocused(Runnable r) {
        if (hasWindowFocus()) {
            r.run();
        } else {
            mRunWhenWindowFocused.add(r);
        }
    }

    /**
     * Simple wrapper around {@link #runWhenWindowFocused}
     * to ensure that it runs in the UI thread.
     */
    private void postWhenWindowFocused(final Runnable r) {
        post(new Runnable() {
            @Override
            public void run() {
                runWhenWindowFocused(r);
            }
        });
    }

    public void addItem(boolean needFocus) {
        ValuesDelta values = null;
        // If this is a list, we can freely add. If not, only allow adding the first.
        if (mKind.typeOverallMax == 1) {
            if (getEditorCount() == 1) {
                return;
            }

            // If we already have an item, just make it visible
            ArrayList<ValuesDelta> entries = mState.getMimeEntries(mKind.mimeType);
            if (entries != null && !entries.isEmpty()) {
                values = entries.get(0);
            }
        }

        // Insert a new child, create its view and set its focus
        if (values == null) {
            values = RawContactModifier.insertChild(mState, mKind);
        }

        //modified by xiaodong.lxd
        final View newField = createEditorView(values);
        //final int entryCount = mState.getMimeEntriesCount(mKind.mimeType, true);
//        Log.d(TAG, "sxsexe--->addItem entry count " + mState.getMimeEntriesCount(mKind.mimeType, true));
        if (newField instanceof Editor) {
//            post(new Runnable() {
//                @Override
//                public void run() {
//                    LabeledEditorView labelEditor = (LabeledEditorView)newField;
//                    labelEditor.set;
//                }
//            });

            if(needFocus) {
                postWhenWindowFocused(new Runnable() {
                    @Override
                    public void run() {
                        newField.requestFocus();
                        ((Editor)newField).editNewlyAddedField();
                    }
                });
            }
        }

        // Hide the "add field" footer because there is now a blank field.
//        mAddFieldFooter.setVisibility(View.GONE);

        // Ensure we are visible
        updateSectionVisible();
    }

    public int getEditorCount() {
        return mEditors.getChildCount();
    }

    public DataKind getKind() {
        return mKind;
    }
}
