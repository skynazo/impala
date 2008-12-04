/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.impalaframework.osgi.test;

import java.lang.reflect.Method;

import org.impalaframework.facade.Impala;
import org.impalaframework.module.definition.ModuleDefinitionSource;
import org.impalaframework.module.definition.RootModuleDefinition;
import org.impalaframework.osgi.util.OsgiUtils;
import org.impalaframework.util.ReflectionUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.provisioning.ArtifactLocator;

/**
 * Extends {@link AbstractConfigurableBundleCreatorTests} to support working with Impala in OSGi.
 * @author Phil Zoio
 */
public abstract class OsgiIntegrationTest extends AbstractConfigurableBundleCreatorTests implements ModuleDefinitionSource {

	private ArtifactLocator locator;

	public OsgiIntegrationTest() {
		super();
		locator = new RepositoryArtifactLocator();
	}

	
	/* ********************** Test bundle names ********************* */
	
	@Override
	protected Resource[] getTestBundles() {
		
		ConfigurableFileFilter fileFilter = new ConfigurableFileFilter(
				"osgi: *; main: impala", //include all of osgi plus impala files in main
				"main: build,extender,jmx"); //exclude build, extender and jmx in main
		FileFetcher fileFetcher = new FileFetcher("../osgi-repository", new String[] {"osgi", "main"});
		return fileFetcher.getResources(fileFilter).toArray(new Resource[0]);
	}
	
	protected void postProcessBundleContext(BundleContext context) throws Exception {
		
		Impala.init();
		RootModuleDefinition definition = getModuleDefinition();
		
		ServiceReference serviceReference = context.getServiceReference(ModuleDefinitionSource.class.getName());
		
		Object service = context.getService(serviceReference);
		
		Method method = ReflectionUtils.findMethod(service.getClass(), "inject", new Class[]{Object.class});
		ReflectionUtils.invokeMethod(method, service, new Object[]{definition});
		
		//now fire up extender bundle
		ConfigurableFileFilter fileFilter = new ConfigurableFileFilter(
				"dist:extender;main:extender", //include extender in dist
				null); 
		FileFetcher fileFetcher = new FileFetcher("../osgi-repository", new String[] {"dist", "main"});

		Resource[] addResources = fileFetcher.getResources(fileFilter).toArray(new Resource[0]);
		
		for (Resource resource : addResources) {
			Bundle bundle = OsgiUtils.installBundle(context, resource);
			OsgiUtils.startBundle(bundle);
		}
		
		super.postProcessBundleContext(context);
	}

	protected String[] getTestFrameworkBundlesNames() {
		String[] testFrameworkBundlesNames = super.getTestFrameworkBundlesNames();
		for (int i = 0; i < testFrameworkBundlesNames.length; i++) {
			String bundle = testFrameworkBundlesNames[i];
			
			if (bundle.equals("org.springframework.osgi,log4j.osgi,1.2.15-SNAPSHOT")) {
				bundle = bundle.replace("log4j.osgi,1.2.15-SNAPSHOT", "com.springsource.org.apache.log4j,1.2.15");
				testFrameworkBundlesNames[i] = bundle;
			}
			
		}
		return testFrameworkBundlesNames;
	}
	
	protected ArtifactLocator getLocator() {
		return locator;
	}
	
}

