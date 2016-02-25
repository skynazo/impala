# Overview #

This page describes how to add JSTL support to the [starter application](GettingStartedPart1.md).

# Steps #

1) Download the JSTL jars

If using the Impala ANT build environment, use the _dependency.txt_ entries:

```
runtime from javax.servlet:jstl:1.1.2
runtime from taglibs:standard:1.1.2
```

Use `ant get` to downloads, and modify your Eclipse classpath accordingly.

2) Modify the JSP

Add

```
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
```

to the header of the page. Modify the output as follows:

```
<%=request.getAttribute("message")%>
```

becomes

```
${message}
```

The sample should work as before with these changes.