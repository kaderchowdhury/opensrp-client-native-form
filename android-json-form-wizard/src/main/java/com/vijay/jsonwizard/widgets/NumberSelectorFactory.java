package com.vijay.jsonwizard.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NumberSelectorFactory implements FormWidgetFactory {
    private static CustomTextView selectedTextView;
    private static int selectedItem = -1;
    private static HashMap<ViewParent, CustomTextView> selectedTextViews = new HashMap<>();
    private SelectedNumberClickListener selectedNumberClickListener = new SelectedNumberClickListener();
    private Spinner spinner;

    @SuppressLint("NewApi")

    private static void setSelectedColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors, String textColor) {
        if (customTextView != null && item > -1) {
            if (item == 0) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_left_rounded_background_selected));
            } else if (item == numberOfSelectors - 1) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_right_rounded_background_selected));
            } else {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_normal_background_selected));
            }

            customTextView.setTextColor(Color.parseColor(textColor));
        }
    }

    @SuppressLint("NewApi")
    private static void setDefaultColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors, String textColor) {
        if (customTextView != null && item > -1) {
            if (item == 0) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_left_rounded_background));
            } else if (item == numberOfSelectors - 1) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_right_rounded_background));
            } else {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_normal_background));
            }
            customTextView.setTextColor(Color.parseColor(textColor));
        }
    }

    /**
     * Sets backgrounds according to the selected textviews
     *
     * @param textView
     */
    public static void setBackgrounds(CustomTextView textView) {
        String defaultColor = (String) textView.getTag(R.id.number_selector_default_text_color);
        String selectedColor = (String) textView.getTag(R.id.number_selector_selected_text_color);
        int item = (int) textView.getTag(R.id.number_selector_item);
        int numberOfSelectors = (int) textView.getTag(R.id.number_selector_number_of_selectors);
        ViewParent textViewParent = textView.getParent();

        if (!textView.equals(selectedTextView)) {
            if (selectedTextViews.size() == 0) {
                setSelectedColor(textView.getContext(), textView, item, numberOfSelectors, selectedColor);
                setDefaultColor(textView.getContext(), selectedTextView, selectedItem, numberOfSelectors, defaultColor);
            } else {
                for (HashMap.Entry<ViewParent, CustomTextView> entry : selectedTextViews.entrySet()) {
                    if (textViewParent == entry.getKey()) {
                        if (textView != entry.getValue()) {
                            setSelectedColor(textView.getContext(), textView, item, numberOfSelectors, selectedColor);
                            setDefaultColor(textView.getContext(), entry.getValue(), selectedItem, numberOfSelectors, defaultColor);
                        }
                    } else {
                        if (textView != entry.getValue()) {
                            setSelectedColor(textView.getContext(), textView, item, numberOfSelectors, selectedColor);
                        }
                    }

                }
            }
            selectedTextViews.put(textViewParent, textView);
        }
    }

    public static void setSelectedTextViews(CustomTextView customTextView) {
        int item = (int) customTextView.getTag(R.id.number_selector_item);
        selectedTextView = customTextView;
        selectedItem = item;
    }

    /**
     * Create the spinner with the numbers starting from the last number in hte selectors
     *
     * @param context
     * @param jsonObject
     */
    private static Spinner createDialogSpinner(Context context, JSONObject jsonObject, int spinnerStartNumber, final CommonListener listener, String stepName) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        LinearLayout.LayoutParams layoutParams = FormUtils.getLinearLayoutParams(10, FormUtils.WRAP_CONTENT, 0, 0, 0, 0);
        final Spinner spinner = new Spinner(context, Spinner.MODE_DIALOG);

        List<String> numbers = new ArrayList<>();
        for (int i = spinnerStartNumber; i <= maxValue; i++) {
            numbers.add(String.valueOf(i));
        }
        numbers.add(0, context.getResources().getString(R.string.select_one)); //This is to enable the first item in the spinner selection.

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, numbers);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setDropDownWidth(100);
        }
        spinner.setLayoutParams(layoutParams);
        spinner.setId(ViewUtil.generateViewId());
        spinner.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        spinner.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        spinner.setTag(R.id.openmrs_entity, openMrsEntity);
        spinner.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        spinner.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        spinner.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(listener);
            }
        });

        return spinner;
    }

    public static void setSelectedTextViewText(String viewText) {
        selectedTextView.setText(viewText);
    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            CustomTextView customTextView) {
        if (!(customTextView.getTag(R.id.v_required) instanceof String) || !(customTextView.getTag(R.id.error) instanceof String)) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        }
        Boolean isRequired = Boolean.valueOf((String) customTextView.getTag(R.id.v_required));
        if (!isRequired || !customTextView.isEnabled()) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        }
        String selectedNumber = String.valueOf(customTextView.getText());
        if (!selectedNumber.isEmpty()) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        } else {
            return new ValidationStatus(false, (String) customTextView.getTag(R.id.error), formFragmentView, customTextView);
        }
    }

    private void createNumberSelector(CustomTextView textView) {
        Spinner spinner = (Spinner) textView.getTag(R.id.number_selector_spinner);
        spinner.performClick();
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener
            listener) throws Exception {
        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(context).inflate(getLayout(), null);

        rootLayout.setId(ViewUtil.generateViewId());
        rootLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        if (relevance != null && context instanceof JsonApi) {
            rootLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }
        views.add(rootLayout);
        createTextViews(context, jsonObject, rootLayout, listener, stepName);

        return views;
    }

    @SuppressLint("NewApi")
    private void createTextViews(Context context, JSONObject jsonObject, LinearLayout linearLayout, CommonListener listener, String stepName) throws JSONException {
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int width = ImageUtils.getDeviceWidth(context);
        width = (int) (width - context.getResources().getDimension(R.dimen.native_selector_total_screen_size_padding));
        int numberOfSelectors = jsonObject.optInt(JsonFormConstants.NUMBER_OF_SELECTORS, 5);
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        for (int i = 0; i < numberOfSelectors; i++) {
            CustomTextView customTextView = createCustomView(context, jsonObject, width, numberOfSelectors, listener, linearLayout, i);
            if (i == numberOfSelectors - 1 && numberOfSelectors - 1 < maxValue) {
                spinner = createDialogSpinner(context, jsonObject, (startSelectionNumber + (numberOfSelectors - 1)), listener, stepName);
                spinner.setTag(R.id.number_selector_textview, customTextView);
                customTextView.setTag(R.id.number_selector_spinner, spinner);
                customTextView.setOnClickListener(selectedNumberClickListener);
            } else {
                customTextView.setOnClickListener(listener);
            }
            linearLayout.addView(customTextView);
        }

        if (spinner != null) {
            linearLayout.addView(spinner);
        }
    }

    public String getText(int item, int startSelectionNumber, int numberOfSelectors, int maxValue) {
        String text = startSelectionNumber == 0 ? String.valueOf(item) : startSelectionNumber == 1 ? String.valueOf(item + 1) : String.valueOf(startSelectionNumber + item);
        if (item == numberOfSelectors - 1 && maxValue > Integer.parseInt(text)) {
            text = text + "+";
        }
        return text;
    }

    protected int getLayout() {
        return R.layout.native_form_item_numbers_selector;
    }

    @SuppressLint("NewApi")
    private CustomTextView createCustomView(Context context, JSONObject jsonObject, int width, int numberOfSelectors, CommonListener listener, LinearLayout linearLayout, int item) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
        String selectedTextColor = jsonObject.optString(JsonFormConstants.NUMBER_SELECTOR_SELCTED_TEXT_COLOR, JsonFormConstants.DEFAULT_NUMBER_SELECTOR_TEXT_COLOR);
        String textSize = jsonObject.getString(JsonFormConstants.TEXT_SIZE);
        textSize = textSize == null ? String.valueOf(context.getResources().getDimension(R.dimen.default_text_size)) : String.valueOf(FormUtils.getValueFromSpOrDpOrPx(textSize, context));
        LinearLayout.LayoutParams layoutParams = FormUtils.getLinearLayoutParams(width / numberOfSelectors, FormUtils.WRAP_CONTENT, 1, 2, 1, 2);

        CustomTextView customTextView = FormUtils.getTextViewWith(context, Integer.parseInt(textSize), getText(item, startSelectionNumber,
                numberOfSelectors, maxValue), jsonObject.getString(JsonFormConstants.KEY),
                jsonObject.getString("type"), "", "", "",
                "", layoutParams, FormUtils.FONT_BOLD_PATH, 0, textColor);

        customTextView.setId(ViewUtil.generateViewId());
        customTextView.setPadding(0, 5, 0, 5);
        setDefaultColor(context, customTextView, item, numberOfSelectors, textColor);
        customTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        customTextView.setClickable(true);
        customTextView.setTag(R.id.number_selector_item, item);
        customTextView.setTag(R.id.number_selector_number_of_selectors, numberOfSelectors);
        customTextView.setTag(R.id.number_selector_max_number, maxValue);
        customTextView.setTag(R.id.number_selector_default_text_color, textColor);
        customTextView.setTag(R.id.number_selector_selected_text_color, selectedTextColor);
        customTextView.setTag(R.id.number_selector_start_selection_number, startSelectionNumber);
        customTextView.setTag(R.id.number_selector_listener, listener);
        customTextView.setTag(R.id.v_required, addRequiredTag(jsonObject));
        customTextView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        customTextView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        customTextView.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        customTextView.setTag(R.id.openmrs_entity, openMrsEntity);
        customTextView.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        if (item == numberOfSelectors - 1) {
            customTextView.setTag(R.id.number_selector_layout, linearLayout);
            customTextView.setTag(R.id.number_selector_jsonObject, jsonObject);
        }

        return customTextView;
    }

    private String addRequiredTag(JSONObject jsonObject) throws JSONException {
        String required = "false";
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue) && Boolean.TRUE.toString().equalsIgnoreCase(requiredValue)) {
                required = "true";
            }
        }

        return required;
    }

    private class SelectedNumberClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createNumberSelector((CustomTextView) view);
        }
    }

}
