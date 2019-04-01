package org.cirdles.topsoil.plot.impl

import javafx.scene.Group
import javafx.scene.paint.Color
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.selection.selection
import kubed.shape.circle
import kubed.shape.lineSegment
import org.cirdles.topsoil.plot.AbstractPlotkt
import org.cirdles.topsoil.plot.PlotProperty
import org.cirdles.topsoil.plot.PlotProperty.*
import org.cirdles.topsoil.variable.Variable
import org.cirdles.topsoil.variable.Variables.*

class BasePlotkt(dataIn: List<Map<Variable<*>, Any?>>, propsIn: Map<PlotProperty, Any?>): AbstractPlotkt(){


    init {
        data = dataIn
        properties = propsIn
    }

    override fun createGraph(){

        //width = 600.0
        //height = 400.0


        xScale = scaleLinear {
            domain(listOf(data.map {it[X].toString().toDouble()}.min() as Double,
                    data.map {it[X].toString().toDouble()}.max() as Double))
            range(listOf(padding, outerWidth - padding * 2))
        }
        yScale = scaleLinear {
            domain(listOf(data.map {it[Y].toString().toDouble()}.min() as Double,
                    data.map {it[Y].toString().toDouble()}.max() as Double))
            range(listOf(outerHeight - padding, padding))
        }
        rScale = scaleLinear {
            domain(listOf(data.map {it[Y].toString().toDouble()}.min() as Double,
                    data.map {it[Y].toString().toDouble()}.max() as Double))
            range(listOf(2.0, 5.0))
        }

        val unct = properties[UNCERTAINTY] as Double

        val newCircle = circle<List<Double>> {
            radius { _, _ -> 2.0 }      // "d" is the data that was passed
            translateX { d, _ -> xScale(d[0]) }
            translateY { d, _ -> yScale(d[1]) }
        }


        val hLine = lineSegment<List<Double>> {
            startX { d, _ -> xScale(d[0] - (unct * d[2])) }
            startY { d, _ -> yScale(d[1]) }
            endX {d, _ -> xScale( d[0] + (unct * d[2])) }
            endY {d, _ -> yScale( d[1] ) }
            stroke(Color.BLACK)
        }

        val vLine = lineSegment<List<Double>> {
            startX { d, _ -> xScale(d[0]) }
            startY { d, _ -> yScale(d[1] - (unct * d[3])) }
            endX {d, _ -> xScale( d[0]) }
            endY {d, _ -> yScale( d[1] + (unct * d[3]) ) }
            stroke(Color.BLACK)
        }

        val topCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - 0.2 * (unct * d[2])) }
            startY { d, _ -> yScale(d[1] + (unct * d[3])) }
            endX {d, _ -> xScale( d[0] + 0.2 * (unct * d[2])) }
            endY {d, _ -> yScale( d[1] + (unct * d[3]) ) }
            stroke(Color.BLACK)
        }

        val leftCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - (unct * d[2])) }
            startY { d, _ -> yScale(d[1] - 0.2 * (unct * d[3])) }
            endX {d, _ -> xScale( d[0] - (unct * d[2])) }
            endY {d, _ -> yScale( d[1] + 0.2 * (unct * d[3]) ) }
            stroke(Color.BLACK)
        }

        val bottomCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - 0.2 * (unct * d[2])) }
            startY { d, _ -> yScale(d[1] - (unct * d[3])) }
            endX {d, _ -> xScale( d[0] + 0.2 * (unct * d[2])) }
            endY {d, _ -> yScale( d[1] - (unct * d[3]) ) }
            stroke(Color.BLACK)
        }

        val rightCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] + (unct * d[2])) }
            startY { d, _ -> yScale(d[1] - 0.2 * (unct * d[3])) }
            endX {d, _ -> xScale( d[0] + (unct * d[2])) }
            endY {d, _ -> yScale( d[1] + 0.2 * (unct * d[3]) ) }
            stroke(Color.BLACK)
        }

        val reformedData = reformData()

        root.selectAll<List<Double>>("Circles")
                .data(reformedData)
                .enter()
                .append { d, _, _ -> newCircle(d) }

        if(properties[UNCTBARS] as Boolean) {

            root.selectAll<List<Double>>(".HLine")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> hLine(d) }

            root.selectAll<List<Double>>(".VLine")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> vLine(d) }

            root.selectAll<List<Double>>(".topCap")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> topCap(d) }

            root.selectAll<List<Double>>(".leftCap")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> leftCap(d) }

            root.selectAll<List<Double>>(".bottomCap")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> bottomCap(d) }

            root.selectAll<List<Double>>(".rightCap")
                    .data(reformedData)
                    .enter()
                    .append { d, _, _ -> rightCap(d) }
        }

        val xAxis = axisBottom(xScale) {
            tickCount = 8
            formatter = { d -> "%.3f".format(d) }
        }

        xAxis(root.selectAll<Unit>(".xAxis")
                .append { -> Group() }
                .classed("axis", "xAxis")
                .translateY(outerHeight - padding))

        val yAxis = axisLeft(yScale) {
            tickCount = 6
            formatter = { d -> "%.3f".format(d) }
        }

        yAxis(root.selection<Unit>()
                .append { -> Group() }
                .classed("axis", "yAxis")
                .translateX(padding))

        this.content = root
    }

    private fun reformData(): MutableList<List<Double>>{
        var reformedData = mutableListOf<List<Double>>()

        data.forEach {
            var x = it[X].toString().toDouble()
            var y = it[Y].toString().toDouble()
            var sigmaX = it[SIGMA_X].toString().toDouble()
            var sigmaY = it[SIGMA_Y].toString().toDouble()
            var rho = it[RHO].toString().toDouble()

            reformedData.add(listOf(x, y, sigmaX, sigmaY, rho))
        }

        return reformedData
    }

}