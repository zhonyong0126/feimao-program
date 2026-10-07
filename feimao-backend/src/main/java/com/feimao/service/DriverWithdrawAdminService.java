package com.feimao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverWithdrawAdminService {
  private final JdbcTemplate jdbc;
  private final WechatTransferService transfers;

  public DriverWithdrawAdminService(JdbcTemplate jdbc, WechatTransferService transfers) {
    this.jdbc = jdbc;
    this.transfers = transfers;
  }

  public Map<String, Object> list(int page, int limit, Integer status) {
    int safePage = Math.max(1, page);
    int safeLimit = Math.max(1, Math.min(100, limit));
    String condition = status == null ? "" : " and w.status=?";
    Object[] countArgs = status == null ? new Object[0] : new Object[] {status};
    Integer total =
        jdbc.queryForObject(
            "select count(*) from fm_withdraw w where 1=1" + condition,
            Integer.class,
            countArgs);
    List<Map<String, Object>> records;
    String sql =
        "select w.id,w.driver_id,w.amount,w.status,w.reject_reason,w.transfer_batch_id,"
            + "w.review_note,w.review_time,w.create_time,d.name driver_name,d.mobile,d.plate_number "
            + "from fm_withdraw w left join fm_driver d on d.id=w.driver_id where 1=1"
            + condition
            + " order by w.id desc limit ? offset ?";
    if (status == null) {
      records = jdbc.queryForList(sql, safeLimit, (safePage - 1) * safeLimit);
    } else {
      records = jdbc.queryForList(sql, status, safeLimit, (safePage - 1) * safeLimit);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("records", records);
    result.put("total", total == null ? 0 : total);
    result.put("page", safePage);
    result.put("limit", safeLimit);
    return result;
  }

  @Transactional
  public String approve(long id, String note) {
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "select w.driver_id,w.amount,d.openid from fm_withdraw w "
                + "join fm_driver d on d.id=w.driver_id and d.deleted=0 "
                + "where w.id=? and w.status=0 for update",
            id);
    if (rows.isEmpty()) throw new IllegalStateException("提现记录不存在或已审核");
    Map<String, Object> row = rows.get(0);
    BigDecimal amount = (BigDecimal) row.get("amount");
    Object openidValue = row.get("openid");
    if (openidValue == null || String.valueOf(openidValue).trim().isEmpty()) {
      throw new IllegalStateException("司机未绑定微信，不能转账");
    }
    String batchId =
        transfers.transfer(
            String.valueOf(openidValue),
            "FMDW" + id,
            amount.movePointRight(2).longValueExact(),
            "司机提现");
    int changed =
        jdbc.update(
            "update fm_withdraw set status=1,transfer_batch_id=?,review_note=?,review_time=?,update_time=? "
                + "where id=? and status=0",
            batchId,
            clean(note),
            LocalDateTime.now(),
            LocalDateTime.now(),
            id);
    if (changed != 1) throw new IllegalStateException("提现状态已变化");
    return batchId;
  }

  @Transactional
  public boolean reject(long id, String reason) {
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "select driver_id,amount from fm_withdraw where id=? and status=0 for update", id);
    if (rows.isEmpty()) return false;
    Map<String, Object> row = rows.get(0);
    long driverId = ((Number) row.get("driver_id")).longValue();
    BigDecimal amount = (BigDecimal) row.get("amount");
    LocalDateTime now = LocalDateTime.now();
    int changed =
        jdbc.update(
            "update fm_withdraw set status=2,reject_reason=?,review_time=?,update_time=? "
                + "where id=? and status=0",
            clean(reason),
            now,
            now,
            id);
    if (changed != 1) return false;
    if (jdbc.update(
            "update fm_driver set balance=balance+?,update_time=? where id=?", amount, now, driverId)
        != 1) {
      throw new IllegalStateException("司机记录不存在，退款失败");
    }
    jdbc.update(
        "insert into fm_driver_balance_log(driver_id,amount,type,remark,create_time) "
            + "values(?,?,3,'提现拒绝退款',?)",
        driverId,
        amount,
        now);
    return true;
  }

  private String clean(String value) {
    if (value == null) return null;
    String cleaned = value.trim();
    return cleaned.length() <= 255 ? cleaned : cleaned.substring(0, 255);
  }
}
