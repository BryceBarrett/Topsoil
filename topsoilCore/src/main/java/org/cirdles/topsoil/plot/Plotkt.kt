package org.cirdles.topsoil.plot

interface Plotkt: Displayable{

    /**
     * Simple setter method for initalizing any/all data needed for the
     * plot.
     *
     * @param dataIn a list of maps
     */
    fun setData(dataIn: List<Map<String, Object>>)

}