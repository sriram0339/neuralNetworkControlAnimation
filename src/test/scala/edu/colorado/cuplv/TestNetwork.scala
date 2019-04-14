package edu.colorado.cuplv

import org.scalatest.FunSuite
import com.github.fons.nr.ode.{Solver, OdeSolverT, Factory}
import scala.math.{Pi, sin, cos}
import scala.util.{Try, Success, Failure}


class TestNetwork  extends FunSuite {

    test("Load Neural Network1 "){
        val n = NeuralNetworkFactory.readFromFile("networks/controller_network.nt")
        val retVal = n.evalNetwork(List(1.0, 2.5, -1.0, -2.0))
        assert(math.abs( retVal.head - 11.1848) <= 0.001)
        val retVal2 = n.evalNetwork(List(1.0, 0.0, -1.0, -1.0))
        assert(math.abs( retVal2.head - 15.3946) <= 0.001)
        val retVal3 = n.evalNetwork(List(0, 0, 0, -1.0))
        assert(math.abs( retVal3.head - 18.8080) <= 0.001)
        val retVal4 = n.evalNetwork(List(-1.0, -2.5, -1.0, -2.0))
        assert(math.abs( retVal4.head ) <= 0.001)
    }

    test("ODE Solve") {
        // ode to solve
        // t : independent variable.
        // x : depended variables.

        def f(t: Double, x: Double*): Double = -Pi * x(1)
        def g(t: Double, x: Double*): Double = Pi * x(0)

        //exact solution
        def fx(t: Double) = cos(Pi * t)
        def gx(t: Double) = sin(Pi * t)

        /**
        // Method    : Solver.RKE56; embedded Rung-Kutta
        // step size : 0.2
        // initial conditions : (0.0, List(1.0, 0.0))
        //                      0.0 : initial independent variable
        // set of ode's       : List(f,g)
        // accuracy           : 0.0001;
        // Because an accuracy is specified an adaptive step method will be used.
          **/

        val ode : Try[OdeSolverT] = Factory(Solver.RKE56, 0.02, (0.0, List(1.0, 0.0)), List(f, g), 0.00001)
        ode match {
            case Success(ode1) => {
                ode1(0.12) match {
                    case Success(odeRes) => {
                        println("method : " + Solver.RKE56 + " for 0.12")
                        println("returned : " + odeRes(0.12))
                        println(s"First: ${odeRes.first}, end = ${odeRes.last}, head=${odeRes.head}, tail=${odeRes.tail}, dataSet=${odeRes.dataSet}")
                        println("exact    : " + fx(0.12) + ", " + gx(0.12))
                    }
                    case _ => println("failed")
                }
            }
            case _ => println("failed")
        }
    }

}
