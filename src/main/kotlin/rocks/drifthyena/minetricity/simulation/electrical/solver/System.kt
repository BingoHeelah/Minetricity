package rocks.drifthyena.minetricity.simulation.electrical.solver

import org.ejml.data.DMatrixRMaj
import org.ejml.data.DMatrixSparseCSC
import org.ejml.sparse.FillReducing
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC
import rocks.drifthyena.minetricity.simulation.electrical.component.CircuitSimulatable

data class MonaSystemOptions(
    val components: List<CircuitSimulatable>,
    val totalMatrixSize: Int,
    val dt: Double,
)

class MonaSystem(val options: MonaSystemOptions) {
    private var matrixDirty = true
    val dt = options.dt
    val components = options.components
    var totalTime = 0.0
        private set

    fun setMatrixModified() {
        matrixDirty = true
    }

    val matrix = DMatrixSparseCSC(options.totalMatrixSize, options.totalMatrixSize)
    val voltages = DoubleArray(options.totalMatrixSize)
    val knowns = DMatrixRMaj(options.totalMatrixSize, 1)
    val psiCurrent = DoubleArray(options.totalMatrixSize)
    var psiWrapper = DMatrixRMaj.wrap(options.totalMatrixSize, 1, psiCurrent)
    var psiHistory1 = DoubleArray(psiCurrent.size)
    var psiHistory2 = DoubleArray(psiCurrent.size)
    val solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE)
    val stamper = Stamper(this)

    init {
        options.components.forEach {
            it.acceptSolver(this)
            it.stamp(stamper)
        }

        solver.setA(matrix)
    }

    fun readHistory(index: Int, secondOrder: Boolean = false): Double {
        if (index < 0) return 0.0

        if (secondOrder) {
            return psiHistory2[index]
        } else {
            return psiHistory1[index]
        }
    }

    fun readNodeVoltage(index: Int): Double {
        if (index < 0) return 0.0

        //return (psiCurrent[index] - psiHistory1[index]) / dt
        return voltages[index]
    }

    fun solveMultipleSteps(amount: Int) {
        for (i in 1..amount) stepInternal()
    }

    fun solveSingleStep() {
        stepInternal()
    }

    private fun stepInternal() {
        totalTime += dt

        knowns.zero()

        if (matrixDirty) {
            solver.setA(matrix)
            matrixDirty = false
        }

        components.forEach {
            it.preSolve()
        }

        solver.solve(knowns, psiWrapper)

        components.forEach {
            it.postSolve()
        }

        for (j in 0..voltages.size-1) {
            voltages[j] = (psiCurrent[j] - psiHistory1[j]) / dt
        }

        updateHistory()
    }

    private fun updateHistory() {
        System.arraycopy(psiHistory1, 0, psiHistory2, 0, psiCurrent.size)
        System.arraycopy(psiCurrent, 0, psiHistory1, 0, psiCurrent.size)
        psiWrapper = DMatrixRMaj.wrap(options.totalMatrixSize, 1, psiCurrent)
    }
}
