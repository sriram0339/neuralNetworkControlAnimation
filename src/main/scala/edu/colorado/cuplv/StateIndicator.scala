package edu.colorado.cuplv

/*
  StateIndicator: Utility for drawing the state indicator bars and listeners for mouse clicks.

  Author: Sriram Sankaranarayanan
  Date: April 13, 2019
*/
import java.awt.Color

import scala.swing.{Graphics2D}
import java.awt.{Font}

class StateIndicator(val min: Double, val max:Double, val x_min: Double, val x_max: Double, val y: Int, val ht: Int, name: String) {
    def render(x: Double, g: Graphics2D)= {
        g.setPaint(Color.BLACK)
        g.drawRect(x_min.toInt,y,(x_max - x_min).toInt,ht)
        g.setPaint(Color.LIGHT_GRAY)
        val width = x_max - x_min
        val wx = ((x - min)*width/(max - min)).toInt
        if (wx >= 0 && wx <= x_max - x_min) {
            g.fillRect(x_min.toInt, y, wx, ht)
        } else if (wx <= 0) {
            g.setPaint(Color.RED)
            g.fillRect(x_min.toInt + wx,y, -wx, ht )
        } else {
            g.setPaint(Color.RED)
            g.fillRect(x_min.toInt, y, wx, ht)
        }
        g.setPaint(Color.RED)
        g.fillPolygon(Array(x_min.toInt+wx, x_min.toInt+wx-10, x_min.toInt+wx+10), Array(y,y-5,y-5),3)
        g.setFont(new Font("Verdana", Font.ITALIC, 14))
        g.drawString( f"$x%1.3f", x_min.toInt + wx, y-8)
        g.setFont(new Font("Verdana", Font.BOLD, 16))
        g.drawString(name, x_min.toInt - 50, y+ht/2)
    }

    def click(xPos: Double, yPos: Double): Option[Double] = {
        if ((xPos >= x_min && xPos <= x_max) && (yPos >= y && yPos <= y+ht)){
            Some(
                (max - min) * (xPos - x_min)/(x_max - x_min) + min
            )
        } else
            None
    }

}
