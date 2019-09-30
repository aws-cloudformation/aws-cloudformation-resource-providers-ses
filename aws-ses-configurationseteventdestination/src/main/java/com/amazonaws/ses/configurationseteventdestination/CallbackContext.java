package com.amazonaws.ses.configurationseteventdestination;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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
