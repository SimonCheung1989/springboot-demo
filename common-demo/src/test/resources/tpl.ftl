<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<style type="text/css">
body {
    margin-left: 45px;
    margin-right: 45px;
    font-family: Arial Unicode MS;
    font-size: 10px;
}
 
table {
    margin: auto;
    width: 100%;
    border-collapse: collapse;
    border: 1px solid #444444;
}
 
th,td {
    border: 1px solid #444444;
    font-size: 10px;
    margin-left: 5px;
}
 
.mcContent {
    line-height: 180%;
    padding: 20px;
}
 
.logo {
    text-align: center;
}
 
.title {
    text-align: center;
    font-weight: bold;
    font-size: 20px;
}
 
.notes {
    font-weight: normal;
    margin-left: 5px;
    margin-right: 5px;
    line-height: 18px;
}
 
.text_content {
    margin-left: 5px;
    margin-right: 5px;
    line-height: 18px;
}
 
.sum_insured_first_row {
    width: 20%;
}
 
.sum_insured_span {
    font-size: 10px;
}
 
.special_agreements_div {
    page-break-before: always;
    font-size: 14px;
    margin-top: 20px;
}
 
.special_agreements_div .special_agreements {
    font-size: 18px;
    font-weight: bold;
}
 
.title_right {
    width: 100%;
    margin: 0 auto;
}
 
.title_right p {
    text-align: left;
    margin: 0;
    margin-left: 50%;
    padding: 0;
}
 
@page {
    size: 8.5in 11in;
    @
    bottom-center
    {
    content
    :
    "page "
    counter(
    page
    )
    " of  "
    counter(
    pages
    );
}
 
.signature {
    margin: 0 auto;
    clear: both;
    font-size: 16px;
    font-weight: bold;
}
 
.signature_table {
/*     font-size: 16px; */
    font-weight: bold;
}
 
</style>
</head>
<body>
    作者:<a href="http://www.xdemo.org/">http://www.xdemo.org/</a>
    <div>
        <p>你好：${name}</p>
        <div class="logo"><!--这里的图片使用相对与ITextRenderer.getSharedContext().setBaseURL("file:/"+imageDiskPath);的路径-->
            图片支持<img src="logo1.png" />
        </div>
        <div>
            <p>Hello PDF: 中文支持</p>
            <div style="border:1px solid red;color:red;">
                样式支持,红边框，红字
            </div>
            <div style="border:10px solid blue;color:blue;">
                样式支持,蓝色10像素的边框，蓝字
            </div>
            <hr/>
            <table>
                <tr style="background:gray;">
                    <th>A</th>
                    <th>B</th>
                    <th>C</th>
                    <th>D</th>
                </tr>
                <tr>
                    <td>100</td>
                    <td>29</td>
                    <td>32</td>
                    <td>43</td>
                </tr>
                <tr>
                    <td>100</td>
                    <td>29</td>
                    <td>32</td>
                    <td>43</td>
                </tr>
                <tr>
                    <td>100</td>
                    <td>29</td>
                    <td>32</td>
                    <td>43</td>
                </tr>
                <tr>
                    <td>100</td>
                    <td>29</td>
                    <td>32</td>
                    <td>43</td>
                </tr>
                <tr>
                    <td>100</td>
                    <td>29</td>
                    <td>32</td>
                    <td>43</td>
                </tr>
                <tr>
                <#list nameList as list>
                  <td>${list}</td>
                 </#list>
                </tr>
            </table>
        </div>
    </div>
</body>
</html>
