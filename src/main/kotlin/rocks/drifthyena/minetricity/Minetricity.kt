package rocks.drifthyena.minetricity

import com.mojang.logging.LogUtils
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent

const val MODID = "minetricity"

@Mod(MODID)
class Minetricity(val bus: IEventBus, container: ModContainer) {
    companion object {
        val LOGGER = LogUtils.getLogger()
    }

    init {
        LOGGER.info("Minetricity static init")

        LOGGER.info("Minetricity static init end")
    }
}