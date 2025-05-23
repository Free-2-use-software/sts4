/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.springframework.ide.vscode.boot.java.beans.CachedBean;
import org.springframework.ide.vscode.boot.java.reconcilers.CachedDiagnostics;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.commons.util.UriUtil;

/**
 * Class to capture the major results of scanning one or more files
 * for Spring symbol creation, indexing, and reconciling
 * 
 * @author Martin Lippert
 */
public class SpringIndexerJavaScanResult {
	
	private final Map<String, Long> markedForReconciling; // file + modification timestamp
	private final Set<String> markedForAffetcedFilesIndexing; // file
	
	private final List<CachedSymbol> generatedSymbols;
	private final List<CachedBean> generatedBeans;
	private final List<CachedDiagnostics> generatedDiagnostics;

	private final IJavaProject project;
	private final String[] javaFiles;

	
	public SpringIndexerJavaScanResult(IJavaProject project, String[] javaFiles) {
		this.project = project;
		this.javaFiles = javaFiles;

		this.markedForReconciling = new HashMap<>();
		this.markedForAffetcedFilesIndexing = new HashSet<>();
		
		this.generatedSymbols = new ArrayList<CachedSymbol>();
		this.generatedBeans = new ArrayList<CachedBean>();
		this.generatedDiagnostics = new ArrayList<CachedDiagnostics>();
	}
	
	public SpringIndexerJavaScanResult(IJavaProject project, String[] javaFiles, SymbolHandler symbolHandler,
			CachedSymbol[] symbols, CachedBean[] beans, CachedDiagnostics[] diagnostics) {
		
		this.project = project;
		this.javaFiles = javaFiles;
		
		this.markedForReconciling = new HashMap<>();
		this.markedForAffetcedFilesIndexing = new HashSet<>();

		this.generatedSymbols = Arrays.asList(symbols);
		this.generatedBeans = Arrays.asList(beans);
		this.generatedDiagnostics = Arrays.asList(diagnostics);
	}
	

	public Map<String, Long> getMarkedForReconcilingWithCompleteIndex() {
		return markedForReconciling;
	}
	
	public void markForReconcilingWithCompleteIndex(String file, long lastModified) {
		this.markedForReconciling.put(file, lastModified);
	}
	
	public void markForAffectedFilesIndexing(Collection<String> markedForAffectedFilesIndexing) {
		this.markedForAffetcedFilesIndexing.addAll(markedForAffectedFilesIndexing);
	}
	
	public Set<String> getMarkedForAffectedFilesIndexing() {
		return this.markedForAffetcedFilesIndexing;
	}
	

	public List<CachedBean> getGeneratedBeans() {
		return generatedBeans;
	}
	
	public List<CachedSymbol> getGeneratedSymbols() {
		return generatedSymbols;
	}
	
	public List<CachedDiagnostics> getGeneratedDiagnostics() {
		return generatedDiagnostics;
	}
	
	public void publishResults(SymbolHandler symbolHandler) {
		WorkspaceSymbol[] enhancedSymbols = generatedSymbols.stream().map(cachedSymbol -> cachedSymbol.getEnhancedSymbol()).toArray(WorkspaceSymbol[]::new);
		Map<String, List<SpringIndexElement>> allBeans = generatedBeans.stream().filter(cachedBean -> cachedBean.getBean() != null).collect(Collectors.groupingBy(CachedBean::getDocURI, Collectors.mapping(CachedBean::getBean, Collectors.toList())));
		Map<String, List<Diagnostic>> diagnosticsByDoc = generatedDiagnostics.stream().filter(cachedDiagnostic -> cachedDiagnostic.getDiagnostic() != null).collect(Collectors.groupingBy(CachedDiagnostics::getDocURI, Collectors.mapping(CachedDiagnostics::getDiagnostic, Collectors.toList())));

		// to make sure that files without index elements or diagnostics publish an empty array of diagnostics
		addEmptyDiagnostics(diagnosticsByDoc, javaFiles);
		addEmptyIndexElements(allBeans, javaFiles);

		symbolHandler.addSymbols(this.project, enhancedSymbols, allBeans, diagnosticsByDoc);
	}
	
	public void publishDiagnosticsOnly(SymbolHandler symbolHandler) {
		Map<String, List<Diagnostic>> diagnosticsByDoc = generatedDiagnostics.stream().filter(cachedDiagnostic -> cachedDiagnostic.getDiagnostic() != null).collect(Collectors.groupingBy(CachedDiagnostics::getDocURI, Collectors.mapping(CachedDiagnostics::getDiagnostic, Collectors.toList())));
		addEmptyDiagnostics(diagnosticsByDoc, javaFiles); // to make sure that files without index elements or diagnostics publish an empty array of diagnostics
		symbolHandler.addSymbols(this.project, null, null, diagnosticsByDoc);
	}
	
	private void addEmptyIndexElements(Map<String, List<SpringIndexElement>> allBeans, String[] javaFiles) {
		for (int i = 0; i < javaFiles.length; i++) {
			File file = new File(javaFiles[i]);
			String docURI = UriUtil.toUri(file).toASCIIString();

			if (!allBeans.containsKey(docURI)) {
				allBeans.put(docURI, Collections.emptyList());
			}
		}
	}

	private void addEmptyDiagnostics(Map<String, List<Diagnostic>> diagnosticsByDoc, String[] javaFiles) {
		for (int i = 0; i < javaFiles.length; i++) {
			File file = new File(javaFiles[i]);
			String docURI = UriUtil.toUri(file).toASCIIString();

			if (!diagnosticsByDoc.containsKey(docURI)) {
				diagnosticsByDoc.put(docURI, Collections.emptyList());
			}
		}
	}

}
