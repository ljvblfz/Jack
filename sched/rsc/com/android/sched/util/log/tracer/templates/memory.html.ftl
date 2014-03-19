<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>
      Cumulative memory usage of Events (${probes.memory.name})
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
        data.addColumn('number', 'W/o Children');
        data.addColumn('number', 'W/ Children');

        data.addRows([
<#list stats as elt>
<#if elt.v_memory_without??>
          [ '<a href="${elt.file}">${elt.name}</a>',
            {v: ${elt.v_memory_without}, f: '${elt.f_memory_without}'},
            {v: ${elt.v_memory_with}, f: '${elt.f_memory_with}'}
          ],
</#if>
</#list>
        ]);

        var dataPosWo = new google.visualization.DataTable();

        dataPosWo.addColumn('string', 'Event Name');
        dataPosWo.addColumn('number', 'Allocation w/o Children');

        dataPosWo.addRows([
<#list stats as elt>
<#if elt.v_memory_without??>
<#if (elt.v_memory_without > 0)>
          [ '${elt.name}',
            {v: ${elt.v_memory_without}, f: '${elt.f_memory_without}'},
          ],
</#if>
</#if>
</#list>
        ]);
        dataPosWo.sort([{column: 1, desc: false}]);

        var dataNegWo = new google.visualization.DataTable();

        dataNegWo.addColumn('string', 'Event Name');
        dataNegWo.addColumn('number', 'Deallocation w/o Children');

        dataNegWo.addRows([
<#list stats as elt>
<#if elt.v_memory_without??>
<#if (elt.v_memory_without < 0)>
          [ '${elt.name}',
            {v: ${elt.v_memory_without * -1}, f: '${elt.f_memory_without}'},
          ],
</#if>
</#if>
</#list>
        ]);
        dataNegWo.sort([{column: 1, desc: false}]);

        var dataPosW = new google.visualization.DataTable();

        dataPosW.addColumn('string', 'Event Name');
        dataPosW.addColumn('number', 'Allocation w/ Children');

        dataPosW.addRows([
<#list stats as elt>
<#if elt.v_memory_with??>
<#if (elt.v_memory_with > 0)>
          [ '${elt.name}',
            {v: ${elt.v_memory_with}, f: '${elt.f_memory_with}'},
          ],
</#if>
</#if>
</#list>
        ]);
        dataPosW.sort([{column: 1, desc: false}]);

        var dataNegW = new google.visualization.DataTable();

        dataNegW.addColumn('string', 'Event Name');
        dataNegW.addColumn('number', 'Deallocation w/ Children');

        dataNegW.addRows([
<#list stats as elt>
<#if elt.v_memory_with??>
<#if (elt.v_memory_with < 0)>
          [ '${elt.name}',
            {v: ${elt.v_memory_with * -1}, f: '${elt.f_memory_with}'},
          ],
</#if>
</#if>
</#list>
        ]);
        dataNegW.sort([{column: 1, desc: false}]);

        var chartPosWo = new google.visualization.PieChart(document.getElementById('chartPosWo'));
        chartPosWo.draw(dataPosWo, {title: "Allocation w/o Children"});

        var chartNegWo = new google.visualization.PieChart(document.getElementById('chartNegWo'));
        chartNegWo.draw(dataNegWo, {title: "Deallocation w/o Children"});

        var chartPosW = new google.visualization.PieChart(document.getElementById('chartPosW'));
        chartPosW.draw(dataPosW, {title: "Allocation w/ Children"});

        var chartNegW = new google.visualization.PieChart(document.getElementById('chartNegW'));
        chartNegW.draw(dataNegW, {title: "Deallocation w/ Children"});

        var table = new google.visualization.Table(document.getElementById('table'));
        table.draw(data, {allowHtml: true});
      }

      google.setOnLoadCallback(drawVisualization);
    </script>
  </head>
  <body style="font-family: Arial;border: 0 none;">
  <h2>Cumulative memory usage of Events (${probes.memory.name})</h2>
  <h4>Total memory: ${probes.memory.f_Total}</h4>
<#if probes.memory.filter??>
  <h4>Filter: ${probes.memory.filter}</h4>
</#if>

  <table  cellpadding="5px">
      <tr>
        <td valign="top" rowspan="2">
          <div id="table"></div>
        </td>
        <td valign="top">
          <div id="chartPosWo" style="width: 600px; height: 400px;"></div>
        </td>
        <td valign="top">
          <div id="chartNegWo" style="width: 600px; height: 400px;"></div>
        </td>
      </tr>
      <tr>
        <td valign="top">
          <div id="chartPosW" style="width: 600px; height: 400px;"></div>
        </td>
        <td valign="top">
          <div id="chartNegW" style="width: 600px; height: 400px;"></div>
        </td>
      </tr>
    </table>
  </body>
</html>
