/*******************************************************************************
 * Copyright (c) 2018, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import org.eclipse.lsp4j.WorkspaceSymbol;

/**
 * @author Martin Lippert
 */
public class EnhancedSymbolInformation {
	
	private final WorkspaceSymbol symbol;

	public EnhancedSymbolInformation(WorkspaceSymbol symbol) {
		this.symbol = symbol;
	}
	
	public WorkspaceSymbol getSymbol() {
		return symbol;
	}
	
	@Override
	public String toString() {
		return "EnhancedSymbolInformation [symbol=" + symbol + "]";
	}
	
}
