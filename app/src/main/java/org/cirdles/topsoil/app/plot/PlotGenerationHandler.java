package org.cirdles.topsoil.app.plot;

import com.sun.javafx.stage.StageHelper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.cirdles.topsoil.app.dataset.NumberDataset;
import org.cirdles.topsoil.app.plot.variable.Variable;
import org.cirdles.topsoil.app.plot.variable.Variables;
import org.cirdles.topsoil.app.tab.TopsoilTab;
import org.cirdles.topsoil.app.tab.TopsoilTabPane;
import org.cirdles.topsoil.app.table.TopsoilTableController;
import org.cirdles.topsoil.app.util.serialization.PlotInformation;
import org.cirdles.topsoil.plot.Plot;

import java.util.List;
import java.util.Map;

/**
 * A class containing a set of methods for handling plot generation.
 *
 * @author Jake Marotta
 */
public class PlotGenerationHandler {

    /**
     * Generates a plot for the selected {@code TopsoilTab}.
     *
     * @param tabs  the active TopsoilTabPane
     */
    public static void handlePlotGenerationForSelectedTab(TopsoilTabPane tabs) {
        TopsoilTableController tableController = tabs.getSelectedTab().getTableController();

        // Check for open plots.
        List<Stage> stages = StageHelper.getStages();
        if (stages.size() > 1) {
            Alert plotOverwrite = new Alert(Alert.AlertType.CONFIRMATION,
                                            "Creating a new plot will overwrite the existing plot. " +
                                            "Are you sure you want to continue?",
                                            ButtonType.CANCEL,
                                            ButtonType.YES);
            plotOverwrite.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    for (TopsoilTab tab : tabs.getTopsoilTabs()) {
                        for (PlotInformation plotInfo : tab.getTableController().getTable().getOpenPlots()) {
                            tab.getTableController().getTable().removeOpenPlot(plotInfo.getTopsoilPlotType());
                            plotInfo.getStage().close();
                        }
                    }
                    PlotContext plotContext = generatePlotContext(tableController);
                    generatePlot(tableController, TopsoilPlotType.BASE_PLOT, plotContext);
                }
            });
        } else {
            PlotContext plotContext = generatePlotContext(tableController);
            generatePlot(tableController, TopsoilPlotType.BASE_PLOT, plotContext);
        }
    }

    /**
     * Generates a saved {@code Plot} from a .topsoil file.
     *
     * @param tableController   the TopsoilTableController for the table
     * @param plotType  the TopsoilPlotType of the plot
     * @param plotContext   the PlotContext for the plot
     */
    public static void handlePlotGenerationFromFile(TopsoilTableController tableController, TopsoilPlotType plotType,
                                                    PlotContext plotContext) {
        generatePlot(tableController, plotType, plotContext);
    }

    /**
     * Generates a {@code Plot}.
     *
     * @param tableController   the TopsoilTableController for the table
     * @param plotType  the TopsoilPlotType of the plot
     */
    private static void generatePlot(TopsoilTableController tableController, TopsoilPlotType plotType, PlotContext
            plotContext) {

        List<Map<String, Object>> data = plotContext.getData();

        PlotPropertiesPanelController propertiesPanel = tableController.getTabContent().getPlotPropertiesPanelController();
        Map<String, Object> plotProperties = propertiesPanel.getProperties();

        Plot plot = plotType.getPlot();

        plot.setData(data);
        plot.setProperties(plotProperties);
        propertiesPanel.setPlot(plot);

        // Create Plot Scene
        Parent plotWindow = new PlotWindow(plot);
        Scene scene = new Scene(plotWindow, 800, 600);

        // Create Plot Stage
        Stage plotStage = new Stage();
        plotStage.setScene(scene);
        plotStage.setResizable(false);

        // Connect Plot with PropertiesPanel
        plotStage.setTitle(plotType.getName() + ": " + propertiesPanel.getTitle());
        propertiesPanel.titleProperty().addListener(c -> {
            if (propertiesPanel.getTitle().length() > 0) {
                plotStage.setTitle(plotType.getName() + ": " + propertiesPanel.getTitle());
            } else {
                plotStage.setTitle(plotType.getName());
            }
        });

        plotStage.setOnCloseRequest(closeEvent -> {
            tableController.getTable().removeOpenPlot(plotType);
            propertiesPanel.removePlot();
        });

        // Show Plot
        plotStage.show();

        // Store plot information in TopsoilDataTable
        PlotInformation plotInfo = new PlotInformation(plot, plotType, propertiesPanel.getProperties(), plotContext, plotStage);
        plotInfo.setVariableBindings(plotContext.getBindings());
        tableController.getTable().addOpenPlot(plotInfo);

        // Re-apply data when uncertainty is changed.
        propertiesPanel.uncertaintyProperty().addListener(c -> {
            PlotContext newContext = generatePlotContext(tableController);
            List<Map<String, Object>> newData = newContext.getData();
            plot.setData(newData);
        });
    }

    /**
     * Generates a {@code PlotContext} for the given {@code TopsoilTableController}.
     *
     * @param tableController   the TopsoilTableController for the table
     * @return  PlotContext for the table
     */
    private static PlotContext generatePlotContext(TopsoilTableController tableController) {
        NumberDataset dataset = tableController.getDataset();
        SimplePlotContext plotContext = new SimplePlotContext(dataset);

        // Bind variables
        Variable<Number> variable;
        for (int i = 0; i < dataset.getFields().size(); i++) {
            variable = Variables.VARIABLE_LIST.get(i);
            plotContext.addBinding(variable, dataset.getFields().get(i));
        }

        return plotContext;
    }
}
