package com.feimao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class CommonInitLocationIntegrationTest {
  private static final int FALLBACK_CITY_ID = 1900000001;
  private static final int NEARBY_CITY_ID = 1900000002;
  private static final int NEARBY_DISTRICT_ID = 1900000003;

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void prepareAreas() {
    saveArea(FALLBACK_CITY_ID, 1, "默认城市", 2, "104.000000", "30.000000");
    saveArea(3, FALLBACK_CITY_ID, "默认区县", 3, "104.100000", "30.100000");
    saveArea(NEARBY_CITY_ID, 1, "定位城市", 2, "116.400000", "39.900000");
    saveArea(
        NEARBY_DISTRICT_ID,
        NEARBY_CITY_ID,
        "定位区县",
        3,
        "116.407400",
        "39.904200");
  }

  @Test
  void getReturnsParentCityOfDistrictWithinLegacyRadius() throws Exception {
    mvc.perform(
            get("/api/common/init")
                .param("version", "1.0.0")
                .param("lng", "116.4074")
                .param("lat", "39.9042"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.data.citydata.id").value(NEARBY_CITY_ID))
        .andExpect(jsonPath("$.data.citydata.name").value("定位城市"));
  }

  @Test
  void missingInvalidAndOutOfRangeCoordinatesUseDistrictThreeParentCity() throws Exception {
    mvc.perform(get("/api/common/init").param("version", "1.0.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.citydata.id").value(FALLBACK_CITY_ID));

    mvc.perform(
            post("/api/common/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":\"1.0.0\",\"lng\":\"not-a-number\",\"lat\":\"39\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.citydata.id").value(FALLBACK_CITY_ID));

    mvc.perform(
            post("/api/common/init")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("version=1.0.0&lng=0&lat=0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.citydata.id").value(FALLBACK_CITY_ID));
  }

  private void saveArea(int id, int pid, String name, int level, String lng, String lat) {
    jdbc.update(
        "insert into ymcx_area(id,pid,shortname,name,mergename,level,pinyin,code,zip,`first`,lng,lat) "
            + "values(?,?,?,?,?,?,?,'','','',?,?) on duplicate key update "
            + "pid=values(pid),shortname=values(shortname),name=values(name),"
            + "mergename=values(mergename),level=values(level),pinyin=values(pinyin),"
            + "code=values(code),zip=values(zip),`first`=values(`first`),"
            + "lng=values(lng),lat=values(lat)",
        id,
        pid,
        name,
        name,
        name,
        level,
        "test",
        lng,
        lat);
  }
}
