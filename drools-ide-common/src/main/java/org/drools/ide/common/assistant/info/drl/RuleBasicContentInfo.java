/**
 * Copyright 2010 JBoss Inc
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

package org.drools.ide.common.assistant.info.drl;

public class RuleBasicContentInfo {

	private Integer offset;
	private String content;
	private DRLContentTypeEnum type;
	
	public RuleBasicContentInfo(Integer offset, String content, DRLContentTypeEnum type) {
		this.offset = offset;
		this.content = content;
		this.type = type;
	}
	public Integer getOffset() {
		return offset;
	}
	public String getContent() {
		return content;
	}
	public int getContentLength() {
		return content.length();
	}
	public DRLContentTypeEnum getType() {
		return type;
	}

}
