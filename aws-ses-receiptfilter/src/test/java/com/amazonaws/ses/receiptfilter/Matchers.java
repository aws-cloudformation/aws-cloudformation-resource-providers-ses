package com.amazonaws.ses.receiptfilter;

import software.amazon.awssdk.services.ses.model.ReceiptFilter;

import static org.assertj.core.api.Assertions.assertThat;

public class Matchers {
    public static void assertThatModelsAreEqual(final Object rawModel,
                                          final ReceiptFilter sdkModel) {
        assertThat(rawModel).isInstanceOf(ResourceModel.class);
        ResourceModel model = (ResourceModel) rawModel;
        assertThat(model.getFilter().getIpFilter().getCidr()).isEqualTo(sdkModel.ipFilter().cidr());
        assertThat(model.getFilter().getIpFilter().getPolicy()).isEqualTo(sdkModel.ipFilter().policy().toString());
        assertThat(model.getFilter().getName()).isEqualTo(sdkModel.name());
    }
}
