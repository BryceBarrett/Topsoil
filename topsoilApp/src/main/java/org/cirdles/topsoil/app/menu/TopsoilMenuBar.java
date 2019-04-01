package org.cirdles.topsoil.app.menu;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.cirdles.topsoil.app.ProjectManager;
import org.cirdles.topsoil.app.Topsoil;
import org.cirdles.topsoil.app.control.dialog.DataTableOptionsDialog;
import org.cirdles.topsoil.app.data.DataTable;
import org.cirdles.topsoil.app.data.column.DataColumn;
import org.cirdles.topsoil.app.data.row.DataRow;
import org.cirdles.topsoil.app.data.row.DataSegment;
import org.cirdles.topsoil.app.menu.helpers.HelpMenuHelper;
import org.cirdles.topsoil.app.menu.helpers.VisualizationsMenuHelper;
import org.cirdles.topsoil.app.util.ResourceBundles;
import org.cirdles.topsoil.plot.AbstractPlotkt;
import org.cirdles.topsoil.plot.DefaultProperties;
import org.cirdles.topsoil.plot.PlotProperty;
import org.cirdles.topsoil.plot.PlotType;
import org.cirdles.topsoil.plot.impl.BasePlotkt;
import org.cirdles.topsoil.uncertainty.Uncertainty;
import org.cirdles.topsoil.variable.DependentVariable;
import org.cirdles.topsoil.variable.IndependentVariable;
import org.cirdles.topsoil.variable.Variable;
import org.cirdles.topsoil.variable.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The main {@code MenuBar} for the application.
 *
 * @author marottajb
 */
public class TopsoilMenuBar extends MenuBar {

    private ResourceBundle resources = ResourceBundles.MENU_BAR.getBundle();

    public TopsoilMenuBar() {
        super();
        this.getMenus().addAll(
                new FileMenu(),
                getEditMenu(),
//                getViewMenu(),
                getVisualizationsMenu(),
                getHelpMenu()
        );
    }

    private Menu getEditMenu() {
        MenuItem preferencesItem = new MenuItem(resources.getString("preferences"));
        preferencesItem.setDisable(true);

        MenuItem tableOptionsItem = new MenuItem(resources.getString("tableOptions"));
        tableOptionsItem.setOnAction(event -> {
            DataTableOptionsDialog.showDialog(MenuUtils.getCurrentDataTable(), Topsoil.getPrimaryStage());
        });

        Menu editMenu = new Menu(resources.getString("editMenu"), null,
                tableOptionsItem
//                new SeparatorMenuItem(),
//                preferencesItem
        );
        editMenu.setOnShown(event -> {
            tableOptionsItem.setDisable(ProjectManager.getProject() == null);
        });

        return editMenu;
    }

    private Menu getViewMenu() {
        Menu viewMenu = new Menu(resources.getString("viewMenu"), null);
        return viewMenu;
    }

    private Menu getVisualizationsMenu() {
        MenuItem generatePlotItem = new MenuItem(resources.getString("generatePlot"));
        generatePlotItem.setOnAction(event -> {
            // @TODO Check to make sure proper variables are assigned
            VisualizationsMenuHelper.generatePlot(
                    PlotType.SCATTER,
                    MenuUtils.getCurrentDataTable(),
                    null);
        });

        MenuItem kubedPlotItem = new MenuItem("Generate Kubed Plot");
        kubedPlotItem.setOnAction(event -> {
            DataTable table = MenuUtils.getCurrentDataTable();
            List<Map<Variable<?>, Object>> plotData = new ArrayList<>();
            List<DataSegment> tableAliquots = table.getDataRoot().getChildren();
            Map<Variable<?>, DataColumn<?>> varMap = table.getVariableColumnMap();

            List<DataRow> rows;
            DataColumn column;
            Map<Variable<?>, Object> entry;

            for (DataSegment aliquot : tableAliquots) {
                rows = aliquot.getChildren();
                for (DataRow row : rows) {
                    entry = new HashMap<>();

                    entry.put(Variables.LABEL, row.getLabel());
                    entry.put(Variables.ALIQUOT, aliquot.getLabel());
                    entry.put(Variables.SELECTED, row.isSelected());

                    for (Variable var : Variables.NUMBER_TYPE) {
                        Object value;
                        column = varMap.get(var);
                        if (column != null) {
                            column = varMap.get(var);
                            value = row.getValueForColumn(column).getValue();
                            if (var instanceof DependentVariable && Uncertainty.PERCENT_FORMATS.contains(table.getUncertainty())) {
                                // @TODO The code below assumes that a dep-variable is always dependent on an ind-variable
                                double doubleVal = (double) value;
                                DependentVariable dependentVariable = (DependentVariable) var;
                                IndependentVariable dependency = (IndependentVariable) dependentVariable.getDependency();
                                DataColumn dependentColumn = varMap.get(dependency);
                                doubleVal /= 100;
                                doubleVal *= (Double) row.getValueForColumn(dependentColumn).getValue();
                                value = doubleVal;
                            }
                        } else {
                            if (var instanceof IndependentVariable || var instanceof DependentVariable) {
                                value = 0.0;
                            } else {
                                value = "";
                            }
                        }
                        entry.put(var, value);
                    }
                    plotData.add(entry);
                }
            }

            Map<PlotProperty, Object> plotProperties = new DefaultProperties();
            plotProperties.put(PlotProperty.UNCERTAINTY, 2.0);
            plotProperties.put(PlotProperty.UNCTBARS, false);

            AbstractPlotkt ktPlot = new BasePlotkt(plotData, plotProperties);
            ktPlot.createGraph();
            Stage stage = new Stage();
            stage.setScene(new Scene(ktPlot, 750, 550));
            stage.setTitle("Graph Test");
            stage.show();
        });

        Menu visualizationsMenu = new Menu(resources.getString("visualizationsMenu"), null,
                generatePlotItem,
                new SeparatorMenuItem(),
                kubedPlotItem
        );
        visualizationsMenu.setOnShown(event -> {
            generatePlotItem.setDisable(ProjectManager.getProject() == null);
            kubedPlotItem.setDisable(ProjectManager.getProject() == null);
        });
        return visualizationsMenu;
    }

    private Menu getHelpMenu() {
        MenuItem onlineHelpItem = new MenuItem(resources.getString("onlineHelp"));
        onlineHelpItem.setOnAction(event -> HelpMenuHelper.openOnlineHelp());

        MenuItem reportIssueItem = new MenuItem(resources.getString("reportIssue"));
        reportIssueItem.setOnAction(event -> HelpMenuHelper.openIssueReporter());

        MenuItem aboutItem = new MenuItem(resources.getString("about"));
        aboutItem.setOnAction(event -> HelpMenuHelper.openAboutScreen(Topsoil.getPrimaryStage()));

        return new Menu(resources.getString("helpMenu"), null,
                        onlineHelpItem,
                        reportIssueItem,
                        aboutItem
        );
    }

}
