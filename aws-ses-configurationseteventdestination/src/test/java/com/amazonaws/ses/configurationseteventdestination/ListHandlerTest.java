package com.amazonaws.ses.configurationseteventdestination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetRequest;
import software.amazon.awssdk.services.ses.model.DescribeConfigurationSetResponse;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private static final String configurationSetName = "test-set";
    private static final String eventDestinationName = "test-event";

    @BeforeEach
    public void setup() {
        model = ResourceModel.builder()
                .configurationSetName(configurationSetName)
                .build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ConfigurationSet set = ConfigurationSet.builder().name(configurationSetName).build();
        final EventDestination eventDestination = EventDestination.builder().name(eventDestinationName).build();
        final DescribeConfigurationSetResponse describeResponse = DescribeConfigurationSetResponse.builder()
                .configurationSet(set)
                .eventDestinations(Arrays.asList(Translator.translate(eventDestination)))
                .build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(DescribeConfigurationSetRequest.class),
                        ArgumentMatchers.any()
                );


        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).hasSize(1);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ConfigurationSetDoesNotExist() {
        final ListHandler handler = new ListHandler();
        doThrow(ConfigurationSetDoesNotExistException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(DescribeConfigurationSetRequest.class),
                        ArgumentMatchers.any()
                );
        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
