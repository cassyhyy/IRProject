<%@ page language="java"  import="java.util.*" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page import="java.net.*"%>


<html lang="en">
<head>
  <title>搜索查询</title>
  <meta charset="UTF-8">
  <!-- 表示根据设备的大小调整页面显示的宽度-->
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <!--  Bootstrap需要jQuery的支持  导入jQuery开发包-->
  <script type="text/javascript" src="./js/jquery.min.js"></script>
  <!-- Bootstrap需要的js包-->
  <script type="text/javascript" src="./js/bootstrap.min.js"></script>
  <!--Bootstrap需要的CSS样式-->
  <link rel="stylesheet" href="./css/bootstrap.min.css">
  <script type="text/javascript" src="./js/index.js"></script>
</head>
<body><br><br><br><br><br>
<!--bootstrap：搜索框 -->
<div class="container">
  <div class="row">
    <div class="col-md-6">
      <div class="input-group">
        <input type="text" class="form-control" placeholder="请输入检索关键字" id="keyword" >
        <span class="input-group-btn">
                    <button class="btn btn-primary" href="javascript:void(0);" onclick="search()">检索</button>
                </span>
      </div>
    </div>
  </div>
</div>
<br><br><br><br>

<!--查询结果-->
<div id="cutline">
  <!--<div class="container" id="search-result">
    <div class="row">
      <div class="col-md-6">
        <table class="table table-striped table-hover">
          <caption class="wordterm">flower</caption>
          <thead>
          <tr>
            <th>所在文件编号</th>
            <th>出现次数</th>
          </tr>
          </thead>
          <tbody>
          </tbody>
        </table>
      </div>
      <br><br>
      <div class="col-md-6">
        <div class="panel panel-primary">
          <div class="panel-heading">
          <h3 class="panel-title">doc1-1</h3>
          </div>
          <div class="panel-body">
              If you miss the train I'm on, you will know that I am gone
          </div>
        </div>
      </div>
    </div>
  </div>
  <hr>!-->
</div>

</body>
</html>