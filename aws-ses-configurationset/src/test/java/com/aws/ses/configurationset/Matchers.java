package com.aws.ses.configurationset;

import software.amazon.awssdk.services.ses.model.ConfigurationSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class Matchers {

    public static void assertThatModelsAreEqual(final Object rawModel,
                                                final ConfigurationSet sdkModel) {
        assertThat(rawModel, is(instanceOf(ResourceModel.class)));
        ResourceModel model = (ResourceModel)rawModel;
        assertThat(model.getName(), is(equalTo(sdkModel.name())));
    }
}
