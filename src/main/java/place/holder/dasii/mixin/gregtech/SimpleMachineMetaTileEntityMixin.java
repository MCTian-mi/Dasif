package place.holder.dasii.mixin.gregtech;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import place.holder.dasii.mixin.api.capability.DasiiItemStackHandler;

import java.util.function.Function;

@Mixin(value = SimpleMachineMetaTileEntity.class, remap = false)
public abstract class SimpleMachineMetaTileEntityMixin extends WorkableTieredMetaTileEntity {

    @Unique
    private static final int UPDATE_DISALLOW_SAME_ITEM_INSERT = 114514;

    @Unique
    protected boolean dasif$disallowSameItemInsert = false;

    public SimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @ModifyReturnValue(method = "createGuiTemplate", at = @At("RETURN"))
    protected ModularUI.Builder createGuiTemplate(ModularUI.Builder builder) {
        if (builder != null) {
            if (this.importItems.getSlots() > 1) {
                builder.widget((new ToggleButtonWidget(151, 7, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::dasif$isDisallowSameItemInsert, this::dasif$setDisallowSameItemInsert))
                        .setTooltipText("gregtech.gui.disallow_same_item_insert.tooltip")
                        .shouldUseBaseBackground());
            }
        }
        return builder;
    }

    @ModifyReturnValue(method = "writeToNBT", at = @At("RETURN"))
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("disallowSameItemInsert", dasif$disallowSameItemInsert);
        return data;
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void readFromNBT(NBTTagCompound data, CallbackInfo ci) {
        dasif$disallowSameItemInsert = data.getBoolean("disallowSameItemInsert");
        ((DasiiItemStackHandler) importItems).setAllowSameItemInsert(!dasif$disallowSameItemInsert);
    }

    @Inject(method = "writeInitialSyncData", at = @At("RETURN"))
    public void writeInitialSyncData(PacketBuffer buf, CallbackInfo ci) {
        buf.writeBoolean(dasif$disallowSameItemInsert);
    }

    @Inject(method = "receiveInitialSyncData", at = @At("RETURN"))
    public void receiveInitialSyncData(PacketBuffer buf, CallbackInfo ci) {
        this.dasif$disallowSameItemInsert = buf.readBoolean();
    }

    @Inject(method = "receiveCustomData", at = @At("RETURN"))
    public void receiveCustomData(int dataId, PacketBuffer buf, CallbackInfo ci) {
        if (dataId == UPDATE_DISALLOW_SAME_ITEM_INSERT) {
            this.dasif$disallowSameItemInsert = buf.readBoolean();
        }
    }

    @Unique
    public void dasif$setDisallowSameItemInsert(boolean disallowSameItemFill) {
        this.dasif$disallowSameItemInsert = disallowSameItemFill;
        if (!this.getWorld().isRemote) {
            ((DasiiItemStackHandler) importItems).setAllowSameItemInsert(!disallowSameItemFill);
            this.writeCustomData(UPDATE_DISALLOW_SAME_ITEM_INSERT, (buf) -> buf.writeBoolean(dasif$disallowSameItemInsert));
            this.markDirty();
        }
    }

    @Unique
    public boolean dasif$isDisallowSameItemInsert() {
        return this.dasif$disallowSameItemInsert;
    }
}
