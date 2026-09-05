package rocks.drifthyena.minetricity.simulation.electrical.solver

import rocks.drifthyena.minetricity.util.inverse

class Stamper(val solver: MonaSystem) {
    val matrix = solver.matrix

    companion object {
        fun isNotGround(id: Int): Boolean = (id >= 0)
    }

    var dt = solver.dt

    fun stampResistance(p: Int, n: Int, R: Double) {
        val G = (R * dt).inverse()
        stampPair(p, n, G)
    }

    fun stampInductance(p: Int, n: Int, L: Double) {
        stampPair(p, n, L.inverse())
    }

    fun stampPair(p: Int, n: Int, value: Double) {
        if (isNotGround(p)) {
            stampMatrixRaw(p, p, value)
        }

        if (isNotGround(n)) {
            stampMatrixRaw(n, n, value)
        }

        if (isNotGround(p) && isNotGround(n)) {
            stampMatrixRaw(p, n, -value)
            stampMatrixRaw(n, p, -value)
        }
    }

    // TODO: Should this overwrite or add? Or should we zero out the b vector before preSolve()?
    fun addRhs(index: Int, value: Double) {
        if (!isNotGround(index)) return
        val e = solver.knowns.get(index, 0)
        solver.knowns.set(index, 0, value + e)
    }

    fun stampMatrixRaw(r: Int, c: Int, value: Double) {
        solver.setMatrixModified()
        val e = matrix.get(r, c)
        matrix.set(r, c, e + value)
    }
}