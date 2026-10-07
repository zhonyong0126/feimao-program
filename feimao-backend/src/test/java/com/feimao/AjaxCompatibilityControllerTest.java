package com.feimao;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.feimao.controller.common.AjaxCompatibilityController;
import com.feimao.mapper.AjaxMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AjaxCompatibilityControllerTest {
  private AjaxMapper ajax;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    ajax = mock(AjaxMapper.class);
    mvc = MockMvcBuilders.standaloneSetup(new AjaxCompatibilityController(ajax)).build();
  }

  @Test
  void categoryUsesLegacyTypePidStatusOrderAndOnlyReturnsValueName() throws Exception {
    Map<String, Object> source = new LinkedHashMap<>();
    source.put("value", 12L);
    source.put("name", "测试1");
    source.put("status", "normal");
    when(ajax.selectCategories("page", "0")).thenReturn(Collections.singletonList(source));

    mvc.perform(
            get("/admin/ajax/category")
                .param("type", " <b>page</b> ")
                .param("pid", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value(""))
        .andExpect(jsonPath("$.data[0].value").value(12))
        .andExpect(jsonPath("$.data[0].name").value("测试1"))
        .andExpect(jsonPath("$.data[0].status").doesNotExist());
    verify(ajax).selectCategories("page", "0");

    Select query =
        AjaxMapper.class
            .getMethod("selectCategories", String.class, String.class)
            .getAnnotation(Select.class);
    String sql = String.join(" ", query.value()).replaceAll("\\s+", " ").toLowerCase();
    assertTrue(sql.contains("select id as value,name from ymcx_category where status='normal'"));
    assertTrue(sql.contains("and pid=#{pid}"));
    assertTrue(sql.contains("and type=#{type}"));
    assertTrue(sql.contains("order by weigh desc,id desc"));
  }

  @Test
  void categoryTreatsEmptyAndStringZeroLikeLegacyPhp() throws Exception {
    when(ajax.selectCategories(null, null)).thenReturn(Collections.emptyList());
    mvc.perform(get("/admin/ajax/category").param("type", "0").param("pid", " "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isEmpty());
    verify(ajax).selectCategories(null, null);
  }

  @Test
  void iconMatchesTheOldPdfSvgAndCacheHeaders() throws Exception {
    mvc.perform(get("/admin/ajax/icon").param("suffix", "pdf"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.parseMediaType("image/svg+xml")))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public"))
        .andExpect(header().string(HttpHeaders.PRAGMA, "cache"))
        .andExpect(
            header()
                .string(
                    HttpHeaders.EXPIRES,
                    matchesPattern(
                        "^[A-Z][a-z]{2}, [0-9]{2} [A-Z][a-z]{2} [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} GMT$")))
        .andExpect(content().string(oldPdfSvg()));
  }

  @Test
  void iconCoversFrontendAliasesPostAndLegacyDefaultSuffix() throws Exception {
    mvc.perform(get("/ajax/icon").param("suffix", "long-extension"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(">LONG</tspan>")));
    mvc.perform(get("/index/ajax/icon"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(">FILE</tspan>")));
    mvc.perform(
            post("/ajax/icon")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("suffix", "0"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(">FILE</tspan>")));
  }

  private static String oldPdfSvg() {
    return "        <svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 512 512\" style=\"enable-background:new 0 0 512 512;\" xml:space=\"preserve\">\n"
        + "            <path style=\"fill:#E2E5E7;\" d=\"M128,0c-17.6,0-32,14.4-32,32v448c0,17.6,14.4,32,32,32h320c17.6,0,32-14.4,32-32V128L352,0H128z\"/>\n"
        + "            <path style=\"fill:#B0B7BD;\" d=\"M384,128h96L352,0v96C352,113.6,366.4,128,384,128z\"/>\n"
        + "            <polygon style=\"fill:#CAD1D8;\" points=\"480,224 384,128 480,128 \"/>\n"
        + "            <path style=\"fill:rgb(229,160,168);\" d=\"M416,416c0,8.8-7.2,16-16,16H48c-8.8,0-16-7.2-16-16V256c0-8.8,7.2-16,16-16h352c8.8,0,16,7.2,16,16 V416z\"/>\n"
        + "            <path style=\"fill:#CAD1D8;\" d=\"M400,432H96v16h304c8.8,0,16-7.2,16-16v-16C416,424.8,408.8,432,400,432z\"/>\n"
        + "            <g><text><tspan x=\"220\" y=\"380\" font-size=\"124\" font-family=\"Verdana, Helvetica, Arial, sans-serif\" fill=\"white\" text-anchor=\"middle\">PDF</tspan></text></g>\n"
        + "        </svg>";
  }
}
