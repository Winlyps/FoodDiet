package winlyps.foodDiet

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class FoodDiet : JavaPlugin(), Listener {

    private val playerConsumptionMap = mutableMapOf<Player, ConsumptionTracker>()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    override fun onDisable() {
        playerConsumptionMap.clear()
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (item != null && item.type.isEdible) {
            val tracker = playerConsumptionMap.getOrPut(player) { ConsumptionTracker() }
            tracker.trackConsumption(item.type)

            if (tracker.shouldPenalize()) {
                object : BukkitRunnable() {
                    override fun run() {
                        player.foodLevel = player.foodLevel - 1
                    }
                }.runTaskLater(this, 1)
            }
        }
    }

    private class ConsumptionTracker {
        private var lastItem: Material? = null
        private var consecutiveCount = 0

        fun trackConsumption(item: Material) {
            if (item == lastItem) {
                consecutiveCount++
            } else {
                lastItem = item
                consecutiveCount = 1
            }
        }

        fun shouldPenalize(): Boolean {
            return consecutiveCount > 3
        }
    }
}