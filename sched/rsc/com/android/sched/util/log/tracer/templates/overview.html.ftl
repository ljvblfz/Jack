<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>
      Overview
    </title>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load('visualization', '1', {packages: ['table']});
    </script>
    <script type="text/javascript">
      function drawVisualization() {
        // Create and populate the data table.
        var reports = new google.visualization.DataTable();

        reports.addColumn('string',  'Report');
        reports.addColumn('boolean', 'Has Filter');
        reports.addColumn('string',  'Summary');

        reports.addRows([
<#list templates as elt>
<#if elt.summary??>
          [ '${elt.name}' , ${elt.filter}, '<a href="${elt.file}">${elt.summary}</a>' ],
</#if>
</#list>
        ]);

        // Create and populate the data table.
        var props = new google.visualization.DataTable();

        props.addColumn('string', 'Name');
        props.addColumn('string', 'Value');

        props.addRows([
          [ '<div align=right>Date</div>', '${systems.date}' ],
<#if systems.host_name??>
          [ '<div align=right>Host name</div>', '${systems.host_name}' ],
</#if>
          [ '<div align=right>Architecture</div>', '${systems.os_arch}' ],
          [ '<div align=right>Processor count</div>', '${systems.os_proc_nb}' ],
          [ '<div align=right>OS Name</div>', '${systems.os_name}' ],
          [ '<div align=right>OS Version</div>', '${systems.os_version}' ],
          [ '<div align=right>VM Vendor</div>', '${systems.vm_vendor}' ],
          [ '<div align=right>VM Name</div>', '${systems.vm_name}' ],
          [ '<div align=right>VM Version</div>', '${systems.vm_version}' ],
          [ '<div align=right>VM Options</div>', '${systems.vm_options}' ],
          [ '<div align=right>VM Garbage Collectors</div>', '${systems.vm_collectors}' ],
          [ '<div align=right>VM Maximum Memory</div>', '${systems.vm_memory_max}' ],
<#if systems.os_memory_physical??>
          [ '<div align=right>Physical memory</div>', '${systems.os_memory_physical}' ],
</#if>
<#if systems.os_memory_swap??>
          [ '<div align=right>Swap memory</div>', '${systems.os_memory_swap}' ],
 </#if>
 <#if systems.os_memory_committed??>
          [ '<div align=right>Committed memory</div>', '${systems.os_memory_committed}' ]
</#if>
       ]);

<#if descs.data?size != 0>
        // Create and populate the data table.
        var descs = new google.visualization.DataTable();

<#list descs.header?chunk(2) as header>
        descs.addColumn('${header[1]}', '${header[0]}');
</#list>

        descs.addRows([
<#list descs.data as data>
          [
<#list data?chunk(2) as value>
<#if value[0]??>
<#if value[0]?is_string>
<#if value[1]?has_content>
            '<a href="${value[1]}">${value[0]}</a>',
<#else>
            '${value[0]}',
</#if>
<#else>
<#if !value[0]?is_nan && !value[0]?is_infinite>
            {v: ${value[0]}, f: '${value[1]}'},
<#else>
            {v: 0, f: '${value[1]}'},
</#if>
</#if>
</#if>
</#list>
          ],
</#list>
        ]);

        var tableDescs = new google.visualization.Table(document.getElementById('descs'));
        tableDescs.draw(descs, {title: 'Statistics', allowHtml: true});
</#if>

        var tableReports = new google.visualization.Table(document.getElementById('reports'));
        tableReports.draw(reports, {title: 'Probes', allowHtml: true});

        var tableProps = new google.visualization.Table(document.getElementById('props'));
        tableProps.draw(props, {title: 'Properties', allowHtml: true});
      }

      google.setOnLoadCallback(drawVisualization);
    </script>
  </head>
  <body style="font-family: Arial;border: 0 none;">
  <h2>Overview</h2>

    <table cellpadding="5px">
      <tr>
        <td><div align="center"><b>System Properties</b></div></td>
        <td><div align="center"><b>Probes</b></div></td>
      <tr>
        <td valign="top">
          <div id="props"></div>
        </td>
        <td valign="top">
          <div id="reports"></div>
        </td>
      </tr>
      <tr>
        <td valign="top">
          <div align="center"><b><a href="${systems.config}">Configuration Properties</a></b></div>
        </td>
      </tr>
    </table>

<#if descs.data?size != 0>
  <table cellpadding="5px">
    <tr>
      <td><div align="center"><b>Statistics</b></div></td>
    <tr>
      <td valign="top">
        <div id="descs"></div>
      </td>
    </tr>
  </table>
</#if>

  </body>
</html>
