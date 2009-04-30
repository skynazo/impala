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

package org.impalaframework.service.registry;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.exception.InvalidStateException;
import org.impalaframework.service.registry.event.ServiceAddedEvent;
import org.impalaframework.service.registry.event.ServiceReferenceFilter;
import org.impalaframework.service.registry.event.ServiceRegistryEvent;
import org.impalaframework.service.registry.event.ServiceRegistryEventListener;
import org.impalaframework.service.registry.event.ServiceRemovedEvent;
import org.springframework.util.Assert;

public class ServiceRegistryImpl implements ServiceRegistry {

	private static Log logger = LogFactory.getLog(ServiceRegistryImpl.class);

	private Map<String, ServiceRegistryReference> services = new ConcurrentHashMap<String, ServiceRegistryReference>();
	private Map<Object, String> entities = new IdentityHashMap<Object, String>();

	// use CopyOnWriteArrayList to support non-blocking thread-safe iteration
	private List<ServiceRegistryEventListener> listeners = new CopyOnWriteArrayList<ServiceRegistryEventListener>();
	private Map<String, List<ServiceRegistryEventListener>> taggedListeners = new ConcurrentHashMap<String, List<ServiceRegistryEventListener>>();

	private Object registryLock = new Object();
	private Object listenersLock = new Object();
	private Object taggedListenersLock = new Object();

	public void addEventListener(String tagName,
			ServiceRegistryEventListener listener) {
		Assert.notNull(tagName);
		Assert.notNull(listener);

		List<ServiceRegistryEventListener> list = null;
		synchronized (taggedListenersLock) {
			list = taggedListeners.get(tagName);
			if (list == null) {
				list = new CopyOnWriteArrayList<ServiceRegistryEventListener>();
				taggedListeners.put(tagName, list);
			}
			synchronized (list) {
				list.add(listener);
				if (logger.isDebugEnabled())
					logger.debug("Added service registry listener " + listener + " for tag '" + tagName + "'");
			}
		}
	}

	/**
	 * Adds to global event listeners to which all service registry events will
	 * be broadcast
	 */
	public void addEventListener(ServiceRegistryEventListener listener) {
		Assert.notNull(listener);
		synchronized (listenersLock) {
			listeners.add(listener);
		}
		if (logger.isDebugEnabled())
			logger.debug("Added service registry listener " + listener);
	}

	public void removeListener(String tagName,
			ServiceRegistryEventListener listener) {
		if (tagName != null) {
			removeEventListener(tagName, listener);
		} else {
			synchronized (listenersLock) {
				removeEventListener(listener);
			}
		}
	}

	public void removeEventListener(String tagName,
			ServiceRegistryEventListener listener) {
		List<ServiceRegistryEventListener> list = null;
		synchronized (taggedListenersLock) {
			list = taggedListeners.get(tagName);
			if (list != null) {
				synchronized (list) {
					list.remove(listener);
				}
			}
		}
	}

	/**
	 * Removes global event listeners to which all service registry events will
	 * be broadcast
	 */

	public void removeEventListener(ServiceRegistryEventListener listener) {
		List<ServiceRegistryEventListener> listeners = this.listeners;
		removeListener(listener, listeners);
	}

	public void addService(String beanName, String moduleName, Object service, ClassLoader classLoader) {
		addService(beanName, moduleName, service, null, null, classLoader);
	}

	public void addService(String beanName, String moduleName, Object service,
			List<String> tags, ClassLoader classLoader) {
		addService(beanName, moduleName, service, tags, null, classLoader);
	}

	public void addService(String beanName, String moduleName, Object service,
			Map<String, ?> attributes, ClassLoader classLoader) {
		addService(beanName, moduleName, service, null, attributes, classLoader);
	}

	public void addService(String beanName, String moduleName, Object service,
			List<String> tags, Map<String, ?> attributes, ClassLoader classLoader) {
		BasicServiceRegistryReference serviceReference = null;
		synchronized (registryLock) {
			serviceReference = new BasicServiceRegistryReference(service, beanName,
					moduleName, tags, attributes, classLoader);
			services.put(beanName, serviceReference);
			entities.put(service, beanName);
		}
		if (logger.isDebugEnabled())
			logger.debug("Added service bean '" + beanName
					+ "' contributed from module '" + moduleName
					+ "' to service registry, with tags " + tags
					+ " and attributes " + attributes);

		//FIXME issue 23: is any additional locking here necessary
		ServiceAddedEvent event = new ServiceAddedEvent(serviceReference);
		invokeListeners(event);
	}

	public void remove(Object service) {
		ServiceRegistryReference serviceReference = null;
		String beanName = null;
		synchronized (registryLock) {
			beanName = entities.remove(service);
			if (beanName != null) {
				serviceReference = services.remove(beanName);
			}
		}

		if (serviceReference != null) {
			if (logger.isDebugEnabled())
				logger.debug("Removed from service reference bean '" + beanName
						+ "' contributed from module '"
						+ serviceReference.getContributingModule() + "'");
			//FIXME issue 23: is any additional locking here necessary
			ServiceRemovedEvent event = new ServiceRemovedEvent(
					serviceReference);
			invokeListeners(event);
		}
	}

	public ServiceRegistryReference getService(String beanName) {
		return services.get(beanName);
	}

	public ServiceRegistryReference getService(String beanName, Class<?> type) {
		ServiceRegistryReference serviceReference = services.get(beanName);

		if (serviceReference != null) {
			Object bean = serviceReference.getBean();
			if (!type.isAssignableFrom(bean.getClass())) {
				throw new InvalidStateException("Service reference bean "
						+ bean + " is not assignable from type " + type);
			}
		}
		return serviceReference;
	}

	/* ************ helper methods * ************** */

	private List<ServiceRegistryEventListener> getListeners() {
		return listeners;
	}

	private void invokeListeners(ServiceRegistryEvent event) {
		List<ServiceRegistryEventListener> listeners = getListeners();
		for (ServiceRegistryEventListener listener : listeners) {
			listener.handleServiceRegistryEvent(event);
		}
		ServiceRegistryReference serviceReference = event.getServiceReference();
		List<String> tags = serviceReference.getTags();

		for (String tag : tags) {
			List<ServiceRegistryEventListener> list = null;
			synchronized (taggedListenersLock) {
				list = taggedListeners.get(tag);
				if (list != null) {
					synchronized (list) {
						for (ServiceRegistryEventListener listener : list) {
							listener.handleServiceRegistryEvent(event);
						}
					}
				}
			}
		}
	}

	private void removeListener(ServiceRegistryEventListener listener,
			List<ServiceRegistryEventListener> listeners) {
		for (Iterator<ServiceRegistryEventListener> iterator = listeners
				.iterator(); iterator.hasNext();) {
			ServiceRegistryEventListener currentListener = iterator.next();
			if (currentListener == listener) {
				iterator.remove();
				if (logger.isDebugEnabled())
					logger.debug("Removed service registry listener "
							+ listener);
			}
		}
	}

	public Collection<ServiceRegistryReference> getServices(
			ServiceReferenceFilter filter) {
		List<ServiceRegistryReference> serviceList = new LinkedList<ServiceRegistryReference>();
		Collection<ServiceRegistryReference> values = services.values();
	    for (ServiceRegistryReference serviceReference : values) {
			if (filter.matches(serviceReference)) {
				serviceList.add(serviceReference);
			}
		}
		return serviceList;
	}

}