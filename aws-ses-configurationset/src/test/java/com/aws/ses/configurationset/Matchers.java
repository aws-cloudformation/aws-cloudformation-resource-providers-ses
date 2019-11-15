package com.aws.ses.configurationset;

import software.amazon.awssdk.services.ses.model.ConfigurationSet;

import static org.assertj.core.api.Assertions.assertThat;

class Matchers {

    static void assertThatModelsAreEqual(final Object rawModel,
                                                final ConfigurationSet sdkModel) {
        assertThat(rawModel).isInstanceOf(ResourceModel.class);
        ResourceModel model = (ResourceModel)rawModel;
        assertThat(model.getName()).isEqualTo(sdkModel.name());
    }
}
