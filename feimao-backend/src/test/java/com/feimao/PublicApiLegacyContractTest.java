package com.feimao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.feimao.common.BusinessResponse;
import com.feimao.controller.common.IndexController;
import com.feimao.controller.user.UserController;
import com.feimao.entity.BusinessUser;
import com.feimao.entity.BusinessUserToken;
import com.feimao.mapper.BusinessUserTokenMapper;
import com.feimao.mapper.PublicCatalogMapper;
import com.feimao.mapper.UserProfileMapper;
import com.feimao.mapper.VerificationCodeMapper;
import com.feimao.service.BusinessUserService;
import com.feimao.service.MiniProgramCodeService;
import com.feimao.service.UserTokenService;
import com.feimao.service.VerificationCodeService;
import com.feimao.service.WechatLoginService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.mail.javamail.JavaMailSender;

class PublicApiLegacyContractTest {

  @Test
  void indexAcceptsLegacyLargePageSizeAndUsesZeroLastPageForEmptyResult() {
    PublicCatalogMapper mapper = mock(PublicCatalogMapper.class);
    when(mapper.selectLeaseCars(0, 0, 9_999_999_999L, 0L))
        .thenReturn(Collections.emptyList());
    when(mapper.leaseCarCount(0)).thenReturn(0);
    IndexController controller = new IndexController(mapper);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("page", 1);
    body.put("list_rows", 9_999_999_999L);

    @SuppressWarnings("unchecked")
    Map<String, Object> page = (Map<String, Object>) controller.leaseCarList(body).getData();
    assertEquals(9_999_999_999L, ((Number) page.get("per_page")).longValue());
    assertEquals(0L, ((Number) page.get("last_page")).longValue());
    verify(mapper).selectLeaseCars(0, 0, 9_999_999_999L, 0L);
  }

  @Test
  void indexMerchantRelationsKeepTheLegacyFieldsForEachBusinessType() {
    PublicCatalogMapper mapper = mock(PublicCatalogMapper.class);
    Map<String, Object> lease = row("mid", 2L, "pick_type", 1, "merchant_image", "a,b",
        "avatar", "avatar.jpg", "administrative", "四川", "address", "西昌",
        "merchant_name", "门店", "name", "车辆", "image", "car.jpg");
    when(mapper.selectLeaseMerchants(7L)).thenReturn(mutable(lease));
    IndexController controller = new IndexController(mapper);

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result =
        (List<Map<String, Object>>) controller.leaseMerchantCarList(row("platform_car_id", 7)).getData();
    @SuppressWarnings("unchecked")
    Map<String, Object> merchant = (Map<String, Object>) result.get(0).get("merchant");
    assertEquals("四川", merchant.get("administrative"));
    assertEquals("四川西昌", merchant.get("address"));
    assertEquals("avatar.jpg", merchant.get("index_image"));
    assertEquals("车辆", result.get(0).get("name"));
    assertEquals("car.jpg", result.get(0).get("image"));
  }

  @Test
  void legacyEmptyBusinessObjectsRemainJsonArrays() {
    PublicCatalogMapper catalog = mock(PublicCatalogMapper.class);
    when(catalog.selectMapMerchants(30D, 120D)).thenReturn(Collections.emptyList());
    @SuppressWarnings("unchecked")
    Map<String, Object> mapResult =
        (Map<String, Object>)
            new IndexController(catalog)
                .mapMerchantList(row("lng", 120, "lat", 30, "merchant_id", 1))
                .getData();
    assertEquals(Collections.emptyList(), mapResult.get("data"));

    UserProfileMapper profiles = mock(UserProfileMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    when(tokens.userId("token")).thenReturn(7L);
    when(profiles.lockApplicationUser("merchant", 7L))
        .thenReturn(row("status", 1, "partner_id", 0L));
    when(profiles.lockApplicationUser("shop", 7L))
        .thenReturn(row("status", 1, "partner_id", 0L));
    UserController users =
        new UserController(
            profiles, tokens, mock(WechatLoginService.class), mock(MiniProgramCodeService.class));
    @SuppressWarnings("unchecked")
    Map<String, Object> merchant =
        (Map<String, Object>) users.merchantApply("token", row("type", 1)).getData();
    @SuppressWarnings("unchecked")
    Map<String, Object> shop =
        (Map<String, Object>) users.shopApply("token", row("type", 1)).getData();
    assertEquals(Collections.emptyList(), merchant.get("merchant"));
    assertEquals(Collections.emptyList(), shop.get("shop"));
  }

  @Test
  void userTokenReadsLegacyRolePrefixAndNewLoginPersistsIt() {
    BusinessUserTokenMapper tokenMapper = mock(BusinessUserTokenMapper.class);
    BusinessUserService users = mock(BusinessUserService.class);
    BusinessUserToken stored = new BusinessUserToken();
    stored.setToken("token");
    stored.setUserId(11_111_111_117L);
    stored.setExpiretime(System.currentTimeMillis() / 1000 + 60);
    when(tokenMapper.selectOne(any())).thenReturn(stored);
    BusinessUser user = new BusinessUser();
    user.setId(7L);
    user.setStatus("normal");
    when(users.getById(7L)).thenReturn(user);
    UserTokenService service = new UserTokenService(tokenMapper, users);

    assertEquals(7L, service.userId("token"));
    String issued = service.issueLogin(7L);
    assertEquals(36, issued.length());
    ArgumentCaptor<BusinessUserToken> inserted = ArgumentCaptor.forClass(BusinessUserToken.class);
    verify(tokenMapper).insert(inserted.capture());
    assertEquals(11_111_111_117L, inserted.getValue().getUserId());
  }

  @Test
  void expiredVerificationCodeUsesLegacyTwoMinuteWindowAndIsRemoved() {
    VerificationCodeMapper mapper = mock(VerificationCodeMapper.class);
    Map<String, Object> code = row("id", 3L, "code", "1234", "times", 0,
        "createtime", System.currentTimeMillis() / 1000 - 121);
    when(mapper.latestSms("138" + "0000" + "0000", "register")).thenReturn(code);
    VerificationCodeService service =
        new VerificationCodeService(
            mapper, mock(JavaMailSender.class), new RestTemplateBuilder(), "", "", "", "");

    assertFalse(service.checkSmsCode("138" + "0000" + "0000", "register", "1234"));
    verify(mapper).deleteSms("138" + "0000" + "0000", "register");
    verify(mapper, never()).incrementSms(anyLong());
  }

  @Test
  void addressDeleteDistinguishesMissingIdMissingRowAndDeleteFailure() {
    UserProfileMapper mapper = mock(UserProfileMapper.class);
    UserTokenService tokens = mock(UserTokenService.class);
    when(tokens.userId("token")).thenReturn(7L);
    UserController controller =
        new UserController(
            mapper, tokens, mock(WechatLoginService.class), mock(MiniProgramCodeService.class));

    assertEquals("Address id not", controller.addressDel("token", Collections.emptyMap()).getMsg());
    when(mapper.addressExists(3L, 7L)).thenReturn(0);
    BusinessResponse<?> missing = controller.addressDel("token", row("address_id", 3));
    assertEquals("地址不存在", missing.getMsg());
    assertEquals("", missing.getData());
    when(mapper.addressExists(3L, 7L)).thenReturn(1);
    when(mapper.deleteAddress(3L, 7L)).thenReturn(0);
    assertEquals("操作失败!", controller.addressDel("token", row("address_id", 3)).getMsg());
  }

  private static List<Map<String, Object>> mutable(Map<String, Object> value) {
    List<Map<String, Object>> rows = new ArrayList<>();
    rows.add(value);
    return rows;
  }

  private static Map<String, Object> row(Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (int i = 0; i < values.length; i += 2) row.put(values[i].toString(), values[i + 1]);
    return row;
  }
}
