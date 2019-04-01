package org.cirdles.topsoil.plot.base

import javafx.scene.Group
import javafx.scene.paint.Color
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.selection.selection
import kubed.shape.area
import kubed.shape.circle
import kubed.shape.line
import kubed.shape.lineSegment
import org.cirdles.topsoil.plot.AbstractPlotkt

class BasePlotkt(dataIn: List<Map<String, Any>>, propsIn: Map<String, Any>): AbstractPlotkt(){


    init {
        data = dataIn
        properties = propsIn
    }

    override fun createGraph(){

        //width = 600.0
        //height = 400.0



        xScale = scaleLinear {
            domain(listOf(data.map {it.get("x").toString().toDouble()}.min() as Double,
                    data.map {it.get("x").toString().toDouble()}.max() as Double))
            range(listOf(padding, outerWidth - padding * 2))
        }
        yScale = scaleLinear {
            domain(listOf(data.map {it.get("y").toString().toDouble()}.min() as Double,
                    data.map {it.get("y").toString().toDouble()}.max() as Double))
            range(listOf(outerHeight - padding, padding))
        }
        rScale = scaleLinear {
            domain(listOf(data.map {it.get("y").toString().toDouble()}.min() as Double,
                    data.map {it.get("y").toString().toDouble()}.max() as Double))
            range(listOf(2.0, 5.0))
        }


        val newCircle = circle<List<Double>> {
            radius { _, _ -> 2.0 }      // "d" is the data that was passed
            translateX { d, _ -> xScale(d[0]) }
            translateY { d, _ -> yScale(d[1]) }
        }


        val hLine = lineSegment<List<Double>> {
            startX { d, _ -> xScale(d[0] - (properties.get("uncertainty") as Double * d[2])) }
            startY { d, _ -> yScale(d[1]) }
            endX {d, _ -> xScale( d[0] + (properties.get("uncertainty") as Double * d[2])) }
            endY {d, _ -> yScale( d[1] ) }
            stroke(Color.BLACK)
        }

        val vLine = lineSegment<List<Double>> {
            startX { d, _ -> xScale(d[0]) }
            startY { d, _ -> yScale(d[1] - (properties.get("uncertainty") as Double * d[3])) }
            endX {d, _ -> xScale( d[0]) }
            endY {d, _ -> yScale( d[1] + (properties.get("uncertainty") as Double * d[3]) ) }
            stroke(Color.BLACK)
        }

        val topCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - 0.2 * (properties.get("uncertainty") as Double * d[2])) }
            startY { d, _ -> yScale(d[1] + (properties.get("uncertainty") as Double * d[3])) }
            endX {d, _ -> xScale( d[0] + 0.2 * (properties.get("uncertainty") as Double * d[2])) }
            endY {d, _ -> yScale( d[1] + (properties.get("uncertainty") as Double * d[3]) ) }
            stroke(Color.BLACK)
        }

        val leftCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - (properties.get("uncertainty") as Double * d[2])) }
            startY { d, _ -> yScale(d[1] - 0.2 * (properties.get("uncertainty") as Double * d[3])) }
            endX {d, _ -> xScale( d[0] - (properties.get("uncertainty") as Double * d[2])) }
            endY {d, _ -> yScale( d[1] + 0.2 * (properties.get("uncertainty") as Double * d[3]) ) }
            stroke(Color.BLACK)
        }

        val bottomCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] - 0.2 * (properties.get("uncertainty") as Double * d[2])) }
            startY { d, _ -> yScale(d[1] - (properties.get("uncertainty") as Double * d[3])) }
            endX {d, _ -> xScale( d[0] + 0.2 * (properties.get("uncertainty") as Double * d[2])) }
            endY {d, _ -> yScale( d[1] - (properties.get("uncertainty") as Double * d[3]) ) }
            stroke(Color.BLACK)
        }

        val rightCap = lineSegment<List<Double>>{
            startX { d, _ -> xScale(d[0] + (properties.get("uncertainty") as Double * d[2])) }
            startY { d, _ -> yScale(d[1] - 0.2 * (properties.get("uncertainty") as Double * d[3])) }
            endX {d, _ -> xScale( d[0] + (properties.get("uncertainty") as Double * d[2])) }
            endY {d, _ -> yScale( d[1] + 0.2 * (properties.get("uncertainty") as Double * d[3]) ) }
            stroke(Color.BLACK)
        }

        val reformedData = reformData()

        root.selectAll<List<Double>>("Circles")
                .data(reformedData)
                .enter()
                .append { d, _, _ -> newCircle(d) }

        if(properties.get("showCrosses") as Boolean) {

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
            var x = it.get("x").toString().toDouble()
            var y = it.get("y").toString().toDouble()
            var sigma_x = it.get("sigma_x").toString().toDouble()
            var sigma_y = it.get("sigma_y").toString().toDouble()
            var rho = it.get("rho").toString().toDouble()

            reformedData.add(listOf(x, y, sigma_x, sigma_y, rho))
        }

        return reformedData
    }

}