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

package org.impalaframework.spring.service.proxy;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import org.impalaframework.service.ServiceRegistry;
import org.impalaframework.service.ServiceRegistryReference;
import org.impalaframework.service.registry.BasicServiceRegistryReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class DynamicServiceProxyFactoryCreatorTest extends TestCase {

    private DynamicServiceProxyFactoryCreator creator;
    private ServiceRegistry serviceRegistry;
    private Class<?>[] classes;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        classes = new Class[]{List.class};
        creator = new DynamicServiceProxyFactoryCreator();
        serviceRegistry = createMock(ServiceRegistry.class);
        creator.setServiceRegistry(serviceRegistry);
    }
    
    @SuppressWarnings("unchecked")
    public void testDynamicProxyFactory() throws Exception {
        final List<String> list = new ArrayList<String>();
        ServiceRegistryReference ref = new BasicServiceRegistryReference(list, "mybean", "mymod", ClassUtils.getDefaultClassLoader());
        expect(serviceRegistry.getService("mykey", classes)).andReturn(ref);
        expect(serviceRegistry.getService("mykey", classes)).andReturn(ref);
        
        replay(serviceRegistry);
        final ProxyFactory proxyFactory = creator.createDynamicProxyFactory(classes, "mykey");
        
        final List proxy = (List) proxyFactory.getProxy();
        proxy.add("obj");
        
        verify(serviceRegistry);
        
        assertTrue(list.contains("obj"));
    }
    
    @SuppressWarnings("unchecked")
    public void testStaticProxyFactory() throws Exception {
        final List<String> list = new ArrayList<String>();
        ServiceRegistryReference ref = new BasicServiceRegistryReference(list, "mybean", "mymod", ClassUtils.getDefaultClassLoader());
        
        replay(serviceRegistry);
        final ProxyFactory proxyFactory = creator.createStaticProxyFactory(new Class<?>[]{List.class}, ref);
        
        final List proxy = (List) proxyFactory.getProxy();
        proxy.add("obj");
        
        verify(serviceRegistry);
    }
    
}
