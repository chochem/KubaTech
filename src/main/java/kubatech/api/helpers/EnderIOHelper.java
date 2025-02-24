/*
 * KubaTech - Gregtech Addon Copyright (C) 2022 - 2023 kuba6000 This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version. This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this library. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package kubatech.api.helpers;

import kubatech.api.LoaderReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.IBossDisplayData;

import crazypants.enderio.EnderIO;

public class EnderIOHelper {

    public static boolean canEntityBeCapturedWithSoulVial(Entity entity, String entityID) {
        if (!LoaderReference.EnderIO) return true;
        if (ReflectionHelper.<Boolean>callMethod(EnderIO.itemSoulVessel, "isBlackListed", false, entityID))
            return false;
        return crazypants.enderio.config.Config.soulVesselCapturesBosses || !(entity instanceof IBossDisplayData);
    }

    public static boolean canEntityBeCapturedWithSoulVial(Entity entity) {
        return canEntityBeCapturedWithSoulVial(entity, EntityList.getEntityString(entity));
    }
}
