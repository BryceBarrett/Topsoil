package org.cirdles.topsoil.plot

import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import kubed.scale.LinearScale
import kubed.scale.scaleLinear
import kubed.shape.area

abstract class AbstractPlotkt(): ScrollPane() {


    protected val margin = Insets(110.0, 75.0, 75.0, 75.0)
    protected var data: List<Map<String, Any>>
    protected var properties: Map<String, Any>


    protected var xScale : LinearScale<Double>
    protected var yScale : LinearScale<Double>
    protected var rScale : LinearScale<Double>

    protected var padding = 20.0
    protected var outerWidth = 600.0
    protected var outerHeight = 400.0
    protected val innerWidth = outerWidth - margin.left - margin.right
    protected val innerHeight = outerHeight - margin.top - margin.bottom

    protected val root = Group()


    //private val width = 600.0
    //private val height = 300.0

    init {
        data = emptyList()
        properties = emptyMap()


        //this.setMaxSize(width + (2 * padding), height + (2 * padding))
        //this.setPadding(Insets(padding))
        //root.translateX = padding
        //root.translateY = padding

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

        //Initialize the graphing area
        root.prefWidth(outerWidth)
        root.prefHeight(outerHeight)
        root.translateX = margin.left + 30.0
        root.translateY = margin.top

        val plotArea = area<Unit> {
            x0 { _, margin.left, _ -> }
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