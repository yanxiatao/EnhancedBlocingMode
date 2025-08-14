package top.chexson.enhancedblockingmode.mixin.patternprovider;

import appeng.api.stacks.AEKeyType;

import appeng.me.storage.CompositeStorage;
import appeng.api.storage.MEStorage;
import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.util.ConfigManager;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.chexson.enhancedblockingmode.PatternProviderTargetCache;
import top.chexson.enhancedblockingmode.mixin.EnSettings;

import java.util.ArrayList;
import java.util.Set;


@Mixin(PatternProviderLogic.class)
public abstract class MixinPatternProvider {
    @Final
    @Shadow(remap = false)
    private ConfigManager configManager;
    @Final
    @Shadow
    private IManagedGridNode mainNode;
    @Final
    @Shadow
    private PatternProviderLogicHost host;

    @Shadow
    public abstract boolean isBlocking();

    @Shadow
    @Final
    private PatternProviderTargetCache[] targetCaches;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"),
            remap = false)
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        configManager.registerSetting(EnSettings.BLOCKING_MODE, BlockingMode.ENHANCED);
    }
    @Unique
    private boolean isEnhancedBlocking() {
        return this.configManager.getSetting(EnSettings.BLOCKING_MODE) == BlockingMode.ENHANCED;
    }

    @Inject(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderLogic$1PushTarget;target()Lappeng/helpers/patternprovider/PatternProviderTarget;"))
    private void EnhancedPush(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if isEnhancedBlocking() && isBlocking() && adapter. {

        }
    }
}