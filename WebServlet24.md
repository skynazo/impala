# Overview #

When creating an initial starter Impala application, from Impala 1.0.1 an application using the 2.5 Servlet API will be set up to start with.

If you need to create a 2.4 application, for example, to run on a container without 2.5 support, you will need to make the following changes.

# Steps #

1)
Change the header of your _WEB-INF/web.xml_ as follows:

```
<web-app xmlns="http://java.sun.com/xml/ns/j2ee"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
          version="2.4">
```

2)
Change the servlet and JSP versions. In the host project's dependencies.txt

```
provided from javax.servlet:servlet-api:2.5
provided from javax.servlet.jsp:jsp-api:2.1
```

becomes

```
provided from javax.servlet:servlet-api:2.4
provided from javax.servlet.jsp:jsp-api:2.0
```

3)
If you are using JSP support, change the version of Jasper used:

```
provided from org.apache.tomcat:jasper:6.0.20
provided from org.apache.tomcat:jasper-el:6.0.20
provided from org.apache.tomcat:jasper-jdt:6.0.20
provided from org.apache.tomcat:juli:6.0.20
```

becomes

```
provided from tomcat:jasper-runtime:5.5.15
provided from tomcat:jasper-compiler:5.5.15
provided from tomcat:jasper-compiler-jdt:5.5.15
```

(You may wish to change the versions slightly, if appropriate).

4)
From the build project, run

```
ant get
```

to download the dependencies. Then remove the unwanted jar files from the local repository folder, and update the classpath. The last two steps are manual tasks.