(function ($) {
  'use strict';

  var page = window.businessFundPage;
  if (!page) return;

  var cash = page.section.indexOf('cash-') === 0;
  var kind = page.section.substring(page.section.indexOf('-') + 1);
  var legacyLedger = !cash && (kind === 'platform' || kind === 'system');
  var endpoint = cash
    ? '/api/admin/business/cash/' + kind
    : '/api/admin/business/finance/' + kind;
  var platformTypes = {
    1: '租车订单支付',
    2: '租车押金退款',
    3: '商家余额提现',
    4: '酒店余额提现',
    5: '取消订单退款'
  };
  var platformUserTypes = { 1: '用户', 2: '门店', 3: '酒店' };
  var systemTypes = {
    1: '正常下单平台获取',
    2: '扫门店码平台获取',
    3: '扫酒店码平台获取'
  };
  var state = {
    current: 1,
    last: 1,
    total: 0,
    rows: [],
    recycle: false,
    selected: {}
  };

  function esc(value) {
    return $('<div>').text(value == null || value === '' ? '-' : String(value)).html();
  }

  function field(value) {
    return $('<div>').text(value == null ? '' : String(value)).html();
  }

  function typeText(row) {
    if (row.type_text) return row.type_text;
    if (kind === 'platform') return platformTypes[Number(row.type)] || '未知类型';
    if (kind === 'system') return systemTypes[Number(row.type)] || '未知类型';
    return ({ 1: '收入', 2: '支出' })[Number(row.type)] || row.type;
  }

  function display(key, value, row) {
    if (cash && key === 'status') {
      return esc(({ 1: '待审核', 2: '已支付', 3: '已拒绝' })[Number(value)] || value);
    }
    if (key === 'type') return esc(typeText(row));
    if (kind === 'platform' && key === 'nickname') {
      var userType = row.user_type_text || platformUserTypes[Number(row.user_type)] || '未知主体';
      return '<span>' + esc(value) + '</span><br><small class="text-muted">' + esc(userType) + '</small>';
    }
    return esc(adminDetailValue(key, value));
  }

  function dateValue(value, endOfDay) {
    if (!value) return '';
    var timestamp = Math.floor(new Date(value).getTime() / 1000);
    if (!Number.isFinite(timestamp)) return '';
    return endOfDay ? timestamp + 59 : timestamp;
  }

  function params() {
    var values = [];
    $('#fundFilters').find('input,select').each(function () {
      var value = $(this).val();
      if (value === '') return;
      if (this.id === 'createdFrom') value = dateValue(value, false);
      if (this.id === 'createdTo') value = dateValue(value, true);
      if (value !== '') values.push(this.id + '=' + encodeURIComponent(value));
    });
    return values.length ? '&' + values.join('&') : '';
  }

  function typeOptions(options) {
    return Object.keys(options).map(function (key) {
      return '<option value="' + key + '">' + esc(options[key]) + '</option>';
    }).join('');
  }

  function filters() {
    if (cash) {
      return '<input id="keyword" class="form-control" placeholder="提现单号/名称/联系人/手机号">' +
        '<select id="status"><option value="">全部状态</option><option value="1">待审核</option><option value="2">已支付</option><option value="3">已拒绝</option></select>';
    }
    if (kind === 'platform') {
      return '<input id="nickname" class="form-control" placeholder="用户/门店/酒店名称">' +
        '<input id="amountMin" type="number" step="0.01" class="form-control" placeholder="最小金额">' +
        '<input id="amountMax" type="number" step="0.01" class="form-control" placeholder="最大金额">' +
        '<select id="type"><option value="">全部流水类型</option>' + typeOptions(platformTypes) + '</select>' +
        '<input id="outTradeNo" class="form-control" placeholder="订单号/提现单号">' +
        '<input id="createdFrom" type="datetime-local" class="form-control" title="开始时间">' +
        '<input id="createdTo" type="datetime-local" class="form-control" title="结束时间">';
    }
    if (kind === 'system') {
      return '<input id="amountMin" type="number" step="0.01" class="form-control" placeholder="最小收益">' +
        '<input id="amountMax" type="number" step="0.01" class="form-control" placeholder="最大收益">' +
        '<select id="type"><option value="">全部收益类型</option>' + typeOptions(systemTypes) + '</select>' +
        '<input id="orderNumber" class="form-control" placeholder="订单号">' +
        '<input id="createdFrom" type="datetime-local" class="form-control" title="开始时间">' +
        '<input id="createdTo" type="datetime-local" class="form-control" title="结束时间">';
    }
    return '<input id="ownerId" type="number" class="form-control" placeholder="所属方编号">' +
      '<input id="type" type="number" class="form-control" placeholder="流水类型">';
  }

  function openCashEditor() {
    var owner = kind === 'merchant' ? 'merchant_id' : 'shop_id';
    var ownerLabel = kind === 'merchant' ? '门店编号' : '酒店编号';
    $('#fundForm').data('id', '').html(
      '<div class="form-group"><label>提现单号</label><input class="form-control" name="out_trade_no"></div>' +
      '<div class="form-group"><label>' + ownerLabel + '</label><input class="form-control" name="' + owner + '" type="number" min="1" required></div>' +
      '<div class="form-group"><label>提现金额</label><input class="form-control" name="amount" type="number" min="0" step="0.01" value="0.00"></div>' +
      '<div class="form-group"><label>状态</label><select class="form-control" name="status"><option value="1">待审核</option><option value="2">已支付</option><option value="3">已拒绝</option></select></div>' +
      '<div class="form-group"><label>备注</label><input class="form-control" name="note"></div>' +
      '<div class="form-group"><label>支付时间</label><input class="form-control" name="pay_time" type="datetime-local"></div>' +
      '<div class="form-group"><label>支付单号</label><input class="form-control" name="pay_number"></div>' +
      '<div class="form-group"><label>实付金额</label><input class="form-control" name="pay_money" type="number" min="0" step="0.01" value="0.00"></div>'
    );
    $('#fundEditorMask .card-header').contents().first()[0].textContent = '新增' + page.title;
    $('#fundEditorMask').prop('hidden', false);
  }

  function openFinanceEditor(row) {
    row = row || {};
    var ownerLabel = kind === 'merchant' ? '门店编号' : '酒店编号';
    var moneyType = kind === 'merchant'
      ? '<div class="form-group"><label>佣金类型</label><input class="form-control" name="money_type" type="number" min="0" value="' + field(row.money_type == null ? 1 : row.money_type) + '"></div>'
      : '';
    $('#fundForm').data('id', row.id || '').html(
      '<div class="form-group"><label>' + ownerLabel + '</label><input class="form-control" name="owner_id" type="number" min="1" required value="' + field(row[kind + '_id'] || '') + '"></div>' +
      '<div class="form-group"><label>变更余额</label><input class="form-control" name="money" type="number" step="0.01" required value="' + field(row.money == null ? '0.00' : row.money) + '"></div>' +
      '<div class="form-group"><label>变更前余额</label><input class="form-control" name="before" type="number" step="0.01" required value="' + field(row.before == null ? '0.00' : row.before) + '"></div>' +
      '<div class="form-group"><label>变更后余额</label><input class="form-control" name="after" type="number" step="0.01" required value="' + field(row.after == null ? '0.00' : row.after) + '"></div>' +
      '<div class="form-group"><label>类型</label><input class="form-control" name="type" type="number" min="0" value="' + field(row.type == null ? 1 : row.type) + '"></div>' + moneyType +
      '<div class="form-group"><label>备注</label><input class="form-control" name="memo" value="' + field(row.memo || '') + '"></div>' +
      '<div class="form-group"><label>是否删除</label><select class="form-control" name="is_delete"><option value="1">否</option><option value="2"' + (Number(row.is_delete) === 2 ? ' selected' : '') + '>是</option></select></div>' +
      '<div class="form-group"><label>订单编号</label><input class="form-control" name="order_id" type="number" min="0" required value="' + field(row.order_id == null ? '' : row.order_id) + '"></div>'
    );
    $('#fundEditorMask .card-header').contents().first()[0].textContent = (row.id ? '修改' : '新增') + page.title;
    $('#fundEditorMask').prop('hidden', false);
  }

  function updateSummary(data) {
    if (state.recycle) {
      $('#amountValue,#moneyValue,#inMoneyValue,#outMoneyValue,#cashMoneyValue,#systemMoneyValue').text('-');
      return;
    }
    if (kind === 'platform') {
      var platform = data.extend || data;
      $('#moneyValue').text(platform.money || 0);
      $('#inMoneyValue').text(platform.in_money || 0);
      $('#outMoneyValue').text(platform.out_money || 0);
      $('#cashMoneyValue').text(platform.cash_money || 0);
    } else if (kind === 'system') {
      var system = data.extend || data;
      $('#systemMoneyValue').text(system.system_money || 0);
    } else {
      $('#amountValue').text(cash ? ((data.extend && data.extend.money) || 0) : (data.amount || 0));
    }
  }

  function load() {
    var url = endpoint + (state.recycle ? '/recycle' : '') + '?page=' + state.current + '&limit=10' + (state.recycle ? '' : params());
    api(url, 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      var data = response.data || {};
      state.rows = data.data || [];
      state.total = Number(data.total || 0);
      state.current = Number(data.current_page || 1);
      state.last = Number(data.last_page || 1);
      state.selected = {};
      updateSummary(data);
      render();
    });
  }

  function columns() {
    if (state.recycle && legacyLedger) return [['id', '编号'], ['deletetime', '删除时间']];
    if (cash) {
      return [['out_trade_no', '提现单号'], ['owner_name', '申请方'], ['owner_mobile', '手机号'], ['amount', '金额'], ['status', '状态'], ['createtime', '申请时间']];
    }
    if (kind === 'platform') {
      return [['nickname', '用户'], ['amount', '金额'], ['type', '流水类型'], ['out_trade_no', '订单/提现单号'], ['createtime', '创建时间']];
    }
    if (kind === 'system') {
      return [['money', '收益金额'], ['type', '收益类型'], ['order_number', '订单号'], ['createtime', '创建时间']];
    }
    return [['owner_name', '所属方'], ['order_number', '订单号'], ['money', '金额'], ['type', '类型'], ['memo', '说明'], [state.recycle ? 'deletetime' : 'createtime', state.recycle ? '删除时间' : '时间']];
  }

  function businessAction(row, index) {
    if (kind === 'system' || (kind === 'platform' && [1, 2, 5].indexOf(Number(row.type)) >= 0)) {
      return '<button class="btn-link business-detail" data-index="' + index + '">订单详情</button>';
    }
    if (kind === 'platform' && Number(row.type) === 3) {
      return '<button class="btn-link business-detail" data-index="' + index + '">门店提现详情</button>';
    }
    if (kind === 'platform' && Number(row.type) === 4) {
      return '<button class="btn-link business-detail" data-index="' + index + '">酒店提现详情</button>';
    }
    return '';
  }

  function rowActions(row, index) {
    if (state.recycle) {
      return '<button class="btn-link restore" data-id="' + row.id + '">恢复</button> ' +
        '<button class="btn-link danger destroy" data-id="' + row.id + '">彻底删除</button>';
    }
    if (legacyLedger) {
      var detail = businessAction(row, index);
      return detail + (detail ? ' ' : '') + '<button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    }
    var actions = '<button class="btn-link detail" data-index="' + index + '">详情</button>';
    if (cash) {
      if (Number(row.status) === 1) {
        actions += ' <button class="btn-link approve" data-id="' + row.id + '">通过</button>' +
          ' <button class="btn-link danger reject" data-id="' + row.id + '">拒绝</button>';
      }
      return actions + ' <button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    }
    if (kind === 'merchant' || kind === 'shop') {
      return actions + ' <button class="btn-link edit" data-index="' + index + '">修改</button>' +
        ' <button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    }
    return actions;
  }

  function hasCheckbox() {
    return cash || legacyLedger;
  }

  function updateBulkButtons() {
    var disabled = Object.keys(state.selected).length === 0;
    $('#bulkDeleteCashButton,#bulkDeleteFinanceButton,#bulkRestoreFundButton,#bulkDestroyFundButton').prop('disabled', disabled);
  }

  function render() {
    var cols = columns();
    var checkboxHeader = hasCheckbox()
      ? '<th><input type="checkbox" id="selectAllFund" aria-label="全选"></th>'
      : '';
    var idHeader = legacyLedger ? '' : '<th>编号</th>';
    $('#fundHead').html(checkboxHeader + idHeader + cols.map(function (column) {
      return '<th>' + column[1] + '</th>';
    }).join('') + '<th>操作</th>');

    if (!state.rows.length) {
      $('#fundBody').html('<tr><td colspan="' + (cols.length + 3) + '" class="empty-state">暂无记录</td></tr>');
    } else {
      $('#fundBody').html(state.rows.map(function (row, index) {
        var checkbox = hasCheckbox()
          ? '<td><input type="checkbox" class="fund-select" value="' + row.id + '" aria-label="选择记录"></td>'
          : '';
        var idCell = legacyLedger ? '' : '<td>' + esc(row.id) + '</td>';
        return '<tr>' + checkbox + idCell + cols.map(function (column) {
          return '<td>' + display(column[0], row[column[0]], row) + '</td>';
        }).join('') + '<td>' + rowActions(row, index) + '</td></tr>';
      }).join(''));
    }
    updateBulkButtons();
    $('#totalText').text('共 ' + state.total + ' 条记录');
    $('#totalValue').text(state.total);
    $('#pageText').text(state.current + ' / ' + state.last);
    $('#prevButton').prop('disabled', state.current <= 1);
    $('#nextButton').prop('disabled', state.current >= state.last);
  }

  function show(row) {
    $('#detailBody').html(Object.keys(row).map(function (key) {
      return '<tr><th style="width:150px">' + esc(adminDetailLabel(key)) + '</th><td>' + esc(adminDetailValue(key, row[key])) + '</td></tr>';
    }).join(''));
    $('#detailMask').prop('hidden', false);
  }

  function showBusiness(row) {
    var url;
    if (kind === 'system' || [1, 2, 5].indexOf(Number(row.type)) >= 0) {
      url = '/api/admin/business/orders/' + row.order_id;
    } else if (Number(row.type) === 3) {
      url = '/api/admin/business/cash/merchant/' + row.order_id;
    } else if (Number(row.type) === 4) {
      url = '/api/admin/business/cash/shop/' + row.order_id;
    }
    if (!url || !row.order_id) return alert('关联业务记录不存在');
    api(url, 'GET').done(function (response) {
      if (response.code === 1) show(response.data || {});
      else alert(response.msg);
    });
  }

  function batch(path, method, message) {
    var ids = Object.keys(state.selected).map(Number);
    if (!ids.length || !confirm(message.replace('{count}', ids.length))) return;
    api(endpoint + path, method, { ids: ids }).done(function (response) {
      if (response.code === 1) load();
      else alert(response.msg);
    });
  }

  $('#fundFilters').html(filters() +
    '<button class="btn btn-primary" id="searchButton">查询</button>' +
    '<button class="btn btn-secondary" id="resetButton">重置</button>')
    .on('click', '#searchButton', function () { state.current = 1; load(); })
    .on('click', '#resetButton', function () {
      $('#fundFilters input,#fundFilters select').val('');
      state.current = 1;
      load();
    });

  $('#prevButton').on('click', function () {
    if (state.current > 1) { state.current--; load(); }
  });
  $('#nextButton').on('click', function () {
    if (state.current < state.last) { state.current++; load(); }
  });
  $('.close-detail').on('click', function () { $('#detailMask').prop('hidden', true); });
  $('.close-fund-editor').on('click', function () { $('#fundEditorMask').prop('hidden', true); });

  $('#recycleFundButton').on('click', function () {
    state.recycle = !state.recycle;
    state.current = 1;
    state.selected = {};
    $(this).html(state.recycle
      ? '<i class="fas fa-arrow-left"></i> 返回列表'
      : '<i class="fas fa-recycle"></i> 回收站');
    $('#addFundButton,#fundFilters,#fundSummary').prop('hidden', state.recycle);
    if (legacyLedger) {
      $('#bulkDeleteFinanceButton').prop('hidden', state.recycle);
      $('#bulkRestoreFundButton,#bulkDestroyFundButton').prop('hidden', !state.recycle);
    }
    load();
  });

  $('#addFundButton').on('click', function () {
    if (cash) openCashEditor();
    else openFinanceEditor();
  });

  $('#saveFundButton').on('click', function () {
    var form = document.getElementById('fundForm');
    if (!form.reportValidity()) return;
    var body = {};
    $('#fundForm').serializeArray().forEach(function (item) { body[item.name] = item.value; });
    if (body.pay_time) body.pay_time = Math.floor(new Date(body.pay_time).getTime() / 1000);
    var id = $('#fundForm').data('id');
    api(endpoint + (id ? '/' + id : ''), id ? 'PUT' : 'POST', body).done(function (response) {
      if (response.code === 1) {
        $('#fundEditorMask').prop('hidden', true);
        load();
      } else alert(response.msg);
    });
  });

  $('#fundHead').on('change', '#selectAllFund', function () {
    var checked = this.checked;
    $('.fund-select').each(function () {
      this.checked = checked;
      if (checked) state.selected[this.value] = true;
      else delete state.selected[this.value];
    });
    updateBulkButtons();
  });

  $('#fundBody').on('change', '.fund-select', function () {
    if (this.checked) state.selected[this.value] = true;
    else delete state.selected[this.value];
    updateBulkButtons();
  });

  $('#bulkDeleteCashButton').on('click', function () {
    batch('', 'DELETE', '确定删除选中的 {count} 条提现记录吗？');
  });
  $('#bulkDeleteFinanceButton').on('click', function () {
    batch('', 'DELETE', '确定删除选中的 {count} 条资金记录吗？');
  });
  $('#bulkRestoreFundButton').on('click', function () {
    batch('/restore', 'POST', '确定恢复选中的 {count} 条资金记录吗？');
  });
  $('#bulkDestroyFundButton').on('click', function () {
    batch('/destroy', 'DELETE', '彻底删除后无法恢复，确定删除选中的 {count} 条资金记录吗？');
  });

  $('#fundBody')
    .on('click', '.detail', function () {
      var row = state.rows[Number($(this).data('index'))];
      if (cash) {
        api(endpoint + '/' + row.id, 'GET').done(function (response) {
          if (response.code === 1) show(response.data || {});
          else alert(response.msg);
        });
      } else show(row);
    })
    .on('click', '.business-detail', function () {
      showBusiness(state.rows[Number($(this).data('index'))]);
    })
    .on('click', '.edit', function () {
      openFinanceEditor(state.rows[Number($(this).data('index'))]);
    })
    .on('click', '.restore', function () {
      api(endpoint + '/restore/' + $(this).data('id'), 'POST', {}).done(function (response) {
        if (response.code === 1) load();
        else alert(response.msg);
      });
    })
    .on('click', '.destroy', function () {
      if (!confirm('彻底删除后无法恢复，确定继续吗？')) return;
      api(endpoint + '/destroy/' + $(this).data('id'), 'DELETE').done(function (response) {
        if (response.code === 1) load();
        else alert(response.msg);
      });
    })
    .on('click', '.remove', function () {
      if (!confirm(cash ? '确定删除这条提现记录吗？' : '确定删除这条资金记录吗？')) return;
      api(endpoint + '/' + $(this).data('id'), 'DELETE').done(function (response) {
        if (response.code === 1) load();
        else alert(response.msg);
      });
    })
    .on('click', '.reject', function () {
      var note = prompt('请输入拒绝原因');
      if (note === null) return;
      if (!confirm('拒绝后提现金额将退回余额，确定继续吗？')) return;
      api(endpoint + '/reject', 'POST', { id: Number($(this).data('id')), note: note }).done(function (response) {
        if (response.code === 1) load();
        else alert(response.msg);
      });
    })
    .on('click', '.approve', function () {
      var note = prompt('请输入审核备注', '');
      if (note === null) return;
      if (!confirm('通过后将发起真实微信转账，确定继续吗？')) return;
      api(endpoint + '/approve', 'POST', { id: Number($(this).data('id')), note: note }).done(function (response) {
        if (response.code === 1) {
          alert('转账已提交，批次号：' + ((response.data && response.data.batch_id) || ''));
          load();
        } else alert(response.msg);
      });
    });

  load();
})(jQuery);
