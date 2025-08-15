package top.chexson.enhancedblockingmode.mixin.patternprovider;


import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;
import top.chexson.enhancedblockingmode.EnSettings;
import top.chexson.enhancedblockingmode.IPatternProvider;

@Mixin(value = PatternProviderMenu.class,remap = false)
public abstract class MixinPatternProviderMenu implements IPatternProvider {



    @Shadow
    @Final
    protected PatternProviderLogic logic;
    @Unique
    @GuiSync(8)
    public EnhanceBlockingMode enbm$EnhanceBlockingMode = EnhanceBlockingMode.DEFAULT;

    @Override
    public EnhanceBlockingMode enhancedBlockingMode$getBlockingMode() {
        return enbm$EnhanceBlockingMode;
    }

    @Inject(method = "broadcastChanges", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getUnlockStack()Lappeng/api/stacks/GenericStack;"))
    public void broadcastChanges(CallbackInfo ci) {
        enbm$EnhanceBlockingMode = logic.getConfigManager().getSetting(EnSettings.ENHANCED_BLOCKING_MODE);
    }
}
