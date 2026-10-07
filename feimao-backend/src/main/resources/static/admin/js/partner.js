(function ($) {
  'use strict';
  var page = window.businessPartnerPage;
  if (!page) return;
  var endpoint = '/api/admin/business/partners/' + page.role;
  var state = { current: 1, last: 1, total: 0, rows: [], users: [], editingId: null, recycle: false };
  var common = [
    { key: 'user_id', label: '绑定用户', type: 'user', required: true },
    { key: 'username', label: '登录账号', required: true }, { key: 'password', label: '登录密码', type: 'password' },
    { key: 'name', label: '名称', required: true }, { key: 'nickname', label: '联系人' },
    { key: 'mobile', label: '手机号' }, { key: 'address', label: '地址' }, { key: 'avatar', label: '头像地址', type: 'upload' },
    { key: 'image', label: '环境图片（多个用逗号分隔）', type: 'uploads' }, { key: 'intro', label: '简介', type: 'textarea' },
    { key: 'status', label: '状态', type: 'status' }
  ];
  var merchant = [
    { key: 'business', label: '营业时间' }, { key: 'car_num', label: '可租车辆数', type: 'number', required: true },
    { key: 'min_price', label: '最低起租价', type: 'number', required: true },
    { key: 'pick_type', label: '取车方式', type: 'pick-type' }, { key: 'point', label: '经纬度（经度,纬度）', type: 'point', required: true },
    { key: 'commission_normal_rate', label: '普通订单佣金(%)', type: 'number' },
    { key: 'commission_merchant_rate', label: '商户扫码佣金(%)', type: 'number' },
    { key: 'commission_shop_rate', label: '门店扫码佣金(%)', type: 'number' }
  ].concat([1, 2, 3, 4].map(function (i) { return { key: 'service_distance' + i, label: '服务距离' + i, type: 'number' }; }))
    .concat([1, 2, 3, 4, 5].map(function (i) { return { key: 'service_money' + i, label: '服务费用' + i, type: 'number' }; }));
  var fields = common.concat(page.role === 'merchant' ? merchant : [{ key: 'commission_shop_rate', label: '门店佣金(%)', type: 'number' }]);
  var columns = page.role === 'merchant' ? [
    ['name','名称'],['username','登录账号'],['avatar','头像'],['nickname','联系人'],['mobile','手机号'],['business','营业时间'],['car_num','车辆数量'],['min_price','最低价格'],['money','余额'],['commission_money','佣金'],['commission_rate','分佣比例'],['qrimage','小程序码'],['address','地址'],['image','环境图片'],['user_nickname','用户'],['examine_status','审核状态'],['status','状态'],['createtime','创建时间']
  ] : [
    ['name','名称'],['username','登录账号'],['avatar','头像'],['nickname','联系人'],['mobile','手机号'],['money','余额'],['commission_money','佣金'],['commission_shop_rate','门店佣金比例'],['qrimage','小程序码'],['address','地址'],['image','环境图片'],['user_nickname','用户'],['examine_status','审核状态'],['status','状态'],['createtime','创建时间']
  ];
  function escapeHtml(value) { return $('<div>').text(value == null ? '' : String(value)).html(); }
  function text(value) { return value == null || value === '' ? '-' : escapeHtml(value); }
  function cell(key, row) {
    if (key === 'avatar' || key === 'qrimage' || key === 'image') { var src=String(row[key]||'').split(',')[0]; return src?'<img src="'+text(src)+'" alt="图片" style="width:48px;height:48px;object-fit:cover;border-radius:4px">':'-'; }
    if (key === 'address') return text((page.role === 'merchant' ? (row.administrative || '') : '') + (row.address || ''));
    if (key === 'commission_rate') return '正常下单：'+text(row.commission_normal_rate)+'%<br>扫门店码：'+text(row.commission_merchant_rate)+'%<br>扫酒店码：'+text(row.commission_shop_rate)+'%';
    if (key === 'commission_shop_rate') return text(row[key]) + '%';
    if (key === 'examine_status') return ({1:'待审核',2:'已通过',3:'已拒绝'})[Number(row[key])] || text(row[key]);
    if (key === 'status') return Number(row[key]) === 1 ? '启用' : '禁用';
    if (key === 'createtime') { var d=new Date(Number(row[key])*1000),p=function(v){return String(v).padStart(2,'0');};return Number(row[key])?d.getFullYear()+'-'+p(d.getMonth()+1)+'-'+p(d.getDate())+' '+p(d.getHours())+':'+p(d.getMinutes())+':'+p(d.getSeconds()):'-'; }
    return text(row[key]);
  }
  function load() {
    var status = $('#statusFilter').val();
    var examineStatus = $('#examineStatusFilter').val();
    var url = endpoint + (state.recycle ? '/recycle' : '') + '?page=' + state.current + '&limit=10' + (state.recycle ? '' : '&keyword=' + encodeURIComponent($('#keyword').val() || '') + (status ? '&status=' + status : '') + (examineStatus ? '&examineStatus=' + examineStatus : ''));
    api(url, 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg || '加载失败');
      var data = response.data || {}; state.rows = data.data || []; state.total = Number(data.total || 0); state.current = Number(data.current_page || 1); state.last = Number(data.last_page || 1); render();
    }).fail(function () { alert('加载失败，请重新登录后重试'); });
  }
  function render() {
    var select = page.role === 'shop' && !state.recycle;
    $('#partnerHead').html((select?'<th><input id="selectPage" type="checkbox" aria-label="选择当前页"></th>':'')+'<th>ID</th>'+columns.map(function(c){return '<th>'+c[1]+'</th>';}).join('')+'<th>操作</th>');
    if (!state.rows.length) $('#partnerBody').html('<tr><td colspan="8" class="empty-state">暂无记录</td></tr>');
    else $('#partnerBody').html(state.rows.map(function (row, index) {
      var actions='<button class="btn-link detail" data-id="'+row.id+'">详情</button>';
      if(state.recycle)actions+=' <button class="btn-link restore" data-id="'+row.id+'">恢复</button> <button class="btn-link danger destroy" data-id="'+row.id+'">彻底删除</button>';
      else { if(page.role==='merchant')actions+=' <a class="btn-link" href="/admin/merchant-services/cars?merchantId='+row.id+'">门店车辆</a>'; actions+=' <button class="btn-link edit" data-id="'+row.id+'">编辑</button>'; if(Number(row.examine_status)===1)actions+=' <button class="btn-link approve" data-id="'+row.id+'">审核通过</button> <button class="btn-link danger reject" data-id="'+row.id+'">审核拒绝</button>'; actions+=' <button class="btn-link status" data-index="'+index+'">'+(Number(row.status)===1?'禁用':'启用')+'</button> <button class="btn-link danger remove" data-id="'+row.id+'">删除</button>'; }
      return '<tr>'+(select?'<td><input class="partner-select" type="checkbox" value="'+row.id+'" aria-label="选择记录"></td>':'')+'<td>'+text(row.id)+'</td>'+columns.map(function(c){return '<td>'+cell(c[0],row)+'</td>';}).join('')+'<td>'+actions+'</td></tr>';
    }).join(''));
    $('#totalText').text('共 ' + state.total + ' 条记录'); $('#pageText').text(state.current + ' / ' + state.last); $('#prevButton').prop('disabled', state.current <= 1); $('#nextButton').prop('disabled', state.current >= state.last); $('#bulkDeleteButton').prop('disabled', true);
  }
  function input(field, value) {
    var addRequired = ['password','avatar','image','nickname','mobile','address'];
    if (page.role === 'merchant') addRequired = addRequired.concat(['business','car_num','min_price','commission_normal_rate','commission_merchant_rate','commission_shop_rate','pick_type','point']);
    else addRequired.push('commission_shop_rate');
    var editRequired = page.role === 'merchant' ? ['avatar','address','car_num','min_price','commission_normal_rate','commission_merchant_rate','commission_shop_rate','pick_type','point'] : ['avatar','commission_shop_rate'];
    var required = field.required || (state.editingId ? editRequired.indexOf(field.key) >= 0 : addRequired.indexOf(field.key) >= 0);
    if (field.type === 'user') {
      if (state.editingId) return '<input class="form-control" value="' + escapeHtml(value || '') + '" disabled>';
      return '<select class="form-select" name="user_id" required><option value="">请选择用户</option>' + state.users.map(function (user) { return '<option value="' + user.id + '"' + (Number(value) === Number(user.id) ? ' selected' : '') + '>' + text(user.nickname || ('用户' + user.id)) + '（编号' + user.id + '）</option>'; }).join('') + '</select>';
    }
    if (field.type === 'textarea') return '<textarea class="form-control" name="' + field.key + '" rows="3">' + escapeHtml(value === '-' ? '' : value) + '</textarea>';
    if (field.type === 'status') return '<select class="form-select" name="status"><option value="1">启用</option><option value="2"' + (Number(value) === 2 ? ' selected' : '') + '>禁用</option></select>';
    if (field.type === 'pick-type') return '<select class="form-select" name="pick_type"><option value="1">到店取车</option><option value="2"' + (Number(value) === 2 ? ' selected' : '') + '>到店取车+上门送车</option></select>';
    if (field.type === 'point') return '<div class="upload-field"><input id="merchantPoint" class="form-control" name="point" value="' + escapeHtml(value || '') + '" readonly' + (required ? ' required' : '') + '><button id="locateMerchantAddress" class="btn btn-secondary" type="button">地址定位</button></div><div id="merchantMap" style="height:360px;margin-top:12px;border-radius:6px"></div><small id="merchantMapHint" class="text-muted">点击地图选择位置，系统会自动填写经纬度和地址。</small>';
    if (field.type === 'upload' || field.type === 'uploads') return '<div class="upload-field"><input class="form-control" name="' + field.key + '" value="' + escapeHtml(value || '') + '"' + (required ? ' required' : '') + '><button class="btn btn-secondary" type="button" onclick="uploadInto(this.previousElementSibling,' + (field.type === 'uploads') + ')">上传</button></div>';
    return '<input class="form-control" name="' + field.key + '" type="' + (field.type || 'text') + '" value="' + escapeHtml(value == null ? '' : value) + '"' + (required ? ' required' : '') + (field.key === 'password' && state.editingId ? ' placeholder="不修改请留空"' : '') + '>';
  }
  function openEditor(row) {
    if (row && page.role === 'merchant' && !row.point) row.point = String(row.lng || '') + ',' + String(row.lat || '');
    state.editingId = row ? row.id : null; $('#editorTitle').text((row ? '编辑' : '新增') + page.title);
    $('#partnerForm').html(fields.map(function (field) { var serviceField = /^service_(distance|money)/.test(field.key) ? ' pickup-service-field' : ''; return '<div class="form-group' + serviceField + '"><label>' + field.label + '</label>' + input(field, row && row[field.key]) + '</div>'; }).join(''));
    if (page.role === 'merchant') $('#partnerForm').append('<input id="merchantAdministrative" type="hidden" name="administrative" value="' + escapeHtml(row && row.administrative || '') + '">');
    $('#editorMask').prop('hidden', false); syncPickupFields(); initMerchantMap(row || {});
  }
  function syncPickupFields() { $('.pickup-service-field').toggle(Number($('#partnerForm [name="pick_type"]').val()) !== 1); }
  function initMerchantMap(row) {
    if (page.role !== 'merchant') return;
    if (!window.TMap) { $('#merchantMapHint').text('地图加载失败，请检查网络后重试。'); return; }
    var parts = String(row.point || '').split(','), lng = Number(parts[0] || row.lng), lat = Number(parts[1] || row.lat);
    if (!isFinite(lng) || !isFinite(lat) || !lng || !lat) { lng = 116.307503; lat = 39.984104; }
    var center = new TMap.LatLng(lat, lng), map = new TMap.Map('merchantMap', { zoom: 14, center: center });
    var marker = new TMap.MultiMarker({ map: map, geometries: [{ id: 'merchant', position: center }] });
    function choose(nextLat, nextLng) {
      var position = new TMap.LatLng(nextLat, nextLng);
      marker.setGeometries([{ id: 'merchant', position: position }]);
      map.setCenter(position);
      $('#merchantPoint').val(Number(nextLng).toFixed(6) + ',' + Number(nextLat).toFixed(6));
      new TMap.service.Geocoder().getAddress({ location: position }).then(function (result) {
        if (!result || !result.result) return;
        var component = result.result.address_component || {};
        var administrative = String(component.province || '') + String(component.city || '') + String(component.district || '');
        var street = String(component.street || '') + String(component.street_number || '');
        $('#merchantAdministrative').val(administrative);
        $('#partnerForm [name="address"]').val(street || result.result.address || '');
      });
    }
    map.on('click', function (event) { choose(event.latLng.getLat(), event.latLng.getLng()); });
    $('#locateMerchantAddress').off('click').on('click', function () {
      var address = $('#partnerForm [name="address"]').val();
      if (!address) return alert('请先填写地址');
      new TMap.service.Geocoder().getLocation({ address: address }).then(function (result) {
        var location = result && result.result && result.result.location;
        if (location) choose(location.lat, location.lng);
      }).catch(function () { alert('地址定位失败，请在地图上选择位置'); });
    });
  }
  function showRecycled(row) { if(!row)return; var hidden={password:true,salt:true}; $('#detailBody').html(Object.keys(row).filter(function(key){return !hidden[key];}).map(function(key){return '<tr><th style="width:150px">'+text(adminDetailLabel(key))+'</th><td>'+text(adminDetailValue(key,Array.isArray(row[key])?row[key].join(', '):row[key]))+'</td></tr>';}).join('')); $('#detailMask').prop('hidden',false); }
  function detail(id, edit) {
    api(endpoint + '/' + id, 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg); var row = response.data || {};
      if (edit) return openEditor(row);
      var hidden = { password: true, salt: true }; $('#detailBody').html(Object.keys(row).filter(function (key) { return !hidden[key]; }).map(function (key) { return '<tr><th style="width:150px">' + text(adminDetailLabel(key)) + '</th><td>' + text(adminDetailValue(key, Array.isArray(row[key]) ? row[key].join(', ') : row[key])) + '</td></tr>'; }).join('')); $('#detailMask').prop('hidden', false);
    });
  }
  function openExamine(id,type){$('#examineId').val(id);$('#examineType').val(type);$('#approveFields').toggle(type===2);$('#rejectFields').toggle(type===3);$('#examineTitle').text(type===2?'审核通过':'审核拒绝');$('#loginUsername,#loginPassword,#examineNote').val('');$('#examineMask').prop('hidden',false);}
  $('#searchButton').on('click', function () { state.current = 1; load(); }); $('#resetButton').on('click', function () { $('#keyword,#statusFilter,#examineStatusFilter').val(''); state.current = 1; load(); }); $('#recycleButton').on('click',function(){state.recycle=!state.recycle;state.current=1;$(this).html(state.recycle?'<i class="fas fa-arrow-left"></i> 返回列表':'<i class="fas fa-recycle"></i> 回收站');$('#addButton').prop('hidden',state.recycle);$('.resource-toolbar').prop('hidden',state.recycle);load();});
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } }); $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } }); $('#addButton').on('click', function () { openEditor(null); });
  $('.close-editor').on('click', function () { $('#editorMask').prop('hidden', true); }); $('.close-detail').on('click', function () { $('#detailMask').prop('hidden', true); }); $('.close-examine').on('click',function(){$('#examineMask').prop('hidden',true);});
  $('#partnerBody').on('click', '.detail', function () { if(state.recycle)return showRecycled(state.rows.filter(function(row){return Number(row.id)===Number($(this).data('id'));}.bind(this))[0]); detail($(this).data('id'), false); }).on('click', '.edit', function () { detail($(this).data('id'), true); }).on('click','.restore',function(){api(endpoint+'/restore/'+$(this).data('id'),'POST',{}).done(function(r){if(r.code===1)load();else alert(r.msg);});}).on('click','.destroy',function(){if(!confirm('彻底删除后无法恢复，确定继续吗？'))return;api(endpoint+'/destroy/'+$(this).data('id'),'DELETE').done(function(r){if(r.code===1)load();else alert(r.msg);});}).on('click','.approve',function(){openExamine($(this).data('id'),2);}).on('click','.reject',function(){openExamine($(this).data('id'),3);}).on('click', '.status', function () { var row = state.rows[Number($(this).data('index'))]; api(endpoint + '/' + row.id, 'PUT', { status: Number(row.status) === 1 ? 2 : 1 }).done(function (r) { if (r.code === 1) load(); else alert(r.msg); }); }).on('click', '.remove', function () { if (!confirm('确定删除该记录吗？')) return; api(endpoint + '/' + $(this).data('id'), 'DELETE').done(function (r) { if (r.code === 1) load(); else alert(r.msg); }); });
  function syncPartnerSelection(){var boxes=$('#partnerBody .partner-select'),selected=boxes.filter(':checked');$('#selectPage').prop('checked',boxes.length>0&&selected.length===boxes.length);$('#bulkDeleteButton').prop('disabled',selected.length===0);}
  $('#partnerBody').on('change','.partner-select',syncPartnerSelection);$('#partnerHead').on('change','#selectPage',function(){$('#partnerBody .partner-select').prop('checked',this.checked);syncPartnerSelection();});
  $('#bulkDeleteButton').on('click',function(){var ids=$('#partnerBody .partner-select:checked').map(function(){return Number(this.value);}).get();if(!ids.length||!confirm('确定删除选中的 '+ids.length+' 家酒店吗？'))return;api(endpoint,'DELETE',{ids:ids}).done(function(r){if(r.code===1)load();else alert(r.msg);});});
  $('#submitExamine').on('click',function(){var type=Number($('#examineType').val()),payload={id:Number($('#examineId').val()),type:type,username:$('#loginUsername').val().trim(),password:$('#loginPassword').val(),note:$('#examineNote').val().trim()};if(type===2&&(!payload.username||!payload.password))return alert('通过审核时必须设置登录账号和密码');if(type===2)$('.approval-setting').each(function(){payload[$(this).data('key')]=Number($(this).val());});if(!confirm(type===2?'确认审核通过吗？':'确认审核拒绝吗？'))return;api('/api/admin/business/applications/'+page.role+'/examine','POST',payload).done(function(r){if(r.code===1){$('#examineMask').prop('hidden',true);load();}else alert(r.msg);});});
  $('#savePartner').on('click', function () { var form = document.getElementById('partnerForm'); if (!form.reportValidity()) return; var data = {}; $(form).serializeArray().forEach(function (item) { if (item.value !== '') data[item.name] = item.value; }); api(endpoint + (state.editingId ? '/' + state.editingId : ''), state.editingId ? 'PUT' : 'POST', data).done(function (r) { if (r.code === 1) { $('#editorMask').prop('hidden', true); load(); } else alert(r.msg); }); });
  $('#partnerForm').on('change', '[name="pick_type"]', syncPickupFields);
  $('#addButton').prop('disabled', true);
  api('/api/admin/business/partners/user-options?role=' + encodeURIComponent(page.role), 'GET').done(function (response) { if (response.code === 1) state.users = response.data || []; }).always(function () { $('#addButton').prop('disabled', false); });
  load();
})(jQuery);
