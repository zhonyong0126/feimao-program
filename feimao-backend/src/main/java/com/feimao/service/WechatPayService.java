package com.feimao.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class WechatPayService {
  private final RestTemplate http;
  private final String appId, mchId, apiKey, notifyBase, certPath;
  private final SecureRandom random = new SecureRandom();

  public WechatPayService(
      RestTemplateBuilder builder,
      @Value("${business.wechat.app-id:}") String appId,
      @Value("${business.wechat-pay.merchant-id:}") String mchId,
      @Value("${business.wechat-pay.api-key:}") String apiKey,
      @Value("${business.wechat-pay.notify-base-url:}") String notifyBase,
      @Value("${business.wechat-pay.cert-path:}") String certPath) {
    this.http = builder.build();
    this.appId = appId;
    this.mchId = mchId;
    this.apiKey = apiKey;
    this.notifyBase = notifyBase;
    this.certPath = certPath;
  }

  public boolean configured() {
    return !appId.isEmpty() && !mchId.isEmpty() && !apiKey.isEmpty() && !notifyBase.isEmpty();
  }

  public Map<String, String> unifiedOrder(
      String openid, String number, String body, long cents, String notifyPath) {
    if (!configured())
      throw new IllegalStateException(
          "微信支付未配置，请设置 WECHAT_APP_ID、WECHAT_PAY_MCH_ID、WECHAT_PAY_API_KEY 和 WECHAT_PAY_NOTIFY_BASE_URL");
    Map<String, String> p = new TreeMap<>();
    p.put("appid", appId);
    p.put("mch_id", mchId);
    p.put("nonce_str", nonce());
    p.put("body", body);
    p.put("out_trade_no", number);
    p.put("total_fee", String.valueOf(cents));
    p.put("spbill_create_ip", "127.0.0.1");
    p.put("notify_url", notifyBase.replaceAll("/$", "") + notifyPath);
    p.put("trade_type", "JSAPI");
    p.put("openid", openid);
    p.put("sign", sign(p));
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_XML);
    String xml =
        http.postForObject(
            "https://api.mch.weixin.qq.com/pay/unifiedorder",
            new HttpEntity<>(xml(p), h),
            String.class);
    Map<String, String> result = parse(xml);
    if (!"SUCCESS".equals(result.get("return_code"))
        || !"SUCCESS".equals(result.get("result_code")))
      throw new IllegalStateException(
          result.getOrDefault("err_code_des", result.getOrDefault("return_msg", "微信支付下单失败")));
    Map<String, String> js = new LinkedHashMap<>();
    js.put("appId", appId);
    js.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
    js.put("nonceStr", nonce());
    js.put("package", "prepay_id=" + result.get("prepay_id"));
    js.put("signType", "MD5");
    js.put("paySign", sign(js));
    return js;
  }

  public boolean verify(Map<String, String> values) {
    String supplied = values.get("sign");
    Map<String, String> copy = new TreeMap<>(values);
    copy.remove("sign");
    return supplied != null && supplied.equals(sign(copy));
  }

  public void refund(String transactionId, String refundNumber, long totalCents, long refundCents) {
    if (!configured() || certPath.isEmpty())
      throw new IllegalStateException("微信退款未配置，请设置支付参数和 WECHAT_PAY_CERT_PATH");
    try {
      Map<String, String> p = new TreeMap<>();
      p.put("appid", appId);
      p.put("mch_id", mchId);
      p.put("nonce_str", nonce());
      p.put("transaction_id", transactionId);
      p.put("out_refund_no", refundNumber);
      p.put("total_fee", String.valueOf(totalCents));
      p.put("refund_fee", String.valueOf(refundCents));
      p.put("refund_desc", "租车押金退款");
      p.put("sign", sign(p));
      KeyStore ks = KeyStore.getInstance("PKCS12");
      try (FileInputStream in = new FileInputStream(certPath)) {
        ks.load(in, mchId.toCharArray());
      }
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, mchId.toCharArray());
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(kmf.getKeyManagers(), null, null);
      HttpsURLConnection c =
          (HttpsURLConnection)
              new URL("https://api.mch.weixin.qq.com/secapi/pay/refund").openConnection();
      c.setSSLSocketFactory(context.getSocketFactory());
      c.setRequestMethod("POST");
      c.setDoOutput(true);
      c.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
      try (OutputStream out = c.getOutputStream()) {
        out.write(xml(p).getBytes(StandardCharsets.UTF_8));
      }
      java.io.InputStream response =
          c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
      java.util.Scanner scanner = new java.util.Scanner(response, "UTF-8").useDelimiter("\\A");
      Map<String, String> result = parse(scanner.hasNext() ? scanner.next() : "");
      if (!"SUCCESS".equals(result.get("return_code"))
          || !"SUCCESS".equals(result.get("result_code")))
        throw new IllegalStateException(
            result.getOrDefault("err_code_des", result.getOrDefault("return_msg", "微信退款失败")));
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("微信退款请求失败", e);
    }
  }

  public Map<String, String> parse(String xml) {
    try {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      f.setFeature("http://xml.org/sax/features/external-general-entities", false);
      f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      Document d =
          f.newDocumentBuilder()
              .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      Map<String, String> out = new HashMap<>();
      NodeList nodes = d.getDocumentElement().getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node n = nodes.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) out.put(n.getNodeName(), n.getTextContent());
      }
      return out;
    } catch (Exception e) {
      throw new IllegalArgumentException("支付回调格式错误", e);
    }
  }

  public String successXml() {
    return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
  }

  public String failXml(String msg) {
    return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["
        + escape(msg)
        + "]]></return_msg></xml>";
  }

  private String sign(Map<String, String> values) {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String> e : new TreeMap<>(values).entrySet())
      if (e.getValue() != null && !e.getValue().isEmpty() && !"sign".equals(e.getKey()))
        b.append(e.getKey()).append('=').append(e.getValue()).append('&');
    b.append("key=").append(apiKey);
    return DigestUtils.md5DigestAsHex(b.toString().getBytes(StandardCharsets.UTF_8))
        .toUpperCase(Locale.ROOT);
  }

  private String nonce() {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return DigestUtils.md5DigestAsHex(bytes);
  }

  private static String xml(Map<String, String> p) {
    StringBuilder b = new StringBuilder("<xml>");
    for (Map.Entry<String, String> e : p.entrySet())
      b.append('<')
          .append(e.getKey())
          .append("><![CDATA[")
          .append(e.getValue())
          .append("]]></")
          .append(e.getKey())
          .append('>');
    return b.append("</xml>").toString();
  }

  private static String escape(String s) {
    return s == null ? "error" : s.replace("]]>", "] ]>");
  }
}
