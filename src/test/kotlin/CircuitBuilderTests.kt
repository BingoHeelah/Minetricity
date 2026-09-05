import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertThrowsExactly
import rocks.drifthyena.minetricity.simulation.electrical.component.NortonSinusoidalVoltageSource
import rocks.drifthyena.minetricity.simulation.electrical.component.Resistor
import rocks.drifthyena.minetricity.simulation.electrical.solver.CircuitBuilder

class CircuitBuilderTests {
    @Test
    fun `Basic voltage source and resistor with explicit ground`() {
        val builder = CircuitBuilder()
        val R = Resistor()
        val Vs = NortonSinusoidalVoltageSource(170.0)
        builder.addComponents(R)
        builder.addComponents(Vs)
        builder.joinPins(Vs.p, R.p)
        builder.joinPins(Vs.n, R.n)
        builder.ground(Vs.n)

        var built: CircuitBuilder.BuilderResult? = null
        assertDoesNotThrow("Builder exception") { built = builder.build() }

        assert(built!!.components.size == 2) { "Expected 2 components, got ${built.components.size} instead"}
        assert(built.nodeCount == 1) { "Expected 1 non-ground node, got ${built.nodeCount} instead"}

        val e = assertThrowsExactly<IllegalArgumentException>("Expected error from building built builder") { builder.build() }
        require(e.message == "Illegal circuit builder state: Circuit is already built!!") { "Wrong message: $e"}
    }

    @Test
    fun `Basic voltage source and resistor with implicit ground`() {
        val builder = CircuitBuilder()
        val R = Resistor()
        val Vs = NortonSinusoidalVoltageSource(170.0)
        builder.addComponents(R)
        builder.addComponents(Vs)
        builder.joinPins(Vs.p, R.p)
        builder.joinPins(Vs.n, R.n)

        var built: CircuitBuilder.BuilderResult? = null
        assertDoesNotThrow("Builder exception") { built = builder.build() }

        assert(built!!.components.size == 2) { "Expected 2 components, got ${built.components.size} instead"}
        assert(built.nodeCount == 1) { "Expected 1 non-ground node, got ${built.nodeCount} instead"}

        val e = assertThrowsExactly<IllegalArgumentException>("Expected error from building built builder") { builder.build() }
        require(e.message == "Illegal circuit builder state: Circuit is already built!!") { "Wrong message: $e"}
    }

    @Test
    fun `Throws on component with tied pins that wasn't added to the builder`() {
        val builder = CircuitBuilder()
        val R = Resistor()
        val Vs = NortonSinusoidalVoltageSource(170.0)
        builder.addComponents(Vs)
        builder.joinPins(Vs.p, R.p)
        builder.joinPins(Vs.n, R.n)
        builder.ground(Vs.n)

        val e = assertThrowsExactly<IllegalArgumentException>("Expected error from building built builder") { builder.build() }
        require(e.message == "One or more components have their pins tied, but are not present in the builder list") { "Wrong message: $e"}
    }
}