
package MyCSP.heuristic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.IntVar;


import MyCSP.heuristic.values.SelectionAggregation;
import MyCSP.heuristic.variables.Exploration;


public class Search {

    // ************************************************************************************
    // GENERIC PATTERNS
    // ************************************************************************************


    /**
     * Builds your own search strategy based on <b>binary</b> decisions.
     *
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param decisionOperator defines how to modify the domain of the selected variable with the selected value
     * @param vars         variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                           IntValueSelector valSelector,
                                           DecisionOperator<IntVar> decisionOperator,
                                           IntVar... vars) {
        return new IntStrategy(vars, varSelector, valSelector, decisionOperator);
    }

    /**
     * Builds your own assignment strategy based on <b>binary</b> decisions.
     * Selects a variable X and a value V to make the decision X = V.
     * Note that value assignments are the public static decision operators.
     * Therefore, they are not mentioned in the search heuristic name.
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param vars         variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                           IntValueSelector valSelector,
                                           IntVar... vars) {
        return intVarSearch(varSelector, valSelector, DecisionOperatorFactory.makeIntEq(), vars);
    }

    /**
     * Builds a default search heuristics of integer variables
     * Variable selection relies on {@link #domOverWDegSearch(IntVar...)}
     * Value selection relies on InDomainBest for optimization and InDomainMin for satisfaction
     * @param vars         variables to branch on
     * @return a default search strategy
     */
    public static AbstractStrategy<IntVar> intVarSearch(IntVar... vars) {
        Model model = vars[0].getModel();
        IntValueSelector valueSelector;
        if(model.getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                || !(model.getObjective() instanceof IntVar)){
                valueSelector = new IntDomainMin();
        }else{
            valueSelector = new IntDomainBest();
            Solution lastSolution = new Solution(model, vars);
            model.getSolver().plugMonitor((IMonitorSolution) lastSolution::record);
            valueSelector = new IntDomainLast(lastSolution, valueSelector);
        }
        return new DomOverWDeg(vars, 0,valueSelector);
    }


	/**
	 * 自定义启发式
	 */

	public static AbstractStrategy<IntVar> MyHeuristicSearch(IntVar[] vars, int[][] mat, int[][] opm, int[] dm) {
//		return new Heuristic(vars, 0, new IntDomainMin(), mat, opm, dm);
//		return new DomOverWDeg(vars, 0, new SelectionAggregation(vars, 0, mat, opm, dm));
		return intVarSearch(new Exploration<IntVar>(vars[0].getModel()), new SelectionAggregation(vars, 0, mat, opm, dm), vars);
//		return intVarSearch(new Exploration<IntVar>(vars[0].getModel()), new SUStandardization(vars, 0, mat, opm, dm), vars);
		
		
//		return intVarSearch(new Exploration<IntVar>(vars[0].getModel()), new SAStandardization(vars, 6, mat, opm, dm), vars);
	}
}