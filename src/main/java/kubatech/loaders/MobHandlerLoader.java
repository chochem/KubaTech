package kubatech.loaders;

import static kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber.DIAMOND_SPIKES_DAMAGE;
import static kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber.MOB_SPAWN_INTERVAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dreammaster.main.MainRegistry;
import com.dreammaster.modcustomdrops.CustomDrops;
import com.kuba6000.mobsinfo.api.MobDrop;
import com.kuba6000.mobsinfo.api.MobOverride;
import com.kuba6000.mobsinfo.api.MobRecipe;
import com.kuba6000.mobsinfo.api.event.MobNEIRegistrationEvent;
import com.kuba6000.mobsinfo.api.event.PostMobRegistrationEvent;
import com.kuba6000.mobsinfo.api.event.PostMobsOverridesLoadEvent;
import com.kuba6000.mobsinfo.api.event.PreMobsRegistrationEvent;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.util.GT_Utility;
import kubatech.Tags;
import kubatech.api.LoaderReference;
import kubatech.api.helpers.ReflectionHelper;
import kubatech.config.Config;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber;

public class MobHandlerLoader {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID + "[Mob Handler Loader]");

    private static MobHandlerLoader instance = null;

    public static void init() {
        instance = new MobHandlerLoader();
        MinecraftForge.EVENT_BUS.register(instance);
    }

    public static Map<String, MobEECRecipe> recipeMap = new HashMap<>();

    public static class MobEECRecipe {

        public final List<MobDrop> mOutputs;

        public final MobRecipe recipe;

        public final int mEUt = 2000;
        public final int mDuration;

        public MobEECRecipe(List<MobDrop> transformedDrops, MobRecipe recipe) {
            this.mOutputs = transformedDrops;
            this.recipe = recipe;
            mDuration = Math.max(MOB_SPAWN_INTERVAL, (int) ((recipe.maxEntityHealth / DIAMOND_SPIKES_DAMAGE) * 10d));
        }

        public ItemStack[] generateOutputs(Random rnd, GT_MetaTileEntity_ExtremeExterminationChamber MTE,
            double attackDamage, int lootinglevel, boolean preferInfernalDrops) {
            MTE.lEUt = mEUt;
            MTE.mMaxProgresstime = Math.max(MOB_SPAWN_INTERVAL, (int) ((recipe.maxEntityHealth / attackDamage) * 10d));
            ArrayList<ItemStack> stacks = new ArrayList<>(this.mOutputs.size());
            for (MobDrop o : this.mOutputs) {
                int chance = o.chance;
                if (o.playerOnly) {
                    chance = (int) ((double) chance * Config.MobHandler.playerOnlyDropsModifier);
                    if (chance < 1) chance = 1;
                }
                int amount = o.stack.stackSize;
                if (o.lootable && lootinglevel > 0) {
                    chance += lootinglevel * 5000;
                    if (chance > 10000) {
                        int div = (int) Math.ceil(chance / 10000d);
                        amount *= div;
                        chance /= div;
                    }
                }
                if (chance == 10000 || rnd.nextInt(10000) < chance) {
                    ItemStack s = o.stack.copy();
                    s.stackSize = amount;
                    if (o.enchantable != null) EnchantmentHelper.addRandomEnchantment(rnd, s, o.enchantable);
                    if (o.damages != null) {
                        int rChance = rnd.nextInt(recipe.mMaxDamageChance);
                        int cChance = 0;
                        for (Map.Entry<Integer, Integer> damage : o.damages.entrySet()) {
                            cChance += damage.getValue();
                            if (rChance <= cChance) {
                                s.setItemDamage(damage.getKey());
                                break;
                            }
                        }
                    }
                    stacks.add(s);
                }
            }

            if (LoaderReference.InfernalMobs) {
                InfernalMobsCore infernalMobsCore = InfernalMobsCore.instance();
                if (recipe.infernalityAllowed && mEUt * 8 < MTE.getMaxInputVoltage()
                    && !infernalMobsCore.getDimensionBlackList()
                        .contains(
                            MTE.getBaseMetaTileEntity()
                                .getWorld().provider.dimensionId)) {
                    int p = 0;
                    int mods = 0;
                    if (recipe.alwaysinfernal
                        || (preferInfernalDrops && rnd.nextInt(infernalMobsCore.getEliteRarity()) == 0)) {
                        p = 1;
                        if (rnd.nextInt(infernalMobsCore.getUltraRarity()) == 0) {
                            p = 2;
                            if (rnd.nextInt(infernalMobsCore.getInfernoRarity()) == 0) p = 3;
                        }
                    }
                    ArrayList<ItemStack> infernalstacks = null;
                    if (p > 0) if (p == 1) {
                        infernalstacks = infernalMobsCore.getDropIdListElite();
                        mods = infernalMobsCore.getMinEliteModifiers();
                    } else if (p == 2) {
                        infernalstacks = infernalMobsCore.getDropIdListUltra();
                        mods = infernalMobsCore.getMinUltraModifiers();
                    } else {
                        infernalstacks = infernalMobsCore.getDropIdListInfernal();
                        mods = infernalMobsCore.getMinInfernoModifiers();
                    }
                    if (infernalstacks != null) {
                        ItemStack infernalstack = infernalstacks.get(rnd.nextInt(infernalstacks.size()))
                            .copy();
                        // noinspection ConstantConditions
                        EnchantmentHelper.addRandomEnchantment(
                            rnd,
                            infernalstack,
                            infernalstack.getItem()
                                .getItemEnchantability());
                        stacks.add(infernalstack);
                        MTE.lEUt *= 8L;
                        MTE.mMaxProgresstime *= mods * InfernalMobsCore.instance()
                            .getMobModHealthFactor();
                    }
                }
            }

            return stacks.toArray(new ItemStack[0]);
        }

    }

    @SubscribeEvent
    public void onPreMobsRegistration(PreMobsRegistrationEvent event) {
        recipeMap.clear();
    }

    @SubscribeEvent
    public void onPostMobRegistration(PostMobRegistrationEvent event) {
        if (!event.drops.isEmpty() && event.recipe.isUsableInVial) {
            ArrayList<MobDrop> drops = (ArrayList<MobDrop>) event.drops.clone();
            drops.removeIf(d -> d.chance == 0);
            if (!drops.isEmpty()) {
                recipeMap.put(event.currentMob, new MobEECRecipe(drops, event.recipe));
                event.drops.stream()
                    .filter(d -> d.chance == 0)
                    .forEach(
                        d -> d.additionalInfo.add(StatCollector.translateToLocal("kubatech.mobhandler.eec_disabled")));
            }
        }
    }

    @SubscribeEvent
    public void onPostOverridesConfigLoad(PostMobsOverridesLoadEvent event) {
        if (LoaderReference.GTNHCoreMod) {
            LOG.info("Detected GTNH Core Mod, parsing custom drops from there.");
            CustomDrops coredrops = ReflectionHelper.getField(MainRegistry.Module_CustomDrops, "_mCustomDrops", null);
            if (coredrops != null) {
                @SuppressWarnings("unchecked")
                ArrayList<CustomDrops.CustomDrop> customdrops = (ArrayList<CustomDrops.CustomDrop>) ((ArrayList<CustomDrops.CustomDrop>) coredrops
                    .getCustomDrops()).clone();
                for (CustomDrops.CustomDrop customdrop : customdrops) {
                    try {
                        Class<?> eclass = Class.forName(customdrop.getEntityName());
                        if (!EntityLiving.class.isAssignableFrom(eclass)) continue;
                        String ename = (String) EntityList.classToStringMapping.get(eclass);
                        if (ename == null) continue;
                        MobOverride override = event.overrides.computeIfAbsent(ename, k -> new MobOverride());
                        for (CustomDrops.CustomDrop.Drop drop : customdrop.getDrops()) {
                            String[] parts = drop.getItemName()
                                .split(":");
                            ItemStack stack = GameRegistry.findItemStack(parts[0], parts[1], 1);
                            if (stack == null) continue;
                            if (parts.length > 2) stack.setItemDamage(Integer.parseInt(parts[2]));
                            String pNBT = ReflectionHelper.getField(drop, "mTag", null);
                            if (pNBT != null && !pNBT.isEmpty()) {
                                try {
                                    stack.stackTagCompound = (NBTTagCompound) JsonToNBT.func_150315_a(pNBT);
                                } catch (Exception ignored) {}
                            }
                            int chance = drop.getChance() * 100;
                            int amount = drop.getAmount();
                            if (drop.getIsRandomAmount()) {
                                // average chance formula
                                // chance *= ((((amount * (amount + 1d)) / 2d)) + 1d) / (amount + 1d);
                                chance *= (2d + (amount * amount) + amount) / (2d * (amount + 1d));
                                amount = 1;
                                if (chance > 10000) {
                                    int div = (int) Math.ceil(chance / 10000d);
                                    amount *= div;
                                    chance /= div;
                                }
                            }
                            stack.stackSize = amount;
                            // Drops from coremod are player only
                            MobDrop mobDrop = new MobDrop(
                                stack,
                                MobDrop.DropType.Normal,
                                chance,
                                null,
                                null,
                                false,
                                true);
                            mobDrop.additionalInfo.add(
                                StatCollector.translateToLocalFormatted(
                                    "kubatech.mobhandler.eec_chance",
                                    (((double) chance / 100d) * Config.MobHandler.playerOnlyDropsModifier)));
                            override.additions.add(mobDrop);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @SubscribeEvent
    public void onMobNEIRegistration(MobNEIRegistrationEvent event) {
        MobEECRecipe recipe = recipeMap.get(event.mobName);
        if (recipe != null) {
            event.additionalInformation.addAll(
                Arrays.asList(
                    GT_Utility.trans("153", "Usage: ") + GT_Utility.formatNumbers(recipe.mEUt) + " EU/t",
                    GT_Utility.trans("158", "Time: ") + GT_Utility.formatNumbers(recipe.mDuration / 20d) + " secs"));
        }
    }
}