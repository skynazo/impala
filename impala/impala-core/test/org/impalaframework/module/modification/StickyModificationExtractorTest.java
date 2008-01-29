package org.impalaframework.module.modification;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.impalaframework.module.definition.ModuleDefinition;
import org.impalaframework.module.definition.RootModuleDefinition;
import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.module.modification.ModificationExtractor;
import org.impalaframework.module.modification.ModuleStateChange;
import org.impalaframework.module.modification.Transition;
import org.impalaframework.module.modification.TransitionSet;
import org.impalaframework.module.modification.StickyModificationExtractor;
import org.impalaframework.module.modification.StrictModificationExtractor;

public class StickyModificationExtractorTest extends TestCase {
	
	public final void testCheckOriginal() {
		RootModuleDefinition parentSpec1 = ModificationTestUtils.spec("app-context1.xml", "plugin1, plugin2, plugin3");
		RootModuleDefinition parentSpec2 = ModificationTestUtils.spec("app-context1.xml", "plugin1 (myPlugins:one), plugin2");

		ModuleDefinition plugin2 = parentSpec2.findChildDefinition("plugin2", true);
		ModuleDefinition plugin4 = new SimpleModuleDefinition(plugin2, "plugin4");
		new SimpleModuleDefinition(plugin4, "plugin5");
		new SimpleModuleDefinition(plugin4, "plugin6");

		ModificationExtractor calculator = new StrictModificationExtractor();
		TransitionSet transitions = calculator.getTransitions(parentSpec1, parentSpec2);
		
		Iterator<? extends ModuleStateChange> iterator = doAssertions(transitions, 6);
		
		ModuleStateChange change3 = iterator.next();
		assertEquals("plugin3", change3.getModuleDefinition().getName());
		assertEquals(Transition.LOADED_TO_UNLOADED, change3.getTransition());
		
		//now show that the sticky calculator has the same set of changes, but omits the last one
		ModificationExtractor stickyCalculator = new StickyModificationExtractor();
		TransitionSet stickyTransitions = stickyCalculator.getTransitions(parentSpec1, parentSpec2);
		doAssertions(stickyTransitions, 5);
	}
	
	public final void testAddParentLocations() {
		RootModuleDefinition parentSpec1 = ModificationTestUtils.spec("app-context1.xml", "plugin1, plugin2, plugin4");
		RootModuleDefinition parentSpec2 = ModificationTestUtils.spec("app-context1.xml,extra-context.xml", "plugin1, plugin2, plugin3");

		//now show that the sticky calculator has the same set of changes, but omits the last one
		ModificationExtractor stickyCalculator = new StickyModificationExtractor();
		TransitionSet stickyTransitions = stickyCalculator.getTransitions(parentSpec1, parentSpec2);
		
		Collection<? extends ModuleStateChange> pluginTransitions = stickyTransitions.getModuleTransitions();
		assertEquals(2, pluginTransitions.size());
		
		Iterator<? extends ModuleStateChange> iterator = pluginTransitions.iterator();
		ModuleStateChange first = iterator.next();
		assertEquals(Transition.CONTEXT_LOCATIONS_ADDED, first.getTransition());
		assertEquals(RootModuleDefinition.NAME, first.getModuleDefinition().getName());

		ModuleStateChange second = iterator.next();
		assertEquals(Transition.UNLOADED_TO_LOADED, second.getTransition());
		assertEquals("plugin3", second.getModuleDefinition().getName());
		
		RootModuleDefinition newSpec = stickyTransitions.getNewRootModuleDefinition();
		Collection<String> moduleNames = newSpec.getModuleNames();
		assertEquals(4, moduleNames.size());
		assertNotNull(newSpec.getModule("plugin1"));
		assertNotNull(newSpec.getModule("plugin2"));
		assertNotNull(newSpec.getModule("plugin3"));
		assertNotNull(newSpec.getModule("plugin4"));
		assertEquals(2, newSpec.getContextLocations().size());
	}
	
	public final void testAddParentLocationsInReverse() {
		RootModuleDefinition parentSpec1 = ModificationTestUtils.spec("app-context1.xml,extra-context.xml", "plugin1, plugin2, plugin3");
		RootModuleDefinition parentSpec2 = ModificationTestUtils.spec("app-context1.xml", "plugin1, plugin2, plugin4");

		//now show that the sticky calculator has the same set of changes, but omits the last one
		ModificationExtractor stickyCalculator = new StickyModificationExtractor();
		TransitionSet stickyTransitions = stickyCalculator.getTransitions(parentSpec1, parentSpec2);
		
		Collection<? extends ModuleStateChange> pluginTransitions = stickyTransitions.getModuleTransitions();
		assertEquals(1, pluginTransitions.size());
		
		Iterator<? extends ModuleStateChange> iterator = pluginTransitions.iterator();
		ModuleStateChange second = iterator.next();
		assertEquals(Transition.UNLOADED_TO_LOADED, second.getTransition());
		assertEquals("plugin4", second.getModuleDefinition().getName());
		
		RootModuleDefinition newSpec = stickyTransitions.getNewRootModuleDefinition();
		Collection<String> moduleNames = newSpec.getModuleNames();
		assertEquals(4, moduleNames.size());
		assertNotNull(newSpec.getModule("plugin1"));
		assertNotNull(newSpec.getModule("plugin2"));
		assertNotNull(newSpec.getModule("plugin3"));
		assertNotNull(newSpec.getModule("plugin4"));
		assertEquals(2, newSpec.getContextLocations().size());
	}
	
	private Iterator<? extends ModuleStateChange> doAssertions(TransitionSet transitions, int expectedSize) {
		Collection<? extends ModuleStateChange> pluginTransitions = transitions.getModuleTransitions();
		assertEquals(expectedSize, pluginTransitions.size());
		Iterator<? extends ModuleStateChange> iterator = pluginTransitions.iterator();

		ModuleStateChange change1 = iterator.next();
		assertEquals("plugin1", change1.getModuleDefinition().getName());
		assertEquals(Transition.LOADED_TO_UNLOADED, change1.getTransition());
		
		ModuleStateChange change2 = iterator.next();
		assertEquals("plugin1", change2.getModuleDefinition().getName());
		assertEquals(Transition.UNLOADED_TO_LOADED, change2.getTransition());
		
		ModuleStateChange change3 = iterator.next();
		assertEquals("plugin4", change3.getModuleDefinition().getName());
		assertEquals(Transition.UNLOADED_TO_LOADED, change3.getTransition());
		
		ModuleStateChange change4 = iterator.next();
		assertEquals("plugin5", change4.getModuleDefinition().getName());
		assertEquals(Transition.UNLOADED_TO_LOADED, change4.getTransition());
		
		ModuleStateChange change5 = iterator.next();
		assertEquals("plugin6", change5.getModuleDefinition().getName());
		assertEquals(Transition.UNLOADED_TO_LOADED, change5.getTransition());
		
		return iterator;
	}

}
