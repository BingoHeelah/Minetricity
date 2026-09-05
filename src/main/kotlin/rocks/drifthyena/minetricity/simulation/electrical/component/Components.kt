package rocks.drifthyena.minetricity.simulation.electrical.component

import rocks.drifthyena.minetricity.simulation.electrical.solver.MonaSystem
import rocks.drifthyena.minetricity.simulation.electrical.solver.Stamper
import rocks.drifthyena.minetricity.util.FULL_RADIAN_ROTATION
import rocks.drifthyena.minetricity.util.SQRT_2
import rocks.drifthyena.minetricity.util.inverse
import kotlin.math.sin

class ComponentPin(val owner: CircuitBuildable) {
    companion object {
        const val GROUND = -1
        const val VIRTUAL = -2
    }

    var nodeId = VIRTUAL
}

const val MIN_RESISTANCE = 1e-9
const val MAX_RESISTANCE = 1e+9

/**
 * Interface for building a circuit. [collectPins] collects any external pins that get joined into other components.
 * [getAdditionalSlots] and [assignSlots] are used for anything that needs to expose and integrate internal pins, or
 * extra off-diagonals. A good example would be a transformer winding with series resistance.
 */
interface CircuitBuildable {
    fun collectPins(): List<ComponentPin>
    fun getAdditionalSlots(): Int = 0
    fun assignSlots(starting: Int, ending: Int) {}
}

/**
 * Interface for the *simulation* side of things. I have a separate interface to allow for dynamic events such as power
 * flashes that aren't bound to the buildable side of things for cleanliness.
 */
interface CircuitSimulatable {
    var solver: MonaSystem?

    fun acceptSolver(newSolver: MonaSystem) {
        require(solver == null) { "Attempt to set new solver during active simulation!" }
        solver = newSolver
    }

    fun releaseSolver() {
        solver = null
    }

    fun preSolve() {}
    fun postSolve() {}
    fun reset() {}
    fun stamp(stamper: Stamper)
}

interface CircuitComponent: CircuitSimulatable, CircuitBuildable

abstract class MonaComponent : CircuitComponent {
    val p = ComponentPin(this)
    val n = ComponentPin(this)

    override var solver: MonaSystem? = null

    override fun collectPins() = listOf(p, n)
}

class Resistor(val R: Double = 100.0): MonaComponent() {
    var resistance = R
        set(value) {
            if (field == value) return
            val value = value.coerceIn(MIN_RESISTANCE, MAX_RESISTANCE)

            solver?.stamper?.stampResistance(p.nodeId, n.nodeId, value - field)

            field = value
        }

    override fun stamp(stamper: Stamper) {
        stamper.stampResistance(p.nodeId, n.nodeId, resistance)
    }
}

class Inductor(var L: Double = 0.01): MonaComponent() {
    var inductance = L
        set(value) {
            if (field == value) return

            solver?.stamper?.stampInductance(p.nodeId, n.nodeId, value - field)

            field = value
        }

    override fun stamp(stamper: Stamper) {
        stamper.stampInductance(p.nodeId, n.nodeId, inductance)
    }
}

/**
 * Stamps a tiny resistor. The advantage is it allocates no extra slots in the matrix.
 */
open class NortonVoltageSource(val V: Double = 48.0): MonaComponent() {
    var voltage = V
    val ARRRR = 1e-4

    override fun preSolve() {
        val prevFluxP = solver!!.readHistory(p.nodeId)
        val prevFluxN = solver!!.readHistory(n.nodeId)
        val dFlux = prevFluxP - prevFluxN
        val gEq = 1.0 / (ARRRR * solver!!.dt)
        val targetFlux = dFlux + (voltage * solver!!.dt)
        val I = gEq * targetFlux

        solver!!.stamper.addRhs(p.nodeId, I)
        solver!!.stamper.addRhs(n.nodeId, -I)
    }

    override fun stamp(stamper: Stamper) {
        stamper.stampResistance(p.nodeId, n.nodeId, ARRRR)
    }
}

class NortonSinusoidalVoltageSource(var peakVoltage: Double, var frequency: Double = 60.0, var offset: Double = 0.0): NortonVoltageSource() {
    companion object {
        fun fromRms(v: Double) = NortonSinusoidalVoltageSource(v * SQRT_2)
    }

    var angle = offset

    override fun preSolve() {
        val t = solver!!.totalTime
        voltage = peakVoltage * sin(2.0 * Math.PI * frequency * t + offset)

        super.preSolve()
    }

    override fun postSolve() {
        /**angle += ((solver!!.dt * frequency * FULL_RADIAN_ROTATION) % FULL_RADIAN_ROTATION)
        voltage = sin(angle) * peakVoltage*/
    }
}
