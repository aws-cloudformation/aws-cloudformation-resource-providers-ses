package com.amazonaws.ses.receiptfilter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TranslatorTest {
    @Test
    public void test_translate_ReceiptIpFilter() {
        final software.amazon.awssdk.services.ses.model.ReceiptIpFilter in = software.amazon.awssdk.services.ses.model.ReceiptIpFilter.builder()
                .cidr("10.0.0.1/24")
                .policy("Allow")
                .build();
        IpFilter out = Translator.translate(in);
        final software.amazon.awssdk.services.ses.model.ReceiptIpFilter result = Translator.translate(out);
        assertThat(out).isNotNull();
        assertThat(in).isEqualTo(result);

        out = Translator.translate((software.amazon.awssdk.services.ses.model.ReceiptIpFilter) null);
        assertThat(out).isNull();
    }

    @Test
    public void test_translate_ReceiptFilter() {
        final software.amazon.awssdk.services.ses.model.ReceiptFilter in = software.amazon.awssdk.services.ses.model.ReceiptFilter.builder()
                .ipFilter(software.amazon.awssdk.services.ses.model.ReceiptIpFilter.builder()
                        .cidr("10.0.0.1/24")
                        .policy("Allow")
                        .build())
                .name("test")
                .build();
        Filter out = Translator.translate(in);
        final software.amazon.awssdk.services.ses.model.ReceiptFilter result = Translator.translate(out);
        assertThat(out).isNotNull();
        assertThat(in).isEqualTo(result);

        out = Translator.translate((software.amazon.awssdk.services.ses.model.ReceiptFilter) null);
        assertThat(out).isNull();
    }

    @Test
    public void test_translate_IpFilter() {
        final IpFilter in = IpFilter.builder()
                .cidr("10.0.0.1/24")
                .policy("Allow")
                .build();
        software.amazon.awssdk.services.ses.model.ReceiptIpFilter out = Translator.translate(in);
        final IpFilter result = Translator.translate(out);
        assertThat(out).isNotNull();
        assertThat(in).isEqualTo(result);

        out = Translator.translate((IpFilter) null);
        assertThat(out).isNull();
    }

    @Test
    public void test_translate_Filter() {
        final Filter in = Filter.builder()
                .ipFilter(IpFilter.builder()
                        .cidr("10.0.0.1/24")
                        .policy("Allow")
                        .build())
                .name("test")
                .build();
        software.amazon.awssdk.services.ses.model.ReceiptFilter out = Translator.translate(in);
        final Filter result = Translator.translate(out);
        assertThat(out).isNotNull();
        assertThat(in).isEqualTo(result);

        out = Translator.translate((Filter) null);
        assertThat(out).isNull();
    }
}
