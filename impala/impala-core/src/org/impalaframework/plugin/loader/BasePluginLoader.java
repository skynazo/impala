package org.impalaframework.plugin.loader;

import org.impalaframework.plugin.spec.PluginSpec;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;

/**
 * @author Phil Zoio
 */
public abstract class BasePluginLoader implements PluginLoader {
	
	public GenericApplicationContext newApplicationContext(ApplicationContext parent, PluginSpec pluginSpec, ClassLoader classLoader) {
		Assert.notNull(classLoader, "classloader cannot be null");
		
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setBeanClassLoader(classLoader);
		//FIXME add this beanFactory.addBeanPostProcessor(new PluginMetadataPostProcessor(null));

		// create the application context, and set the class loader
		GenericApplicationContext context = new GenericApplicationContext(beanFactory, parent);
		context.setClassLoader(classLoader);
		return context;
	}
	
	public XmlBeanDefinitionReader newBeanDefinitionReader(ConfigurableApplicationContext context, PluginSpec plugin) {
		final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		return new XmlBeanDefinitionReader(PluginUtils.castToBeanDefinitionRegistry(beanFactory));
	}

	public void afterRefresh(ConfigurableApplicationContext context, PluginSpec plugin) {
	}
}
