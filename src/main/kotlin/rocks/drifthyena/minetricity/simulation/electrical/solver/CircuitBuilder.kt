package rocks.drifthyena.minetricity.simulation.electrical.solver

import rocks.drifthyena.minetricity.simulation.electrical.component.CircuitComponent
import rocks.drifthyena.minetricity.simulation.electrical.component.ComponentPin


class Pinwheel() {
    var id = -2
        set(value) {
            if (value == field) return
            println("DEBUG: Pinwheel got assigned new id $value, old $field")
            pins.forEach { it.nodeId = value }
            field = value
        }

    val pins = mutableListOf<ComponentPin>()
}

class CircuitBuilder {
    val components = hashSetOf<CircuitComponent>()
    val groundPins = hashSetOf<ComponentPin>()
    val pinwheels = hashSetOf<Pinwheel>()
    val pinwheelMap = hashMapOf<ComponentPin, Pinwheel>()
    var building = true
    var grounded = false

    private fun validateBuilding(msg: String?) {
        require(building) {
            "Illegal circuit builder state: ${msg?: "(no extra info)"}"
        }
    }

    fun addComponent(comp: CircuitComponent) {
        validateBuilding("Tried to add component on built circuit")
        components.add(comp)
    }

    fun addComponents(vararg comp: CircuitComponent) {
        validateBuilding("Tried to add component on built circuit")

        components.addAll(comp)
    }

    /**
     * Joins component pins. If any one is grounded they all get OWWOWOOOWOWOWOWOWWOWOWOWOW GROUNDED GROUNDED GROUNDED
     */
    fun joinPins(vararg pins: ComponentPin) {
        validateBuilding("Tried to join pins on built circuit")

        if (areAnyGrounded(pins.toList())) {
            val suckers = hashSetOf<Pinwheel>()

            pins.forEach {
                val wheel = pinwheelMap[it]

                if (null != wheel) {
                    pinwheelMap.remove(it, wheel)
                    suckers.add(wheel)
                }

                groundPins.add(it)
            }

            pinwheels.removeAll(suckers)

            return
        }

        val wheels = hashSetOf<Pinwheel>()

        pins.forEach { pin ->
            if (null != pinwheelMap[pin]) {
                wheels.add(pinwheelMap[pin]!!) // If this throws then we had a solar particle event...
            }
        }

        val newWheel: Pinwheel = when(wheels.size) {
            0 -> {
                Pinwheel()
            }

            1 -> {
                wheels.first()
            }

            else -> {
                val hehe = wheels.first()

                wheels.forEach { byebye ->
                    if (byebye == hehe) return@forEach

                    byebye.pins.forEach { nomore ->
                        pinwheelMap.remove(nomore, byebye)
                        hehe.pins.add(nomore)
                    }

                    pinwheels.remove(byebye)
                }

                wheels.first()
            }
        }

        pins.forEach { pin ->
            newWheel.pins.add(pin)
            pinwheelMap[pin] = newWheel
        }

        pinwheels.add(newWheel)
    }

    fun areAnyGrounded(pins: List<ComponentPin>): Boolean {
        pins.forEach {
            if (groundPins.contains(it)) return true
        }

        return false
    }

    /**
     * Grounds a [pin]. If it is found in any pinwheel, it removes all pin(s) in the wheel and grounds them too.
     */
    fun ground(pin: ComponentPin) {
        validateBuilding("Tried to ground a pin on a built circuit")

        val wheelie = pinwheelMap[pin]

        if (null != wheelie) {
            wheelie.pins.forEach { pin ->
                pinwheelMap.remove(pin, wheelie)
                groundPins.add(pin)
            }

            pinwheels.remove(wheelie)
        } else {
            groundPins.add(pin)
        }

        grounded = true
    }

    fun build(): BuilderResult {
        validateBuilding("Circuit is already built!!")

        require(pinwheels.isNotEmpty()) {
            "No pins were joined!"
        }

        pinwheelMap.forEach { (pin, pinwheel) ->
            require(components.contains(pin.owner)) {
                "One or more components have their pins tied, but are not present in the builder list"
            }
        }

        /** Just pick the biggest one */
        if (!grounded) {
            var biggest = pinwheels.first()

            pinwheels.forEach {
                if (it == biggest) return@forEach

                if (it.pins.size > biggest.pins.size) {
                    biggest = it
                }
            }

            biggest.pins.forEach { ground(it) }

            grounded = true
        }

        groundPins.forEach {
            it.nodeId = -1
        }

        var numNodes = 0

        pinwheels.forEach { wheel ->
            wheel.id = numNodes++
        }

        var index = numNodes

        components.forEach { component ->
            val requested = component.getAdditionalSlots()

            if (requested > 0) {
                component.assignSlots(index, index + requested)
                index += requested
            }
        }

        building = false

        return BuilderResult(
            nodeCount = numNodes,
            extraSlotCount = index - numNodes,
            components = components.toList()
        )
    }

    data class BuilderResult(
        val nodeCount: Int,
        val extraSlotCount: Int,
        val components: List<CircuitComponent>,
    )
}