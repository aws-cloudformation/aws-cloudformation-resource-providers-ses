package com.amazonaws.ses.configurationseteventdestination;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslatorTest {
    @Test
    public void test_translate_SdkEventDestination() {
        final software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration dimensionConfiguration = software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration.builder()
                .defaultDimensionValue("5")
                .dimensionName("test")
                .dimensionValueSource("test")
                .build();
        final software.amazon.awssdk.services.ses.model.CloudWatchDestination cloudWatchDestination = software.amazon.awssdk.services.ses.model.CloudWatchDestination.builder()
                .dimensionConfigurations(Arrays.asList(dimensionConfiguration))
                .build();
        final software.amazon.awssdk.services.ses.model.KinesisFirehoseDestination kinesisFirehoseDestination = software.amazon.awssdk.services.ses.model.KinesisFirehoseDestination.builder()
                .deliveryStreamARN("test")
                .iamRoleARN("test")
                .build();
        final software.amazon.awssdk.services.ses.model.SNSDestination snsDestination = software.amazon.awssdk.services.ses.model.SNSDestination.builder()
                .topicARN("test")
                .build();
        final software.amazon.awssdk.services.ses.model.EventDestination in = software.amazon.awssdk.services.ses.model.EventDestination.builder()
                .cloudWatchDestination(cloudWatchDestination)
                .enabled(true)
                .kinesisFirehoseDestination(kinesisFirehoseDestination)
                .matchingEventTypesWithStrings(Arrays.asList("send", "reject", "bounce"))
                .name("test")
                .snsDestination(snsDestination)
                .build();
        final com.amazonaws.ses.configurationseteventdestination.EventDestination out = Translator.translate(in);
        final software.amazon.awssdk.services.ses.model.EventDestination result = Translator.translate(out);
        assertThat(out).isNotNull();
        assertThat(in).isEqualTo(result);
    }
}
