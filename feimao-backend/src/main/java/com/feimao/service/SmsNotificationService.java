package com.feimao.service;

import com.feimao.mapper.OrderNotificationMapper;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsNotificationService {
  private static final Logger LOG = LoggerFactory.getLogger(SmsNotificationService.class);

  private final OrderNotificationMapper orders;
  private final RestTemplate http;
  private final String url;
  private final String appCode;
  private final String pickupTemplate;
  private final String deliveryTemplate;
  private final String returnTemplate;

  public SmsNotificationService(
      OrderNotificationMapper orders,
      RestTemplateBuilder builder,
      @Value("${business.sms.url:}") String url,
      @Value("${business.sms.app-code:}") String appCode,
      @Value("${business.sms.pickup-template-id:}") String pickupTemplate,
      @Value("${business.sms.delivery-template-id:}") String deliveryTemplate,
      @Value("${business.sms.return-template-id:767ee50550bead9ff6f501441bb4c923}")
          String returnTemplate) {
    this.orders = orders;
    this.http =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(15))
            .build();
    this.url = url;
    this.appCode = appCode;
    this.pickupTemplate = pickupTemplate;
    this.deliveryTemplate = deliveryTemplate;
    this.returnTemplate = returnTemplate;
  }

  @Async
  public void notifyMerchantOrderPaid(long orderId) {
    try {
      Map<String, Object> row = orders.selectPaidRentalContact(orderId);
      if (row == null || row.isEmpty()) return;
      String template =
          ((Number) row.get("pick_type")).intValue() == 1 ? pickupTemplate : deliveryTemplate;
      send(row.get("mobile"), template);
    } catch (Exception exception) {
      LOG.warn("门店订单短信发送失败，orderId={}", orderId, exception);
    }
  }

  @Async
  public void notifyMerchantReturnRequested(long orderId) {
    try {
      Map<String, Object> row = orders.selectReturnRequestedContact(orderId);
      if (row == null || row.isEmpty()) return;
      send(row.get("mobile"), returnTemplate);
    } catch (Exception exception) {
      LOG.warn("门店还车短信发送失败，orderId={}", orderId, exception);
    }
  }

  private void send(Object mobile, String template) {
    if (url.isEmpty()
        || appCode.isEmpty()
        || template == null
        || template.isEmpty()
        || mobile == null
        || mobile.toString().trim().isEmpty()) return;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Authorization", "APPCODE " + appCode);
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("templateId", template);
    form.add("receive", mobile.toString());
    http.postForObject(url, new HttpEntity<>(form, headers), String.class);
  }
}
