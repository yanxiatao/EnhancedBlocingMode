package top.chexson.enhancedblockingmode.mixin.patternprovider;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.util.ConfigManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.chexson.enhancedblockingmode.mixin.EnSettings;

import java.util.Set;


@Mixin(PatternProviderLogic.class)
public abstract class MixinPatternProvider {

    @Shadow
    @Final
    private IConfigManager configManager;

    @Shadow
    public abstract boolean isBlocking();


    @Shadow
    @Final
    private Set<AEKey> patternInputs;

    @Shadow
    public abstract boolean adapterAcceptsAll(PatternProviderTarget adapter, KeyCounter[] inputHolder);

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci, @Local(ordinal = 1) ConfigManager configManager  ) {
        configManager.registerSetting(EnSettings.BLOCKING_MODE, BlockingMode.ENHANCED);
    }


    @Inject(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderLogic$1PushTarget;target()Lappeng/helpers/patternprovider/PatternProviderTarget;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void EnhancedPush(CallbackInfoReturnable<Boolean> cir,IPatternDetails patternDetails, KeyCounter[] inputHolder,Direction direction,
                              PatternProviderTarget adapter) {

        if (configManager.getSetting(EnSettings.BLOCKING_MODE) == BlockingMode.ENHANCED && isBlocking() && adapter.containsPatternInput(patternInputs)) {
            for (AEKey patternInput : patternInputs) {
                if (!(adapter.containsPatternInput((Set<AEKey>) patternInput))) {
                    continue;
                }
                if (adapterAcceptsAll(adapter, inputHolder)) {
                    patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                        var inserted = adapter.insert(what, amount, Actionable.MODULATE);
                        if (inserted < amount) {
                            this.addToSendList(what, amount - inserted);
                        }
                    });
                    onPushPatternSuccess(patternDetails);
                    this.sendDirection = direction;
                    this.sendStacksOut();
                    ++roundRobinIndex;
                    cir.setReturnValue(true);
                }

            }
        }
    }
    @Shadow
    private int roundRobinIndex;
    @Shadow
    protected abstract boolean sendStacksOut();


    @Shadow
    protected abstract void onPushPatternSuccess(IPatternDetails patternDetails);

    @Shadow
    protected abstract void addToSendList(AEKey what, long l);

    @Shadow
    private Direction sendDirection;

}