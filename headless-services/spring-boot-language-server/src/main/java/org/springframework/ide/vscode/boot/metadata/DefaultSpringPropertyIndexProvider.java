/*******************************************************************************
 * Copyright (c) 2016, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.metadata;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class DefaultSpringPropertyIndexProvider implements SpringPropertyIndexProvider {

	private JavaProjectFinder javaProjectFinder;
	private SpringPropertiesIndexManager indexManager;

	private Runnable changeHandler = null;

	private ProgressService progressService = ProgressService.NO_PROGRESS;

	public DefaultSpringPropertyIndexProvider(JavaProjectFinder javaProjectFinder, ProjectObserver projectObserver, SimpleLanguageServer server, ValueProviderRegistry valueProviders, BootJavaConfig config) {
		this.javaProjectFinder = javaProjectFinder;
		this.indexManager = new SpringPropertiesIndexManager(valueProviders, projectObserver, server.getWorkspaceService().getFileObserver());
		this.indexManager.addListener(info -> {
			if (changeHandler != null) {
				changeHandler.run();
			}
		});
		server.onCommand("sts/common-properties/reload", params -> CompletableFuture.completedFuture(indexManager.reloadCommonProperties()));
		if (config != null) {
			config.addListener(v -> indexManager.setCommonPropertiesFile(config.getCommonPropertiesFile()));
		}
	}

	@Override
	public SpringPropertyIndex getIndex(IDocument doc) {
		Optional<IJavaProject> jp = javaProjectFinder.find(new TextDocumentIdentifier(doc.getUri()));
		if (jp.isPresent()) {
			return indexManager.get(jp.get(), progressService);
		}
		return SpringPropertyIndex.EMPTY_INDEX;
	}

	@Override
	public SpringPropertyIndex getIndex(IJavaProject project) {
		return indexManager.get(project, progressService);
	}

	public void setProgressService(ProgressService progressService) {
		this.progressService = progressService;
	}

	@Override
	public void onChange(Runnable changeHandler) {
		this.changeHandler = changeHandler;
	}

}
