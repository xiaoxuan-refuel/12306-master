<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>城市管理</title>
    <jsp:include page="/common/backend_common.jsp"/>
    <jsp:include page="/common/page.jsp"/>
</head>
<body class="no-skin" youdao="bind" style="background: white">
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>

<div class="page-header">
    <h1>
        城市管理
        <small>
            <i class="ace-icon fa fa-angle-double-right"></i>
            维护支持的城市
        </small>
    </h1>
</div>
<div class="main-content-inner">
    <div class="col-sm-12">
        <div class="col-xs-12">
            <div class="table-header">
                城市列表&nbsp;&nbsp;
                <a class="green" href="#">
                    <i class="ace-icon fa fa-plus-circle orange bigger-130 city-add"></i>
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
                                名称
                            </th>
                            <th class="sorting_disabled" rowspan="1" colspan="1" aria-label="">
                                操作
                            </th>
                        </tr>
                        </thead>
                        <tbody id="cityList"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="dialog-city-form" style="display: none;">
    <form id="cityForm">
        <table class="table table-striped table-bordered table-hover dataTable no-footer" role="grid">
            <tr>
                <td><label for="name">名称</label></td>
                <td>
                    <input type="text" name="name" id="name" value="" class="text ui-widget-content ui-corner-all">
                    <input type="hidden" name="id" id="trainCityId"/>
                </td>
            </tr>
        </table>
    </form>
</div>


<script id="cityListTemplate" type="x-tmpl-mustache">
{{#cityList}}
<tr role="row" class="city-name odd" data-id="{{id}}"><!--even -->
    <td><a href="#" class="city-edit" data-id="{{id}}">{{id}}</a></td>
    <td><a href="#" class="city-edit" data-id="{{id}}">{{name}}</a></td>
    <td>
        <div class="hidden-sm hidden-xs action-buttons">
            <a class="green city-edit" href="#" data-id="{{id}}">
                <i class="ace-icon fa fa-pencil bigger-100"></i>
            </a>
        </div>
    </td>
</tr>
{{/cityList}}
</script>

<script type="application/javascript">
    $(function() {

        var cityListTemplate = $('#cityListTemplate').html();
        Mustache.parse(cityListTemplate);
        var cityMap = {};

        loadCityList();

        function loadCityList() {
            $.ajax({
                url: "/admin/train/city/list.json",
                success : function (result) {
                    if (result.ret) {
                        var cityList = result.data;
                        var rendered = Mustache.render(cityListTemplate, {cityList: result.data});
                        $(cityList).each(function (i, city) {
                            cityMap[city.id] = city;
                        });
                        $("#cityList").html(rendered);
                        bindCityClick();
                    } else {
                        showMessage("加载城市列表", result.msg, false);
                    }
                }
            })
        }

        // 绑定部门点击事件
        function bindCityClick() {

            $(".city-edit").click(function(e) {
                e.preventDefault();
                e.stopPropagation();
                var cityId = $(this).attr("data-id");
                $("#dialog-city-form").dialog({
                    modal: true,
                    title: "编辑城市",
                    open: function(event, ui) {
                        $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                        $("#cityForm")[0].reset();
                        $("#trainCityId").val(cityId);
                        var targetCity = cityMap[cityId];
                        if (targetCity) {
                            $("#name").val(targetCity.name);
                        }
                    },
                    buttons : {
                        "更新": function(e) {
                            e.preventDefault();
                            updateCity(false, function (data) {
                                $("#dialog-city-form").dialog("close");
                            }, function (data) {
                                showMessage("更新城市", data.msg, false);
                            })
                        },
                        "取消": function () {
                            $("#dialog-city-form").dialog("close");
                        }
                    }
                });
            })
        }


        $(".city-add").click(function() {
            $("#dialog-city-form").dialog({
                modal: true,
                title: "新增城市",
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close", $(this).parent()).hide();
                    $("#cityForm")[0].reset();
                },
                buttons : {
                    "添加": function(e) {
                        e.preventDefault();
                        updateCity(true, function (data) {
                            $("#dialog-city-form").dialog("close");
                            loadCityList();
                        }, function (data) {
                            showMessage("新增城市", data.msg, false);
                        })
                    },
                    "取消": function () {
                        $("#dialog-city-form").dialog("close");
                    }
                }
            });
        });

        function updateCity(isCreate, successCallback, failCallback) {
            $.ajax({
                url: isCreate ? "/admin/train/city/save.json" : "/admin/train/city/update.json",
                data: $("#cityForm").serializeArray(),
                type: 'POST',
                success: function(result) {
                    if (result.ret) {
                        loadCityList();
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