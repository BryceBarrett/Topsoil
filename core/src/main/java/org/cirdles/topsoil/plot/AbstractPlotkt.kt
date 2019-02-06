package org.cirdles.topsoil.plot

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import kubed.scale.LinearScale
import kubed.scale.scaleLinear

abstract class AbstractPlotkt(): ScrollPane() {

    protected var data: List<Map<String, Any>>
    protected var properties: Map<String, Any>


    protected var xScale : LinearScale<Double>
    protected var yScale : LinearScale<Double>
    protected var rScale : LinearScale<Double>

    protected var padding = 20.0


    //private val width = 600.0
    //private val height = 300.0

    init {
        data = emptyList()
        properties = emptyMap()


        this.setMaxSize(width + (2 * padding), height + (2 * padding))
        this.setPadding(Insets(padding))

        //Setting dummy variables for the scales

        xScale = scaleLinear {
            domain(0.0, 1.0)
            range(0.0, 1.0)
        }

        yScale = scaleLinear {
            domain(0.0, 1.0)
            range(0.0, 1.0)
        }

        rScale = scaleLinear {
            domain(0.0, 1.0)
            range(0.0, 1.0)
        }
    }

    /**
     * Function set up to create everything that will be used within the graph.
     * This includes the shapes, final bounds of the graph, final scale values for the graph
     * and anything else needed to render the graph.
     *
     * This is where the Group kubed object will be created and then assigned to the content of
     * the scrollpane.
     */
    abstract fun createGraph()

}