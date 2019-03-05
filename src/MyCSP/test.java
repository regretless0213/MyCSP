package MyCSP;

import static MyCSP.heuristic.Search.MyHeuristicSearch;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class test  extends AbstractProblem{

	@Override
	public void buildModel() {
		model = new Model("test");
		// TODO Auto-generated method stub
//		int[] p = {1,2,1,2,1};
//		int[] q = {2,3,3,5,5};

//		Tuples t = 
//		IntVar[][] sum = model.intVarMatrix("matrix", 5, 10, 0, 1);
//		for(int i = 0; i < 5 ; i++) {
//			Constraint cntr = model.sum(sum[i], "=", p[i]);
//			for(int j = 0; j < q[i];j++) {
//				IntVar[] subsum = new IntVar[q[i]];
//				subsum = 
//				Constraint cntrs = model.t
//			}
//		}
		IntVar s = model.intVar(0, 5, false);
		IntVar t = model.intVar(0, 5);

		System.out.println(s+" "+t);
	}
	@Override
	public void configureSearch() {
		// TODO Auto-generated method stub
//		model.getSolver().setSearch(inputOrderLBSearch());
	}
	
	@Override
	public void solve() {
		model.getSolver().solve();
		// TODO Auto-generated method stub
		for(int i = 0;i < model.getNbVars();i++) {
			model.getVar(i);
		}
		
	}
	
	public static void main(String[] args) {
		new test().execute(args);
	}

}
