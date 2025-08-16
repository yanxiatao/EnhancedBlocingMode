package top.chexson.enhancedblockingmode.mixin.misc;


import appeng.api.config.Setting;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;

import static top.chexson.enhancedblockingmode.EnSettings.ENHANCED_BLOCKING_MODE;
import static top.chexson.enhancedblockingmode.EnhanceBlockingMode.*;

@Mixin(value = SettingToggleButton.class,remap = false)
public abstract class MixinSettingToggleButton {
    @Shadow(remap = false)
    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val,
                                                        ButtonToolTips title, ButtonToolTips hint){}
    @Shadow(remap = false)
    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val,
                                                        ButtonToolTips title, Component... tooltipLines){}


    @Redirect(method = "<init>(Lappeng/api/config/Setting;Ljava/lang/Enum;Ljava/util/function/Predicate;Lappeng/client/gui/widgets/SettingToggleButton$IHandler;)V",
            at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/widgets/SettingToggleButton;registerApp(Lappeng/client/gui/Icon;Lappeng/api/config/Setting;Ljava/lang/Enum;Lappeng/core/localization/ButtonToolTips;Lappeng/core/localization/ButtonToolTips;)V",ordinal = 0))
    private <T extends Enum<T>> void register(Icon icon, Setting<T> setting, T val, ButtonToolTips title, ButtonToolTips hint) {

        MixinSettingToggleButton.registerApp(Icon.CLEAR, ENHANCED_BLOCKING_MODE,DEFAULT,
                ButtonToolTips.InterfaceBlockingMode,
                Component.translatable("gui.enhancedblockingmode.enhanced_blocking_mode.default"));
        MixinSettingToggleButton.registerApp(Icon.BLOCKING_MODE_YES, ENHANCED_BLOCKING_MODE,ENHANCED,
                ButtonToolTips.InterfaceBlockingMode,
                Component.translatable("gui.enhancedblockingmode.enhanced_blocking_mode.enhanced"));
    }
}
