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

package org.impalaframework.module.runtime;

import org.impalaframework.classloader.ClassLoaderFactory;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.RuntimeModule;
import org.springframework.util.Assert;

public class SimpleModuleRuntime extends BaseModuleRuntime {
    
    private ClassLoaderFactory classLoaderFactory;
    
    @Override
    protected RuntimeModule doLoadModule(ModuleDefinition definition) {
        Assert.notNull(definition);
        Assert.notNull(classLoaderFactory);
        
        ClassLoader parentClassLoader = null;
        final ModuleDefinition parentDefinition = definition.getParentDefinition();

        if (parentDefinition != null) {
            parentClassLoader = getClassLoaderRegistry().getClassLoader(parentDefinition.getName());
        }
        final ClassLoader classLoader = classLoaderFactory.newClassLoader(parentClassLoader, definition);
        return new SimpleRuntimeModule(classLoader, definition);
    }

    @Override
    protected void doCloseModule(RuntimeModule runtimeModule) {
    }

    public String getRuntimeName() {
        return "simple";
    }

    public void setClassLoaderFactory(ClassLoaderFactory classLoaderFactory) {
        this.classLoaderFactory = classLoaderFactory;
    }

}