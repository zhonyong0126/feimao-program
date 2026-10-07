package com.feimao;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LegacyFormRequestContractTest {
  @Autowired MockMvc mvc;

  @Test
  void oldFormPostIsReadByJsonBackedControllers() throws Exception {
    mvc.perform(
            post("/api/sms/check")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("mobile=123&event=register&captcha=0000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("手机号不正确"))
        .andExpect(jsonPath("$.time").isNumber())
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void jsonPostContractRemainsAvailable() throws Exception {
    mvc.perform(
            post("/api/sms/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"123\",\"event\":\"register\",\"captcha\":\"0000\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("手机号不正确"));
  }

  @Test
  void verificationSuccessTextStaysInMsgInsteadOfData() throws Exception {
    com.feimao.common.BusinessResponse<Void> response =
        com.feimao.common.BusinessResponse.successMessage("发送成功");
    org.junit.jupiter.api.Assertions.assertEquals(1, response.getCode());
    org.junit.jupiter.api.Assertions.assertEquals("发送成功", response.getMsg());
    org.junit.jupiter.api.Assertions.assertNull(response.getData());
  }

  @Test
  void commonUploadKeepsOriginalNoLoginBoundary() throws Exception {
    mvc.perform(multipart("/api/common/upload"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("未上传文件或超出服务器上传限制"));
  }

  @Test
  void commonInitKeepsLegacyParametersAndNullablePayloadShape() throws Exception {
    mvc.perform(
            post("/api/common/init")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("version=1.0.0&lng=113.5&lat=22.5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.data.citydata").value(nullValue()))
        .andExpect(jsonPath("$.data.versiondata").value(nullValue()))
        .andExpect(jsonPath("$.data.coverdata").value(nullValue()))
        .andExpect(jsonPath("$.data.uploaddata.cdnurl").value("http://localhost"))
        .andExpect(jsonPath("$.data.uploaddata.uploadurl").value("http://localhost/api/common/upload"))
        .andExpect(jsonPath("$.data.uploaddata.maxsize").value("10mb"))
        .andExpect(
            jsonPath("$.data.uploaddata.mimetype")
                .value("jpg,png,bmp,jpeg,gif,webp,zip,rar,wav,mp4,mp3,webm"))
        .andExpect(jsonPath("$.data.uploaddata.chunking").value(false))
        .andExpect(jsonPath("$.data.uploaddata.chunksize").value(2097152))
        .andExpect(
            jsonPath("$.data.uploaddata.savekey")
                .value("/uploads/{year}{mon}{day}/{filemd5}{.suffix}"))
        .andExpect(jsonPath("$.data.uploaddata.multipart").isArray())
        .andExpect(jsonPath("$.data.uploaddata.multiple").value(false))
        .andExpect(jsonPath("$.data.uploaddata.storage").value("local"));
  }

  @Test
  void commonInitAcceptsLegacyGetRequests() throws Exception {
    mvc.perform(get("/api/common/init").param("version", "1.0.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.data.uploaddata.uploadurl").value("http://localhost/api/common/upload"));
  }

  @Test
  void commonInitRejectsLegacyFalsyVersionWithOriginalMessage() throws Exception {
    mvc.perform(get("/api/common/init"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("未知参数"));

    mvc.perform(
            post("/api/common/init")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("version=0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("未知参数"));
  }

  @Test
  void disabledLegacyChunkUploadReturnsBusinessErrorInsteadOfHttpBadRequest() throws Exception {
    mvc.perform(
            post("/api/common/upload")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("chunkid=legacy-chunk"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.msg").value("未开启分片上传功能"));
  }
}
