package top.chexson.enhancedblockingmode;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Objects;
import java.util.Set;

public class PatternProviderTargetCache {
    private final IActionSource src;


    public PatternProviderTargetCache(IActionSource src) {
        this.src = src;
    }
    private PatternProviderTarget wrapMeStorage(MEStorage storage) {
        return new PatternProviderTarget() {
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what,amount,type, PatternProviderTargetCache.this.src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for(Object2LongMap.Entry<AEKey> stack : storage.getAvailableStacks()) {
                    if(patternInputs.contains(((AEKey)stack.getKey()).dropSecondary())) {
                        return true;
                    }
                }
                return false;
            }
            public  boolean onlyHasPatternInput(Set<AEKey> patternInput) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInput.contains(stack.getKey().dropSecondary())) continue;
                    return false;
                }
                return true;
            }
        };
    }
}
