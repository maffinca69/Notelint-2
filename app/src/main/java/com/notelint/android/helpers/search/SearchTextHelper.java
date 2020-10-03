package com.notelint.android.helpers.search;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.notelint.android.R;
import com.notelint.android.activities.EditorActivity;
import com.notelint.android.utils.Util;

import org.apache.commons.lang3.StringUtils;

public class SearchTextHelper {

    private EditText editText;
    private Activity activity;

    public SearchTextHelper(Activity activity, EditText editText) {
        this.activity = activity;
        this.editText = editText;
    }

    public TextWatcher searchableWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final String searchText = s.toString();
            if (!StringUtils.isEmpty(s)) {
                new Handler(Looper.getMainLooper()).post(() -> search(searchText));
            } else {
                setDefaultText();
            }
        }
    };

    private void setDefaultText() {
        editText.setText(Html.fromHtml(editText.getText().toString().replaceAll("\\n", "<br />")).toString());
    }

    public void prepareSearch() {
        activity.findViewById(R.id.toolbar_title).setVisibility(View.GONE);
        activity.findViewById(R.id.search).setVisibility(View.VISIBLE);
        EditText editText = ((EditorActivity) activity).findViewById(R.id.search);
        editText.addTextChangedListener(searchableWatcher);
        editText.requestFocus();
        Util.showKeyboard();
    }

    public void cancelSearch() {
        activity.findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.search).setVisibility(View.GONE);
        new Handler(Looper.getMainLooper()).post(() -> {
            ((EditText) ((EditorActivity) activity).findViewById(R.id.search)).removeTextChangedListener(searchableWatcher);
            setDefaultText();
        });

    }

    private void search(String search) {
        String fullText = editText.getText().toString();
        if (StringUtils.containsIgnoreCase(fullText, search.trim())) {
            // For scroll
            int indexOfCriteria = fullText.indexOf(search);
            int lineNumber = editText.getLayout().getLineForOffset(indexOfCriteria);

            // Prepare text
            String highlighted = "<font color='red'><b>" + search + "</b></font>";
            fullText = fullText.replace(search, highlighted).replaceAll("\\n", "<br />");

            editText.setText(Html.fromHtml(fullText));

            // Scroll to find text
            ScrollView scrollView = ((ScrollView) activity.findViewById(R.id.edit_scroll));
            scrollView.post(() -> scrollView.scrollTo(0, editText.getLayout().getLineTop(lineNumber)));
        }
    }
}
