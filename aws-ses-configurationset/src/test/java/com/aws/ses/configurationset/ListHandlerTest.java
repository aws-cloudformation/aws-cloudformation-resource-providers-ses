package com.aws.ses.configurationset;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import software.amazon.awssdk.services.ses.model.ConfigurationSet;
import software.amazon.awssdk.services.ses.model.ListConfigurationSetsResponse;

import java.util.ArrayList;
import java.util.List;

import static com.aws.ses.configurationset.Matchers.assertThatModelsAreEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void test_HandleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final List<ConfigurationSet> existingResources = new ArrayList<>();
        ConfigurationSet set1 = ConfigurationSet.builder().name("set1").build();
        ConfigurationSet set2 = ConfigurationSet.builder().name("set2").build();
        existingResources.add(set1);
        existingResources.add(set2);
        final ListConfigurationSetsResponse listResponse = ListConfigurationSetsResponse.builder()
            .configurationSets(existingResources)
            .build();

        doReturn(listResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response, is(not(nullValue())));
        assertThat(response.getStatus(), is(equalTo(OperationStatus.SUCCESS)));
        assertThat(response.getCallbackContext(), is(nullValue()));
        assertThat(response.getCallbackDelaySeconds(), is(equalTo(0)));
        assertThat(response.getResourceModel(), is(nullValue()));
        assertThat(response.getResourceModels().size(), is(equalTo(2)));
        assertThatModelsAreEqual(response.getResourceModels().get(0), set1);
        assertThatModelsAreEqual(response.getResourceModels().get(1), set2);
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getErrorCode(), is(nullValue()));
    }

}
