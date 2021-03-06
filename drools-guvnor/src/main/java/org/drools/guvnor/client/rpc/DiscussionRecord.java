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

package org.drools.guvnor.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * This is a discussion record item - persisted via Discussion.java (xstream) to XML, as a
 * a property on an AssetItem.
 * 
 * @author Michael Neale
 */
public class DiscussionRecord implements IsSerializable {

    public DiscussionRecord() {}
    public DiscussionRecord(String userName, String note) {
        this.author = userName;
        this.note = note;
    }
    public long timestamp = (new Date()).getTime();
    public String note;
    public String author;
}
