
package MyCSP.heuristic.variables;

import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.IntList;

public class MidExploration<V extends Variable> implements VariableSelector<V> {
	private IntList sets;

    public MidExploration() {
        sets = new IntList();
    }
    @Override
    public V getVariable(V[] variables) {
        sets.clear();
        for (int idx = 0; idx < variables.length; idx++) {
            if (!variables[idx].isInstantiated()) {
                sets.add(idx);
            }
        }
        if (sets.size() > 0) {
            int mid_idx = sets.get(sets.size()/2);
//            System.out.println(mid_idx);
            return variables[mid_idx];
        } else return null;
    }
}
