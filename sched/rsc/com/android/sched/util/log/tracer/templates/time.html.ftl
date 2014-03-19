<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>
      Cumulative time of Events (${probes.time.name})
    </title>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load('visualization', '1', {packages: ['corechart', 'table']});
    </script>
    <script type="text/javascript">
      function drawVisualization() {
        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addColumn('string', 'Event Name');
        data.addColumn('string', 'Event Name');
        data.addColumn('number', 'W/o Children');
        data.addColumn('number', 'W/ Children');

        data.addRows([
<#list stats as elt>
<#if elt.v_time_without??>
          [ '<a href="${elt.file}">${elt.name}</a>', '${elt.name}',
            {v: ${elt.v_time_without}, f: '${elt.f_time_without}'},
            {v: ${elt.v_time_with}, f: '${elt.f_time_with}'}
          ],
</#if>
</#list>
        ]);

        var dataViewWo = new google.visualization.DataView(data);
        dataViewWo.setColumns([1, 2]);
        var dataWo = dataViewWo.toDataTable();
        dataWo.sort([{column: 1, desc: false}]);

        var pieWo = new google.visualization.PieChart(document.getElementById('pieWo'));
        pieWo.draw(dataWo, {title: "W/o Children"});

        var dataViewW = new google.visualization.DataView(data);
        dataViewW.setColumns([1, 3]);
        var dataW = dataViewW.toDataTable();
        dataW.sort([{column: 1, desc: false}]);

        var pieW = new google.visualization.PieChart(document.getElementById('pieW'));
        pieW.draw(dataW, {title: "W/ Children"});

        var dataView = new google.visualization.DataView(data);
        dataView.setColumns([0, 2, 3]);

        var table = new google.visualization.Table(document.getElementById('table'));
        table.draw(dataView.toDataTable(), {allowHtml: true});
      }

      google.setOnLoadCallback(drawVisualization);
    </script>
  </head>
  <body style="font-family: Arial;border: 0 none;">
  <h2>Cumulative time of Events (${probes.time.name})</h2>
  <h4>Summary: ${probes.time.f_Total}</h4>
<#if probes.time.filter??>
  <h4>Filter: ${probes.time.filter}</h4>
</#if>

  <table cellpadding="5px">
      <tr>
        <td valign="top" rowspan="2">
          <div id="table"></div>
        </td>
        <td valign="top">
          <div id="pieWo" style="width: 600px; height: 400px;"></div>
        </td>
      </tr>
      <tr>
        <td valign="top">
          <div id="pieW" style="width: 600px; height: 400px;"></div>
        </td>
      </tr>
    </table>
  </body>
</html>
