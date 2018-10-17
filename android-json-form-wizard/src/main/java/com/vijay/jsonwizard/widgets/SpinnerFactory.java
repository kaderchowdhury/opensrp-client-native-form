package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nipun on 30/05/15.
 */
public class SpinnerFactory implements FormWidgetFactory {

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        String openMrsEntityParent = jsonObject.getString("openmrs_entity_parent");
        String openMrsEntity = jsonObject.getString("openmrs_entity");
        String openMrsEntityId = jsonObject.getString("openmrs_entity_id");
        String relevance = jsonObject.optString("relevance");
        String labelInfoText = jsonObject.optString(JsonFormConstants.LABEL_INFO_TEXT,"");
        String labelInfoTitle = jsonObject.optString(JsonFormConstants.LABEL_INFO_TITLE,"");

        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        RelativeLayout spinnerRelativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.native_form_item_spinner, null);
        MaterialSpinner spinner = spinnerRelativeLayout.findViewById(R.id.material_spinner);
        ImageView spinnerInfoIconImageView = spinnerRelativeLayout.findViewById(R.id.spinner_info_icon);

        String hint = jsonObject.optString(JsonFormConstants.HINT);
        if (!TextUtils.isEmpty(hint)) {
            spinner.setHint(jsonObject.getString(JsonFormConstants.HINT));
            spinner.setFloatingLabelText(jsonObject.getString(JsonFormConstants.HINT));
        }

       // spinner.setId(ViewUtil.generateViewId());
        canvasIds.put(spinner.getId());

        spinner.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        spinner.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        spinner.setTag(R.id.openmrs_entity, openMrsEntity);
        spinner.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        spinner.setTag(R.id.type, jsonObject.getString("type"));
        spinner.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));

        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue)) {
                spinner.setTag(R.id.v_required, requiredValue);
                spinner.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR));
            }
        }

        String valueToSelect = "";
        int indexToSelect = -1;
        if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE))) {
            valueToSelect = jsonObject.optString(JsonFormConstants.VALUE);
        }

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            spinner.setEnabled(!jsonObject.getBoolean(JsonFormConstants.READ_ONLY));
            spinner.setFocusable(!jsonObject.getBoolean(JsonFormConstants.READ_ONLY));
        }

        JSONArray valuesJson = jsonObject.optJSONArray("values");
        String[] values = null;
        if (valuesJson != null && valuesJson.length() > 0) {
            values = new String[valuesJson.length()];
            for (int i = 0; i < valuesJson.length(); i++) {
                values[i] = valuesJson.optString(i);
                if (valueToSelect.equals(values[i])) {
                    indexToSelect = i;
                }
            }
        }

        if (values != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.native_form_simple_list_item_1, values);

            spinner.setAdapter(adapter);

            spinner.setSelection(indexToSelect + 1, true);
            spinner.setOnItemSelectedListener(listener);
        }
        ((JsonApi) context).addFormDataView(spinner);
       // views.add(spinner);
        FormUtils.showInfoIcon(jsonObject,listener,labelInfoText,labelInfoTitle,spinnerInfoIconImageView);
        spinner.setTag(R.id.canvas_ids, canvasIds.toString());
        if (relevance != null && context instanceof JsonApi) {
            spinner.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(spinner);
        }
        views.add(spinnerRelativeLayout);
        return views;
    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            MaterialSpinner spinner) {
        if (!(spinner.getTag(R.id.v_required) instanceof String) || !(spinner.getTag(R.id.error) instanceof String)) {
            return new ValidationStatus(true, null, formFragmentView, spinner);
        }
        Boolean isRequired = Boolean.valueOf((String) spinner.getTag(R.id.v_required));
        if (!isRequired || !spinner.isEnabled()) {
            return new ValidationStatus(true, null, formFragmentView, spinner);
        }
        int selectedItemPosition = spinner.getSelectedItemPosition();
        if (selectedItemPosition > 0) {
            return new ValidationStatus(true, null, formFragmentView, spinner);
        }
        return new ValidationStatus(false, (String) spinner.getTag(R.id.error), formFragmentView, spinner);
    }
}
