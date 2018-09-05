/*window.onload = function(){
	console.log("init...");
	//��ʼ��ҳ��ʱ��Ҳ�����������ʼ��
    $.ajax({
        url: "./InitServlet",
        type: "post",
        dataType: "text",
        data: {status:"init"},
		success:function(data){
        	console.log(data);
		}
    });
};*/

function search(){
    var keyValue = $('#keyword').val();
    if (keyValue == '') {
        document.getElementById('keyword').placeholder = "请输入内容再进行搜索！";
        //document.getElementById('search-result').style.visibility = 'hidden';
        //document.getElementById('document-content').style.visibility = 'hidden';
    }else {
        //document.getElementById('search-result').style.visibility = 'visible';
        //document.getElementById('document-content').style.visibility = 'visible';

        //var item =[{"term":"2","result":[{"doc_id":"1","frequency":"1"},{"doc_id":"9","frequency":"1"}],"doc":[{"doc_id":"1-2","content":"oh my god"},{"doc_id":"9-1","content":"you bad bad"}]}];
        $.ajax({
            url: "./SearchServlet",
            type: "post",
            dataType: "json",
            data: {keyword: keyValue},
            success: function (data) {
                $("#cutline").empty();
                var str;
                console.log("接收数据："+data.toString());
                /*if(data == null){
                    $("#cultline").append('<h3>无搜索结果</h3>');
                }else{*/
                    $.each(data, function () {
                        var h1 = this;

                        str = '<div class="container" id="search-result">'+'<div class="row"><div class="col-md-6">'
                            +'<table class="table table-striped table-hover"><caption class="wordterm">'+h1.term+'</caption>'
                            +'<thead><tr><th>所在文件编号</th><th>出现次数</th></tr></thead><tbody>';

                        $.each(h1.map, function () {
                            str += "<tr><td>" + this.doc_id + "</td><td>" + this.frequency + "</td></tr>";
                        });

                        str+='</tbody></table></div><br><br><div class="col-md-6">';


                        $.each(h1.result,function(){
                            str+='<div class="panel panel-primary"><div class="panel-heading"><h3 class="panel-title">';
                            str+=this.doc_id;
                            str += '</h3></div><div class="panel-body">'+this.content+'</div></div>';
                        });

                        str+='</div></div></div><hr>';
                        $("#cutline").append(str);
                    });
                /*$.each(h1.result, function(){
                    var h2 = this;
                    $(".panel").append('<div class="panel-heading">\
                        <h3 class="panel-title">' + h2.doc_id + '</h3>\
                    </div>\
                    <div class="panel-body">' + h2.content+ '\
                    </div>')
                });*/
            }
        });
    }
}

