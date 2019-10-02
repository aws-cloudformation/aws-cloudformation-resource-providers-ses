package com.amazonaws.ses.configurationseteventdestination;

import software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Translator {
    public static software.amazon.awssdk.services.ses.model.EventDestination translate(com.amazonaws.ses.configurationseteventdestination.EventDestination in) {
        if (in == null) return null;
        return software.amazon.awssdk.services.ses.model.EventDestination.builder()
                .cloudWatchDestination(translate(in.getCloudWatchDestination()))
                .enabled(in.getEnabled())
                .kinesisFirehoseDestination(translate(in.getKinesisFirehoseDestination()))
                .matchingEventTypesWithStrings(in.getMatchingEventTypes())
                .name(in.getName())
                .snsDestination(translate(in.getSNSDestination()))
                .build();
    }

    private static software.amazon.awssdk.services.ses.model.CloudWatchDestination translate(com.amazonaws.ses.configurationseteventdestination.CloudWatchDestination in) {
        if (in == null) return null;
        if (in.getDimensionConfigurations() == null || in.getDimensionConfigurations().isEmpty())
            return software.amazon.awssdk.services.ses.model.CloudWatchDestination.builder().dimensionConfigurations(Collections.emptyList()).build();
        return software.amazon.awssdk.services.ses.model.CloudWatchDestination.builder()
                .dimensionConfigurations(in.getDimensionConfigurations().stream().map(n -> translate(n)).collect(Collectors.toList()))
                .build();
    }

    private static software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration translate(com.amazonaws.ses.configurationseteventdestination.DimensionConfigurations in) {
        if (in == null) return null;
        return software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration.builder()
                .defaultDimensionValue(in.getDefaultDimensionValue())
                .dimensionName(in.getDimensionName())
                .dimensionValueSource(in.getDimensionValueSource())
                .build();
    }

    private static software.amazon.awssdk.services.ses.model.KinesisFirehoseDestination translate(com.amazonaws.ses.configurationseteventdestination.KinesisFirehoseDestination in) {
        if (in == null) return null;
        return software.amazon.awssdk.services.ses.model.KinesisFirehoseDestination.builder()
                .deliveryStreamARN(in.getDeliveryStreamARN())
                .iamRoleARN(in.getIAMRoleARN())
                .build();
    }

    private static software.amazon.awssdk.services.ses.model.SNSDestination translate(com.amazonaws.ses.configurationseteventdestination.SNSDestination in) {
        if (in == null) return null;
        return software.amazon.awssdk.services.ses.model.SNSDestination.builder()
                .topicARN(in.getTopicARN())
                .build();
    }

    public static com.amazonaws.ses.configurationseteventdestination.EventDestination translate(software.amazon.awssdk.services.ses.model.EventDestination in) {
        if (in == null) return null;
        return com.amazonaws.ses.configurationseteventdestination.EventDestination.builder()
                .cloudWatchDestination(translate(in.cloudWatchDestination()))
                .enabled(in.enabled())
                .kinesisFirehoseDestination(translate(in.kinesisFirehoseDestination()))
                .matchingEventTypes(in.matchingEventTypesAsStrings())
                .name(in.name())
                .sNSDestination(translate(in.snsDestination()))
                .build();
    }

    private static com.amazonaws.ses.configurationseteventdestination.CloudWatchDestination translate(software.amazon.awssdk.services.ses.model.CloudWatchDestination in) {
        if (in == null) return null;
        return com.amazonaws.ses.configurationseteventdestination.CloudWatchDestination.builder()
                .dimensionConfigurations(in.dimensionConfigurations().stream().map(n -> translate(n)).collect(Collectors.toList()))
                .build();
    }

    private static com.amazonaws.ses.configurationseteventdestination.DimensionConfigurations translate(software.amazon.awssdk.services.ses.model.CloudWatchDimensionConfiguration in) {
        if (in == null) return null;
        return com.amazonaws.ses.configurationseteventdestination.DimensionConfigurations.builder()
                .defaultDimensionValue(in.defaultDimensionValue())
                .dimensionName(in.dimensionName())
                .dimensionValueSource(in.dimensionValueSourceAsString())
                .build();
    }

    private static com.amazonaws.ses.configurationseteventdestination.KinesisFirehoseDestination translate(software.amazon.awssdk.services.ses.model.KinesisFirehoseDestination in) {
        if (in == null) return null;
        return com.amazonaws.ses.configurationseteventdestination.KinesisFirehoseDestination.builder()
                .deliveryStreamARN(in.deliveryStreamARN())
                .iAMRoleARN(in.iamRoleARN())
                .build();
    }

    private static com.amazonaws.ses.configurationseteventdestination.SNSDestination translate(software.amazon.awssdk.services.ses.model.SNSDestination in) {
        if (in == null) return null;
        return com.amazonaws.ses.configurationseteventdestination.SNSDestination.builder()
                .topicARN(in.topicARN())
                .build();
    }
}
