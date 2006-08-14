-libraryjars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/jsse.jar

-dontoptimize
-dontwarn

# Application
-keepclasseswithmembers public class org.nees.buffalo.rdv.DataViewer {
    public static void main(java.lang.String[]);
}

# Data panel extensions
-keep class * extends org.nees.buffalo.rdv.datapanel.DataPanel

# Commons logging classes
-keep class org.apache.commons.logging.Log
-keep class org.apache.commons.logging.impl.LogFactoryImpl
-keep class org.apache.commons.logging.impl.Jdk14Logger {
	<init>(java.lang.String);
}

# Xerces classes
-keep class org.apache.xerces.parsers.DOMParser
-keep class org.apache.xerces.parsers.XIncludeAwareParserConfiguration
-keep class org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl
-keep class org.apache.xerces.jaxp.DocumentBuilderFactoryImpl

# Swing L&F
-keep class * extends javax.swing.plaf.ComponentUI {
    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent);
}

# JFreeChart and JCommon resources
-keep class org.jfree.chart.resources.JFreeChartResources
-keep class org.jfree.resources.JCommonResources

# Keep JGoodies Looks
-keep class com.jgoodies.looks.**