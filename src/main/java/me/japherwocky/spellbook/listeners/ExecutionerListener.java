package me.japherwocky.spellbook.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.japherwocky.spellbook.Spellbook;
import me.japherwocky.spellbook.SpellbookConfig;
import me.japherwocky.spellbook.enchants.SpellbookEnchant;
import me.japherwocky.spellbook.enchants.ExecutionerEnchant;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("UnstableApiUsage")
public class ExecutionerListener implements Listener {

    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment executioner = registry.get(ExecutionerEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onExecutionerDamage(EntityDamageByEntityEvent event) {
        if (executioner == null) return;
        SpellbookEnchant enchant = SpellbookConfig.ENCHANTS.get(ExecutionerEnchant.KEY);
        if (!(enchant instanceof ExecutionerEnchant executionerEnchant)) return;
        Entity damager = event.getDamageSource().getCausingEntity();
        if (damager == null) return;
        if (!damager.equals(event.getDamageSource().getDirectEntity())) return;
        if (!(damager instanceof LivingEntity damagerEntity)) return;

        EntityEquipment damagerEquipment = damagerEntity.getEquipment();
        if (damagerEquipment == null) return;
        int level = Spellbook.getSumOfEnchantLevels(damagerEquipment, executioner);
        if (level == 0) return;

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingEntity)) return;

        AttributeInstance maxHealthAttribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return;
        double targetMaxHealth = maxHealthAttribute.getValue();

        double targetHealthPercentage = livingEntity.getHealth() / targetMaxHealth;

        if (targetHealthPercentage < executionerEnchant.getMaxDamageHpThreshold()) {
            double damageMultiplier = 1 + (executionerEnchant.getDamageMultiplierPerLevel() * level);
            event.setDamage(event.getDamage() * damageMultiplier);
        }

    }

}
