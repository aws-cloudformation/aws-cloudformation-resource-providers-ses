package com.amazonaws.ses.receiptfilter;

public class Translator {
    public static com.amazonaws.ses.receiptfilter.Filter translate (software.amazon.awssdk.services.ses.model.ReceiptFilter in){
        if(in ==null) return null;
        return com.amazonaws.ses.receiptfilter.Filter.builder()
                .ipFilter(translate(in.ipFilter()))
                .name(in.name())
                .build();
    }
    public static com.amazonaws.ses.receiptfilter.IpFilter translate (software.amazon.awssdk.services.ses.model.ReceiptIpFilter in){
        if(in == null) return null;
        return  com.amazonaws.ses.receiptfilter.IpFilter.builder()
                .policy(in.policy().toString())
                .cidr(in.cidr())
                .build();
    }

    public static software.amazon.awssdk.services.ses.model.ReceiptIpFilter translate(com.amazonaws.ses.receiptfilter.IpFilter in) {
        if(in ==null) return null;
        return software.amazon.awssdk.services.ses.model.ReceiptIpFilter.builder()
                .policy(in.getPolicy())
                .cidr(in.getCidr())
                .build();
    }

    public static software.amazon.awssdk.services.ses.model.ReceiptFilter translate (com.amazonaws.ses.receiptfilter.Filter in){
        if(in ==null) return null;
        return software.amazon.awssdk.services.ses.model.ReceiptFilter.builder()
                .ipFilter(translate(in.getIpFilter()))
                .name(in.getName())
                .build();
    }
}
