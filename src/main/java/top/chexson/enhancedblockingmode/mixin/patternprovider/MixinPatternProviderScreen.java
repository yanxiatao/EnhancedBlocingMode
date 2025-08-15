package top.chexson.enhancedblockingmode.mixin.patternprovider;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import appeng.client.gui.AEBaseScreen;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;
import top.chexson.enhancedblockingmode.EnSettings;
import top.chexson.enhancedblockingmode.IPatternProvider;

@Mixin(value = PatternProviderScreen.class,remap = false)
public abstract class MixinPatternProviderScreen<C extends PatternProviderMenu> extends AEBaseScreen<C> {
    @Unique
    private ServerSettingToggleButton<EnhanceBlockingMode> enhancedBlockingMode$blockingMode;

    public MixinPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }


    @Inject(method = "<init>",at=@At("TAIL"),remap = false)
    private void init(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        this.enhancedBlockingMode$blockingMode = new ServerSettingToggleButton<>(
                EnSettings.ENHANCED_BLOCKING_MODE,
                EnhanceBlockingMode.DEFAULT
        );
        this.addToLeftToolbar(this.enhancedBlockingMode$blockingMode);
    }

    @Inject(method = "updateBeforeRender", at=@At("TAIL"),remap = false)
    private void updateBeforeRender(CallbackInfo ci) {
        this.enhancedBlockingMode$blockingMode.set(((IPatternProvider) menu).enhancedBlockingMode$getBlockingMode());
    }

}
