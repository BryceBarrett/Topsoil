package org.cirdles.topsoil.app.util.serialization;

import org.cirdles.topsoil.app.dataset.entry.Entry;
import org.cirdles.topsoil.app.dataset.NumberDataset;
import org.cirdles.topsoil.app.dataset.TopsoilRawData;
import org.cirdles.topsoil.app.dataset.field.Field;

import org.cirdles.topsoil.app.isotope.IsotopeType;

import org.cirdles.topsoil.app.plot.*;
import org.cirdles.topsoil.app.plot.variable.Variable;
import org.cirdles.topsoil.app.plot.variable.Variables;

import org.cirdles.topsoil.app.tab.TopsoilTab;
import org.cirdles.topsoil.app.tab.TopsoilTabPane;

import org.cirdles.topsoil.app.dataset.entry.TopsoilDataEntry;
import org.cirdles.topsoil.app.dataset.entry.TopsoilPlotEntry;
import org.cirdles.topsoil.app.table.TopsoilDataTable;
import org.cirdles.topsoil.app.table.TopsoilTableController;
import org.cirdles.topsoil.plot.Plot;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import static org.cirdles.topsoil.app.util.serialization.TableDataKeys.*;
import static org.cirdles.topsoil.app.util.serialization.PlotDataKeys.*;

/**
 * A serializable class which stores information about all open tables and plots for serialization to a .topsoil
 * file. Data is read from a {@link TopsoilTabPane}, the {@link TopsoilTab}s of which each contain a
 * {@link TopsoilDataTable} filled with information about a specific table, including information about plots associated
 * with that table. This information is extracted, placed into a serializable format, then stored in {@link HashMap}s.
 *
 * @author Jake Marotta
 * @see TopsoilSerializer
 * @see TopsoilTab
 * @see TopsoilDataTable
 * @see TopsoilTabPane
 */
class SerializableTopsoilSession implements Serializable {

    // TODO Check all casts and map calls.

    //***********************
    // Attributes
    //***********************

    private static final long serialVersionUID = 5793097708181607507L;

    /**
     * A {@code List} of {@code Map}s that contain all of the data for the Topsoil session.
     */
    private List<Map<String, Serializable>> data;

    // Static maps for re-creating non-serializable information.
    /**
     * A {@code Map} of {@code String}s to {@code IsotopeType}s, so that we can store the abbreviation of each
     * {@code IsotopeType} as a {@code String}, which is serializable.
     */
    private static Map<String, IsotopeType> ISOTOPE_TYPES;

    /**
     * A {@code Map} of {@code String}s to {@code TopsoilPlotType}s, so that we can store the name of each
     * {@code TopsoilPlotType} as a {@code String}, which is serializable.
     */
    private static Map<String, TopsoilPlotType> TOPSOIL_PLOT_TYPES;

    /**
     * A {@code Map} of {@code String}s to {@code UncertaintyFormat}s, so that we can store the name of each {@code
     * UncertaintyFormat} as a {@code String}, which is serliazable.
     */
    private static Map<String, UncertaintyFormat> UNCERTAINTY_FORMATS;

    /**
     * A {@code Map} of {@code String}s to {@code Variable}s, so that we can store the name of each
     * {@code Variable} as a {@code String}, which is serializable.
     */
    private static Map<String, Variable<Number>> VARIABLES;

    static {
        ISOTOPE_TYPES = new HashMap<>();
        for (IsotopeType type : IsotopeType.ISOTOPE_TYPES) {
            ISOTOPE_TYPES.put(type.getAbbreviation(), type);
        }
        TOPSOIL_PLOT_TYPES = new HashMap<>();
        for (TopsoilPlotType type : TopsoilPlotType.TOPSOIL_PLOT_TYPES) {
            TOPSOIL_PLOT_TYPES.put(type.getName(), type);
        }
        UNCERTAINTY_FORMATS = new HashMap<>();
        for (UncertaintyFormat format : UncertaintyFormats.UNCERTAINTY_FORMATS) {
            UNCERTAINTY_FORMATS.put(format.getName(), format);
        }
        VARIABLES = new HashMap<>();
        for (Variable v : Variables.VARIABLE_LIST) {
            VARIABLES.put(v.getName(), v);
        }
    }

    //***********************
    // Constructors
    //***********************

    /**
     * Constructs an instance of {@code SerializableTopsoilSession}, using
     * the provided {@code TopsoilTabPane} as a data source.
     *
     * @param tabs  a TopsoilTabPane which contains tables
     */
    public SerializableTopsoilSession(TopsoilTabPane tabs) {
        this.data = new ArrayList<>();

        List<TopsoilTab> topsoilTabs = tabs.getTopsoilTabs();
        for (TopsoilTab tab : topsoilTabs) {
            this.storeTable(tab.getTableController());
        }
    }

    //***********************
    // Methods
    //***********************

    /** {@inheritDoc}
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /**
     * Uses the specified {@code TopsoilTableController} to take information about a table and put it into
     * {@code Serializable} formats.
     *
     * @param tableController   the TopsoilTableController for the table
     */
    private void storeTable(TopsoilTableController tableController) {
        HashMap<String, Serializable> tableData = new HashMap<>();

        // Title of the TopsoilDataTable
        tableData.put(TABLE_TITLE, tableController.getTable().getTitle());

        // Arrangement of the TableView's headers
        tableData.put(TABLE_HEADERS, tableController.getTable().getColumnNames());

        // IsotopeType of TopsoilDataTable
        tableData.put(TABLE_ISOTOPE_TYPE, tableController.getTable().getIsotopeType().getAbbreviation());

        // Data stored in TopsoilDataTable
        tableData.put(TABLE_DATA, new ArrayList<>(tableController.getTable().getDataAsArrays()));

        // Plots which visualize data in this table
        ArrayList<Map<String, Serializable>> plots = new ArrayList<>();
        for (PlotInformation plotInfo : tableController.getTable().getOpenPlots()) {
            plots.add(convertPlotInformation(plotInfo));
        }
        tableData.put(TABLE_PLOTS, plots);

        tableData.put(TABLE_PLOT_PROPERTIES, new HashMap<>(tableController.getTabContent().getPlotPropertiesPanelController()
                                                                      .getProperties()));

        tableData.put(TABLE_UNCERTAINTY_FORMAT, tableController.getTabContent().getPlotPropertiesPanelController()
                                                     .getUncertainty().getName());

        this.data.add(tableData);
    }

    /**
     * Converts {@code PlotInformation} stored within a {@link TopsoilDataTable} into a {@code HashMap}.
     *
     * @param plotInfo  a PlotInformation object
     * @return  a Map of String keys to Serializable values containing plot information
     */
    private Map<String, Serializable> convertPlotInformation(PlotInformation plotInfo) {
        HashMap<String, Serializable> plotOptions = new HashMap<>();

        plotOptions.put(PLOT_TYPE, plotInfo.getTopsoilPlotType().getName());

        // Convert VariableBindings
        // TODO Variable bindings should be stored.
//        HashMap<String, Serializable> bindings = new HashMap<>();
//        for (VariableBinding binding : plotInfo.getVariableBindings()) {
//            HashMap<String, Serializable> bindingMap = new HashMap<>();
//            bindingMap.put("Variable Name", binding.getVariable().getName());
//            bindingMap.put("Field Name", binding.getField().getName());
//            bindingMap.put("Variable Format Name", binding.getFormat().getName());
//            System.out.println("Format: " + bindingMap.get("Variable Format Name"));
//            bindings.put(binding.getVariable().getName(), bindingMap);
//        }
//        plotOptions.put("Variable Bindings", bindings);

//        plotOptions.put("Properties", new HashMap<>(properties));

        return plotOptions;
    }

    /**
     * Re-generates a plot using data from the specified {@code TopsoilTableController} and a {@code HashMap}
     * containing information about the old plot.
     *
     * @param tableController   a TopsoilTableController containing table data
     * @param plot  a HashMap of information about the plot to re-create
     */
    private void loadPlot(TopsoilTableController tableController, HashMap<String, Serializable> plot) {

        List<Field<Number>> fields = tableController.getTable().getFields();

        List<TopsoilDataEntry> dataEntries = tableController.getTable().getCopyOfDataAsEntries();
        List<Entry> plotEntries = new ArrayList<>();

        // Copy TopsoilDataEntries into TopsoilPlotEntries
        for (int i = 0; i < dataEntries.size(); i ++) {
            plotEntries.add(new TopsoilPlotEntry());
            for (int j = 0; j < tableController.getTable().getColumnNames().length; j++) {
                double currentValue = dataEntries.get(i).getProperties().get(j).getValue();
                plotEntries.get(i).set(fields.get(j), currentValue);
            }
        }

        NumberDataset dataset = new NumberDataset(tableController.getTable().getTitle(), new TopsoilRawData<>(fields,
                                                                                                    plotEntries));
        PlotContext plotContext = new SimplePlotContext(dataset);

        /*
        TODO | Variable Bindings should be restored from information in the serialized file. Right now, they are
        TODO | re-formed from the order of columns in the TableView.
         */
//        // Bind Variables
//        Map<String, Serializable> bindingMaps = (Map<String, Serializable>) plot.get("Variable Bindings");
//        Collection<VariableBinding> bindings = new ArrayList<>();
//        for (Serializable b : bindingMaps.values()) {
//            Map<String, Serializable> bindingMap = (Map<String, Serializable>) b;
//            Variable<Number> variable = VARIABLES.get((String) bindingMap.get("Variable Name"));
//            Field<Number> field = new NumberField((String) bindingMap.get("Field Name"));
//            VariableFormat<Number> format;
//            if (bindingMap.get("Variable Format Name").equals("Identity")) {
//                format = new IdentityVariableFormat<>();
//            } else {
//                format = VARIABLE_FORMATS.get((String) bindingMap.get("Variable Format Name"));
//            }
//            plotContext.addBinding(variable, field, format);
//        }

        for (int i = 0; i < Variables.VARIABLE_LIST.size(); i++) {
            Variable<Number> variable = Variables.VARIABLE_LIST.get(i);
            Field<Number> field = fields.get(i);
            plotContext.addBinding(variable, field);

        }

        PlotGenerationHandler.handlePlotGenerationFromFile(tableController, TOPSOIL_PLOT_TYPES.get(plot.get(PLOT_TYPE)), plotContext);
    }

    /**
     * Adds each stored {@link TopsoilDataTable} and their corresponding {@link Plot}s to the specified
     * {@code TopsoilTabPane}.
     *
     * @param tabs  the target TopsoilTabPane
     */
    public void loadDataToTopsoilTabPane(TopsoilTabPane tabs) {
        TopsoilDataTable table;
        for (Map<String, Serializable> tableData : this.data) {
            String[] headers = (String[]) tableData.get(TABLE_HEADERS);

            ArrayList<Double[]> storedEntries = (ArrayList<Double[]>) tableData.get("Data");
            TopsoilDataEntry[] dataEntries = new TopsoilDataEntry[storedEntries.size()];
            for (int index = 0; index < storedEntries.size(); index++) {
                dataEntries[index] = new TopsoilDataEntry(storedEntries.get(index));
            }

            table = new TopsoilDataTable(
                    headers,
                    this.ISOTOPE_TYPES.get((String) tableData.get(TABLE_ISOTOPE_TYPE)),
                    dataEntries
            );
            table.setTitle((String) tableData.get(TABLE_TITLE));
            tabs.add(table);

            TopsoilTableController tableController = tabs.getSelectedTab().getTableController();
            tableController.getTabContent().getPlotPropertiesPanelController().setUncertainty(UNCERTAINTY_FORMATS.get
                    ((String) tableData.get(TABLE_UNCERTAINTY_FORMAT)));
            tableController.getTabContent().getPlotPropertiesPanelController().setProperties((HashMap<String, Object>) tableData.get
                    (TABLE_PLOT_PROPERTIES));

            for (HashMap<String, Serializable> plot : (ArrayList<HashMap<String, Serializable>>) tableData.get(TABLE_PLOTS)) {
                this.loadPlot(tableController, plot);
            }
        }
    }
}
