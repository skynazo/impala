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

package org.impalaframework.web.jsp;

import junit.framework.TestCase;

import org.impalaframework.web.AttributeServletContext;

public class JasperServletFactoryBeanTest extends TestCase {

    public void testAfterPropertiesSet() throws Exception {
        //Thread.currentThread().setContextClassLoader(new URLClassLoader(arg0));
        
        JasperServletFactoryBean factoryBean = new JasperServletFactoryBean();
        factoryBean.setServletName("jspServlet");
        factoryBean.setServletContext(new AttributeServletContext());
        factoryBean.afterPropertiesSet();
    }

}
