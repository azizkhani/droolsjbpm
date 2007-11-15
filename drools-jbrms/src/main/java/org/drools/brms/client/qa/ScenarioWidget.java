package org.drools.brms.client.qa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.brms.client.common.DirtyableFlexTable;
import org.drools.brms.client.common.ErrorPopup;
import org.drools.brms.client.common.FormStylePopup;
import org.drools.brms.client.common.ImageButton;
import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.testing.ExecutionTrace;
import org.drools.brms.client.modeldriven.testing.FactData;
import org.drools.brms.client.modeldriven.testing.FieldData;
import org.drools.brms.client.modeldriven.testing.Fixture;
import org.drools.brms.client.modeldriven.testing.RetractFact;
import org.drools.brms.client.modeldriven.testing.Scenario;
import org.drools.brms.client.modeldriven.testing.VerifyFact;
import org.drools.brms.client.modeldriven.testing.VerifyField;
import org.drools.brms.client.modeldriven.testing.VerifyRuleFired;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ScenarioWidget extends Composite {

    private DirtyableFlexTable layout;
	private String[] availableRules;
	private Scenario scenario;
	private SuggestionCompletionEngine sce;




	public ScenarioWidget(Scenario scenario, String[] ruleList, SuggestionCompletionEngine eng) {
    	layout = new DirtyableFlexTable();
    	this.availableRules = ruleList;
    	this.scenario = scenario;
    	this.sce = eng;
        render();

        layout.setStyleName("model-builder-Background");


        initWidget(layout);
        setWidth("100%");
        setHeight("100%");

    }


	private void render() {
		layout.clear();
		ScenarioHelper hlp = new ScenarioHelper();
		List fixtures = hlp.lumpyMap(scenario.fixtures);



        int layoutRow = 0;

        for (int i = 0; i < fixtures.size(); i++) {
			Object f = fixtures.get(i);
			if (f instanceof ExecutionTrace) {
				layout.setWidget(layoutRow, 0, new Label("EXPECT"));
				ExecutionTrace ex = (ExecutionTrace) f;

				layout.setWidget(layoutRow, 1, new ExecutionWidget(ex));
				layout.setWidget(layoutRow, 2, getNewExpectationButton(ex, scenario, availableRules));
				layout.getFlexCellFormatter().setHorizontalAlignment(layoutRow, 2, HasHorizontalAlignment.ALIGN_LEFT);
			} else if (f instanceof Map) {
				layout.setWidget(layoutRow, 0, new Label("GIVEN"));
				layoutRow++;
				Map facts = (Map) f;
				VerticalPanel vert = new VerticalPanel();
		        for (Iterator iterator = facts.entrySet().iterator(); iterator.hasNext();) {
		            Map.Entry e = (Map.Entry) iterator.next();
		            List factList = (List) facts.get(e.getKey());
		            vert.add(new DataInputWidget((String)e.getKey(), factList, false));
		        }
		        layout.setWidget(layoutRow, 1, vert);
			} else {
				List l = (List) f;
				Fixture first = (Fixture) l.get(0);
				if (first instanceof VerifyFact) {
					doVerifyFacts(l, layout, layoutRow);
				} else if (first instanceof VerifyRuleFired) {
					layout.setWidget(layoutRow, 1, new VerifyRulesFiredWidget(l, availableRules, scenario));
				} else {
					layout.setWidget(layoutRow, 1, new RetractWidget(l));
				}

			}
			layoutRow++;
		}


        layout.setWidget(layoutRow, 0, new Label("Configuration"));
        layoutRow++;
        //config section
        ConfigWidget conf = new ConfigWidget(scenario, availableRules);
        layout.setWidget(layoutRow, 1, conf);

        //global section
        Map globals = hlp.lumpyMapGlobals(scenario.globals);
        VerticalPanel globalPanel = new VerticalPanel();
        for (Iterator iterator = globals.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry e = (Map.Entry) iterator.next();
            globalPanel.add(new DataInputWidget((String)e.getKey(), (List) globals.get(e.getKey()), true));
        }
        layout.setWidget(layoutRow, 0, new Label("Globals"));
        layoutRow++;
        layout.setWidget(layoutRow, 1, globalPanel);
	}


	private Widget getNewExpectationButton(final ExecutionTrace ex,
			final Scenario sc, final String[] ruleList) {

		Image add = new ImageButton("images/new_item.gif", "Add a new expectation.", new ClickListener() {
			public void onClick(Widget w) {
				final FormStylePopup pop = new FormStylePopup("images/rule_asset.gif", "New expectation");

		        final ListBox rules = new ListBox();
		        for (int i = 0; i < ruleList.length; i++) {
		            rules.addItem(ruleList[i]);
		        }
		        pop.addRow(rules);
		        Button ok = new Button("Add");

		        ok.addClickListener(new ClickListener() {
		            public void onClick(Widget w) {
		                String r = rules.getItemText(rules.getSelectedIndex());
		                VerifyRuleFired vr = new VerifyRuleFired(r, null, new Boolean(true));
		                sc.insertAfter(ex, vr);
		                render();
		                pop.hide();
		            }
		        });

		        HorizontalPanel h = new HorizontalPanel();
		        h.add(rules);
		        h.add(ok);
				pop.addAttribute("Expect a rule:", h);

				final ListBox facts = new ListBox();
				List names = sc.getFactNamesInScope(ex);
				for (Iterator iterator = names.iterator(); iterator.hasNext();) {
					facts.addItem((String) iterator.next());
				}

				ok = new Button("Add");
				ok.addClickListener(new ClickListener() {
					public void onClick(Widget w) {
						String factName = facts.getItemText(facts.getSelectedIndex());
						sc.insertAfter(ex, new VerifyFact(factName, new ArrayList()));
						render();
					}
				});

				h = new HorizontalPanel();
				h.add(facts);
				h.add(ok);
				pop.addAttribute("Expect values on a fact:", h);

				pop.setPopupPosition(Window.getClientWidth()/3, Window.getClientHeight()/3 );
				pop.show();
			}
		});


		return add;
	}






	private void doVerifyFacts(List l, FlexTable layout, int layoutRow) {
		VerticalPanel vert = new VerticalPanel();
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			final VerifyFact f = (VerifyFact) iterator.next();
			HorizontalPanel h = new HorizontalPanel();
			h.add(new VerifyFactWidget(f, scenario, sce));
			Image del = new ImageButton("images/delete_obj.gif", "Delete the expectation for this fact.", new ClickListener() {
				public void onClick(Widget w) {
					if (Window.confirm("Are you sure you want to remove this expectation?")) {
						scenario.removeFixture(f);
						render();
					}
				}
			});
			h.add(del);
			vert.add(h);
		}
		layout.setWidget(layoutRow, 1, vert);

	}




}


/**
 * For capturing input for all the facts of a given type.
 * @author Michael Neale
 */
class DataInputWidget extends Composite {


    private Grid outer;



	public DataInputWidget(String factType, List defList, boolean isGlobal) {

        outer = new Grid(2, 1);
        outer.getCellFormatter().setStyleName(0, 0, "modeller-fact-TypeHeader");
        outer.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE );
        outer.setStyleName("modeller-fact-pattern-Widget");


        if (isGlobal) {
            outer.setWidget(0, 0, new Label("Global: " + factType));
        } else {
            FactData first = (FactData) defList.get(0);
            if (first.isModify) {
            	outer.setWidget(0, 0, new Label("Modify: " + factType));
            } else {
            	outer.setWidget(0, 0, new Label("Insert: " + factType));
            }
        }

        FlexTable t = render(defList);

        outer.setWidget(1, 0, t);
        initWidget(outer);
    }

	private FlexTable render(final List defList) {
		DirtyableFlexTable t = new DirtyableFlexTable();

		//This will work out what row is for what field
        Map fields = new HashMap();
        int col = 0;
        int totalCols = defList.size();
        for (Iterator iterator = defList.iterator(); iterator.hasNext();) {
            final FactData d = (FactData) iterator.next();
            for (int i = 0; i < d.fieldData.size(); i++) {
                final FieldData fd = (FieldData) d.fieldData.get(i);
                if (!fields.containsKey(fd.name)) {
                    int idx = fields.size() + 1;
                    fields.put(fd.name, new Integer(idx));
                    t.setWidget(idx, 0, new Label(fd.name + ":"));
                    Image del = new ImageButton("images/delete_item_small.gif", "Remove this row.", new ClickListener() {
        				public void onClick(Widget w) {
        					if (Window.confirm("Are you sure you want to remove this row ?")) {
        						ScenarioHelper.removeFields(defList, fd.name);
        						outer.setWidget(1, 0, render(defList));

        					}
        				}
        			});
                    t.setWidget(idx, totalCols + 1, del);
                    t.getCellFormatter().setHorizontalAlignment(idx, 0, HasHorizontalAlignment.ALIGN_RIGHT);
                }
            }
        }

        //now we go through the facts and the fields, adding them to the grid
        //if a fact is missing a FieldData, we will add it in (so people can enter data later on)
          col = 0;
        for (Iterator iterator = defList.iterator(); iterator.hasNext();) {
            FactData d = (FactData) iterator.next();
            t.setWidget(0, ++col, new Label(d.name));
            Map presentFields = new HashMap(fields);
            for (int i = 0; i < d.fieldData.size(); i++) {
                FieldData fd = (FieldData) d.fieldData.get(i);
                int fldRow = ((Integer) fields.get(fd.name)).intValue();
                t.setWidget(fldRow, col, editableCell(fd));
                presentFields.remove(fd.name);
            }

            for (Iterator missing = presentFields.entrySet().iterator(); missing.hasNext();) {
                Map.Entry e = (Map.Entry) missing.next();
                int fldRow = ((Integer) e.getValue()).intValue();
                FieldData fd = new FieldData((String) e.getKey(), "");
                d.fieldData.add(fd);
                t.setWidget(fldRow, col, editableCell(fd));
            }
        }
        return t;
	}



	private Widget editableCell(final FieldData fd) {
        final TextBox tb = new TextBox();
        tb.setText(fd.value);
        tb.setTitle("Value for: " + fd.name);
        tb.addChangeListener(new ChangeListener() {
            public void onChange(Widget w) {
                fd.value = tb.getText();
            }
        });
        return tb;
    }




}

class ConfigWidget extends Composite {
    public ConfigWidget(final Scenario sc, final String[] fullRuleList) {

        final ListBox box = new ListBox(true);

        for (int i = 0; i < sc.rules.size(); i++) {
            box.addItem((String)sc.rules.get(i));
        }
        HorizontalPanel filter = new HorizontalPanel();

        final Image add = new ImageButton("images/new_item.gif", "Add a new rule.");
        add.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                showRulePopup(w, box, fullRuleList, sc.rules);
            }
        });

        final Image remove = new ImageButton("images/trash.gif", "Remove selected rule.");
        remove.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                if (box.getSelectedIndex() == -1) {
                    Window.alert("Please choose a rule to remove.");
                } else {
                    String r = box.getItemText(box.getSelectedIndex());
                    sc.rules.remove(r);
                    box.removeItem(box.getSelectedIndex());
                }
            }
        });
        VerticalPanel actions = new VerticalPanel();
        actions.add(add); actions.add(remove);




        final ListBox drop = new ListBox();
        drop.addItem("Allow these rules to fire:", "inc");
        drop.addItem("Prevent these rules from firing:", "exc");
        drop.addItem("All rules may fire");
        drop.addChangeListener(new ChangeListener() {
            public void onChange(Widget w) {
                String s = drop.getValue(drop.getSelectedIndex());
                if (s.equals("inc")) {
                    sc.inclusive = true;
                    add.setVisible(true); remove.setVisible(true); box.setVisible(true);
                } else if (s.equals("exc")) {
                    sc.inclusive = false;
                    add.setVisible(true); remove.setVisible(true); box.setVisible(true);
                } else {
                    sc.rules.clear();
                    box.clear();
                    box.setVisible(false); add.setVisible(false); remove.setVisible(false);
                }
            }
        });

        if (sc.rules.size() > 0) {
        	drop.setSelectedIndex((sc.inclusive) ? 0 : 1);
        }


        filter.add(drop);
        filter.add(box);
        filter.add(actions);

        initWidget(filter);
    }

    private void showRulePopup(Widget w, final ListBox box, String[] fullRuleList, final List filterList) {
        final FormStylePopup pop = new FormStylePopup("images/rule_asset.gif", "Select rule");
        final ListBox rules = new ListBox();
        for (int i = 0; i < fullRuleList.length; i++) {
            rules.addItem(fullRuleList[i]);
        }
        pop.addRow(rules);
        Button ok = new Button("Add");
        pop.addRow(ok);
        ok.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                String r = rules.getItemText(rules.getSelectedIndex());
                filterList.add(r);
                box.addItem(r);
                pop.hide();
            }
        });
        pop.setPopupPosition(w.getAbsoluteLeft(), w.getAbsoluteTop());
        pop.show();

    }


}

class ExecutionWidget extends Composite {
    public ExecutionWidget(final ExecutionTrace ext) {


    	final Widget dt = simulDate(ext);
    	dt.setVisible(ext.scenarioSimulatedDate != null);

    	final ListBox choice = new ListBox();
    	choice.addItem("Use real date and time");
    	choice.addItem("Use a simulated date and time");
    	choice.setSelectedIndex((ext.scenarioSimulatedDate == null) ? 0 : 1);
    	choice.addChangeListener(new ChangeListener() {
			public void onChange(Widget w) {
				if (choice.getSelectedIndex() == 0) {
					dt.setVisible( false );
					ext.scenarioSimulatedDate = null;
				} else {
					dt.setVisible(true);
				}
			}
		});

    	HorizontalPanel p = new HorizontalPanel();
    	p.add(new Image("images/execution_trace.gif"));
    	p.add(choice);
    	p.add(dt);



        initWidget(p);
    }



    private Widget simulDate(final ExecutionTrace ext) {
    	HorizontalPanel ab = new HorizontalPanel();
        final String fmt = "dd-MMM-YYYY";
        final TextBox dt = new TextBox();
        if (ext.scenarioSimulatedDate == null) {
            dt.setText("<" + fmt + ">");
        } else {
            dt.setText(ext.scenarioSimulatedDate.toLocaleString());
        }

        final Label dateHint = new Label();




        dt.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget arg0, char arg1, int arg2) {


			}

			public void onKeyPress(Widget arg0, char arg1, int arg2) {


			}

			public void onKeyUp(Widget w, char arg1, int arg2) {
				try {

					Date d = new Date(dt.getText());
					dateHint.setText(d.toLocaleString());

				} catch (Exception e) {
					dateHint.setText("...");
				}

			}

        });

        dt.addChangeListener(new ChangeListener() {
            public void onChange(Widget w) {
                if (dt.getText().trim().equals("")) {
                    dt.setText("<current date and time>");
                } else {
                    try {
                        Date d = new Date(dt.getText());
                        ext.scenarioSimulatedDate = d;
                        dt.setText(d.toLocaleString());
                        dateHint.setText("");
                    } catch (Exception e) {
                        ErrorPopup.showMessage("Bad date format - please try again (try the format of " + fmt + ").");
                    }
                }
            }
        });
        ab.add(dt);
        ab.add(dateHint);
        return ab;
    }


}

class VerifyFactWidget extends Composite {
    private Grid outer;

	public VerifyFactWidget(final VerifyFact vf, final Scenario sc, final SuggestionCompletionEngine sce) {
        outer = new Grid(2, 1);
        outer.getCellFormatter().setStyleName(0, 0, "modeller-fact-TypeHeader");
        outer.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE );
        outer.setStyleName("modeller-fact-pattern-Widget");
        HorizontalPanel ab = new HorizontalPanel();
        ab.add(new Label("Expect [" + vf.factName + "]"));

        Image add = new ImageButton("images/add_field_to_fact.gif", "Add a field to this expectation.", new ClickListener() {
			public void onClick(Widget w) {
				String t = (String) sc.getVariableTypes().get(vf.factName);
				String[] fields = (String[]) sce.fieldsForType.get(t);
				final FormStylePopup pop = new FormStylePopup("images/rule_asset.gif", "Choose a field to add");
				final ListBox b = new ListBox();
				for (int i = 0; i < fields.length; i++) {
					b.addItem(fields[i]);
				}
				pop.addRow(b);
				Button ok = new Button("OK");
				ok.addClickListener(new ClickListener() {
									public void onClick(Widget w) {
										String f = b.getItemText(b.getSelectedIndex());
										vf.fieldValues.add(new VerifyField(f, "", "=="));
								        FlexTable data = render(vf);
								        outer.setWidget(1, 0, data);
								        pop.hide();
									}
								});
				pop.addRow(ok);
				pop.setPopupPosition(w.getAbsoluteLeft(), w.getAbsoluteTop());
				pop.show();

			}
		});

        ab.add(add);
        outer.setWidget(0, 0, ab);
        initWidget(outer);

        FlexTable data = render(vf);
        outer.setWidget(1, 0, data);

    }

	private FlexTable render(final VerifyFact vf) {
		FlexTable data = new FlexTable();
        for (int i = 0; i < vf.fieldValues.size(); i++) {
            final VerifyField fld = (VerifyField) vf.fieldValues.get(i);
            data.setWidget(i, 0, new Label(fld.fieldName + ":"));
            data.getFlexCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_RIGHT);

            final ListBox opr = new ListBox();
            opr.addItem("equals", "==");
            opr.addItem("does not equal", "!=");
            if (fld.operator.equals("==")) {
                opr.setSelectedIndex(0);
            } else {
                opr.setSelectedIndex(1);
            }
            opr.addChangeListener(new ChangeListener() {
                public void onChange(Widget w) {
                    fld.operator = opr.getValue(opr.getSelectedIndex());
                }
            });

            data.setWidget(i, 1, opr);

            final TextBox input = new TextBox();
            input.setText(fld.expected);
            input.addChangeListener(new ChangeListener() {
                public void onChange(Widget w) {
                    fld.expected = input.getText();
                }
            });
            data.setWidget(i, 2, input);

            Image del = new ImageButton("images/delete_item_small.gif", "Remove this field expectation.", new ClickListener() {
				public void onClick(Widget w) {
					if (Window.confirm("Are you sure you want to remove this field expectation?")) {
						vf.fieldValues.remove(fld);
				        FlexTable data = render(vf);
				        outer.setWidget(1, 0, data);
					}
				}
			});
            data.setWidget(i, 3, del);

        }
		return data;
	}

}

class VerifyRulesFiredWidget extends Composite {
    private Grid outer;

	/**
     * @param rfl List<VeryfyRuleFired>
     * @param rules = the list of rules to choose from
     * @param scenario = the scenario to add/remove from
     */
    public VerifyRulesFiredWidget(final List rfl, final String[] rules, final Scenario scenario) {
        outer = new Grid(2, 1);

        outer.getCellFormatter().setStyleName(0, 0, "modeller-fact-TypeHeader");
        outer.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE );
        outer.setStyleName("modeller-fact-pattern-Widget");

        outer.setWidget(0, 0, new Label("Expect rules"));
        initWidget(outer);

        FlexTable data = render(rfl, scenario);
        outer.setWidget(1, 0, data);
    }



	private FlexTable render(final List rfl, final Scenario sc) {
		FlexTable data = new DirtyableFlexTable();


        for (int i = 0; i < rfl.size(); i++) {
            final VerifyRuleFired v = (VerifyRuleFired) rfl.get(i);
            data.setWidget(i, 0, new Label(v.ruleName + ":"));
            data.getFlexCellFormatter().setAlignment(i, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);


            final ListBox b = new ListBox();
            b.addItem("fired at least once", "y");
            b.addItem("did not fire", "n");
            b.addItem("fired this many times: ", "e");
            final TextBox num = new TextBox();
            num.setVisibleLength(5);

            if (v.expectedFire != null ) {
                b.setSelectedIndex((v.expectedFire.booleanValue()) ? 0 : 1);
                num.setVisible(false);
            } else {
                b.setSelectedIndex(2);
                String xc = (v.expectedCount != null)? "" + v.expectedCount.intValue() : "0";
                num.setText(xc);
            }

            b.addChangeListener(new ChangeListener() {
                public void onChange(Widget w) {
                    String s = b.getValue(b.getSelectedIndex());
                    if (s.equals("y") || s.equals("n")) {
                        num.setVisible(false);
                        v.expectedCount = null;
                    } else {
                        num.setVisible(true);
                        v.expectedFire = null;
                        num.setText("1"); v.expectedCount = new Integer(1);
                    }
                }
            });

            num.addChangeListener(new ChangeListener() {
                public void onChange(Widget w) {
                    v.expectedCount = new Integer(num.getText());
                }
            });

            HorizontalPanel h = new HorizontalPanel();
            h.add(b); h.add(num);
            data.setWidget(i, 1, h);

            Image del = new ImageButton("images/delete_item_small.gif", "Remove this rule expectation.", new ClickListener() {
				public void onClick(Widget w) {
					if (Window.confirm("Are you sure you want to remove this rule expectation?")) {
						rfl.remove(v);
						sc.removeFixture(v);
						outer.setWidget(1, 0, render(rfl, sc));
					}
				}
			});

            data.setWidget(i, 2, del);

            //we only want numbers here...
            num.addKeyboardListener(new KeyboardListener() {
                    public void onKeyDown(Widget arg0, char arg1, int arg2) {}

                    public void onKeyPress(Widget w, char c, int i) {
                        if (Character.isLetter( c ) ) {
                            ((TextBox) w).cancelKey();
                        }
                    }

                    public void onKeyUp(Widget arg0, char arg1, int arg2) {}
                } );
        }
		return data;
	}
}

class RetractWidget extends Composite {
	public RetractWidget(List retList) {
        Grid outer = new Grid(1, 1);
        outer.getCellFormatter().setStyleName(0, 0, "modeller-fact-TypeHeader");
        outer.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE );
        outer.setStyleName("modeller-fact-pattern-Widget");
        outer.setWidget(0, 0, new Label("Retract facts"));
        VerticalPanel inner = new VerticalPanel();
        for (Iterator iterator = retList.iterator(); iterator.hasNext();) {
			RetractFact r = (RetractFact) iterator.next();
			inner.add(new Label(r.name));
		}
        outer.setWidget(1, 0, inner);
        initWidget(outer);
	}
}
