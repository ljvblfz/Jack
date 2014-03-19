<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>
      ${name})
    </title>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load('visualization', '1', {packages: ['corechart', 'table']});
    </script>
    <script type="text/javascript">
      function drawVisualization() {

<#assign idx = 0>
<#list tables as table>
<#if table.data?size != 0>
        // Create and populate the data table.
        var data_${idx} = new google.visualization.DataTable();

<#list table.header?chunk(2) as header>
        data_${idx}.addColumn('${header[1]}', '${header[0]}');
</#list>

        data_${idx}.addRows([
<#list table.data as data>
          [
<#list data?chunk(2) as value>
<#if value[0]?is_string>
<#if value[1]?has_content>
            '<a href="${value[1]}">${value[0]}</a>',
<#else>
            '${value[0]}',
</#if>
<#else>
            {v: ${value[0]}, f: '${value[1]}'},
</#if>
</#list>
          ],
</#list>
        ]);

        var table_${idx} = new google.visualization.Table(document.getElementById('id_${idx}'));
        table_${idx}.draw(data_${idx},  {allowHtml: true});
</#if>
<#assign idx = idx + 1>
</#list>

      }

      google.setOnLoadCallback(drawVisualization);
    </script>
  </head>
  <body style="font-family: Arial;border: 0 none;">
  <h2>${name}</h2>

<#assign idx = 0>
<#list tables as table>
<#if table.data?size != 0>
  <h3>${table.name}</h3>

  <table cellpadding="5px">
    <tr>
      <td valign="top" rowspan="2">
        <div id="id_${idx}"></div>
      </td>
    </tr>
  </table>

</#if>
<#assign idx = idx + 1>
</#list>
  </body>
</html>
