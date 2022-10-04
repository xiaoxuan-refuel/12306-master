<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>12306前台</title>
    <jsp:include page="/common/frontend_common.jsp"/>
<body class="no-skin" youdao="bind" style="background: white">
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>

<div>
    <div class="navbar-container ace-save-state" id="navbar-container">

        <div class="pull-left">
            <a href="#" class="navbar-brand">
                12306车次查询
            </a>
        </div>
        <div class="pull-right">
            <a href="#" class="login navbar-brand" style="display:none;">
                Mock登陆
            </a>
            <a href="#" class="profile navbar-brand" style="display:none;">
            </a>
            <a href="#" class="logout navbar-brand" style="display:none;">注销</a>
        </div>
        <div class="main-content-inner">
            <div class="col-sm-12">
                <div class="col-xs-12">
                    <div>
                        <div id="dynamic-table_wrapper" class="dataTables_wrapper form-inline no-footer">
                            <div class="col-xs-12">
                                <div class="dataTables_length" id="dynamic-table_length">
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    出发站点<select id="search-start" name="start" style="width: 100px;"> </select>
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    到达站点<select id="search-end" name="end" style="width: 100px;"></select>
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    出发日<input id="search-date" type="text" name="end" class="form-control input-sm"
                                              placeholder="yyyyMMdd，必填" aria-controls="dynamic-table">
                                    &nbsp;&nbsp;&nbsp;&nbsp;
                                    <button class="btn btn-info fa research" type="button">
                                        查询
                                    </button>
                                </div>
                            </div>
                            <table id="dynamic-table"
                                   class="table table-striped table-bordered table-hover dataTable no-footer"
                                   role="grid"
                                   aria-describedby="dynamic-table_info" style="font-size:14px">
                                <thead>
                                <tr role="row">
                                    <th tabindex="1" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        车次
                                    </th>
                                    <th tabindex="2" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        剩余座位数
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
        <div class="contactUsers" style="display:none;">
            <a href="#" class="navbar-brand">
                选择乘车人
            </a>
            <div class="col-sm-12">
                <div class="col-xs-12">
                    <div>
                        <div id="dynamic-table_wrapper2" class="dataTables_wrapper form-inline no-footer">
                            <table id="dynamic-table2"
                                   class="table table-striped table-bordered table-hover dataTable no-footer"
                                   role="grid"
                                   aria-describedby="dynamic-table_info" style="font-size:14px">
                                <thead>
                                <tr role="row">
                                    <th tabindex="1" aria-controls="dynamic-table" rowspan="1" colspan="1">

                                    </th>
                                    <th tabindex="2" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        姓名
                                    </th>
                                    <th tabindex="3" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        成人/儿童
                                    </th>
                                    <th tabindex="4" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        证件号
                                    </th>
                                </tr>
                                </thead>
                                <tbody id="travellerList"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="myOrders" style="display:none;">
            <a href="#" class="navbar-brand">
                我的订单
            </a>
            <div class="col-sm-12">
                <div class="col-xs-12">
                    <div>
                        <div id="dynamic-table_wrapper3" class="dataTables_wrapper form-inline no-footer">
                            <table id="dynamic-table3"
                                   class="table table-striped table-bordered table-hover dataTable no-footer"
                                   role="grid"
                                   aria-describedby="dynamic-table_info" style="font-size:14px">
                                <thead>
                                <tr role="row">
                                    <th tabindex="1" aria-controls="dynamic-table" rowspan="1" colspan="1">
                                        日期
                                    </th>
                                    <th tabindex="3" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        出发站
                                    </th>
                                    <th tabindex="4" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        到达站
                                    </th>
                                    <th tabindex="5" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        座位信息
                                    </th>
                                    <th tabindex="6" aria-controls="dynamic-table" rowspan="5" colspan="1">
                                        状态
                                    </th>
                                    <th class="sorting_disabled" rowspan="1" colspan="1" aria-label="">
                                        操作
                                    </th>
                                </tr>
                                </thead>
                                <tbody id="orderList"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div><!-- /.navbar-container -->
</div>

<script id="numberListTemplate" type="x-tmpl-mustache">
{{#numberList}}
<tr role="row" class="number-name odd" data-id="{{number}}">
    <td>{{number}}</td>
    <td>{{leftCount}}</td>
    <td>
        <div class="hidden-sm hidden-xs action-buttons">
            <a class="green grab_ticket" href="#" data-id="{{number}}">
                抢票
            </a>
        </div>
    </td>
</tr>
{{/numberList}}
</script>

<script id="travellerListTemplate" type="x-tmpl-mustache">
{{#travellerList}}
<tr role="row" class="traveller-name odd" data-id="{{id}}">
    <td><input type="checkbox" value="{{id}}" name="travellerId"></input></td>
    <td>{{name}}</td>
    <td>{{showAdultFlag}}</td>
    <td>{{idNumber}}</td>
</tr>
{{/travellerList}}
</script>

<script id="orderListTemplate" type="x-tmpl-mustache">
{{#orderList}}
<tr role="row" class="order-detail odd" data-id="{{trainOrder.orderId}}">
    <td>{{trainOrder.ticket}}</td>
    <td>{{fromStationName}}</td>
    <td>{{toStationName}}</td>
    <td>{{seatInfo}}</td>
    <td>{{showStatus}}</td>
    <td>
         <div class="hidden-sm hidden-xs action-buttons">
         {{#showPay}}
            <a class="green pay_order" href="#" data-id="{{trainOrder.orderId}}">
                立即支付
            </a>
         {{/showPay}}
         {{#showCancel}}
            <a class="refund_order" href="#" data-id="{{trainOrder.orderId}}">
                退款
            </a>
         {{/showCancel}}
        </div>
    </td>
</tr>
{{/orderList}}
</script>

<script type="application/javascript">
    $(function () {
        var numberListTemplate = $('#numberListTemplate').html();
        Mustache.parse(numberListTemplate);

        var travellerListTemplate = $('#travellerListTemplate').html();
        Mustache.parse(travellerListTemplate);

        var orderListTemplate = $('#orderListTemplate').html();
        Mustache.parse(orderListTemplate);

        var isLogin = false;
        var hasResult = false;

        $.ajax({
            url: '/stationList.json',
            type: 'GET',
            success: function (result) {
                if (result.ret) {
                    var optionStr = '<option value="0"> </option>';
                    $(result.data).each(function (i, station) {
                        optionStr += Mustache.render("<option value='{{id}}'>{{name}}</option>", {
                            id: station.id,
                            name: station.name
                        });
                    });
                    $("#search-start").html(optionStr);
                    $("#search-end").html(optionStr);
                } else {
                    showMessage("获取车站信息", result.msg, false);
                }
            }
        });

        function checkLogin() {
            $.ajax({
                url: '/info.json',
                type: 'GET',
                success: function (result) {
                    if (result.ret && result.data != null) {
                        bindLogin(result.data.name);
                    } else if (result.code == 2 || result.data == null) { // 未登录
                        bindLogout();
                    } else {
                        isLogin = false;
                        showMessage("加载信息", result.msg, false);
                    }
                }
            });
        }

        checkLogin();

        $(".logout").click(function (e) {
            e.preventDefault();
            $.ajax({
                url: '/logout.json',
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        bindLogout();
                    } else {
                        showMessage("注销", result.msg, false);
                    }
                }
            });
        });

        $(".login").click(function (e) {
            e.preventDefault();
            $.ajax({
                url: '/mockLogin.json',
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        checkLogin();
                    } else {
                        showMessage("登陆", result.msg, false);
                    }
                }
            });
        });

        $(".profile").click(function (e) {
            e.preventDefault();
            console.log("profile click");
        });

        // login成功后的操作
        function bindLogin(name) {
            $(".profile").html("欢迎, " + name).show();
            $(".logout").show();
            $(".login").hide();
            $(".myOrders").show();
            isLogin = true;
            if (hasResult) {
                showContactDetail();
            }
            getOrders();
        }

        // logout之后及未登录的的操作
        function bindLogout() {
            $(".profile").hide();
            $(".logout").hide();
            $(".login").show();
            $(".myOrders").hide();
            isLogin = false;
            $(".contactUsers").hide();
        }

        $(".research").click(function (e) {
            e.preventDefault();
            var start = $("#search-start").val();
            var end = $("#search-end").val();
            var date = $("#search-date").val();
            if (start <= 0) {
                alert("请选择出发地");
                return;
            }
            if (end <= 0) {
                alert("请选择到达地");
                return;
            }
            if (date == '' || date.length != 8) {
                alert("请输入yyyyMMdd格式的日期");
                return;
            }
            $.ajax({
                url: '/front/searchLeftCount.json',
                data: {
                    fromStationId: start,
                    toStationId: end,
                    date: date
                },
                type: 'POST',
                success: function (result) {
                    if (result.ret) {
                        if(result.data && result.data.length > 0) {
                            hasResult = true;
                            var rendered = Mustache.render(numberListTemplate, {
                                numberList: result.data
                            });
                            $("#numberList").html(rendered);
                            bindClick();
                            if (isLogin) {
                                showContactDetail();
                            }
                        } else {
                            hasResult = false;
                            showMessage("查询车票", "未查询到相关的车次", false);
                            $(".contactUsers").hide();
                        }
                    } else {
                        showMessage("查询车票", result.msg, false);
                    }
                }
            });
        });

        function showContactDetail() {
            $(".contactUsers").show();
            $.ajax({
                url: '/user/getTravellers.json',
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        var rendered = Mustache.render(travellerListTemplate, {
                            travellerList: result.data,
                            "showAdultFlag": function() {
                                return this.adultFlag == 1 ? "儿童" : "成人";
                            }
                        });
                        $("#travellerList").html(rendered);
                    } else {
                        showMessage("获取常用联系人", result.msg, false);
                    }
                }
            })
        }

        function bindClick() {
            $(".grab_ticket").click(function (e) {
                e.preventDefault();
                var number = $(this).attr("data-id");
                var start = $("#search-start").val();
                var end = $("#search-end").val();
                var date = $("#search-date").val();
                var travellerIdArray = [];
                $("#travellerList").find(":checked").each(function(i, obj) {
                    travellerIdArray.push($(obj).val());
                });
                if (travellerIdArray.length == 0) {
                    if (!isLogin) {
                        alert("请登录并选定乘车人");
                    } else {
                        alert("请选择乘车人");
                    }
                    return;
                }
                if (start <= 0) {
                    alert("请选择出发地");
                    return;
                }
                if (end <= 0) {
                    alert("请选择到达地");
                    return;
                }
                if (date == '' || date.length != 8) {
                    alert("请输入yyyyMMdd格式的日期");
                    return;
                }
                $.ajax({
                    url: '/front/grab.json',
                    data: {
                        fromStationId: start,
                        toStationId: end,
                        date: date,
                        number: number,
                        travellerIds: travellerIdArray.join(",")
                    },
                    type: 'POST',
                    success: function (result) {
                        if (result.ret) {
                            showMessage("预约车票成功", "请在半小时内完成支付，车票数:" + result.data.trainOrderDetailList.length, false);
                            getOrders();
                        } else {
                            showMessage("预约车票", result.msg, false);
                        }
                    }
                })
            })
        }

        function getOrders() {
            $.ajax({
                url: '/user/getOrderList.json',
                type: 'GET',
                success: function (result) {
                    if (result.ret) {
                        var rendered = Mustache.render(orderListTemplate, {
                            orderList: result.data,
                            "showStatus":function () {
                                return showOrderStatus(this.trainOrder.status);
                            }
                        });
                        $("#orderList").html(rendered);
                        bindOrderClick();
                    } else {
                        showMessage("获取订单", result.msg, false);
                    }
                }
            })
        }

        function bindOrderClick() {
            $(".pay_order").click(function (e) {
                e.preventDefault();
                var orderId = $(this).attr("data-id");
                $.ajax({
                    url: '/front/mockPay.json',
                    data: {
                        orderId: orderId
                    },
                    type: 'POST',
                    success: function (result) {
                        if (result.ret) {
                            getOrders();
                        } else {
                            showMessage("支付订单", result.msg, false);
                        }
                    }
                })
            });
            $(".refund_order").click(function(e) {
                e.preventDefault();
                var orderId = $(this).attr("data-id");
                $.ajax({
                    url: '/front/mockCancel.json',
                    data: {
                        orderId: orderId
                    },
                    type: 'POST',
                    success: function (result) {
                        if (result.ret) {
                            getOrders();
                        } else {
                            showMessage("取消订单", result.msg, false);
                        }
                    }
                })
            })
        }

        function showOrderStatus(status) {
            if (status == 10) {
                return "等待支付";
            } else if (status == 20) {
                return "已支付";
            } else if (status == 30) {
                return "超时未支付自动取消";
            } else if (status == 40) {
                return "支付后取消";
            } else {
                return "未知状态";
            }
        }
    })
</script>