package me.badbones69.crazyenchantments.enchantments;

import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.FileManager.Files;
import me.badbones69.crazyenchantments.api.enums.CEnchantments;
import me.badbones69.crazyenchantments.api.events.EnchantmentUseEvent;
import me.badbones69.crazyenchantments.api.objects.EnchantedArrow;
import me.badbones69.crazyenchantments.api.objects.ItemBuilder;
import me.badbones69.crazyenchantments.multisupport.*;
import me.badbones69.crazyenchantments.multisupport.Support.SupportedPlugins;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Bows implements Listener {
	
	private CrazyEnchantments ce = CrazyEnchantments.getInstance();
	private List<EnchantedArrow> enchantedArrows = new ArrayList<>();
	private Material web = new ItemBuilder().setMaterial("COBWEB", "WEB").getMaterial();
	private List<Block> webBlocks = new ArrayList<>();
	private boolean isv1_13_Up = Version.getCurrentVersion().isNewer(Version.v1_12_R1);
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBowShoot(final EntityShootBowEvent e) {
		if(e.isCancelled()) return;
		ItemStack bow = e.getBow();
		if(ce.hasEnchantments(bow)) {
			if(e.getProjectile() instanceof Arrow) {
				Arrow arrow = (Arrow) e.getProjectile();
				enchantedArrows.add(new EnchantedArrow(arrow, e.getEntity(), bow, ce.getEnchantmentsOnItem(bow)));
				if(ce.hasEnchantment(bow, CEnchantments.MULTIARROW)) {
					if(CEnchantments.MULTIARROW.isActivated()) {
						int power = ce.getLevel(bow, CEnchantments.MULTIARROW);
						if(CEnchantments.MULTIARROW.chanceSuccessful(bow)) {
							if(e.getEntity() instanceof Player) {
								EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.MULTIARROW, bow);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									for(int i = 1; i <= power; i++) {
										Arrow spawnedArrow = e.getEntity().getWorld().spawn(e.getProjectile().getLocation(), Arrow.class);
										spawnedArrow.setShooter(e.getEntity());
										spawnedArrow.setBounce(false);
										Vector v = new Vector(randomSpred(), 0, randomSpred());
										spawnedArrow.setVelocity(e.getProjectile().getVelocity().add(v));
										if(((Arrow) e.getProjectile()).isCritical()) {
											spawnedArrow.setCritical(true);
										}
										if(e.getProjectile().getFireTicks() > 0) {
											spawnedArrow.setFireTicks(e.getProjectile().getFireTicks());
										}
										if(isv1_13_Up) {
											spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
										}
									}
								}
							}else {
								for(int i = 1; i <= power; i++) {
									Arrow spawnedArrow = e.getEntity().getWorld().spawn(e.getProjectile().getLocation(), Arrow.class);
									spawnedArrow.setShooter(e.getEntity());
									spawnedArrow.setBounce(false);
									Vector v = new Vector(randomSpred(), 0, randomSpred());
									spawnedArrow.setVelocity(e.getProjectile().getVelocity().add(v));
									if(((Arrow) e.getProjectile()).isCritical()) {
										spawnedArrow.setCritical(true);
									}
									if(e.getProjectile().getFireTicks() > 0) {
										spawnedArrow.setFireTicks(e.getProjectile().getFireTicks());
									}
									if(isv1_13_Up) {
										spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onland(ProjectileHitEvent e) {
		if(e.getEntity() instanceof Arrow) {
			EnchantedArrow arrow = getEnchantedArrow((Arrow) e.getEntity());
			if(arrow != null) {
				if(arrow.hasEnchantment(CEnchantments.STICKY_SHOT)) {
					if(CEnchantments.STICKY_SHOT.isActivated()) {
						if(CEnchantments.STICKY_SHOT.chanceSuccessful(arrow.getBow())) {
							if(Version.getCurrentVersion().isNewer(Version.v1_10_R1)) {
								if(e.getHitEntity() == null) {//If the arrow hits a block.
									Location entityLocation = e.getEntity().getLocation();
									if(entityLocation.getBlock().getType() == Material.AIR) {
										entityLocation.getBlock().setType(web);
										webBlocks.add(entityLocation.getBlock());
										e.getEntity().remove();
										new BukkitRunnable() {
											@Override
											public void run() {
												entityLocation.getBlock().setType(Material.AIR);
												webBlocks.remove(entityLocation.getBlock());
											}
										}.runTaskLater(ce.getPlugin(), 5 * 20);
									}
								}else {//If the arrow hits an entity.
									Entity en = e.getHitEntity();
									List<Location> locations = new ArrayList<>();
									Location enLocation = en.getLocation();
									locations.add(enLocation.clone().add(1, 0, 1));//Top Left
									locations.add(enLocation.clone().add(1, 0, 0));//Top Middle
									locations.add(enLocation.clone().add(1, 0, -1));//Top Right
									locations.add(enLocation.clone().add(0, 0, 1));//Center Left
									locations.add(enLocation.clone());//Center Middle
									locations.add(enLocation.clone().add(0, 0, -1));//Center Right
									locations.add(enLocation.clone().add(-1, 0, 1));//Bottom Left
									locations.add(enLocation.clone().add(-1, 0, 0));//Bottom Middle
									locations.add(enLocation.clone().add(-1, 0, -1));//Bottom Right
									for(Location loc : locations) {
										if(loc.getBlock().getType() == Material.AIR) {
											loc.getBlock().setType(web);
											webBlocks.add(loc.getBlock());
										}
									}
									e.getEntity().remove();
									new BukkitRunnable() {
										@Override
										public void run() {
											for(Location loc : locations) {
												if(loc.getBlock().getType() == web) {
													loc.getBlock().setType(Material.AIR);
													webBlocks.remove(loc.getBlock());
												}
											}
										}
									}.runTaskLater(ce.getPlugin(), 5 * 20);
								}
							}else {//If the arrow hits something.
								if(e.getEntity().getNearbyEntities(.5, .5, .5).isEmpty()) {//Checking to make sure it doesn't hit an entity.
									Location entityLocation = e.getEntity().getLocation();
									if(entityLocation.getBlock().getType() == Material.AIR) {
										entityLocation.getBlock().setType(web);
										webBlocks.add(entityLocation.getBlock());
										e.getEntity().remove();
										new BukkitRunnable() {
											@Override
											public void run() {
												entityLocation.getBlock().setType(Material.AIR);
												webBlocks.remove(entityLocation.getBlock());
											}
										}.runTaskLater(ce.getPlugin(), 5 * 20);
									}
								}
							}
						}
					}
				}
				if(arrow.hasEnchantment(CEnchantments.BOOM)) {
					if(CEnchantments.BOOM.isActivated()) {
						if(CEnchantments.BOOM.chanceSuccessful(arrow.getBow())) {
							Methods.explode(arrow.getShooter(), arrow.getArrow());
							arrow.getArrow().remove();
						}
					}
				}
				if(arrow.hasEnchantment(CEnchantments.LIGHTNING)) {
					if(CEnchantments.LIGHTNING.isActivated()) {
						Location loc = arrow.getArrow().getLocation();
						if(CEnchantments.LIGHTNING.chanceSuccessful(arrow.getBow())) {
							Player shooter = (Player) arrow.getShooter();
							loc.getWorld().spigot().strikeLightningEffect(loc, true);
							int lightningSoundRange = Files.CONFIG.getFile().getInt("Settings.EnchantmentOptions.Lightning-Sound-Range", 160);
							try {
								loc.getWorld().playSound(loc, ce.getSound("ENTITY_LIGHTNING_BOLT_IMPACT", "ENTITY_LIGHTNING_IMPACT"), (float) lightningSoundRange / 16f, 1);
							}catch(Exception ignore) {
							}
							if(SupportedPlugins.NO_CHEAT_PLUS.isPluginLoaded()) {
								NoCheatPlusSupport.exemptPlayer(shooter);
							}
							if(SupportedPlugins.SPARTAN.isPluginLoaded()) {
								SpartanSupport.cancelNoSwing(shooter);
							}
							if(SupportedPlugins.AAC.isPluginLoaded()) {
								AACSupport.exemptPlayer(shooter);
							}
							for(LivingEntity entity : Methods.getNearbyLivingEntities(loc, 2D, arrow.getArrow())) {
								EntityDamageByEntityEvent damageByEntityEvent = new EntityDamageByEntityEvent(shooter, entity, DamageCause.LIGHTNING, 5D);
								Bukkit.getPluginManager().callEvent(damageByEntityEvent);
								if(!damageByEntityEvent.isCancelled()) {
									if(Support.allowsPVP(entity.getLocation())) {
										if(!Support.isFriendly(arrow.getShooter(), entity)) {
											if(!arrow.getShooter().getUniqueId().equals(entity.getUniqueId())) {
												entity.damage(5D);
											}
										}
									}
								}
							}
							if(SupportedPlugins.NO_CHEAT_PLUS.isPluginLoaded()) {
								NoCheatPlusSupport.unexemptPlayer(shooter);
							}
							if(SupportedPlugins.AAC.isPluginLoaded()) {
								AACSupport.unexemptPlayer(shooter);
							}
						}
					}
				}
				//Removes the arrow from the list after 5 ticks. This is done because the onArrowDamage event needs the arrow in the list so it can check.
				new BukkitRunnable() {
					@Override
					public void run() {
						enchantedArrows.remove(arrow);// Removes it from the list.
					}
				}.runTaskLaterAsynchronously(ce.getPlugin(), 5);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onArrowDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Arrow) {
			if(e.getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) e.getEntity();
				EnchantedArrow arrow = getEnchantedArrow((Arrow) e.getDamager());
				if(arrow != null) {
					ItemStack bow = arrow.getBow();
					if(Support.isFriendly(arrow.getShooter(), e.getEntity())) {// Damaged player is friendly.
						if(arrow.hasEnchantment(CEnchantments.DOCTOR)) {
							if(CEnchantments.DOCTOR.isActivated()) {
								int heal = 1 + arrow.getLevel(CEnchantments.DOCTOR);
								if(entity.getHealth() < entity.getMaxHealth()) {
									if(entity instanceof Player) {
										EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.DOCTOR, bow);
										Bukkit.getPluginManager().callEvent(event);
										if(!event.isCancelled()) {
											if(entity.getHealth() + heal < entity.getMaxHealth()) {
												entity.setHealth(entity.getHealth() + heal);
											}
											if(entity.getHealth() + heal >= entity.getMaxHealth()) {
												entity.setHealth(entity.getMaxHealth());
											}
										}
									}else {
										if(entity.getHealth() + heal < entity.getMaxHealth()) {
											entity.setHealth(entity.getHealth() + heal);
										}
										if(entity.getHealth() + heal >= entity.getMaxHealth()) {
											entity.setHealth(entity.getMaxHealth());
										}
									}
								}
							}
						}
					}
					if(!e.isCancelled()) {
						if(!Support.isFriendly(arrow.getShooter(), entity)) {// Damaged player is an enemy.
							if(arrow.hasEnchantment(CEnchantments.STICKY_SHOT)) {
								if(CEnchantments.STICKY_SHOT.isActivated()) {
									if(CEnchantments.STICKY_SHOT.chanceSuccessful(bow)) {
										List<Location> locations = new ArrayList<>();
										Location enLocation = entity.getLocation();
										locations.add(enLocation.clone().add(1, 0, 1));//Top Left
										locations.add(enLocation.clone().add(1, 0, 0));//Top Middle
										locations.add(enLocation.clone().add(1, 0, -1));//Top Right
										locations.add(enLocation.clone().add(0, 0, 1));//Center Left
										locations.add(enLocation.clone());//Center Middle
										locations.add(enLocation.clone().add(0, 0, -1));//Center Right
										locations.add(enLocation.clone().add(-1, 0, 1));//Bottom Left
										locations.add(enLocation.clone().add(-1, 0, 0));//Bottom Middle
										locations.add(enLocation.clone().add(-1, 0, -1));//Bottom Right
										for(Location loc : locations) {
											if(loc.getBlock().getType() == Material.AIR) {
												loc.getBlock().setType(web);
												webBlocks.add(loc.getBlock());
											}
										}
										arrow.getArrow().remove();
										new BukkitRunnable() {
											@Override
											public void run() {
												for(Location loc : locations) {
													if(loc.getBlock().getType() == web) {
														loc.getBlock().setType(Material.AIR);
														webBlocks.remove(loc.getBlock());
													}
												}
											}
										}.runTaskLater(ce.getPlugin(), 5 * 20);
									}
								}
							}
							if(arrow.hasEnchantment(CEnchantments.PULL)) {
								if(CEnchantments.PULL.isActivated()) {
									if(CEnchantments.PULL.chanceSuccessful(bow)) {
										Vector v = arrow.getShooter().getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(3);
										if(entity instanceof Player) {
											EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.PULL, bow);
											Bukkit.getPluginManager().callEvent(event);
											Player player = (Player) e.getEntity();
											if(!event.isCancelled()) {
												if(SupportedPlugins.SPARTAN.isPluginLoaded()) {
													SpartanSupport.cancelSpeed(player);
													SpartanSupport.cancelFly(player);
													SpartanSupport.cancelClip(player);
													SpartanSupport.cancelNormalMovements(player);
													SpartanSupport.cancelNoFall(player);
													SpartanSupport.cancelJesus(player);
												}
												if(SupportedPlugins.AAC.isPluginLoaded()) {
													AACSupport.exemptPlayerTime(player);
												}
												entity.setVelocity(v);
											}
										}else {
											entity.setVelocity(v);
										}
									}
								}
							}
							if(arrow.hasEnchantment(CEnchantments.ICEFREEZE)) {
								if(CEnchantments.ICEFREEZE.isActivated()) {
									if(CEnchantments.ICEFREEZE.chanceSuccessful(bow)) {
										if(entity instanceof Player) {
											EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.ICEFREEZE, bow);
											Bukkit.getPluginManager().callEvent(event);
											if(!event.isCancelled()) {
												entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1));
											}
										}else {
											entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1));
										}
									}
								}
							}
							if(arrow.hasEnchantment(CEnchantments.PIERCING)) {
								if(CEnchantments.PIERCING.isActivated()) {
									if(CEnchantments.PIERCING.chanceSuccessful(bow)) {
										if(entity instanceof Player) {
											EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.PIERCING, bow);
											Bukkit.getPluginManager().callEvent(event);
											if(!event.isCancelled()) {
												e.setDamage(e.getDamage() * 2);
											}
										}else {
											e.setDamage(e.getDamage() * 2);
										}
									}
								}
							}
							if(arrow.hasEnchantment(CEnchantments.VENOM)) {
								if(CEnchantments.VENOM.isActivated()) {
									if(CEnchantments.VENOM.chanceSuccessful(bow)) {
										if(entity instanceof Player) {
											EnchantmentUseEvent event = new EnchantmentUseEvent((Player) e.getEntity(), CEnchantments.VENOM, bow);
											Bukkit.getPluginManager().callEvent(event);
											if(!event.isCancelled()) {
												entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2 * 20, arrow.getLevel(CEnchantments.VENOM) - 1));
											}
										}else {
											entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2 * 20, arrow.getLevel(CEnchantments.VENOM) - 1));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onWebBreak(BlockBreakEvent e) {
		if(webBlocks.contains(e.getBlock())) {
			e.setCancelled(true);
		}
	}
	
	private EnchantedArrow getEnchantedArrow(Arrow arrow) {
		for(EnchantedArrow enchantedArrow : enchantedArrows) {
			if(enchantedArrow.getArrow() != null && enchantedArrow.getArrow().equals(arrow)) {
				return enchantedArrow;
			}
		}
		return null;
	}
	
	private ArrayList<CEnchantments> getEnchantments() {
		ArrayList<CEnchantments> enchants = new ArrayList<>();
		enchants.add(CEnchantments.BOOM);
		enchants.add(CEnchantments.DOCTOR);
		enchants.add(CEnchantments.ICEFREEZE);
		enchants.add(CEnchantments.LIGHTNING);
		enchants.add(CEnchantments.PIERCING);
		enchants.add(CEnchantments.VENOM);
		enchants.add(CEnchantments.PULL);
		return enchants;
	}
	
	private float randomSpred() {
		float spread = (float) .2;
		return -spread + (float) (Math.random() * ((spread - -spread)));
	}
	
}