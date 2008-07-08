package org.drools.guvnor.client.ruleeditor;

/**
 * This is used to notify that a save even is about to occur.
 * @author Michael Neale
 *
 */
public interface SaveEventListener {

	void onSave();

	void onAfterSave();
}
