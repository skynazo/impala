package net.java.impala.spring.plugin;

import java.util.Arrays;

import junit.framework.TestCase;
import net.java.impala.location.PropertyClassLocationResolver;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlBeansetApplicationPluginLoaderTest extends TestCase {

	private static final String plugin4 = "impala-sample-dynamic-plugin4";

	private ConfigurableApplicationContext parent;

	private ConfigurableApplicationContext child;

	public final void testInitialPluginSpec() {
		BeansetPluginSpec pluginSpec = new SimpleBeansetPluginSpec(plugin4);
		loadChild(pluginSpec);
		System.out.println(Arrays.toString(child.getBeanDefinitionNames()));
		assertTrue(child.containsBean("bean1"));
		assertTrue(child.containsBean("importedBean1"));
		assertTrue(child.containsBean("importedBean2"));
	}
	
	public final void testModifiedPluginSpec() {
		BeansetPluginSpec pluginSpec = new SimpleBeansetPluginSpec(plugin4, "alternative: myImports");
		loadChild(pluginSpec);
		System.out.println(Arrays.toString(child.getBeanDefinitionNames()));
		assertTrue(child.containsBean("bean1"));
		assertTrue(child.containsBean("importedBean1"));
		assertFalse(child.containsBean("importedBean2"));
	}

	private void loadChild(BeansetPluginSpec pluginSpec) {
		PropertyClassLocationResolver locationResolver = new PropertyClassLocationResolver();
		parent = new ClassPathXmlApplicationContext("parentTestContext.xml");
		XmlBeansetApplicationPluginLoader pluginLoader = new XmlBeansetApplicationPluginLoader(locationResolver);
		ClassLoader classLoader = pluginLoader.newClassLoader(new ApplicationContextSet(),
						pluginSpec, parent);
		child = pluginLoader.newApplicationContext(parent, classLoader);
		XmlBeanDefinitionReader xmlReader = pluginLoader.newBeanDefinitionReader(child, pluginSpec);
		xmlReader.setBeanClassLoader(classLoader);
		xmlReader.loadBeanDefinitions(pluginLoader.getSpringConfigResources(new ApplicationContextSet(), pluginSpec, classLoader));
		child.refresh();
	}

	public void tearDown() {
		try {
			child.close();
			parent.close();
		} catch (Exception e) {			
		}
	}
}
