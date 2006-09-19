package org.drools.brms.client.breditor;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/** This encapsulates a DSL line component of a rule. */
public class EditableLine extends Composite {
    
    /** The main panel of the composite. */
    private Panel panel;
   
    /** 
     * This is the list of widgets that are used to display/capture data 
     * Should be Label, TextBox or Button (for editing mode).
     */
    private Widget[] widgets;
    
    public EditableLine(Widget[] items) {
        widgets = items;
        panel = new HorizontalPanel();
        initWidget( panel ); 
        makeReadOnly();
        
    }
    
    public void makeReadOnly() {
        panel.clear();
        panel.add( new Label(toString()) );
    }
    
    public void makeEditable() {
        panel.clear();        
        for ( int i = 0; i < widgets.length; i++ ) {
            panel.add( widgets[i] );            
        }
    }
    
    /**
     * Returns the content.
     */
    public String toString() {
        String result = "";
        for ( int i=0; i < widgets.length; i++ ) {
            Widget element = widgets[i];
            if (element instanceof Label) {
                result = result + ((Label) element).getText();
            } else if (element instanceof TextBox){
                result = result + ((TextBox) element).getText();
            }            
        }
        return result;
    }


    


     
}
