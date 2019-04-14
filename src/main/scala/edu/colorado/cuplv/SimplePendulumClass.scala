package edu.colorado.cuplv
import java.awt.{Color, Graphics2D}

class SimplePendulumClass(override var curState: List[Double]) extends ODESimulator {
    var cur_input = 0.0
    val delta = 0.05
    var disturbance = 0.0

    override def vectorField(u: Double, w: Double) = List(
        (t: Double, x: Seq[Double]) => {x(1)},
        (t: Double, x: Seq[Double]) => {math.sin(x(0)) - u * math.cos(x(0) + w * math.cos(x(0)))}
    )
    val neuralNetwork = NeuralNetworkFactory.readFromFile("networks/controller_network_adhs.nt")

    val thetaInd = new StateIndicator(-3.1415,3.1415,250,400, 150,25, "\u0398")
    val omegaInd = new StateIndicator(-5,5,250,400, 120,25, "\u03c9")
    val ctrlInd = new StateIndicator(-20, 20, 250, 400, 95,25, "u" )
    override def render(g: Graphics2D): Unit = {
        val theta = curState(0)
        val len = 150
        val xPos = 250
        val dx = -(len * math.sin(theta)).toInt
        val dy = (len * math.cos(theta)).toInt
        g.setPaint(Color.RED)
        g.fillPolygon(Array(xPos-3,xPos+3, xPos+3+dx, xPos-3+dx), Array(400,400,400-dy,400-dy ),4)
        g.setPaint(Color.BLACK)
        g.fillOval(xPos-5,400,10,10)
        /*-- Render the values of the state variables --*/
        thetaInd.render(theta, g)
        omegaInd.render(curState(1), g)
        ctrlInd.render(cur_input, g)
        /*-- Render the network --*/
        val pat = neuralNetwork.getNetworkActivationPattern(curState)
        pat.foldLeft (50) ((y,lyrPat) => {
            NNDraw.drawNNLayer(g, y, lyrPat,nnThresh=2.5)
        } )
    }

    override def click(x: Double, y: Double) = {
        thetaInd.click(x, y) match {
            case Some(th) => setStateComponent(0, th)
            case _ => ()
        }
        omegaInd.click(x,y) match {
            case Some(omega) => setStateComponent(1, omega)
            case _ => ()
        }
    }

      override def step: Unit = {

        val bias = 20.0
        val cur_input_list = neuralNetwork.evalNetwork(curState)
        assert(cur_input_list.length == 1)
        val u = cur_input_list.head - bias
        cur_input = u
        simulate(u, disturbance, delta)
    }

}
