package net.java.impala.spring.web;

import java.io.File;

import javax.servlet.ServletContext;

import net.java.impala.classloader.ParentClassLoader;
import net.java.impala.location.ClassLocationResolver;
import net.java.impala.spring.plugin.ApplicationContextSet;
import net.java.impala.spring.plugin.BasePluginLoader;
import net.java.impala.spring.plugin.PluginLoader;
import net.java.impala.spring.plugin.PluginSpec;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class WebPluginLoader extends BasePluginLoader implements PluginLoader {

	private ServletContext servletContext;
	private ClassLocationResolver classLocationResolver;
	
	public WebPluginLoader(ClassLocationResolver classLocationResolver, ServletContext servletContext) {
		Assert.notNull(servletContext, "ServletContext cannot be null");
		Assert.notNull(classLocationResolver, "classLocationResolver cannot be null");
		this.classLocationResolver = classLocationResolver;
		this.servletContext = servletContext;
	}

	public GenericWebApplicationContext newApplicationContext(ApplicationContext parent, ClassLoader classLoader) {
		final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.setBeanClassLoader(classLoader);
		
		final GenericWebApplicationContext context = new GenericWebApplicationContext(beanFactory);
		context.setParent(parent);
		context.setServletContext(servletContext);
		context.setClassLoader(classLoader);
		
		return context;
	}
	
	public ClassLoader newClassLoader(ApplicationContextSet contextSet, PluginSpec pluginSpec) {
		File[] parentClassLocations = classLocationResolver.getApplicationPluginClassLocations(pluginSpec.getName());
		return new ParentClassLoader(ClassUtils.getDefaultClassLoader(), parentClassLocations);
	}

	public Resource[] getClassLocations(ApplicationContextSet contextSet, PluginSpec pluginSpec) {
		return null;
	}

	public Resource[] getSpringConfigResources(ApplicationContextSet contextSet, PluginSpec pluginSpec, ClassLoader classLoader) {
		return WebResourceUtils.getServletContextResources(pluginSpec.getContextLocations(), servletContext);
	}

}
