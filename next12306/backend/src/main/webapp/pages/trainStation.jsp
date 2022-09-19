<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>站点管理</title>
    <jsp:include page="/common/backend_common.jsp"/>
    <jsp:include page="/common/page.jsp"/>
</head>
<body class="no-skin" youdao="bind" style="background: white">
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>

<div class="page-header">
    <h1>
        站点管理
        <small>
            <i class="ace-icon fa fa-angle-double-right"></i>
            维护支持的站点
        </small>
    </h1>
</div>
<div class="main-content-inner">
    <div class="col-sm-12">
        <div class="col-xs-12">
            <div class="table-header">
                站点列表&nbsp;&nbsp;
                <a class="green" href="#">
                    <i class="ace-icon fa fa-plus-circle orange bigger-130 station-add"></i>
                </a>
            </div>
            <div>
                <div id="dynamic-table_wrapper" class="dataTables_wrapper form-inline no-footer">
                    <table id="dynamic-table" class="table table-striped table-bordered table-hover dataTable no-footer" role="grid"
                           aria-describedby="dynamic-table_info" style="font-size:14px">
                        <thead>
                        <tr role="row">
                            <th tabindex="0" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                ID
                            </th>
                            <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                站点名称
                            </th>
                            <th tabindex="2" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                所属城市
                            </th>
                            <th class="sorting_disabled" rowspan="1" colspan="1" aria-label="">
                                操作
                            </th>
                        </tr>
                        </thead>
                        <tbody id="stationList"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="dialog-station-form" style="display: none;">
    <form id="stationForm">
        <table class="table table-striped table-bordered table-hover dataTable no-footer" role="grid">
            <tr>
                <td><label for="name">名称</label></td>
                <td>
                    <input type="text" name="name" id="name" value="" class="text ui-widget-content ui-corner-all">
                    <input type="hidden" name="id" id="trainStationId"/>
                </td>
            </tr>
            <tr>
                <td><label for="name">所属城市</label></td>
                <td>
                    <select name="cityId" id="cityId" data-placeholder="选择城市" style="width: 200px;"> </select>
                </td>
            </tr>
        </table>
    </form>
</div>


<script id="stationListTemplate" type="x-tmpl-mustache">
{{#stationList}}
<tr role="row" class="station-name odd" data-id="{{id}}"><!--even -->
    <td><a href="#" class="station-edit" data-id="{{id}}">{{id}}</a></td>
    <td><a href="#" class="station-edit" data-id="{{id}}">{{name}}</a></td>
    <td>{{cityName}}</td>
    <td>
        <div class="hidden-sm hidden-xs action-buttons">
            <a class="green station-edit" href="#" data-id="{{id}}">
                <i class="ace-icon fa fa-pencil bigger-100"></i>
            </a>
        </div>
    </td>
</tr>
{{/stationList}}
</script>

<script type="application/javascript">
    $(function() {

        var stationListTemplate = $('#stationListTemplate').html();
        Mustache.parse(stationListTemplate);
        var stationMap = {};
        var cityMap = {};

        loadCityList();

        loadStationList();

        function loadCityList() {
            $.ajax({
                url: "/admin/train/city/list.json",
                success : function (result) {
                    if (result.ret) {
                        $(result.data).each(function (i, city) {
                            cityMap[city.id] = city.name;
                        });
                    } else {
                        showMessage("加载城市列表", result.msg, false);
                    }
                }
            })
        }

        function loadStationList() {
            $.ajax({
                url: "/admin/train/station/list.json",
                success : function (result) {
                    if (result.ret) {
                        var rendered = Mustache.render(stationListTemplate, {stationList: result.data});
                        $(result.data).each(function (i, station) {
                            stationMap[station.id] = station;
                        });
                        $("#stationList").html(rendered);
                        bindStationClick();
                    } else {
                        showMessage("加载站点列表", result.msg, false);
                    }
                }
            })
        }

        // 绑定部门点击事件
        function bindStationClick() {

            $(".station-edit").click(function(e) {
                e.preventDefault();
                e.stopPropagation();
                var stationId = $(this).attr("data-id");
                $("#dialog-station-form").dialog({
                    modal: true,
                    title: "编辑站点",
                    open: function(event, ui) {
                        $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                        $("#stationForm")[0].reset();
                        $("#trainStationId").val(stationId);
                        var optionStr = "";

                        $.each(cityMap, function (cityId, cityName) {
                            optionStr += Mustache.render("<option value='{{id}}'>{{name}}</option>", {
                                id: cityId,
                                name: cityName
                            });
                        });
                        $("#cityId").html(optionStr);
                        var targetStation = stationMap[stationId];
                        if (targetStation) {
                            $("#name").val(targetStation.name);
                            $("#cityId").val(targetStation.cityId);
                        }
                    },
                    buttons : {
                        "更新": function(e) {
                            e.preventDefault();
                            updateStation(false, function (data) {
                                $("#dialog-station-form").dialog("close");
                            }, function (data) {
                                showMessage("更新站点", data.msg, false);
                            })
                        },
                        "取消": function () {
                            $("#dialog-station-form").dialog("close");
                        }
                    }
                });
            })
        }


        $(".station-add").click(function() {
            $("#dialog-station-form").dialog({
                modal: true,
                title: "新增站点",
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                    $("#stationForm")[0].reset();
                    var optionStr = "";
                    $.each(cityMap, function (cityId, cityName) {
                        optionStr += Mustache.render("<option value='{{id}}'>{{name}}</option>", {
                            id: cityId,
                            name: cityName
                        });
                    });
                    $("#cityId").html(optionStr);
                },
                buttons : {
                    "添加": function(e) {
                        e.preventDefault();
                        updateStation(true, function (data) {
                            $("#dialog-station-form").dialog("close");
                            loadStationList();
                        }, function (data) {
                            showMessage("新增站点", data.msg, false);
                        })
                    },
                    "取消": function () {
                        $("#dialog-station-form").dialog("close");
                    }
                }
            });
        });

        function updateStation(isCreate, successCallback, failCallback) {
            $.ajax({
                url: isCreate ? "/admin/train/station/save.json" : "/admin/train/station/update.json",
                data: $("#stationForm").serializeArray(),
                type: 'POST',
                success: function(result) {
                    if (result.ret) {
                        loadStationList();
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