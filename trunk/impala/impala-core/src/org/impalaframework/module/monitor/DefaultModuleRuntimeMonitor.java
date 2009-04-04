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

package org.impalaframework.module.monitor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.runtime.BaseModuleRuntime;
import org.impalaframework.module.spi.ModuleRuntimeMonitor;
import org.impalaframework.resolver.ModuleLocationResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Implements a strategy for passing a list of monitorable resources to
 * {@link ModuleChangeMonitor} following the loading of a module.
 * 
 * Implements runtime monitoring strategy based on the assumption that Impala
 * monitors files directly in the classpath. It has no responsibility for
 * copying files from a staging directory or temporary location to their final
 * destination on the module class path.
 * 
 * @author Phil Zoio
 */
public class DefaultModuleRuntimeMonitor implements ModuleRuntimeMonitor {

	private static Log logger = LogFactory.getLog(BaseModuleRuntime.class);
	
	private ModuleLocationResolver moduleLocationResolver;
	
	private ModuleChangeMonitor moduleChangeMonitor;

	/**
	 * Nothing to do, as monitored module resources are already in correct location.
	 */
	public void beforeModuleLoads(ModuleDefinition definition) {
		//nothing to do here. Subclasses may implement strategies for copying modified resources into
		//proper location
	}
	
	/**
	 * Called after module loading takes place. Sets resources to monitor as exactly those which comprise
	 * the resources local to the module class path.
	 */
	public void afterModuleLoaded(ModuleDefinition definition) {
		if (moduleChangeMonitor != null) {
			
			Assert.notNull(moduleLocationResolver, "moduleLocationResolver required if ModuleChangeMonitor is wired in.");
			final String moduleName = definition.getName();
			final List<Resource> locations = moduleLocationResolver.getApplicationModuleClassLocations(moduleName);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Monitoring resources " + locations + " using ModuleChangeMonitor " + moduleChangeMonitor);
			}
			
			moduleChangeMonitor.setResourcesToMonitor(moduleName, locations.toArray(new Resource[0]));
		}
	}
	
	public void setModuleLocationResolver(ModuleLocationResolver moduleLocationResolver) {
		this.moduleLocationResolver = moduleLocationResolver;
	}

	public void setModuleChangeMonitor(ModuleChangeMonitor moduleChangeMonitor) {
		this.moduleChangeMonitor = moduleChangeMonitor;
	}
}