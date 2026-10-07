package com.feimao.controller.user;

import com.feimao.common.BusinessResponse;
import com.feimao.mapper.SignMapper;
import com.feimao.service.UserTokenService;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sign")
@CrossOrigin
public class SignController {
  private final SignMapper signs;
  private final UserTokenService tokens;

  public SignController(SignMapper signs, UserTokenService tokens) {
    this.signs = signs;
    this.tokens = tokens;
  }

  @PostMapping("/index")
  public BusinessResponse<?> index(@RequestHeader(value = "token", required = false) String token) {
    Long uid = tokens.userId(token);
    if (uid == null) return BusinessResponse.error("请先登录");
    Map<String, Object> user =
        signs.selectUserSummary(uid);
    LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    List<Map<String, Object>> dates = new ArrayList<>();
    Set<String> signed = new HashSet<>();
    for (Map<String, Object> r :
        signs.selectSignedDays(uid, monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()))
      signed.add(r.get("day").toString());
    for (int i = 0; i < 7; i++) {
      String day = monday.plusDays(i).toString();
      Map<String, Object> d = new LinkedHashMap<>();
      d.put("date", day);
      d.put("state", signed.contains(day) ? 2 : 1);
      dates.add(d);
    }
    user.put("count_sign", signs.signCount(uid));
    user.put("date_list", dates);
    return BusinessResponse.success("操作成功!", user);
  }

  @PostMapping("/couponList")
  public BusinessResponse<?> coupons(@RequestBody(required = false) Map<String, Object> body) {
    long page = longValue(body, "page", 1), size = longValue(body, "list_rows", 10);
    if (page <= 0) page = 1;
    if (size <= 0) size = 10;
    List<Map<String, Object>> rows =
        signs.selectCoupons(now(), size, (page - 1) * size);
    return BusinessResponse.success("操作成功!", page(rows, signs.couponCount(now()), page, size));
  }

  @PostMapping("/userCouponLogList")
  public BusinessResponse<?> scoreLog(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return BusinessResponse.error("请先登录");
    long page = longValue(body, "page", 1), size = longValue(body, "list_rows", 10);
    if (page <= 0) page = 1;
    if (size <= 0) size = 10;
    List<Map<String, Object>> rows =
        signs.selectScoreLogs(uid, size, (page - 1) * size);
    for (Map<String, Object> r : rows)
      r.put(
          "time",
          Instant.ofEpochSecond(((Number) r.get("createtime")).longValue())
              .atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    return BusinessResponse.success("操作成功!", page(rows, signs.scoreLogCount(uid), page, size));
  }

  @PostMapping("/redeemCoupon")
  @Transactional
  public BusinessResponse<?> redeem(
      @RequestHeader(value = "token", required = false) String token,
      @RequestBody(required = false) Map<String, Object> body) {
    Long uid = tokens.userId(token);
    if (uid == null) return BusinessResponse.error("请先登录");
    long couponId = longValue(body, "coupon_id", 0);
    if (couponId <= 0) return BusinessResponse.error("请选择兑换的优惠券");
    Map<String, Object> coupon = signs.selectCoupon(couponId);
    if (coupon == null || coupon.isEmpty()) return BusinessResponse.error("请选择兑换的优惠券");
    Integer lockedScore = signs.lockUserScore(uid);
    int before = lockedScore == null ? 0 : lockedScore,
        cost = ((Number) coupon.get("score")).intValue();
    if (before < cost) return BusinessResponse.error("积分不足");
    int after = before - cost;
    long current = now();
    signs.updateUserScore(uid, after, current);
    signs.insertUserCoupon(uid, new java.math.BigDecimal(coupon.get("price").toString()),
        new java.math.BigDecimal(coupon.get("reduceprice").toString()), coupon.get("endtime"), current);
    signs.insertScoreLog(
        uid,
        -cost,
        after,
        after,
        "兑换满" + coupon.get("price") + "减" + coupon.get("reduceprice") + "优惠券",
        current);
    return BusinessResponse.success("操作成功!", 1);
  }

  @PostMapping("/signIn")
  @Transactional
  public BusinessResponse<?> sign(@RequestHeader(value = "token", required = false) String token) {
    Long uid = tokens.userId(token);
    if (uid == null) return BusinessResponse.error("请先登录");
    Integer lockedScore = signs.lockUserScore(uid);
    if (signs.todaySignCount(uid) > 0) return BusinessResponse.error("今日已签到");
    int reward = 1;
    String configured = signs.signReward();
    if (configured != null)
      try {
        reward = Integer.parseInt(configured);
      } catch (Exception ignored) {
      }
    int before = lockedScore == null ? 0 : lockedScore, after = before + reward;
    long current = now();
    signs.updateUserScore(uid, after, current);
    signs.insertScoreLog(uid, reward, after, after, "签到奖励" + reward + "积分", current);
    signs.insertSign(uid, reward);
    return BusinessResponse.success("操作成功!", reward);
  }

  private static long now() {
    return System.currentTimeMillis() / 1000;
  }

  private static long longValue(Map<String, Object> b, String k, long d) {
    try {
      return b == null || b.get(k) == null ? d : Long.parseLong(b.get(k).toString());
    } catch (Exception e) {
      return d;
    }
  }

  private static Map<String, Object> page(List<Map<String, Object>> r, int t, long c, long s) {
    Map<String, Object> p = new LinkedHashMap<>();
    p.put("total", t);
    p.put("per_page", s);
    p.put("current_page", c);
    p.put("last_page", t == 0 ? 0 : (t + s - 1) / s);
    p.put("data", r);
    return p;
  }
}
