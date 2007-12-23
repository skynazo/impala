package org.impalaframework.module.operation;

import org.impalaframework.module.bootstrap.ModuleManagementFactory;
import org.impalaframework.module.definition.RootModuleDefinition;
import org.impalaframework.module.holder.ModuleStateHolder;
import org.impalaframework.module.modification.ModificationExtractor;
import org.impalaframework.module.modification.ModificationExtractorRegistry;
import org.impalaframework.module.modification.ModificationExtractorType;
import org.impalaframework.module.modification.TransitionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class ReloadNamedModuleOperation implements ModuleOperation {

	// FIXME unit test

	final Logger logger = LoggerFactory.getLogger(ReloadNamedModuleOperation.class);

	private final ModuleManagementFactory factory;

	private final String moduleToReload;

	public ReloadNamedModuleOperation(final ModuleManagementFactory factory, final String moduleToReload) {
		super();
		Assert.notNull(factory);
		Assert.notNull(moduleToReload);

		this.factory = factory;
		this.moduleToReload = moduleToReload;
	}

	public ModuleOperationResult execute(ModuleOperationInput moduleOperationInput) {

		ModuleStateHolder moduleStateHolder = factory.getModuleStateHolder();
		RootModuleDefinition oldRootDefinition = moduleStateHolder.getRootModuleDefinition();
		RootModuleDefinition newRootDefinition = newRootModuleDefinition();

		ModificationExtractorRegistry modificationExtractor = factory.getModificationExtractorRegistry();
		ModificationExtractor calculator = modificationExtractor
				.getModificationExtractor(ModificationExtractorType.STRICT);
		TransitionSet transitions = calculator.reload(oldRootDefinition, newRootDefinition, moduleToReload);
		moduleStateHolder.processTransitions(transitions);

		boolean result = !transitions.getModuleTransitions().isEmpty();
		return result ? ModuleOperationResult.TRUE : ModuleOperationResult.FALSE;
	}

	protected ModuleManagementFactory getFactory() {
		return factory;
	}

	protected String getModuleToReload() {
		return moduleToReload;
	}

	protected RootModuleDefinition newRootModuleDefinition() {
		RootModuleDefinition newPluginSpec = factory.getModuleStateHolder().cloneRootModuleDefinition();
		return newPluginSpec;
	}
}
