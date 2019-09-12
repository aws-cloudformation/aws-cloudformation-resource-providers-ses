package com.amazonaws.ses.receiptfilter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallbackContext {
    private Boolean stabilization;
}
