<#include "../general/common.ftl">
<#include "../general/summary_table.ftl">
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <title>DATALAND</title>
    <style>
        body {
            background-color: #DADADA;
            margin: 0;
            padding: 0;
        }
        .container {
            background-color: #ffffff;
            width: 520px;
            padding-left: 40px;
            padding-right: 40px;
        }
        table {
            background-color: #ffffff;
            width: 520px;
            font-family: Arial, Helvetica, sans-serif;
            font-size: 14px;
            text-align: left;
            border-collapse: collapse;
            margin: 0;
            padding: 0;
        }
    </style>
</head>
<body>
    <#include "../general/header.ftl">
    <div class="container">
        <table>
            <tbody>
            <@spacerRow />
            <tr>
                <td colspan="3">Weekly Summary 📣<br><br>Data for your request(s) has been updated on Dataland this week.
                    <br>Please note that you may have already reviewed these updates.</td>
            </tr>
            <@spacerRow />
            <!-- NEW DATA -->
            <@renderBoldTitle title="New Data" />
            <@spacerRow />
            <tr>
                <td colspan="3"> <@renderTable data=newData /> </td>
            </tr>
            <@spacerRow />
            <@renderStyledLink url="${baseUrl}/requests" linkText="VIEW NEW DATA ON MY DATA REQUESTS" />
            <@spacerRow />
            <@spacerRow />
            <!-- UPDATED DATA -->
            <@renderBoldTitle title="Updated Data" />
            <@spacerRow />
            <tr>
                <td colspan="3"> <@renderTable data=updatedData /> </td>
            </tr>
            <@spacerRow />
            <@renderStyledLink url="${baseUrl}/requests" linkText="VIEW UPDATED DATA ON MY DATA REQUESTS" />
            <@spacerRow />
            <@spacerRow />
            <!-- NON SOURCEABLE DATA -->
            <@renderBoldTitle title="Non sourceable Data" />
            <@spacerRow />
            <tr>
                <td colspan="3"> <@renderTable data=nonSourceableData /> </td>
            </tr>
            <@spacerRow />
            <@renderStyledLink url="${baseUrl}/requests" linkText="VIEW NON SOURCEABLE DATA ON MY DATA REQUESTS" />
            <@spacerRow />
            <@spacerRow />
            </tbody>
        </table>
    </div>
    <#include "../general/footer.ftl">
    <#include "../general/unsubscribe.ftl">
</body>
</html>
