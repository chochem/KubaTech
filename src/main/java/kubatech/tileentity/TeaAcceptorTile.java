/*
 * KubaTech - Gregtech Addon Copyright (C) 2022 - 2023 kuba6000 This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version. This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this library. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package kubatech.tileentity;

import java.text.NumberFormat;
import java.util.function.BiFunction;

import kubatech.api.enums.ItemList;
import kubatech.api.utils.StringUtils;
import kubatech.loaders.ItemLoader;
import kubatech.loaders.block.KubaBlock;
import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.*;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

public class TeaAcceptorTile extends TileEntity
        implements IInventory, ITileWithModularUI, KubaBlock.IModularUIProvider {

    public TeaAcceptorTile() {
        super();
    }

    private String tileOwner = null;
    private PlayerData playerData = null;
    private long averageInput = 0L;
    private long inAmount = 0L;
    private int ticker = 0;

    public void setTeaOwner(String teaOwner) {
        if (tileOwner == null || tileOwner.isEmpty()) {
            tileOwner = teaOwner;
            playerData = PlayerDataManager.getPlayer(tileOwner);
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound NBTData) {
        super.readFromNBT(NBTData);
        tileOwner = NBTData.getString("tileOwner");
        if (!tileOwner.isEmpty()) {
            playerData = PlayerDataManager.getPlayer(tileOwner);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound NBTData) {
        super.writeToNBT(NBTData);
        NBTData.setString("tileOwner", tileOwner);
    }

    @Override
    public void updateEntity() {
        if (++ticker % 100 == 0) {
            averageInput = inAmount / 100;
            inAmount = 0;
        }
    }

    @Override
    public int getSizeInventory() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        if (playerData != null) {
            playerData.teaAmount += p_70299_2_.stackSize;
            playerData.markDirty();
            inAmount += p_70299_2_.stackSize;
        }
    }

    @Override
    public String getInventoryName() {
        return "Tea acceptor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return p_70300_1_.getCommandSenderName().equals(tileOwner);
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    private static final int minDamage = ItemList.BlackTea.get(1).getItemDamage();
    private static final int maxDamage = ItemList.YellowTea.get(1).getItemDamage();

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return p_94041_2_.getItem() == ItemLoader.kubaitems && p_94041_2_.getItemDamage() >= minDamage
                && p_94041_2_.getItemDamage() <= maxDamage;
    }

    private static final UIInfo<?, ?> UI = KubaBlock.TileEntityUIFactory.apply(ModularUIContainer::new);

    @Override
    public UIInfo<?, ?> getUI() {
        return UI;
    }

    private static TextWidget posCenteredHorizontally(int y, TextWidget textWidget) {
        return (TextWidget) textWidget.setPosProvider(posCenteredHorizontallyProvider.apply(textWidget, y));
    }

    private static final BiFunction<TextWidget, Integer, Widget.PosProvider> posCenteredHorizontallyProvider = (
            TextWidget widget, Integer y) -> (Widget.PosProvider) (screenSize, window,
                    parent) -> new Pos2d((window.getSize().width / 2) - (widget.getSize().width / 2), y);

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(170, 70);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        EntityPlayer player = buildContext.getPlayer();

        builder.widgets(
                posCenteredHorizontally(
                        10,
                        new TextWidget(
                                new Text("Tea Acceptor").format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.DARK_RED))),
                posCenteredHorizontally(30, new DynamicTextWidget(() -> {
                    if (player.getCommandSenderName().equals(tileOwner))
                        return new Text("[Tea]").color(Color.GREEN.normal);
                    else return new Text("This is not your block").color(Color.RED.normal);
                })),
                posCenteredHorizontally(
                        40,
                        new DynamicTextWidget(
                                () -> new Text(
                                        (playerData == null ? "ERROR"
                                                : StringUtils.applyRainbow(
                                                        NumberFormat.getInstance().format(playerData.teaAmount),
                                                        (int) ((playerData.teaAmount / Math.max(1, averageInput * 10))
                                                                % Integer.MAX_VALUE),
                                                        EnumChatFormatting.BOLD.toString()))).shadow())),
                posCenteredHorizontally(
                        50,
                        new DynamicTextWidget(() -> new Text("IN: " + averageInput + "/t").color(Color.BLACK.normal)))
                                .addTooltip(
                                        new Text("Average input from the last 5 seconds").color(Color.GRAY.normal)));
        return builder.build();
    }
}
