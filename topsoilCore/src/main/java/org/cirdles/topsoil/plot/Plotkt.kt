package org.cirdles.topsoil.plot

import org.cirdles.topsoil.variable.Variable

interface Plotkt: Displayable{

    /**
     * Simple setter method for initalizing any/all data needed for the
     * plot.
     *
     * @param dataIn a list of maps
     */
    fun setData(dataIn: List<Map<Variable<*>, Any?>>)

}