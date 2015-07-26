## Introduction ##

[Port knocking](http://www.portknocking.org/) can be used as an authentication mechanism for the Data Turbine server by _knocking_ on a set of prespecified closed ports. Once a correct sequence of _knocks_ is received by the server, the Data Turbine port can be opened allowing RDV to connect.

## Configuring the Server ##

It is beyond the scope of this document to give instructions on how to configure the server. But the server should be configured to open up the Data Turbine port (by default 3333) for the client after a correct knock sequence. See the [port knocking](http://www.portknocking.org/) website for more information.

## The Knock Sequence ##

RDV accepts the knock sequence as an ordered list of ports. These ports will be knocked before connecting to the Data Turbine server. By default, RDV will wait 100 ms after knocking on each port. The knock sequence is defined on the [command line](CommandLineOptions.md) with the `-k` or `--knock` option.

| **Option** | **Long Option** | **Description** | **Default** |
|:-----------|:----------------|:----------------|:------------|
| -k           | --knock         | A list of ports to knock before connecting to the server | none        |

For example:

> `java -jar rdv.jar --knock 1433 2344 1223 -h example.com`

will knock on ports 1433, 2344, and then 1223 at example.com before connecting to the Data Turbine server. If the port knocking sequence is correct, the Data Turbine port will be opened and the connection will succeed.