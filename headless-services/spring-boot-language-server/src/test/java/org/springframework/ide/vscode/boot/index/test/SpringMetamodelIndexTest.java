/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.index.cache.IndexCacheOnDisc;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationAttributeValue;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.DefaultValues;
import org.springframework.ide.vscode.commons.protocol.spring.InjectionPoint;

import com.google.gson.Gson;

public class SpringMetamodelIndexTest {

	private InjectionPoint[] emptyInjectionPoints = new InjectionPoint[0];
	private Set<String> emptySupertypes = new HashSet<>();
	private AnnotationMetadata[] emptyAnnotations = new AnnotationMetadata[0];
	private AnnotationMetadata[] emptyInjectionAnnotations = new AnnotationMetadata[0];
	private Map<String, AnnotationAttributeValue[]> emptyAnnotationAttributes = new LinkedHashMap<>();
	
	private Location locationForDoc1 = new Location("docURI1", new Range(new Position(1, 1), new Position(1, 10)));
	private Location locationForDoc2 = new Location("docURI2", new Range(new Position(2, 1), new Position(2, 10)));

	@Test
	void testEmptyIndex() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		assertNull(index.getBeansOfProject("someProject"));
		assertNull(index.getBeansWithName("someProject", "someBeanName"));
	}

	@Test
	void testSimpleProjectWithBeansPerProject() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanName3", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2, bean3});
		
		Bean[] beans = index.getBeansOfProject("someProject");
		assertNotNull(beans);
		assertEquals(3, beans.length);
		
		List<Object> beansList = Arrays.asList(beans);
		assertTrue(beansList.contains(bean1));
		assertTrue(beansList.contains(bean2));
		assertTrue(beansList.contains(bean3));
		
		Bean anotherBean = new Bean("anotherBean", "beanType", null, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		assertFalse(beansList.contains(anotherBean));
	}
	
	@Test
	void testSimpleProjectWithBeansPerDocument() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanWithDifferentName", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2, bean3});
		
		Bean[] beansByLocation1 = index.getBeansOfDocument(locationForDoc1.getUri());
		assertNotNull(beansByLocation1);
		assertEquals(2, beansByLocation1.length);
		
		List<Object> beansList = Arrays.asList(beansByLocation1);
		assertTrue(beansList.contains(bean1));
		assertTrue(beansList.contains(bean2));
		assertFalse(beansList.contains(bean3));

		Bean[] beansByLocation2 = index.getBeansOfDocument(locationForDoc2.getUri());
		assertNotNull(beansByLocation2);
		assertEquals(1, beansByLocation2.length);
		assertEquals(bean3, beansByLocation2[0]);

		Bean[] beansOfNonExistingLocation = index.getBeansOfDocument("otherDocURI");
		assertEquals(0, beansOfNonExistingLocation.length);
	}
	
	@Test
	void testSimpleProjectWithBeansPerName() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanWithDifferentName", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2, bean3});
		
		Bean[] beansByName = index.getBeansWithName("someProject", "beanName1");
		assertNotNull(beansByName);
		assertEquals(2, beansByName.length);
		
		List<Object> beansList = Arrays.asList(beansByName);
		assertTrue(beansList.contains(bean1));
		assertTrue(beansList.contains(bean2));
		assertFalse(beansList.contains(bean3));
		
		assertNull(index.getBeansWithName("nonExistingProject", "beanName1"));
	}

	@Test
	void testUpdateBeansForSpecificDoc() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanName3", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", locationForDoc1.getUri(), new Bean[] {bean1, bean2});
		index.updateBeans("someProject", locationForDoc2.getUri(), new Bean[] {bean3});
		
		Bean updatedBean1 = new Bean("updated1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean updatedBean2 = new Bean("updated2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", locationForDoc1.getUri(), new Bean[] {updatedBean1, updatedBean2});

		Bean[] beans = index.getBeansOfProject("someProject");
		assertNotNull(beans);
		assertEquals(3, beans.length);
		
		List<Object> beansList = Arrays.asList(beans);
		assertTrue(beansList.contains(updatedBean1));
		assertTrue(beansList.contains(updatedBean2));
		assertTrue(beansList.contains(bean3));
		
		assertFalse(beansList.contains(bean1));
		assertFalse(beansList.contains(bean2));
		
		Bean anotherBean = new Bean("anotherBean", "beanType", null, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		assertFalse(beansList.contains(anotherBean));
	}
	
	@Test
	void testUpdateAllBeansForSpecificProject() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);

		index.updateBeans("someProject", new Bean[] {bean1, bean2});

		Bean bean3 = new Bean("beanName3", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean3});
		
		Bean[] beans = index.getBeansOfProject("someProject");
		assertNotNull(beans);
		assertEquals(1, beans.length);
		
		List<Object> beansList = Arrays.asList(beans);
		assertFalse(beansList.contains(bean1));
		assertFalse(beansList.contains(bean2));
		assertTrue(beansList.contains(bean3));
	}
	
	@Test
	void testRemoveAllBeansForSpecificProject() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanName3", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject1", new Bean[] {bean1, bean2});
		index.updateBeans("someProject2", new Bean[] {bean3});
		
		index.removeBeans("someProject1");
		
		Bean[] beans = index.getBeansOfProject("someProject2");
		assertNotNull(beans);
		assertEquals(1, beans.length);
		
		List<Object> beansList = Arrays.asList(beans);
		assertFalse(beansList.contains(bean1));
		assertFalse(beansList.contains(bean2));
		assertTrue(beansList.contains(bean3));
		
		assertNull(index.getBeansOfProject("someProject1"));
	}
		
	@Test
	void testRemoveAllBeansForSpecificDocument() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean3 = new Bean("beanName3", "beanType", locationForDoc2, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2, bean3});
		index.removeBeans("someProject", locationForDoc1.getUri());
		
		Bean[] beans = index.getBeansOfProject("someProject");
		assertNotNull(beans);
		assertEquals(1, beans.length);
		
		List<Object> beansList = Arrays.asList(beans);
		assertFalse(beansList.contains(bean1));
		assertFalse(beansList.contains(bean2));
		assertTrue(beansList.contains(bean3));
	}
	
	@Test
	void testOverallSerializeDeserializeBeans() {
		Location locationForAnnotation1 = new Location("docURI1", new Range(new Position(100, 5), new Position(100, 20)));
		Location locationForAnnotation2 = new Location("docURI1", new Range(new Position(200, 10), new Position(200, 40)));
		
		Location locationForAttribute1 = new Location("docURI1", new Range(new Position(1000, 100), new Position(1000, 400)));
		Location locationForAttribute2 = new Location("docURI1", new Range(new Position(2000, 200), new Position(2000, 800)));
		Location locationForAttribute3 = new Location("docURI1", new Range(new Position(3000, 400), new Position(3000, 1600)));

		AnnotationAttributeValue annotationAttributeValue1 = new AnnotationAttributeValue("attribute1", locationForAttribute1);
		AnnotationAttributeValue annotationAttributeValue2 = new AnnotationAttributeValue("attribute2", locationForAttribute2);
		AnnotationAttributeValue annotationAttributeValue3 = new AnnotationAttributeValue("attribute3", locationForAttribute3);

		InjectionPoint point1 = new InjectionPoint("point1", "point1-type", locationForDoc2, new AnnotationMetadata[]
				{
						new AnnotationMetadata("anno1", false, locationForAnnotation1, Map.of(
								"attribute1", new AnnotationAttributeValue[] {annotationAttributeValue1, annotationAttributeValue2},
								"attribute2", new AnnotationAttributeValue[] {annotationAttributeValue3})),
						new AnnotationMetadata("anno2", false, locationForAnnotation2, emptyAnnotationAttributes)
				});

		InjectionPoint point2 = new InjectionPoint("point2", "point2-type", locationForDoc1, null);

		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, new InjectionPoint[] {point1, point2}, Set.of("supertype1", "supertype2"), emptyAnnotations);
		String serialized = bean1.toString();
		
		Gson gson = IndexCacheOnDisc.createGson();
		Bean deserializedBean = gson.fromJson(serialized, Bean.class);
		
		assertEquals("beanName1", deserializedBean.getName());
		assertEquals("beanType", deserializedBean.getType());
		assertEquals(locationForDoc1, deserializedBean.getLocation());
		
		InjectionPoint[] points = deserializedBean.getInjectionPoints();
		assertEquals(2, points.length);

		assertEquals("point1", points[0].getName());
		assertEquals("point1-type", points[0].getType());
		assertEquals(locationForDoc2, points[0].getLocation());
		
		assertEquals("point2", points[1].getName());
		assertEquals("point2-type", points[1].getType());
		assertEquals(locationForDoc1, points[1].getLocation());
		assertSame(DefaultValues.EMPTY_ANNOTATIONS, points[1].getAnnotations());
		
		assertTrue(deserializedBean.isTypeCompatibleWith("supertype1"));
		assertTrue(deserializedBean.isTypeCompatibleWith("supertype2"));
		assertFalse(deserializedBean.isTypeCompatibleWith("java.lang.String"));
		
		assertEquals(2, points[0].getAnnotations().length);
		assertEquals("anno1", points[0].getAnnotations()[0].getAnnotationType());
		assertEquals("anno2", points[0].getAnnotations()[1].getAnnotationType());
		
		assertEquals(locationForAnnotation1, points[0].getAnnotations()[0].getLocation());
		assertEquals(locationForAnnotation2, points[0].getAnnotations()[1].getLocation());
		
		Map<String, AnnotationAttributeValue[]> attributes = points[0].getAnnotations()[0].getAttributes();
		assertEquals(2, attributes.get("attribute1").length);
		assertEquals(1, attributes.get("attribute2").length);
		
		assertEquals(annotationAttributeValue1, attributes.get("attribute1")[0]);
		assertEquals(annotationAttributeValue2, attributes.get("attribute1")[1]);
		assertEquals(annotationAttributeValue3, attributes.get("attribute2")[0]);
		
		assertEquals(locationForAttribute1, attributes.get("attribute1")[0].getLocation());
		assertEquals(locationForAttribute2, attributes.get("attribute1")[1].getLocation());
		assertEquals(locationForAttribute3, attributes.get("attribute2")[0].getLocation());

		assertEquals(0, points[1].getAnnotations().length);
	}
	
	@Test
	void testEmptyInjectionPointsOptimizationWithSerializeDeserializeBeans() {
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		String serialized = bean1.toString();
		
		Gson gson = IndexCacheOnDisc.createGson();
		Bean deserializedBean = gson.fromJson(serialized, Bean.class);
		
		assertEquals("beanName1", deserializedBean.getName());
		assertEquals("beanType", deserializedBean.getType());
		assertEquals(locationForDoc1, deserializedBean.getLocation());
		
		assertSame(DefaultValues.EMPTY_INJECTION_POINTS, deserializedBean.getInjectionPoints());
	}
		
	@Test
	void testEmptyInjectionPointsOptimization() {
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		assertSame(DefaultValues.EMPTY_INJECTION_POINTS, bean1.getInjectionPoints());
	}
	
	@Test
	void testEmptyAnnotationOptimization() {
		InjectionPoint point = new InjectionPoint("pointName", "pointType", locationForDoc1, emptyInjectionAnnotations);
		assertSame(DefaultValues.EMPTY_ANNOTATIONS, point.getAnnotations());
	}
	
	@Test
	void testFindNoMatchingBeansWithEmptySupertypes() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType", locationForDoc1, emptyInjectionPoints, emptySupertypes, emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2});
		
		Bean[] matchingBeans = index.getMatchingBeans("someProject", "");
		assertEquals(0, matchingBeans.length);
		
		matchingBeans = index.getMatchingBeans("someProject", "sometype");
		assertEquals(0, matchingBeans.length);
	}

	@Test
	void testFindMatchingBeansWithOneProject() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType1", locationForDoc1, emptyInjectionPoints, Set.of("supertype1", "supertype2"), emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType2", locationForDoc1, emptyInjectionPoints, Set.of("supertype3", "supertype4", "supertype5"), emptyAnnotations);
		
		index.updateBeans("someProject", new Bean[] {bean1, bean2});
		
		Bean[] matchingBeans = index.getMatchingBeans("someProject", "supertype2");
		assertEquals(1, matchingBeans.length);
		assertSame(bean1, matchingBeans[0]);
		
		matchingBeans = index.getMatchingBeans("someProject", "beanType1");
		assertEquals(1, matchingBeans.length);
		assertSame(bean1, matchingBeans[0]);

		matchingBeans = index.getMatchingBeans("someProject", "supertype5");
		assertEquals(1, matchingBeans.length);
		assertSame(bean2, matchingBeans[0]);

		matchingBeans = index.getMatchingBeans("someProject", "sometype");
		assertEquals(0, matchingBeans.length);

		matchingBeans = index.getMatchingBeans("otherProject", "supertype1");
		assertNull(matchingBeans);
	}
		
	@Test
	void testFindMatchingBeansWithMultipleProjects() {
		SpringMetamodelIndex index = new SpringMetamodelIndex();
		Bean bean1 = new Bean("beanName1", "beanType1", locationForDoc1, emptyInjectionPoints, Set.of("supertype1", "supertype2"), emptyAnnotations);
		Bean bean2 = new Bean("beanName2", "beanType2", locationForDoc1, emptyInjectionPoints, Set.of("supertype3", "supertype4, supertype5"), emptyAnnotations);
		
		Bean bean3 = new Bean("beanName3", "beanType1", locationForDoc1, emptyInjectionPoints, Set.of("supertype1", "supertype2"), emptyAnnotations);
		Bean bean4 = new Bean("beanName4", "beanType2", locationForDoc1, emptyInjectionPoints, Set.of("supertype3", "supertype4, supertype5"), emptyAnnotations);

		index.updateBeans("projectA", new Bean[] {bean1, bean2});
		index.updateBeans("projectB", new Bean[] {bean3, bean4});
		
		Bean[] matchingBeans = index.getMatchingBeans("projectA", "supertype2");
		assertEquals(1, matchingBeans.length);
		assertSame(bean1, matchingBeans[0]);
		
		matchingBeans = index.getMatchingBeans("projectA", "beanType2");
		assertEquals(1, matchingBeans.length);
		assertSame(bean2, matchingBeans[0]);
		
		matchingBeans = index.getMatchingBeans("projectB", "supertype2");
		assertEquals(1, matchingBeans.length);
		assertSame(bean3, matchingBeans[0]);
		
		matchingBeans = index.getMatchingBeans("projectB", "beanType2");
		assertEquals(1, matchingBeans.length);
		assertSame(bean4, matchingBeans[0]);
		
		matchingBeans = index.getMatchingBeans("otherProject", "supertype1");
	}
		
}
