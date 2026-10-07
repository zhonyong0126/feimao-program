package com.feimao.controller.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feimao.common.BusinessResponse;
import com.feimao.entity.BusinessAttachment;
import com.feimao.mapper.PublicConfigMapper;
import com.feimao.service.AdminSessionService;
import com.feimao.service.BusinessAttachmentService;
import com.feimao.service.RoleSessionService;
import com.feimao.service.UserTokenService;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class CommonControllerUploadContractTest {
  @TempDir Path uploadRoot;
  private BusinessAttachmentService attachments;
  private CommonController controller;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    attachments = mock(BusinessAttachmentService.class);
    controller =
        new CommonController(
            uploadRoot.toString(),
            mock(PublicConfigMapper.class),
            new ObjectMapper(),
            mock(UserTokenService.class),
            mock(AdminSessionService.class),
            mock(RoleSessionService.class),
            attachments);
    request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
  }

  @Test
  void chunkAndMissingFileErrorsMatchLegacyChineseContract() {
    BusinessResponse<?> chunk =
        controller.upload(null, null, null, "", "legacy-chunk", request);
    assertEquals(0, chunk.getCode());
    assertEquals("未开启分片上传功能", chunk.getMsg());
    assertNull(chunk.getData());

    BusinessResponse<?> missing = controller.upload(null, null, null, "", null, request);
    assertEquals(0, missing.getCode());
    assertEquals("未上传文件或超出服务器上传限制", missing.getMsg());
    assertNull(missing.getData());
  }

  @Test
  void formatIsCheckedBeforeSizeAndUsesLegacyMessage() throws Exception {
    MultipartFile executable = mock(MultipartFile.class);
    when(executable.isEmpty()).thenReturn(false);
    when(executable.getOriginalFilename()).thenReturn("attack.php");
    when(executable.getContentType()).thenReturn("application/octet-stream");
    when(executable.getSize()).thenReturn(11L * 1024 * 1024);

    BusinessResponse<?> response =
        controller.upload(executable, null, null, "", null, request);

    assertEquals("上传文件格式受限制", response.getMsg());
    verify(executable, never()).getBytes();
  }

  @Test
  void oversizedAndInvalidImageErrorsMatchLegacyFormatting() throws Exception {
    MultipartFile oversized = mock(MultipartFile.class);
    when(oversized.isEmpty()).thenReturn(false);
    when(oversized.getOriginalFilename()).thenReturn("large.zip");
    when(oversized.getContentType()).thenReturn("application/zip");
    when(oversized.getSize()).thenReturn(11L * 1024 * 1024);
    assertEquals(
        "当前上传(11M)，最大允许上传文件大小:10M",
        controller.upload(oversized, null, null, "", null, request).getMsg());

    MockMultipartFile invalidImage =
        new MockMultipartFile(
            "file", "fake.jpg", "image/jpeg", "not-an-image".getBytes("UTF-8"));
    assertEquals(
        "上传文件不是有效的图片文件",
        controller.upload(invalidImage, null, null, "", null, request).getMsg());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void successKeepsLegacyMessageAndUrlPayloadOnly() {
    LambdaQueryChainWrapper<BusinessAttachment> query = mock(LambdaQueryChainWrapper.class);
    when(attachments.lambdaQuery()).thenReturn(query);
    when(query.eq(any(SFunction.class), any())).thenReturn(query);
    when(query.count()).thenReturn(0L);
    when(attachments.save(any(BusinessAttachment.class))).thenReturn(true);
    byte[] png =
        Base64.getDecoder()
            .decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    MockMultipartFile file = new MockMultipartFile("file", "pixel.png", "image/png", png);

    BusinessResponse<?> response = controller.upload(file, null, null, "", null, request);

    assertEquals(1, response.getCode());
    assertEquals("上传成功", response.getMsg());
    assertTrue(response.getData() instanceof Map);
    Map<?, ?> data = (Map<?, ?>) response.getData();
    assertEquals(2, data.size());
    assertTrue(data.get("url").toString().matches("/uploads/\\d{8}/[0-9a-f]{32}\\.png"));
    assertEquals("http://localhost:8080" + data.get("url"), data.get("fullurl"));
  }
}
