package org.cirdles.topsoil.chart.concordia;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zeringuej
 */
public class ErrorEllipseTest {

    /**
     * Test of getControlPoints method, of class ErrorEllipse.
     */
    @Test
    public void testGetControlPoints() {
        System.out.println("getControlPoints");
        ErrorEllipse instance = new ErrorEllipse(
                7.2136E-2, 1.1028E-2,
                6.2603E-5, 2.7280E-6,
                5.7610E-1, 2);

        double expResult0_0 = 7.2261E-2;
        double result0_0 = instance.getControlPoints().get(0, 0);
        assertEquals(expResult0_0, result0_0, 1E-6);

        double expResult0_1 = 1.1031E-2;
        double result0_1 = instance.getControlPoints().get(0, 1);
        assertEquals(expResult0_1, result0_1, 1E-6);

        double expResult5_0 = 7.2011E-2;
        double result5_0 = instance.getControlPoints().get(5, 0);
        assertEquals(expResult5_0, result5_0, 1E-6);

        double expResult5_1 = 1.1027E-2;
        double result5_1 = instance.getControlPoints().get(5, 1);
        assertEquals(expResult5_1, result5_1, 1E-6);
    }

}
