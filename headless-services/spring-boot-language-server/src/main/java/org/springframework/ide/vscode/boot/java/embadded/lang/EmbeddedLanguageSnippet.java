/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.embadded.lang;

import java.util.List;

import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;

public interface EmbeddedLanguageSnippet {
	
	List<IRegion> toJavaRanges(IRegion range);
	
	default IRegion toSingleJavaRange(IRegion range) {
		List<IRegion> ranges = toJavaRanges(range);
		int start = ranges.get(0).getOffset();
		int end = ranges.get(ranges.size() - 1).getEnd();
		return new Region(start, end - start);
	}
	
	int toJavaOffset(int offset);
	
	String getText();
	
	default IRegion getTotalRange() {
		return toSingleJavaRange(new Region(0, getText().length()));
	}
	
}
