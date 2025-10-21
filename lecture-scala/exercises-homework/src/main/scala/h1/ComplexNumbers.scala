package h1
import scala.language.implicitConversions

// Add necessary class and object definitions in order to make the statements in the main work.


case class Complex(re: Double, im: Double):
	private def doubleToString(num: Double): String = {
		val numStr = {
			if num == num.toInt
			then num.toInt.toString
			else num.toString
		}

		numStr
	}

	override def toString: String = {
		val reStr = doubleToString(re)
		val imStr = doubleToString(im)

		(re, im) match
			case (_, 0) => s"$reStr"
			case (0, _) => s"${imStr}i"
			case (_, i)
				if (i > 0) => s"$reStr+${imStr}i"
			case (_, i) => s"$reStr${imStr}i"
	}

	def +(other: Complex): Complex = {
		Complex(re + other.re, im + other.im)
	}

	def *(other: Complex): Complex = {
		Complex(re * other.re - im * other.im,
						re * other.im + im * other.re)
	}

	def unary_- : Complex = Complex(-re, -im)


object ComplexNumbers:
	implicit def doubleToComplex(x: Double): Complex = Complex(x, 0)
	object I extends Complex(0, 1)

	def main(args: Array[String]): Unit =

		println(Complex(1,2)) // 1+2i


		println(1 + 2*I + I*3 + 2) // 3+5i

		val c = (2+3*I + 1 + 4*I) * I
		println(-c) // 7-3i

		// println(Complex(0, 1)) // idk if this should be 0+1i or only 1i or perhaps only i
