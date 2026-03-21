package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class AlwaysAirTraceabilityPredicate extends TraceabilityPredicate {

    public AlwaysAirTraceabilityPredicate() {}

    public AlwaysAirTraceabilityPredicate(TraceabilityPredicate predicate) {
        super(predicate);
    }

    public AlwaysAirTraceabilityPredicate(Predicate<MultiblockState> predicate, Supplier<BlockInfo[]> candidates) {
        super(predicate, candidates);
    }

    public AlwaysAirTraceabilityPredicate(SimplePredicate simplePredicate) {
        super(simplePredicate);
    }

    @Override
    public boolean isAir() {
        return true;
    }
}
