/*******************************************************************************
 * Copyright (c) 2024, 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Martin Lippert
 */
public class DependsOnCompletionProcessor implements AnnotationAttributeCompletionProvider {

	private final SpringMetamodelIndex springIndex;

	public DependsOnCompletionProcessor(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}

	@Override
	public List<AnnotationAttributeProposal> getCompletionCandidates(IJavaProject project, ASTNode node) {
		Collection<String> beanNameFromCodeElement = getBeanNameFromSourceCodePosition(node);
		
		return Arrays.stream(this.springIndex.getBeansOfProject(project.getElementName()))
				.map(bean -> bean.getName())
				.filter(beanName -> !beanNameFromCodeElement.contains(beanName))
				.distinct()
				.map(beanName -> new AnnotationAttributeProposal(beanName))
				.collect(Collectors.toList());
	}

	private Collection<String> getBeanNameFromSourceCodePosition(ASTNode node) {
		ASTNode parent = node.getParent();
		if (parent instanceof MethodDeclaration method) {
			Annotation beanAnnotation = ASTUtils.getBeanAnnotation(method);
			return BeanUtils.getBeanNamesFromBeanAnnotation(beanAnnotation);
		}
		else if (parent instanceof TypeDeclaration type) {
			Annotation componentAnnotation = ASTUtils.getAnnotation(type, Annotations.COMPONENT);
			return List.of(BeanUtils.getBeanNameFromComponentAnnotation(componentAnnotation, type));
		}
		
		return List.of();
	}

}
