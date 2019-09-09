package com.amazonaws.ses.receiptfilter;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class TranslatorTest {
    @Test
    public void test_translate_ReceiptIpFilter(){
        final software.amazon.awssdk.services.ses.model.ReceiptIpFilter in = software.amazon.awssdk.services.ses.model.ReceiptIpFilter.builder()
                .cidr("10.0.0.1/24")
                .policy("Allow")
                .build();
        final com.amazonaws.ses.receiptfilter.IpFilter out = Translator.translate(in);
        final software.amazon.awssdk.services.ses.model.ReceiptIpFilter result  = Translator.translate(out);
        assertThat(out, is(not(nullValue())));
        assertThat(result, is(equalTo(in)));
    }

    @Test
    public void test_translate_ReceiptFilter(){
        final software.amazon.awssdk.services.ses.model.ReceiptFilter in = software.amazon.awssdk.services.ses.model.ReceiptFilter.builder()
                .ipFilter(software.amazon.awssdk.services.ses.model.ReceiptIpFilter.builder()
                        .cidr("10.0.0.1/24")
                        .policy("Allow")
                        .build())
                .name("test")
                .build();
        final com.amazonaws.ses.receiptfilter.Filter out = Translator.translate(in);
        final software.amazon.awssdk.services.ses.model.ReceiptFilter result = Translator.translate(out);
        assertThat(out, is(not(nullValue())));
        assertThat(result, is(equalTo(in)));
    }
}
