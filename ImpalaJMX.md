# Impala and JMX #

Impala exposes a number of beans via JMX which make it possible to perform various operations, the most significant of these is to reload modules.

## Configuring Impala ##

By default, Impala's JMX MBeans will be exported to a new MBean server. To turn this off you can use the property `expose.jmx.operations=false` in `impala.properties`.

You can configure Impala to attempt to locate an existing MBean server using the property `jmx.locate.existing.server=true`.

## Setting up the JMX client ##

You expose JMX environment via JVM command line switches, as described in http://java.sun.com/j2se/1.5.0/docs/guide/management/agent.html.

Alternatively, if you have [MX4J](http://mx4j.sourceforge.net/) libraries on your class path, you can enable an HTTP-based MX4J application using the property `expose.mx4j.adaptor=true`. You can choose the HTTP port used for this application using the property `jmx.adaptor.port=true`.

See PropertyConfiguration for more details on JMX properties.