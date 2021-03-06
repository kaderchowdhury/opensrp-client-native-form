package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.validators.edittext.MaxNumericValidator;
import com.vijay.jsonwizard.validators.edittext.MinNumericValidator;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijay on 24-05-2015.
 */
public class NativeEditTextFactory implements FormWidgetFactory {
    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            NativeEditText editText) {
        if (editText.isEnabled()) {
            boolean validate = editText.validate();
            if (!validate) {
                return new ValidationStatus(false, editText.getError().toString(), formFragmentView, editText);
            }
        }
        return new ValidationStatus(true, null, formFragmentView, editText);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener
            listener) throws Exception {
        List<View> views = new ArrayList<>(1);

        RelativeLayout rootLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                getLayout(), null);
        NativeEditText editText = rootLayout.findViewById(R.id.normal_edit_text);

        makeFromJson(stepName, context, formFragment, jsonObject, editText);

        addRequiredValidator(jsonObject, editText);
        addRegexValidator(jsonObject, editText);
        addEmailValidator(jsonObject, editText);
        addUrlValidator(jsonObject, editText);
        addNumericValidator(jsonObject, editText);
        addNumericIntegerValidator(jsonObject, editText);

        JSONArray canvasIds = new JSONArray();
        rootLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(rootLayout.getId());
        editText.setTag(R.id.canvas_ids, canvasIds.toString());

        ((JsonApi) context).addFormDataView(editText);
        views.add(rootLayout);
        return views;
    }

    protected void makeFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                JSONObject jsonObject, NativeEditText editText) throws Exception {

        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String editTextStyle = jsonObject.optString(JsonFormConstants.EDIT_TEXT_STYLE, "");
        String editType = jsonObject.optString(JsonFormConstants.EDIT_TYPE);

        editText.setId(ViewUtil.generateViewId());
        editText.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        editText.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        editText.setTag(R.id.openmrs_entity, openMrsEntity);
        editText.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        editText.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        editText.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));

        if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE))) {
            editText.setText(jsonObject.optString(JsonFormConstants.VALUE));
        }
        //Add edittext style
        if (!TextUtils.isEmpty(editTextStyle)) {
            if (JsonFormConstants.BORDERED_EDIT_TEXT.equals(editTextStyle)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    editText.setBackground(context.getResources()
                            .getDrawable(R.drawable.edit_text_bg));
                } else {
                    editText.setBackgroundDrawable(context.getResources()
                            .getDrawable(R.drawable.edit_text_bg));
                }
            }
        } else {
            editText.setHint(jsonObject.getString(JsonFormConstants.HINT));
        }
        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readyOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            editText.setEnabled(!readyOnly);
            editText.setFocusable(!readyOnly);
        }
        // edit type check
        if (!TextUtils.isEmpty(editType)) {
            if ("number".equals(editType)) {
                editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else if ("name".equals(editType)) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            }
        }

        editText.addTextChangedListener(new GenericTextWatcher(stepName, formFragment, editText));
        if (relevance != null && context instanceof JsonApi) {
            editText.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(editText);
        }

        if (constraints != null && context instanceof JsonApi) {
            editText.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(editText);
        }

    }

    protected int getLayout() {
        return R.layout.native_form_normal_edit_text;
    }

    private void addRequiredValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue) && Boolean.TRUE.toString().equalsIgnoreCase(requiredValue)) {
                editText.addValidator(new RequiredValidator(requiredObject.getString(JsonFormConstants.ERR)));
            }
        }
    }

    private void addRegexValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject regexObject = jsonObject.optJSONObject(JsonFormConstants.V_REGEX);
        if (regexObject != null) {
            String regexValue = regexObject.optString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(regexValue)) {
                editText.addValidator(new RegexpValidator(regexObject.getString(JsonFormConstants.ERR), regexValue));
            }
        }
    }

    private void addEmailValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject emailObject = jsonObject.optJSONObject(JsonFormConstants.V_EMAIL);
        if (emailObject != null) {
            String emailValue = emailObject.optString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(emailValue) && Boolean.TRUE.toString().equalsIgnoreCase(emailValue)) {
                editText.addValidator(new RegexpValidator(emailObject.getString(JsonFormConstants.ERR), android.util.Patterns.EMAIL_ADDRESS
                        .toString()));
            }

        }

    }

    private void addUrlValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject urlObject = jsonObject.optJSONObject(JsonFormConstants.V_URL);
        if (urlObject != null) {
            String urlValue = urlObject.optString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(urlValue) && Boolean.TRUE.toString().equalsIgnoreCase(urlValue)) {
                editText.addValidator(new RegexpValidator(urlObject.getString(JsonFormConstants.ERR), Patterns.WEB_URL.toString()));
            }

        }
    }

    private void addNumericValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject numericObject = jsonObject.optJSONObject(JsonFormConstants.V_NUMERIC);
        if (numericObject != null) {
            String numericValue = numericObject.optString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(numericValue) && Boolean.TRUE.toString().equalsIgnoreCase(numericValue)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.addValidator(new RegexpValidator(numericObject.getString(JsonFormConstants.ERR),
                        "[0-9]*\\.?[0-9]*"));

                if (jsonObject.has(JsonFormConstants.V_MIN)) {
                    JSONObject minValidation = jsonObject.getJSONObject(JsonFormConstants.V_MIN);
                    editText.addValidator(new MinNumericValidator(minValidation.getString(JsonFormConstants.ERR),
                            Double.parseDouble(minValidation.getString(JsonFormConstants.VALUE))));
                }

                if (jsonObject.has(JsonFormConstants.V_MAX)) {
                    JSONObject minValidation = jsonObject.getJSONObject(JsonFormConstants.V_MAX);
                    editText.addValidator(new MaxNumericValidator(minValidation.getString(JsonFormConstants.ERR),
                            Double.parseDouble(minValidation.getString(JsonFormConstants.VALUE))));
                }
            }
        }
    }

    private void addNumericIntegerValidator(JSONObject jsonObject, NativeEditText editText) throws JSONException {
        JSONObject numericIntegerObject = jsonObject.optJSONObject(JsonFormConstants.V_NUMERIC_INTEGER);
        if (numericIntegerObject != null) {
            String numericValue = numericIntegerObject.optString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(numericValue) && Boolean.TRUE.toString().equalsIgnoreCase(numericValue)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                editText.addValidator(new RegexpValidator(numericIntegerObject.getString(JsonFormConstants.ERR),
                        "\\d*"));

                if (jsonObject.has(JsonFormConstants.V_MIN)) {
                    JSONObject minValidation = jsonObject.getJSONObject(JsonFormConstants.V_MIN);
                    editText.addValidator(new MinNumericValidator(minValidation.getString(JsonFormConstants.ERR),
                            Double.parseDouble(minValidation.getString(JsonFormConstants.VALUE))));
                }

                if (jsonObject.has(JsonFormConstants.V_MAX)) {
                    JSONObject minValidation = jsonObject.getJSONObject(JsonFormConstants.V_MAX);
                    editText.addValidator(new MaxNumericValidator(minValidation.getString(JsonFormConstants.ERR),
                            Double.parseDouble(minValidation.getString(JsonFormConstants.VALUE))));
                }
            }
        }
    }
}
