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
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxRouteElementRangesIndexElement;

/**
 * @author Martin Lippert
 */
public class WebfluxRouteElementRangesIndexElementTest {

    @Test
    void testContainsSingleCharacterRange() {
        Range range = new Range(new Position(3, 10), new Position(3, 10));
        WebfluxRouteElementRangesIndexElement information = new WebfluxRouteElementRangesIndexElement(new Range[]{range});

        assertFalse(information.contains(new Position(3, 9)));
        assertTrue(information.contains(new Position(3, 10)));
        assertFalse(information.contains(new Position(3, 11)));
    }

    @Test
    void testContainsSingleLineRange() {
        Range range = new Range(new Position(3, 10), new Position(3, 20));
        WebfluxRouteElementRangesIndexElement information = new WebfluxRouteElementRangesIndexElement(new Range[]{range});

        assertFalse(information.contains(new Position(3, 5)));
        assertTrue(information.contains(new Position(3, 11)));
        assertFalse(information.contains(new Position(3, 25)));

        assertFalse(information.contains(new Position(1, 12)));
        assertFalse(information.contains(new Position(2, 1)));
        assertFalse(information.contains(new Position(4, 21)));
    }

    @Test
    void testContainsMultipleLineRange() {
        Range range = new Range(new Position(2, 10), new Position(4, 5));
        WebfluxRouteElementRangesIndexElement information = new WebfluxRouteElementRangesIndexElement(new Range[]{range});

        assertFalse(information.contains(new Position(1, 1)));
        assertFalse(information.contains(new Position(1, 11)));
        assertFalse(information.contains(new Position(2, 1)));

        assertFalse(information.contains(new Position(2, 9)));
        assertTrue(information.contains(new Position(2, 10)));
        assertTrue(information.contains(new Position(2, 11)));
        assertTrue(information.contains(new Position(2, 40)));
        assertTrue(information.contains(new Position(3, 1)));
        assertTrue(information.contains(new Position(3, 12)));
        assertTrue(information.contains(new Position(3, 50)));
        assertTrue(information.contains(new Position(4, 1)));
        assertTrue(information.contains(new Position(4, 5)));
        assertFalse(information.contains(new Position(4, 6)));
        assertFalse(information.contains(new Position(4, 10)));

        assertFalse(information.contains(new Position(5, 1)));
        assertFalse(information.contains(new Position(5, 20)));
    }

    @Test
    void testContainsMultipleRanges() {
        Range range1 = new Range(new Position(2, 10), new Position(3, 20));
        Range range2 = new Range(new Position(5, 2), new Position(5, 3));
        Range range3 = new Range(new Position(10, 10), new Position(20, 20));
        Range range4 = new Range(new Position(4, 40), new Position(6, 3));

        WebfluxRouteElementRangesIndexElement information = new WebfluxRouteElementRangesIndexElement(new Range[]{range1, range2, range3, range4});

        assertFalse(information.contains(new Position(2, 9)));
        assertTrue(information.contains(new Position(2, 10)));
        assertTrue(information.contains(new Position(3, 19)));
        assertTrue(information.contains(new Position(3, 20)));
        assertFalse(information.contains(new Position(3, 21)));

        assertTrue(information.contains(new Position(5, 1)));
        assertTrue(information.contains(new Position(5, 2)));
        assertTrue(information.contains(new Position(5, 3)));
        assertTrue(information.contains(new Position(5, 4)));

        assertFalse(information.contains(new Position(4, 39)));
        assertTrue(information.contains(new Position(4, 40)));
        assertTrue(information.contains(new Position(4, 41)));

        assertTrue(information.contains(new Position(6, 2)));
        assertTrue(information.contains(new Position(6, 3)));
        assertFalse(information.contains(new Position(6, 4)));

        assertFalse(information.contains(new Position(9, 10)));
        assertFalse(information.contains(new Position(10, 9)));
        assertTrue(information.contains(new Position(10, 10)));
        assertTrue(information.contains(new Position(10, 21)));
        assertTrue(information.contains(new Position(15, 3)));
        assertTrue(information.contains(new Position(20, 20)));
        assertFalse(information.contains(new Position(20, 21)));
        assertFalse(information.contains(new Position(23, 1)));
    }

}
