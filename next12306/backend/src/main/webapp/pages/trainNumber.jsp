<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>车次管理</title>
    <jsp:include page="/common/backend_common.jsp"/>
    <jsp:include page="/common/page.jsp"/>
</head>
<body class="no-skin" youdao="bind" style="background: white">
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>

<div class="page-header">
    <h1>
        车次管理
        <small>
            <i class="ace-icon fa fa-angle-double-right"></i>
            维护车次
        </small>
    </h1>
</div>
<div class="main-content-inner">
    <div class="col-sm-12">
        <div class="col-xs-12">
            <div class="table-header">
                车次列表&nbsp;&nbsp;
                <a class="green" href="#">
                    <i class="ace-icon fa fa-plus-circle orange bigger-130 number-add"></i>
                </a>
            </div>
            <div>
                <div id="dynamic-table_wrapper" class="dataTables_wrapper form-inline no-footer">
                    <table id="dynamic-table" class="table table-striped table-bordered table-hover dataTable no-footer" role="grid"
                           aria-describedby="dynamic-table_info" style="font-size:14px">
                        <thead>
                        <tr role="row">
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                名称
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                始发站
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                到达站
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                列车座位标识
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                列车类型
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                座位数
                            </th>
                            <th class="sorting_disabled" rowspan="1" colspan="1" aria-label="">
                                操作
                            </th>
                        </tr>
                        </thead>
                        <tbody id="numberList"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="dialog-number-form" style="display: none;">
    <form id="numberForm">
        <table class="table table-striped table-bordered table-hover dataTable no-footer" role="grid">
            <tr>
                <td><label for="name">名称</label></td>
                <td>
                    <input type="text" name="name" id="name" value="" class="text ui-widget-content ui-corner-all">
                    <input type="hidden" name="id" id="trainNumberId"/>
                </td>
            </tr>
            <tr>
                <td><label for="trainType">列车座位类型</label></td>
                <td>
                    <select id="trainType" name="trainType" data-placeholder="选择列车座位类型" style="width: 150px;">
                        <option value="CRH2">CRH2</option>
                        <option value="CRH5">CRH5</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td><label for="type">列车类型</label></td>
                <td>
                    <select id="type" name="type" data-placeholder="选择列车类型" style="width: 150px;">
                        <option value="1">高铁</option>
                        <option value="2">动车</option>
                        <option value="3">特快</option>
                        <option value="4">普快</option>
                    </select>
                </td>
            </tr>
        </table>
    </form>
</div>

<div id="dialog-generate-form" style="display: none;">
    <table class="table table-striped table-bordered table-hover dataTable no-footer" role="grid">
        <tr>
            <td><label for="name">发车时间</label></td>
            <td>
                <input type="text" name="fromTime" id="fromTime"/>
                (yyyy-MM-dd HH:mm)
            </td>
        </tr>
    </table>
</div>

<script id="numberListTemplate" type="x-tmpl-mustache">
{{#numberList}}
<tr role="row" class="number-name odd" data-id="{{id}}"><!--even -->
    <td><a href="#" class="number-edit" data-id="{{id}}">{{name}}</a></td>
    <td><a href="#" class="number-edit" data-id="{{id}}">{{fromStation}}</a></td>
    <td><a href="#" class="number-edit" data-id="{{id}}">{{toStation}}</a></td>
    <td>{{trainType}}</td>
    <td>{{showType}}</td>
    <td>{{seatNum}}</td>
    <td>
        <div class="hidden-sm hidden-xs action-buttons">
            <a class="green number-edit" href="#" data-id="{{id}}">
                <i class="ace-icon fa fa-pencil bigger-100"></i>
            </a>
            <a class="red number-generate" href="#" data-id="{{id}}">
                <i class="ace-icon fa fa-flag bigger-100"></i>
            </a>
        </div>
    </td>
</tr>
{{/numberList}}
</script>

<script type="application/javascript">
    $(function() {

        var numberListTemplate = $('#numberListTemplate').html();
        Mustache.parse(numberListTemplate);
        var numberMap = {};

        loadNumberList();

        function loadNumberList() {
            $.ajax({
                url: "/admin/train/number/list.json",
                success : function (result) {
                    if (result.ret) {
                        var numberList = result.data;
                        var rendered = Mustache.render(numberListTemplate, {
                            numberList: result.data,
                            "showType": function() {
                                return showType(this.type);
                            }
                        });
                        $(numberList).each(function (i, number) {
                            numberMap[number.id] = number;
                        });
                        $("#numberList").html(rendered);
                        bindNumberClick();
                    } else {
                        showMessage("加载车次列表", result.msg, false);
                    }
                }
            })
        }

        function showType(type) {
            var typeString = "未知";
            switch (type) {
                case 1: typeString = "高铁"; break;
                case 2: typeString = "动车"; break;
                case 3: typeString = "特快"; break;
                case 4: typeString = "普快"; break;
            }
            return typeString;
        }

        // 绑定点击事件
        function bindNumberClick() {

            $(".number-edit").click(function(e) {
                e.preventDefault();
                e.stopPropagation();
                var numberId = $(this).attr("data-id");
                $("#dialog-number-form").dialog({
                    modal: true,
                    title: "编辑车次",
                    open: function(event, ui) {
                        $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                        $("#numberForm")[0].reset();
                        $("#trainNumberId").val(numberId);
                        var targetNumber = numberMap[numberId];
                        if (targetNumber) {
                            $("#name").val(targetNumber.name);
                        }
                    },
                    buttons : {
                        "更新": function(e) {
                            e.preventDefault();
                            updateNumber(false, function (data) {
                                $("#dialog-number-form").dialog("close");
                            }, function (data) {
                                showMessage("更新车次", data.msg, false);
                            })
                        },
                        "取消": function () {
                            $("#dialog-number-form").dialog("close");
                        }
                    }
                });
            });
            $(".number-generate").click(function(e){
                e.preventDefault();
                e.stopPropagation();
                var numberId = $(this).attr("data-id");
                $("#dialog-generate-form").dialog({
                    modal: true,
                    title: "生成座位",
                    open: function(event, ui) {
                        $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                        $("#fromTime").val('');
                    },
                    buttons : {
                        "生成": function(e) {
                            e.preventDefault();
                            var fromTime = $("#fromTime").val();
                            if (fromTime == '') {
                                showMessage("生成座位", "必须指定发车时间", false);
                                return;
                            }
                            $.ajax({
                                url: "/admin/train/seat/generate.json",
                                data: {
                                    trainNumberId: numberId,
                                    fromTime: fromTime
                                },
                                type: 'POST',
                                success: function(result) {
                                    if (result.ret) {
                                        $("#dialog-generate-form").dialog("close");
                                        showMessage("生成座位", "操作成功", true);
                                    } else {
                                        showMessage("生成座位", result.msg, false);
                                    }
                                }
                            })
                        },
                        "取消": function () {
                            $("#dialog-generate-form").dialog("close");
                        }
                    }
                });
            });
        }

        $(".number-add").click(function() {
            $("#dialog-number-form").dialog({
                modal: true,
                title: "新增车次",
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                    $("#numberForm")[0].reset();
                },
                buttons : {
                    "添加": function(e) {
                        e.preventDefault();
                        updateNumber(true, function (data) {
                            $("#dialog-number-form").dialog("close");
                            loadNumberList();
                        }, function (data) {
                            showMessage("新增车次", data.msg, false);
                        })
                    },
                    "取消": function () {
                        $("#dialog-number-form").dialog("close");
                    }
                }
            });
        });

        function updateNumber(isCreate, successCallback, failCallback) {
            $.ajax({
                url: isCreate ? "/admin/train/number/save.json" : "/admin/train/number/update.json",
                data: $("#numberForm").serializeArray(),
                type: 'POST',
                success: function(result) {
                    if (result.ret) {
                        loadNumberList();
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