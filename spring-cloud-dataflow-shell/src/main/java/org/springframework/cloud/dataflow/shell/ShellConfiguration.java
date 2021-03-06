/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.shell;

import org.springframework.cloud.dataflow.shell.command.ConfigCommands;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Encapsulates various configuration properties for the shell such as {@link Target}.
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
@Component
public class ShellConfiguration {

	private Target target;

	/**
	 * Constructor.
	 */
	public ShellConfiguration() {
	}

	/**
	 * Return the {@link Target} which encapsulates not only the Target URI but also success/error messages + status.
	 *
	 * @return Should never be null. Initialized by {@link ConfigCommands#afterPropertiesSet()}.
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * Set the Dataflow Server {@link Target}.
	 *
	 * @param target Must not be null.
	 */
	public void setTarget(Target target) {
		Assert.notNull(target, "The provided target must not be null.");
		this.target = target;
	}

}
