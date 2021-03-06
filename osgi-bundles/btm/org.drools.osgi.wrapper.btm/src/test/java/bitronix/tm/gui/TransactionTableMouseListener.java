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

import bitronix.tm.journal.TransactionLogRecord;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * <p></p>
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class TransactionTableMouseListener extends MouseAdapter {

    private JFrame frame;
    private JTable table;

    public TransactionTableMouseListener(JFrame frame, JTable table) {
        this.frame = frame;
        this.table = table;
    }

    public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            showDetails();
        }
    }

    private void showDetails() {
        TransactionLogRecord tlog = ((TransactionTableModel)table.getModel()).getRow(table.getSelectedRow());
        new TransactionLogDialog(frame, tlog).show();
    }
}
