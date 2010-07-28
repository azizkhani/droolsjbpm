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

package bitronix.tm.gui;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.utils.PropertyUtils;
import bitronix.tm.resource.ResourceLoader;
import bitronix.tm.resource.common.ResourceBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class ResourcesPanel extends JPanel {

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private JTree resourcesTree = new JTree();
    private JScrollPane resourcesTreeScrollpane = new JScrollPane(resourcesTree);

    private JTextArea activeResource = new JTextArea();

    public ResourcesPanel() {
        setLayout(new GridLayout(1, 1));
        resourcesTree.setModel(new ResourcesTreeModel());

        resourcesTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JTree tree = (JTree) evt.getSource();
                int selectedRow = tree.getSelectionRows()[0] -1;

                ResourceLoader resourceLoader = TransactionManagerServices.getResourceLoader();
                Iterator it = resourceLoader.getResources().entrySet().iterator();
                ResourceBean resource = null;
                for (int i=0; i<selectedRow+1 ;i++) {
                    Map.Entry entry = (Map.Entry) it.next();
                    resource = (ResourceBean) entry.getValue();
                }


                if (resource != null) {
                    try {
                        Map properties = PropertyUtils.getProperties(resource);
                        StringBuffer sb = new StringBuffer();
                        Iterator it2 = properties.entrySet().iterator();
                        while (it2.hasNext()) {
                            Map.Entry entry = (Map.Entry) it2.next();
                            String name = (String) entry.getKey();
                            Object valueObject = entry.getValue();
                            String value = valueObject == null ? null : valueObject.toString();

                            sb.append(name);
                            sb.append('=');
                            sb.append(value);
                            if (it2.hasNext())
                                sb.append('\n');
                        }

                        activeResource.setText(sb.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "error querying resource loader", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                    activeResource.setText("");
            }
        });

        splitPane.add(resourcesTreeScrollpane);
        splitPane.add(activeResource);
        add(splitPane);
    }

}
