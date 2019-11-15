package com.aws.ses.configurationset;

import org.json.JSONObject;

class ResourceModelExtensions {

    static JSONObject getPrimaryIdentifier(final ResourceModel model) {
        final JSONObject identifier = new JSONObject();
        identifier.append("Name", model.getName());
        return identifier;
    }
}
