package place.holder.dasii.mixin.gregtech;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import place.holder.dasii.mixin.api.capability.DasiiItemStackHandler;

import javax.annotation.Nonnull;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = GTItemStackHandler.class, remap = false)
public abstract class GTItemStackHandlerMixin extends ItemStackHandler implements DasiiItemStackHandler {

    @Unique
    protected boolean dasif$allowSameItemInsert = true;

    @Override
    public boolean getAllowSameItemInsert() {
        return dasif$allowSameItemInsert;
    }

    @Override
    public void setAllowSameItemInsert(boolean allowSameFluidFill) {
        this.dasif$allowSameItemInsert = allowSameFluidFill;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!dasif$allowSameItemInsert && !stack.isEmpty()) {
            for (int i = 0; i < getSlots(); i++) {
                if (i != slot && stack.isItemEqual(getStackInSlot(i))) {
                    return stack;
                }
            }
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setBoolean("allowSameItemInsert", dasif$allowSameItemInsert);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        dasif$allowSameItemInsert = nbt.getBoolean("allowSameItemInsert");
        super.deserializeNBT(nbt);
    }
}
