## Introduction ##

RDV can be launched from any website via [Java Web Start](http://java.sun.com/products/javawebstart/).

## Creating a JNLP file ##

To do this, a [JNLP file](http://code.google.com/p/rdv/source/browse/trunk/jnlp/RDV.jnlp) needs to be created like the one below, and put it in a directory on your web server along with the files [rdv.jar](http://code.google.com/p/rdv/downloads/list) and [rdv.png](http://rdv.googlecode.com/svn/wiki/rdv.png). Be sure to change the `codebase` attribute to the URL where you put the web start files. The JNLP file should have the .jnlp extension and your web server should be setup to serve the file with the `application/x-java-jnlp-file` MIME type.

```
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+"
  codebase="http://example.com/rdv/"
  href="rdv.jnlp">
	<information>
		<title>RDV</title>
		<vendor>Palta Software</vendor>
		<homepage href="http://rdv.googlecode.com/"/>
		<description>A visualization environment for scientific and engineering data.</description>
		<icon href="rdv.png"/>
		<offline-allowed/>
	</information>
	<security>
		<all-permissions/>
	</security>
	<resources>
		<j2se version="1.5+" initial-heap-size="64m" max-heap-size="256m"/>
		<jar href="rdv.jar" main="true"/>
	</resources>
	<application-desc>
		<property name="org.apache.commons.logging.Log" value="org.apache.commons.logging.impl.NoOpLog"/>
	</application-desc>
</jnlp>
```

[Command line options](CommandLineOptions.md) can be passed by specifying multiple `argument` elements under the `application-desc` element. For example, to set the host:

```
<argument>--host</argument>
<argument>example.com</argument> 
```

or to specify a configuration file:

```
<argument>http://example.com/my-rdv-setup.rdv</argument>
```

## Launching RDV ##

When the user clicks a link to the JNLP file, java should open the JNLP file are start downloading RDV. If the user doesn't have Java version 1.5 or greater, they will be asked to upgrade. Once RDV is downloaded and the correct version of Java is available, RDV will run. Note that the user must have at least some version of Java installed for this to work, otherwise they will be prompted with a dialog asking them what to do with the JNLP file.

The latest version of RDV can always be launched via web start at http://rdv.googlecode.com/svn/wiki/rdv.jnlp. Give it a try below.

### [![](http://rdv.googlecode.com/svn/wiki/rdv-logo-small.png)](http://rdv.googlecode.com/svn/wiki/rdv.jnlp) [Run RDV ](http://rdv.googlecode.com/svn/wiki/rdv.jnlp) - _Requires [Java](http://www.java.com/getjava/)_ ###