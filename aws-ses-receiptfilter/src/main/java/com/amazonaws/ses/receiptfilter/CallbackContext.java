package com.amazonaws.ses.receiptfilter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
public class CallbackContext {
    private Boolean stabilization;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CallbackContextBuilder implements CallbackContextBuilderMeta {
    }

    private interface CallbackContextBuilderMeta {
        @JsonDeserialize(contentAs = Boolean.class)
        CallbackContextBuilder stabilization(Boolean value);
    }
}
