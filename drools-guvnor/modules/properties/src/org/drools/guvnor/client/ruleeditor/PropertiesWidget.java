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

package org.drools.guvnor.client.ruleeditor;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.grid.GridView;
import com.gwtext.client.widgets.grid.PropertyGridPanel;
import org.drools.guvnor.client.packages.AssetAttachmentFileWidget;
import org.drools.guvnor.client.rpc.RuleAsset;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PropertiesWidget extends AssetAttachmentFileWidget implements SaveEventListener {

    PropertiesHolder properties;

    public PropertiesWidget(final RuleAsset asset, final RuleViewer viewer) {
        super(asset, viewer);

        if (asset.content == null) {
            properties = new PropertiesHolder();
        } else {
            properties = (PropertiesHolder) asset.content;
        }

        PropertyGridPanel grid = new PropertyGridPanel();
        grid.setId("props-grid");
        grid.setNameText("Properties Grid");
        grid.setWidth(300);
        grid.setAutoHeight(true);
        grid.setSorted(false);

        GridView view = new GridView();
        view.setForceFit(true);
        view.setScrollOffset(2); // the grid will never have scrollbars
        grid.setView(view);


        Map<String, String> map = new HashMap<String, String>();
        for (PropertyHolder holder : properties.list) {
            map.put(holder.name, holder.value);
        }

        grid.setSource(map);

        Toolbar toolbar = new Toolbar();
        ToolbarButton button = new ToolbarButton("Add ...", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                //TODO: add new rows to the grid or a value to the properties holder
            }
        });
        toolbar.addButton(button);

        layout.addRow(grid);
    }


    public String getIcon() {
        return "";
    }

    public String getOverallStyleName() {
        return "";
    }

    public void onSave() {
        //todo: fill the properties holder
    }

    public void onAfterSave() {
        //todo:refresh widget
    }

}


