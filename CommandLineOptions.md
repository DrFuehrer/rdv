RDV accepts the following command line options:

| **Option** | **Long Option**   | **Description** | **Default** |
|:-----------|:------------------|:----------------|:------------|
| -h         | --host            | The host name of the Data Turbine server | localhost   |
| -p         | --port            | The port number of the Data Turbine server | 3333        |
| -k         | --knock           | A list of ports to knock before connecting to the server | none        |
| -r         | --playback-rate   | The playback rate | 1           |
| -s         | --time-scale      | The time scale in seconds | 1           |
| -c         | --channels        | A list of channels to subscribe to | none        |
|            | --play            | Start playing back data |             |
|            | --real-time       | Start viewing data in real time |             |
| -?         | --help            | Display usage   |             |

To use these options, run RDV from the command line:

> `java -jar rdv.jar [-h host name] [-p port number] [-k ports] [-r playback rate] [-s time scale] [-c channels] [--play] [--real-time] [-?]`

Additionally a RDV configuration file may be passed in via the command line:

> `java -jar rdv.jar my-rdv-setup.rdv`

The configuration file can also be a URL:

> `java -jar rdv.jar http://example.com/my-rdv-setup.rdv`

When a configuration file is passed on the command line, all other options are igorned.