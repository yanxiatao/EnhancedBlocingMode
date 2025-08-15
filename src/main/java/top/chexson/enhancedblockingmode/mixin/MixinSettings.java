package top.chexson.enhancedblockingmode.mixin;


import appeng.api.config.Setting;
import appeng.api.config.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;
import top.chexson.enhancedblockingmode.EnhancedBlockingMode;

@Mixin(value = Settings.class,remap = false)
public abstract class MixinSettings {


    @Shadow
    protected static <T extends Enum<T>> Setting<T> register(String name, Class<T> enumClass) {
        return null;
    }

    @Unique
    private static final Setting<EnhanceBlockingMode> ENHANCE_BLOCKING_MODE_SETTING = register("enhance_blocking_mode",EnhanceBlockingMode.class);

}
