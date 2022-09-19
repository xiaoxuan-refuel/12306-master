<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>车次详情管理</title>
    <jsp:include page="/common/backend_common.jsp"/>
    <jsp:include page="/common/page.jsp"/>
</head>
<body class="no-skin" youdao="bind" style="background: white">
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>

<div class="page-header">
    <h1>
        车次详情管理
        <small>
            <i class="ace-icon fa fa-angle-double-right"></i>
            维护车次详情，比如车次的站点、到达时间、等待时间
        </small>
    </h1>
</div>
<div class="main-content-inner">
    <div class="col-sm-12">
        <div class="col-xs-12">
            <div class="table-header">
                车次详情列表&nbsp;&nbsp;
                <a class="green" href="#">
                    <i class="ace-icon fa fa-plus-circle orange bigger-130 number-detail-add"></i>
                </a>
            </div>
            <div>
                <div id="dynamic-table_wrapper" class="dataTables_wrapper form-inline no-footer">
                    <table id="dynamic-table" class="table table-striped table-bordered table-hover dataTable no-footer"
                           role="grid"
                           aria-describedby="dynamic-table_info" style="font-size:14px">
                        <thead>
                        <tr role="row">
                            <th tabindex="0" aria-controls="dynamic-table" rowspan="2" colspan="1">
                                车次
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="2" colspan="1">
                                出发 -> 到达
                            </th>
                            <th tabindex="2" aria-controls="dynamic-table" rowspan="1" colspan="1">
                                次序
                            </th>
                            <th tabindex="3" aria-controls="dynamic-table" rowspan="1" colspan="1">
                                相对出发时间(分钟)
                            </th>
                            <th tabindex="4" aria-controls="dynamic-table" rowspan="1" colspan="1">
                                到站等待时间(分钟)
                            </th>
                            <th tabindex="5" aria-controls="dynamic-table" rowspan="3" colspan="1">
                                座位价钱
                            </th>
                            <th class="sorting_disabled" rowspan="1" colspan="2" aria-label="">
                                操作
                            </th>
                        </tr>
                        </thead>
                        <tbody id="numberDetailList"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="dialog-numberDetail-form" style="display: none;">
    <form id="numberDetailForm">
        <table class="table table-striped table-bordered table-hover dataTable no-footer" role="grid">
            <tr>
                <td><label for="trainNumberId">车次</label></td>
                <td>
                    <select id="trainNumberId" name="trainNumberId"></select>
                </td>
            </tr>
            <tr>
                <td><label for="fromStationId">出发站</label></td>
                <td>
                    <select id="fromStationId" name="fromStationId"></select>
                </td>
            </tr>
            <tr>
                <td><label for="toStationId">到达站</label></td>
                <td>
                    <select id="toStationId" name="toStationId"></select>
                </td>
            </tr>
            <tr>
                <td><label for="relativeMinute">相对出发时间</label></td>
                <td>
                    <input type="text" name="relativeMinute" id="relativeMinute" value=""
                           class="text ui-widget-content ui-corner-all">
                </td>
            </tr>
            <tr>
                <td><label for="waitMinute">到站等待时间</label></td>
                <td>
                    <input type="text" name="waitMinute" id="waitMinute" value=""
                           class="text ui-widget-content ui-corner-all">
                </td>
            </tr>
            <tr>
                <td><label for="money">座位价钱</label></td>
                <td>
                    <input type="text" name="money" id="money" value="" class="text ui-widget-content ui-corner-all">
                </td>
            </tr>
            <tr>
                <td><label for="end">是否完成</label></td>
                <td>
                    <select id="end" name="end">
                        <option value="0">未完成</option>
                        <option value="1">完成</option>
                    </select>
                </td>
            </tr>
        </table>
    </form>
</div>


<script id="numberDetailListTemplate" type="x-tmpl-mustache">
{{#numberDetailList}}
<tr role="row" class="numberDetail-name odd" data-id="{{id}}">
    <td>{{trainNumber}}</td>
    <td>{{fromStation}} -> {{toStation}}</td>
    <td>{{stationIndex}}</td>
    <td>{{relativeMinute}}</td>
    <td>{{waitMinute}}</td>
    <td>{{money}}</td>
    <td>
        <div class="hidden-sm hidden-xs action-buttons">
            <a class="red numberDetail-delete" href="#" data-id="{{id}}" data-name="{{name}}">
                <i class="ace-icon fa fa-trash-o bigger-100"></i>
            </a>
        </div>
    </td>
</tr>
{{/numberDetailList}}

</script>

<script type="application/javascript">
    $(function () {

        var numberDetailListTemplate = $('#numberDetailListTemplate').html();
        Mustache.parse(numberDetailListTemplate);
        var numberDetailMap = {};

        loadNumberDetailList();

        function loadNumberDetailList() {
            $.ajax({
                url: "/admin/train/numberDetail/list.json",
                success: function (result) {
                    if (result.ret) {
                        var numberDetailList = result.data;
                        var rendered = Mustache.render(numberDetailListTemplate, {numberDetailList: result.data});
                        $(numberDetailList).each(function (i, numberDetail) {
                            numberDetailMap[numberDetail.id] = numberDetail;
                        });
                        $("#numberDetailList").html(rendered);
                        bindNumberDetailClick();
                    } else {
                        showMessage("加载车次列表", result.msg, false);
                    }
                }
            })
        }


        function bindNumberDetailClick() {
            $(".numberDetail-delete").click(function (e) {
                e.preventDefault();
                e.stopPropagation();
                var id = $(this).attr("data-id");
                if (confirm("确定要删除这条车次详情吗?")) {
                    $.ajax({
                        url: "/admin/train/numberDetail/delete.json",
                        data: {
                            id: id
                        },
                        success: function (result) {
                            if (result.ret) {
                                showMessage("删除车次详情", "操作成功", true);
                                loadNumberDetailList();
                            } else {
                                showMessage("删除车次详情", result.msg, false);
                            }
                        }
                    });
                }
            });
        }


        $(".number-detail-add").click(function () {
            $("#dialog-numberDetail-form").dialog({
                modal: true,
                title: "新增车次详情",
                open: function (event, ui) {
                    $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                    $("#numberDetailForm")[0].reset();
                    loadNumberListAndRender();
                    loadStationAndRender();
                },
                buttons: {
                    "添加": function (e) {
                        e.preventDefault();
                        updateNumberDetail(true, function (data) {
                            $("#dialog-numberDetail-form").dialog("close");
                            loadNumberDetailList();
                        }, function (data) {
                            showMessage("新增车次详情", data.msg, false);
                        })
                    },
                    "添加并继续": function (e) {
                        e.preventDefault();
                        updateNumberDetail(true, function (data) {
                            loadNumberDetailList();
                        }, function (data) {
                            showMessage("新增车次详情", data.msg, false);
                        })
                    },
                    "取消": function () {
                        $("#dialog-numberDetail-form").dialog("close");
                    }
                }
            });
        });

        function loadNumberListAndRender() {
            $.ajax({
                url: "/admin/train/number/list.json",
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        var optionStr = "";
                        $(result.data).each(function (i, obj) {
                            optionStr += Mustache.render("<option value='{{id}}'>{{name}}</option>", {
                                id: obj.id,
                                name: obj.name
                            });
                        });
                        $("#trainNumberId").html(optionStr);
                    } else {
                        showMessage("获取车次", data.msg, false);
                    }
                }
            })
        };

        function loadStationAndRender() {
            $.ajax({
                url: "/admin/train/station/list.json",
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        var optionStr = "";
                        $(result.data).each(function (i, obj) {
                            optionStr += Mustache.render("<option value='{{id}}'>{{name}}</option>", {
                                id: obj.id,
                                name: obj.name
                            });
                        });
                        $("#fromStationId").html(optionStr);
                        $("#toStationId").html(optionStr);
                    } else {
                        showMessage("获取站点", data.msg, false);
                    }
                }
            })
        };

        function updateNumberDetail(isCreate, successCallback, failCallback) {
            $.ajax({
                url: isCreate ? "/admin/train/numberDetail/save.json" : "/admin/train/numberDetail/update.json",
                data: $("#numberDetailForm").serializeArray(),
                type: 'POST',
                success: function (result) {
                    if (result.ret) {
                        loadNumberDetailList();
                        if (successCallback) {
                            successCallback(result);
                        }
                    } else {
                        if (failCallback) {
                            failCallback(result);
                        }
                    }
                }
            })
        }
    })
</script>