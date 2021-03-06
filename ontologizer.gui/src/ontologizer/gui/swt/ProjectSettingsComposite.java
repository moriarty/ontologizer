/*
 * Created on 19.11.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ontologizer.gui.swt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ontologizer.gui.swt.support.FileGridCompositeWidgets;
import ontologizer.gui.swt.support.SWTUtil;
import ontologizer.util.LinkExtractor;
import ontologizer.util.LinkExtractor.Extracted;
import ontologizer.worksets.WorkSet;
import ontologizer.worksets.WorkSetList;

/**
 * A simple expander widget.
 *
 * @author Sebastian Bauer
 */
class Expander extends Composite
{
	private Composite control;
	private Button expandButton;

	private boolean visible;

	public Expander(Composite parent, int style)
	{
		super(parent, style);

		this.setLayout(SWTUtil.newEmptyMarginGridLayout(1));

		expandButton = new Button(this,0);
		expandButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				control.setVisible(!control.getVisible());

				visible = control.getVisible();
				updateButtonText();

			}
		});
		updateButtonText();
	}

	public void setText(String text)
	{
		expandButton.setText(text);
	}

	public void setControl(Composite control)
	{
		this.control = control;
		control.setVisible(visible);
		control.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.FILL_BOTH));
	}

	private void updateButtonText()
	{
		if (!visible)
			expandButton.setText("Show Advanced Options >>");
		else
			expandButton.setText("<< Hide Advanced Options");
		expandButton.pack(true);
		if (getParent() != null)
			getParent().layout();
	}

	void setExpandedState(boolean visible)
	{
		this.visible = visible;
		control.setVisible(visible);
		updateButtonText();
	}
}

/**
 * The Composite of project settings.
 *
 * TODO: Add a ProjectSettings class
 *
 * @author Sebastian Bauer
 */
public class ProjectSettingsComposite extends Composite
{
	/**
	 * Private class holding description for evidence codes.
	 *
	 * @author Sebastian Bauer
	 */
	private static class Evidence
	{
		public String name;
		public String description;
		public String cl;

		public Evidence(String name, String description, String cl)
		{
			this.name = name;
			this.description = description;
			this.cl = cl;
		}
	};

	/**
	 * Supported evidence codes.
	 */
	private static Evidence [] EVIDENCES = new Evidence[]{
		new Evidence("EXP","Inferred from Experiment","Experimental Evidence Codes"),
		new Evidence("IDA","Inferred from Direct Assay","Experimental Evidence Codes"),
		new Evidence("IPI","Inferred from Physical Interaction","Experimental Evidence Codes"),
		new Evidence("IMP","Inferred from Mutant Phenotype","Experimental Evidence Codes"),
		new Evidence("IGI","Inferred from Genetic Interaction","Experimental Evidence Codes"),
		new Evidence("IEP","Inferred from Expression Pattern","Experimental Evidence Codes"),

		new Evidence("ISS","Inferred from Sequence or Structural Similarity","Computational Analysis Evidence Codes"),
		new Evidence("ISO","Inferred from Sequence Orthology","Computational Analysis Evidence Codes"),
		new Evidence("ISA","Inferred from Sequence Alignment","Computational Analysis Evidence Codes"),
		new Evidence("ISM","Inferred from Sequence Model","Computational Analysis Evidence Codes"),
		new Evidence("IGC","Inferred from Genomic Context","Computational Analysis Evidence Codes"),
		new Evidence("IBA","Inferred from Biological aspect of Ancestor","Computational Analysis Evidence Codes"),
		new Evidence("IBD","Inferred from Biological aspect of Descendant","Computational Analysis Evidence Codes"),
		new Evidence("IKR","Inferred from Key Residues","Computational Analysis Evidence Codes"),
		new Evidence("IRD","Inferred from Rapid Divergence","Computational Analysis Evidence Codes"),
		new Evidence("RCA","Inferred from Reviewed Computational Analysis","Computational Analysis Evidence Codes"),

		new Evidence("TAS","Traceable Author Statement","Author Statement Evidence Codes"),
		new Evidence("NAS","Non-traceable Author Statement","Author Statement Evidence Codes"),

		new Evidence("TAS","Traceable Author Statement","Author Statement Evidence Codes"),
		new Evidence("NAS","Non-traceable Author Statement","Author Statement Evidence Codes"),

		new Evidence("IC","Inferred by Curator","Curator Statement Evidence Codes"),
		new Evidence("ND","No biological Data available","Curator Statement Evidence Codes"),

		new Evidence("IEA","Inferred from Electronic Annotation","Automatically-assigned Evidence Codes"),

		new Evidence("NR","Not Recorded","Obsolete Evidence Codes")
	};

	private static Map<String,Evidence> EVIDENCE_MAP;

	/**
	 * Initialize private static data;
	 */
	static
	{
		/* Initialize evidence map */
		EVIDENCE_MAP = new HashMap<String, ProjectSettingsComposite.Evidence>();
		for (Evidence evi : EVIDENCES)
			EVIDENCE_MAP.put(evi.name, evi);
	}

	private Combo workSetCombo = null;
	private FileGridCompositeWidgets ontologyFileGridCompositeWidgets = null;
	private FileGridCompositeWidgets assocFileGridCompositeWidgets = null;
	private FileGridCompositeWidgets mappingFileGridCompositeWidgets = null;
	private Combo subsetCombo;
	private Combo considerCombo;
	private Table evidenceTable;
	private TableColumn evidenceNameColumn;
	private TableColumn evidenceNumberColumn;
	private TableColumn evidenceDescColumn;
	private Composite advancedComposite;
	private Expander advancedExpander;

	private StyledText infoText;
	private Extracted infoTextExtracted;
	private String lastInfoText;

	private Button subsetCheckbox;
	private Button considerCheckbox;

	private List<ISimpleAction> ontologyChangedList = new ArrayList<ISimpleAction>();
	private List<ISimpleAction> associationChangedList = new ArrayList<ISimpleAction>();

	public static interface InfoTextClickListener
	{
		public void click(String href);
	}

	private List<InfoTextClickListener> infoTextClickListenerList = new ArrayList<InfoTextClickListener>();

	private WorkSetList wsl;

	public ProjectSettingsComposite(Composite parent, int style)
	{
		this(parent,style,true);
	}

	public ProjectSettingsComposite(Composite parent, int style, boolean provideAdvancedOptions)
	{
		super(parent, style);

		wsl = new WorkSetList();

		this.setLayout(new GridLayout(3,false));

		Label workSetLabel = new Label(this,0);
		workSetLabel.setText("File Set");
		workSetLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		workSetCombo = new Combo(this,SWT.BORDER);
		workSetCombo.setToolTipText("Choose files from predefined file sets.");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		workSetCombo.setLayoutData(gd);

		ontologyFileGridCompositeWidgets = new FileGridCompositeWidgets(this);
		ontologyFileGridCompositeWidgets.setLabel("Ontology");
		ontologyFileGridCompositeWidgets.setToolTipText("Specifies the ontology file (OBO file format) which defines the GO terms and their structure.");
		ontologyFileGridCompositeWidgets.setFilterExtensions(new String[]{"*.obo","*.*"});
		ontologyFileGridCompositeWidgets.setFilterNames(new String[]{"OBO File","All files"});

		assocFileGridCompositeWidgets = new FileGridCompositeWidgets(this);
		assocFileGridCompositeWidgets.setLabel("Annotations");
		assocFileGridCompositeWidgets.setToolTipText("Specifies the annotation (association) file, which assigns GO terms to the names of the gene products.");
		assocFileGridCompositeWidgets.setFilterExtensions(new String[]{"gene_association.*","*.csv","*.ids","*.*"});
		assocFileGridCompositeWidgets.setFilterNames(new String[]{"Association File","Affymetrix","All files"});

/* TODO: Use ExpandableComposite comp of JFace */

		if (provideAdvancedOptions)
		{
			advancedExpander = new Expander(this,0);
			gd = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.VERTICAL_ALIGN_BEGINNING);
			gd.horizontalSpan = 3;
			advancedExpander.setLayoutData(gd);

			advancedComposite = new Composite(advancedExpander, 0);
			advancedComposite.setLayout(SWTUtil.newEmptyMarginGridLayout(3));
			advancedExpander.setControl(advancedComposite);

			subsetCheckbox = new Button(advancedComposite,SWT.CHECK);
			subsetCheckbox.setText("Use Subset of Ontology");
			subsetCheckbox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			subsetCheckbox.setEnabled(false);
			subsetCheckbox.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					updateSubsetEnabled();
				}
			});

			subsetCombo = new Combo(advancedComposite,SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
			gd.horizontalSpan = 2;
			subsetCombo.setLayoutData(gd);
			subsetCombo.setEnabled(false);

			considerCheckbox = new Button(advancedComposite,SWT.CHECK);
			considerCheckbox.setText("Consider Terms from");
			considerCheckbox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			considerCheckbox.setEnabled(false);
			considerCheckbox.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					updateConsiderEnabled();
				}
			});
			considerCombo = new Combo(advancedComposite,SWT.BORDER);
			considerCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
			Label subOntologyLabel = new Label(advancedComposite,0);
			subOntologyLabel.setText("Subontology");
			considerCombo.setEnabled(false);

			mappingFileGridCompositeWidgets = new FileGridCompositeWidgets(advancedComposite,true);
			mappingFileGridCompositeWidgets.setLabel("Mapping");
			mappingFileGridCompositeWidgets.setToolTipText("Specifies an additional mapping file in which each line consits of a single name mapping. The name of the first column is mapped to the name of the second column before the annotation process begins. Columns should be tab-separated.");
			mappingFileGridCompositeWidgets.setFilterExtensions(new String[]{"*.*"});
			mappingFileGridCompositeWidgets.setFilterNames(new String[]{"All files"});

			Label evidenceLabel = new Label(advancedComposite,0);
			evidenceLabel.setText("Evidences");
			evidenceLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			gd = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_BOTH);
			gd.horizontalSpan = 2;
			evidenceTable = new Table(advancedComposite, SWT.BORDER|SWT.CHECK);
			evidenceTable.setLayoutData(gd);
			evidenceTable.setEnabled(false);
			evidenceTable.setToolTipText("An evidence code specifies how the annotation to the term is supported. This selection defines the evidence codes that are considered.");
			evidenceTable.setMenu(createEvidenceMenu(evidenceTable));
			evidenceNameColumn = new TableColumn(evidenceTable, SWT.NONE);
			evidenceNumberColumn = new TableColumn(evidenceTable, SWT.NONE);
			evidenceDescColumn = new TableColumn(evidenceTable, SWT.NONE);
		}

		/* If a new work set has been selected */
		workSetCombo.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				String currentWorkSet = getSelectedWorksetName();
				if (wsl != null && currentWorkSet.length() > 0)
				{
					WorkSet ws = wsl.get(currentWorkSet);
					if (ws != null)
					{
						setAssociationsFileString(ws.getAssociationPath());
						setDefinitonFileString(ws.getOboPath());

						for (ISimpleAction act : ontologyChangedList)
							act.act();
						for (ISimpleAction act : associationChangedList)
							act.act();
					}
				}
			}
		});

		createInfoText(this);
	}

	/**
	 * Creates the context menu for the evidence table.
	 *
	 * @return
	 */
	private Menu createEvidenceMenu(Control parent)
	{
		Menu evidenceMenu = new Menu(parent);
		MenuItem all = new MenuItem(evidenceMenu, 0);
		all.setText("Select All");
		all.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TableItem [] items = evidenceTable.getItems();
				for (int i=0; i < items.length; i++)
					items[i].setChecked(true);
			}
		});
		MenuItem clear = new MenuItem(evidenceMenu, 0);
		clear.setText("Clear Selection");
		clear.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TableItem [] items = evidenceTable.getItems();
				for (int i=0; i < items.length; i++)
					items[i].setChecked(false);
			}
		});
		return evidenceMenu;
	}

	/**
	 * Makes the info styled text visible (if not done)
	 */
	private void createInfoText(Composite parent)
	{
		if (infoText != null) return;

		infoText = new StyledText(parent, SWT.WRAP);
		infoText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_END);
		gd.horizontalSpan = 3;
		infoText.setLayoutData(gd);
		infoText.addListener(SWT.MouseUp, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				if (infoTextExtracted == null)
					return;

				try
				{
					int offset = infoText.getOffsetAtLocation(new Point(event.x, event.y));
					for (int i = 0; i < infoTextExtracted.starts.length; i++)
					{
						if (infoTextExtracted.starts[i] <= offset && offset < infoTextExtracted.ends[i])
						{
							for (InfoTextClickListener listener : infoTextClickListenerList)
								listener.click(infoTextExtracted.hrefs[i]);
							break;
						}
					}
				} catch(IllegalArgumentException e)
				{
				}
			}
		});
	}

	/**
	 * Updates the enable status of the subset widget in accordance to
	 * the current state of the checkbox.
	 */
	private void updateSubsetEnabled()
	{
		subsetCombo.setEnabled(subsetCheckbox.getSelection() && subsetCombo.getItemCount() > 0);
	}

	/**
	 * Updates the enable status of the consider widget in accordance to
	 * the current state of the checkbox.
	 */
	private void updateConsiderEnabled()
	{
		considerCombo.setEnabled(considerCheckbox.getSelection() && considerCombo.getItemCount() > 0);
	}

	public String getDefinitionFileString()
	{
		return ontologyFileGridCompositeWidgets.getPath();
	}

	/**
	 * Sets the definition file string.
	 *
	 * @param string
	 */
	public void setDefinitonFileString(String string)
	{
		ontologyFileGridCompositeWidgets.setPath(string!=null?string:"");
	}

	/**
	 * Returns the mapping file string.
	 *
	 * @return
	 */
	public String getMappingFileString()
	{
		return mappingFileGridCompositeWidgets.getPath();
	}

	/**
	 * Sets the mapping file string.
	 *
	 * @param string
	 */
	public void setMappingFileString(String string)
	{
		string = string!=null?string:"";
		mappingFileGridCompositeWidgets.setPath(string);
	}

	/**
	 * Returns the currently selected subset.
	 *
	 * @return
	 */
	public String getSubsetString()
	{
		if (!subsetCheckbox.getSelection()) return "";
		int idx = subsetCombo.getSelectionIndex();
		if (idx >= 0)
			return subsetCombo.getItems()[idx];
		return subsetCombo.getText();
	}

	/**
	 * Returns the currently selected subontology.
	 * @return
	 */
	public String getSubontologyString()
	{
		if (!considerCheckbox.getSelection()) return "";

		int idx = considerCombo.getSelectionIndex();
		if (idx >= 0)
			return considerCombo.getItem(idx);

		return considerCombo.getText();

	}

	/**
	 * Returns the association file string.
	 *
	 * @return
	 */
	public String getAssociationsFileString()
	{
		return assocFileGridCompositeWidgets.getPath();
	}

	public void setAssociationsFileString(String string)
	{
		assocFileGridCompositeWidgets.setPath(string!=null?string:"");
	}

	public void updateWorkSetList(WorkSetList wsl)
	{
		workSetCombo.removeAll();
		this.wsl.clear();

		for (WorkSet ws : wsl)
		{
			this.wsl.add(ws.clone());
			workSetCombo.add(ws.getName());
		}
	}

	public String getSelectedWorksetName()
	{
		return workSetCombo.getText();
	}

	public WorkSet getSelectedWorkset()
	{
		WorkSet ws = new WorkSet(workSetCombo.getText());
		ws.setAssociationPath(assocFileGridCompositeWidgets.getPath());
		ws.setOboPath(ontologyFileGridCompositeWidgets.getPath());
		return ws;
	}

	public void setRestrictionChoices(String[] choices)
	{
		if (choices == null) choices = new String[0];
		subsetCheckbox.setEnabled(choices.length > 0);
//		subsetCombo.setEnabled(choices.length > 0);
		subsetCombo.setItems(choices);
		updateSubsetEnabled();
	}

	public void setConsiderChoices(String [] choices)
	{
		if (choices == null) choices = new String[0];
		considerCheckbox.setEnabled(choices.length > 0);
		considerCombo.setItems(choices);
		updateConsiderEnabled();
	}

	public void setConsider(String subontology)
	{
		considerCombo.setText(subontology);
		considerCheckbox.setSelection(subontology.length() > 0);
		updateConsiderEnabled();

		if (subontology.length() > 0)
			advancedExpander.setExpandedState(true);
	}

	public void setRestriction(String subset)
	{
		subsetCombo.setText(subset);
		subsetCheckbox.setSelection(subset.length() > 0);
		updateSubsetEnabled();

		if (subset.length() > 0)
			advancedExpander.setExpandedState(true);
	}

	/**
	 * Sets the given text to the information.
	 *
	 * @param text
	 */
	public void setInfoText(String text)
	{
		if (lastInfoText != null)
		{
			if (lastInfoText.equals(text))
				return;
		}
		infoTextExtracted = new LinkExtractor(text).extract();
		infoText.setText(infoTextExtracted.text);

		/* Apply styles */
		StyleRange [] styles = new StyleRange[infoTextExtracted.starts.length];
		for (int i=0; i < infoTextExtracted.starts.length; i++)
		{
			int start = infoTextExtracted.starts[i];
			int length = infoTextExtracted.ends[i] - start;
			styles[i] = new StyleRange(start, length, null, null);
			styles[i].underline = true;
			styles[i].underlineStyle = SWT.UNDERLINE_LINK;
		}
		infoText.setStyleRanges(styles);

		infoText.pack();
		layout();
	}

	/**
	 * Sets the available evidences.
	 *
	 * @param evidences
	 */
	public void setEvidences(Map<String,Integer> evidences)
	{
		evidenceTable.removeAll();
		ArrayList<Entry<String,Integer>> sortedEvidences = new ArrayList<Entry<String,Integer>>(evidences.entrySet());
		Collections.sort(sortedEvidences, new Comparator<Entry<String,Integer>>()
		{
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
			{
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		for (Entry<String,Integer> ev : sortedEvidences)
		{
			TableItem evi = new TableItem(evidenceTable,0);
			evi.setText(0,ev.getKey());
			evi.setText(1, ev.getValue() + "");
			Evidence realEvidence = EVIDENCE_MAP.get(ev.getKey());
			if (realEvidence != null)
				evi.setText(2,realEvidence.description);
			else
				evi.setText(2,"Unknown");
			evi.setChecked(true);
		}
		evidenceNameColumn.pack();
		evidenceNumberColumn.pack();
		evidenceDescColumn.pack();
		layout();
		evidenceTable.setEnabled(true);
	}

	/**
	 * Clears the evidences.
	 */
	public void clearEvidences()
	{
		evidenceTable.removeAll();
		evidenceTable.setEnabled(false);
	}

	/**
	 * Returns the selected evidences.
	 *
	 * @return
	 */
	public Collection<String> getCheckedEvidences()
	{
		ArrayList<String> selectedEvidences = new ArrayList<String>();
		for (int i=0;i<evidenceTable.getItemCount();i++)
		{
			if (evidenceTable.getItem(i).getChecked())
			{
				selectedEvidences.add(evidenceTable.getItem(i).getText());
			}
		}
		return selectedEvidences;
	}

	/**
	 * Sets the error string of the ontology field.
	 *
	 * @param error can be null or "" to indicate a no-error state.
	 */
	public void setOntologyErrorString(String error)
	{
		ontologyFileGridCompositeWidgets.setErrorString(error);
	}

	/**
	 * Add an action which is invoked when the ontology file is changed.
	 *
	 * @param act
	 */
	public void addOntologyChangedAction(ISimpleAction act)
	{
		ontologyFileGridCompositeWidgets.addTextChangedAction(act);
		ontologyChangedList.add(act);
	}

	/**
	 * Sets the association error string.
	 *
	 * @param error can be null or "" to indicate a no-error state.
	 */
	public void setAssociationErrorString(String error)
	{
		assocFileGridCompositeWidgets.setErrorString(error);
	}

	/**
	 * Adds the action that is invoked when the association file is
	 * changed.
	 *
	 * @param act
	 */
	public void addAssociationChangedAction(ISimpleAction act)
	{
		assocFileGridCompositeWidgets.addTextChangedAction(act);
		associationChangedList.add(act);
	}

	/**
	 * Add an action that is invoked when a link was clicked in the info text.
	 *
	 * @param act
	 */
	public void addInfoTextClickListener(InfoTextClickListener listener)
	{
		infoTextClickListenerList.add(listener);
	}
}
