package top.chexson.enhancedblockingmode.mixin.misc;


import appeng.api.config.Setting;
import appeng.api.config.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.chexson.enhancedblockingmode.EnSettings;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;

@Mixin(value = Settings.class)
public abstract class MixinSettings {



//    @Unique
//    private static final Setting<EnhanceBlockingMode> ENHANCED_BLOCKING_MODE_SETTING = register("enhanced_blocking_mode",EnhanceBlockingMode.class);


    @Shadow
    private static <T extends Enum<T>> Setting<T> register(String name, T firstOption, T... moreOptions) {
        return null;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void init(CallbackInfo ci) {
        EnSettings.ENHANCED_BLOCKING_MODE = register("enhanced_blocking_mode", EnhanceBlockingMode.DEFAULT,EnhanceBlockingMode.ENHANCED);
    }
}
