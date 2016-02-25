## Basic Module Configuration ##

Each module contains a _module.properties_ which sits in the _resources_ folder, and is also available on the module's class path. In practice, this means placing _module.properties_ in the module's _resources_ folder. This file is important because it is used to describe the module's dependencies as well as other configuration for the module.

However, as we see later on this page, it is also possible to externalize part or all of a module's configuration, which is useful if the module configuration needs to be overridden at deployment time. The easiest way to set up a module is through the file _module.properties_.

### module.properties ###

An example of _module.properties_ is shown below:

```
parent=petclinic
type=servlet
context-locations=petclinic-web-context.xml
```

This example is taken from the web module definition of the Impala Petclinic application.

**_parent_:** The name of the parent module. This property is required for all modules apart from the root module.
Remember that each module typically is backed both by its own class loader, and it's own Spring application context.
The relationship between the child
and the parent typically applies both for the class loader and the application context. This means that classes defined in the parent
module are visible to child modules, but not vice versa. Similarly, beans accessible through the parent's `ApplictionContext` are accessible to
beans in the child's `ApplicationContext`, and not vice versa.

**_type_:** The type of the module. The type of the module governs both the mechanism for loading module definitions, and the mechanism for loading the module, that is
the resources for the module as well as its `ApplicationContext`.

The following types are available out of the box:

|_root:_ | Associated with the root module. This may be backed by the system class or application class loader, or by an Impala custom class loader, depending on the configuration. It will also be backed by a Spring `GenericApplicationContext`.|
|:-------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|_application:_ | The module type used for most other non-web modules. Typically backed by an Impala custom class loader and a Spring `GenericApplicationContext`.                                                                                         |
|_servlet_:| This is Impala's main form of web module. It is backed by a `GenericWebApplicationContext` instance, and by an Impala custom class loader. Web modules can be arranged in a hierarchy. However, there can only be one 'root' web module. This module will also contain web application files, such as the _WEB-INF/web.xml_ as well CSS styles, images, etc.|
|_web\_root_:| Similar to the _servlet_ module. There are two main differences. Firstly, _servlet_ modules follow the convention of having Spring configuration files located as class path resources, while _web\_root_ modules are found as servlet context resources, that is, relative to the `ServletContext` root directory. The first is consistent with Impala's typical usage, while the latter is closer to the convention of vanilla Spring applications, which use the _servlet-name_ plus _servlet_ suffix convention to locate Spring config files.|
|_web\_placeholder_: | This is a special type of module which can be used in some configurations to 'turn off' the functionality that would otherwise be provided by a servlet module. The usage of this type of module is explained in WebConfiguration.       |

**_context-locations_:** A comma-separated list of Spring configuration files which make up the bean definitions for the module. Not required -
if not specified the name _%MODULE\_NAME%-context.xml_ is assumed.

**_depends-on_:** A comma-separated list of modules on which the current module depends. By default, the _parent_ will be included as the first item
on this list. However, this can be overridden by explicitly including the parent module in the list. Note that _depends-on_ only applies when the graph-based class loader is used.
In this case, module classes will have visibility of public classes visible to dependencies.
See [Impala configuration](ImpalaConfiguration.md) for more information.

**_optional-depends-on_**: A comma-separated list of modules on which the current module will depend _if they are present_. In other words, if the optional dependency is present, it can be used as the source for classes, resources and beans used by the current module. However, if not, the application will still start without error.

If using this property, care should be taken to ensure that there are not any _hard_ references to the optional dependency.

**_runtime_**: Specifies the runtime which backs the module type. By default, the runtime type is `spring`.

**_capabilities_**: A simple mechanism to tag modules as implementing a particular set of capabilities. For example, if a module adds cacheing support, an entry such as the following may be present:

```
capabilities=cacheing
```

The capabilities for a particular module configuration can be retrieved using
`ModuleDefinition.getCapabilities()`. Also `org.impalaframework.module.metadata.ModuleMetadataHelper` has a method `getCapabilities()` which can retrieve the capabilities of a full set of modules.

**_attributes_**: Any property not included in the above listing is will be picked up from _module.properties_ and is
accessible using `ModuleDefinition.getAttributes()`. This allows custom module types to pick up custom configuration in arbitrary ways.

The properties listed above are the most commonly used properties. It should be noted that more esoteric module types may use other properties. Any of these additional module properties which doesn't have specific meaning for the built in module loading mechanism is still available via the `ModuleDefinition.getAttributes()` method.

The type of module configuration described in this section is also know as an **internal module configuration**, because all of the information required
to describe the module's position in the module hierarchy is contained within the module.
This is also the simplest and most easily reusable configuration.

With this self-describing style of module configuration, all that remains to set up the application as a whole is to list the names of the modules to be used.

An example is the module configuration for the [[web frameworks sample](SamplesWebframework.md)]:

```
<parent>
	<names>
	#The root module
	webframeworks
	webframeworks-service
	
	#Web modules
	webframeworks-web
	webframeworks-struts
	webframeworks-wicket
	webframeworks-tapestry5
	</names>
</parent>
```

Note that from 1.0.3 the `#This is a comment` syntax can be used for comments as shown in the example above.

The snippet above shows an example of the contents of the _moduledefintions.xml_ file, which used for
setting up the module hierarchy in a web application.

**_reloadable_** (from 1.0.3): By default a module is reloadable, provide a mechanism is provide through the runtime environment to support this
(normally via auto-reloading or via JMX).

For some environments, its useful to be able to turn off reloading for specific modules.
For example, for a large application, we probably don't want to support reloading of the entire module
tree on a live server. However, it might be useful to support reloading specific modules.

To turn off reloading for a module, use the following entry in _module.properties_.

```
reloadable=false
```

Note that if a module is not-reloadable, then any ancestors or dependency modules are not reloadable.
This rule, enforced implicitly and automatically, is in place because any module which reloads needs to be able to reload its descendents.

There is one configuration setting that needs to apply to turn on selective reloading of modules:
the Impala configuration property `enforce.reloadability` needs to be set to true. This occurs by default, but can be turned off.

Typical for development enviroments, selective reloadability of modules would not need to apply - it is a feature that would typically
be more useful in a production environment. For this reason, a typical configuration might have `enforce.reloadability`
set to true only for production.

Note that if any module is not reloadable, then it auto-reloading for that module will automatically be disabled.
Also, attempting to reload the module explicitly via JMX will result in an error being reported to the user.

## External Configuration ##

While the internal configuration is generally pretty convenient, there are times
when you don't wish to reuse the default internal module configuration in its entirety.

Suppose, for example, one of your modules consist of a set of JMX Spring beans which you may wish to use in some
environments but not others. One approach to this problem would be to split these beans into a separate module. However, in some cases this
may seem like overkill, and if followed through, could lead to an explosion in the number of modules.

In plain Spring applications, functionality is often turned on or off by the inclusion or otherwise of Spring application context files. This same approach can
be applied within a module, by externally overriding the _context-locations_ property.

In the example below, we are using the _moduledefinitions.xml_ to add an additional context location to the _example-web_ module. Of course,
the _extra-location.xml_ file needs to be present for this example to work.

```
<root>
	<names>
	example-hibernate
	example-dao
	example-service
	example-web
	example-servlet1
	example-servlet2
	example-servlet3
	example-servlet4
	</names>
	<modules>	
		<module>
			<name>example-web</name>
			<context-locations>
				<context-location>example-web.xml</context-location>
				<context-location>extra-location.xml</context-location>
			</context-locations>
		</module>
	</modules>
</root>
```

For each module type, any of the properties can be overridden this way. This is because each module type-specific definition reader can read
module properties both from simple properties (as in a Java properties file), or from an XML definition document.

## Specifying module definitions globally with `ModuleDefinitionSource` ##

There are essentially two parts to applying an overall module configuration:
  1. how modules should be defined internally (type, context locations, etc.)
  1. which modules should be included as part of the application

In the preceeding sections, we determined that most of the former role can be performed using the _module.properties_ file.
We also saw example of how the modules to be included in an application could simply be listed in an _moduledefinitions.xml_
and that the latter file could also be responsible for all or part of the specifing modules' internal configurations.

The question you may be asking is how does this all fit together, and when are the different mechanisms used?

The key to all of this is the interface `ModuleDefinitionSource`. This interface defines a strategy for loading module definitions.
The module definition hierarchy can be loaded by any implementation of `ModuleDefinitionSource`. Remember, module definitions are metadata.
When you load a module definition, you are not loading the module, just an abstract representation of what the module is supposed to contain.

There are a number of implementations of `ModuleDefintionSource`. Which implementation is best to use depends on the circumstances.

The _moduledefinitions.xml_ is used with `XmlModuleDefinitionSource` as convenient for web applications, because it can be implemented through
simply placing an XML file on the web application class loader's class path (for example, in _WEB-INF\classes_).

For integration tests, it's easier to implement `ModuleDefinitionSource` in code the test directly. Here's an example:

```
public class EntryDAOTest implements ModuleDefinitionSource {

	public static void main(String[] args) {
		InteractiveTestRunner.run(EntryDAOTest.class);
	}
	
	protected void setUp() throws Exception {
		//update the module hierarchy as required
		Impala.init(this);
	}
	
	public void testDAO() {
		EntryDAO dao = Impala.getBean("entryDAO", EntryDAO.class);
		... do a bunch of tests on the DAO
	}

	public RootModuleDefinition getModuleDefinition() {
		return new TestDefinitionSource(new String[]{"example-dao", "example-hibernate"}).getModuleDefinition();
	}
}
```

The our example integration test is using an instance of `TestDefinitionSource`, where the names of modules required
for the test are passed in an array.

## Advanced Configuration ##

It's pretty straightforward to introduce new module types. Here you may need to override the `typeReaders` bean in the Impala Spring configuration, as well as the
`moduleLoaders` property of the `moduleLoaderRegistry` Impala Spring bean.

For more information on how to do this, see ImpalaConfiguration.