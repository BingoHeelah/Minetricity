import org.junit.jupiter.api.Test
import rocks.drifthyena.minetricity.simulation.electrical.component.NortonSinusoidalVoltageSource
import rocks.drifthyena.minetricity.simulation.electrical.component.Resistor
import rocks.drifthyena.minetricity.simulation.electrical.solver.CircuitBuilder
import rocks.drifthyena.minetricity.simulation.electrical.solver.MonaSystem
import rocks.drifthyena.minetricity.simulation.electrical.solver.MonaSystemOptions
import rocks.drifthyena.minetricity.util.inverse

class SolverTests {
    @Test
    fun `Basic voltage source and resistor with explicit ground running at 240 substeps`() {
        val builder = CircuitBuilder()
        val R = Resistor()
        val Vs = NortonSinusoidalVoltageSource(170.0)
        builder.addComponents(R)
        builder.addComponents(Vs)
        builder.joinPins(Vs.p, R.p)
        builder.joinPins(Vs.n, R.n)
        builder.ground(Vs.n)

        var built = builder.build()
        val options = MonaSystemOptions(
            components = built.components,
            totalMatrixSize = built.nodeCount + built.extraSlotCount,
            dt = 1200.0.inverse()
        )

        println(options.totalMatrixSize)

        val system = MonaSystem(options)

        for (i in 1..240) {
            system.solveSingleStep()
            println("Voltage at step $i: ${system.readNodeVoltage(0)}")
        }
    }
}