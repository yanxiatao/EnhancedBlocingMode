package top.chexson.enhancedblockingmode.mixin.patternprovider;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.ConfigManager;
import com.google.common.util.concurrent.Runnables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.chexson.enhancedblockingmode.EnhanceBlockingMode;
import top.chexson.enhancedblockingmode.EnSettings;
import top.chexson.enhancedblockingmode.IPatternProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;


@Mixin(PatternProviderLogic.class)
public abstract class MixinPatternProvider implements IPatternProvider {

    @Shadow
    @Final
    private IActionSource actionSource;
    @Shadow
    @Final
    private IConfigManager configManager;

    @Shadow
    public abstract boolean isBlocking();


    @Shadow
    @Final
    private Set<AEKey> patternInputs;

    @Shadow
    protected abstract boolean adapterAcceptsAll(PatternProviderTarget adapter, KeyCounter[] inputHolder);

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("TAIL"),remap = false
    )
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, CallbackInfo ci) {
        ((ConfigManager)configManager).registerSetting(EnSettings.ENHANCED_BLOCKING_MODE, EnhanceBlockingMode.DEFAULT);
    }

/*
    @Inject(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderLogic;adapterAcceptsAll(Lappeng/helpers/patternprovider/PatternProviderTarget;[Lappeng/api/stacks/KeyCounter;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void EnhancedPush(IPatternDetails patternDetails, KeyCounter[] inputHolder,CallbackInfoReturnable<Boolean> cir,Direction direction,
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

 */

    @Final
    @Shadow
    private List<GenericStack> sendList;
    @Final
    @Shadow
    private IManagedGridNode mainNode;
    @Final
    @Shadow
    private List<IPatternDetails> patterns;
    @Final
    @Shadow
    private PatternProviderLogicHost host;



    @Unique
    public EnhanceBlockingMode enhancedBlockingMode$getBlockingMode() {
        return configManager.getSetting(EnSettings.ENHANCED_BLOCKING_MODE);
    }
    @Unique
    private boolean enhancedBlockingMode$adapterAcceptAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
            for (KeyCounter counter : inputHolder) {
                    for (Object2LongMap.Entry<AEKey> input : counter) {
                        long inserted = target.insert(input.getKey(),input.getLongValue(),Actionable.SIMULATE);
                        if (inserted == 0L) {
                            return false;
                        }
                    }
            }
            return true;
    }

    @Unique
    public boolean enhancedBlockingMode$onlyHasPatternInput(Set<AEKey> patternInputs,BlockEntity thisBe,Direction side) {


        MEStorage storage;
        Level l = thisBe.getLevel();
        BlockPos thisPos = thisBe.getBlockPos();
        BlockPos pos = thisPos.relative(side);
        storage = l.getCapability(AECapabilities.ME_STORAGE, pos, side);
        if (storage == null) {
            var strategies = StackWorldBehaviors.createExternalStorageStrategies((ServerLevel) l,pos,side);
            var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
            for (var entry : strategies.entrySet()) {
                var wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
                if (wrapper != null) {
                    externalStorages.put(entry.getKey(),wrapper);
                }
            }
            storage = new CompositeStorage(externalStorages);
        }



        for (var stack : storage.getAvailableStacks()) {
            if (patternInputs.contains(stack.getKey().dropSecondary())) continue;
            return false;
        }
        return true;
    }

    /**
     * @author 1
     * @reason 1
     */
    @Overwrite
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();

        if (getCraftingLockedReason() != LockCraftingMode.NONE) {
            return false;
        }

        record PushTarget(Direction direction, PatternProviderTarget target) {
        }
        var possibleTargets = new ArrayList<PushTarget>();

        // Push to crafting machines first
        for (var direction : getActiveSides()) {
            var adjPos = be.getBlockPos().relative(direction);
            var adjBeSide = direction.getOpposite();

            var craftingMachine = ICraftingMachine.of(level, adjPos, adjBeSide);
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide)) {
                    onPushPatternSuccess(patternDetails);
                    return true;
                }
                continue;
            }

            var adapter = findAdapter(direction);
            if (adapter == null)
                continue;

            possibleTargets.add(new PushTarget(direction, adapter));
        }

        // If no dedicated crafting machine could be found, and the pattern does not support
        // generic external inventories, stop here.
        if (!patternDetails.supportsPushInputsToExternalInventory()) {
            return false;
        }

        // Rearrange for round-robin
        rearrangeRoundRobin(possibleTargets);

        // Push to other kinds of blocks
        for (var target : possibleTargets) {
            var direction = target.direction();
            var adapter = target.target();

            if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs)) {
                if (this.enhancedBlockingMode$getBlockingMode() == EnhanceBlockingMode.ENHANCED) {
                    Set<AEKey> KeySet = new java.util.HashSet<>(Set.of());
                    for (var KeyCounter : inputHolder) {
                        KeySet.addAll(KeyCounter.keySet());
                    }
                    if ((this.enhancedBlockingMode$onlyHasPatternInput(KeySet,host.getBlockEntity(),direction))) {
                        if (this.enhancedBlockingMode$adapterAcceptAll(adapter,inputHolder)) {
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
                            return true;
                        }
                    }

                }
                continue;
            }

            if (this.adapterAcceptsAll(adapter, inputHolder)) {
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
                return true;
            }
        }

        return false;
    }


    @Shadow
    protected abstract <T> void rearrangeRoundRobin(List<T> list);

    @Shadow
    @Nullable
    protected abstract PatternProviderTarget findAdapter(Direction side);


    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Shadow
    public abstract LockCraftingMode getCraftingLockedReason();

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