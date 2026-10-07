(function ($) {
  'use strict';
  api('/api/admin/business/dashboard', 'GET').done(function (response) {
    if (response.code !== 1) return;
    var data = response.data || {};
    $('#legacyDashboard, .card').find('[data-key]').each(function () { $(this).text(data[$(this).data('key')] == null ? 0 : data[$(this).data('key')]); });
    var labels = data.signup_labels || [], counts = data.signup_counts || [], max = Math.max.apply(Math, counts.concat([1]));
    $('#signupTrend').html(labels.map(function (label, index) {
      var count = Number(counts[index] || 0), height = Math.max(4, Math.round(count / max * 130));
      return '<div class="text-center" style="width:13%"><div class="small font-weight-bold">' + count + '</div><div class="bg-primary mx-auto" style="height:' + height + 'px;width:24px;border-radius:4px 4px 0 0"></div><div class="small text-muted mt-1">' + String(label).slice(5) + '</div></div>';
    }).join(''));
  });
})(jQuery);
