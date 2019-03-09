
package MyCSP.model;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import MyCSP.model.data.DataSet.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static MyCSP.heuristic.Search.MyHeuristicSearch;

public class STGCS extends AbstractProblem {

	private float[] timetmp;
	private int ttindex;
	private List<String> result;

	public STGCS(List<String> rtmp) {
		timetmp = new float[1];
		ttindex = 0;
		result = rtmp;
	}
	public STGCS(float[] tt, int i) {
		timetmp = tt;
		ttindex = i;
		result = new ArrayList<>();
	}

	@Option(name = "-d", aliases = "--data", usage = "Car sequencing data.", required = false)

	CSPLib data = CSPLib.valueOf("random10");
//	MyData data = MyData.valueOf("md_b01");
//	Data data = Data.P41_66;

	IntVar[] CarSeq;
	IntVar[][] cars;

	int nCars, nClasses, nOptions;

	int[] demands;
	int[][] optfreq, matrix;

	@Override
	public void buildModel() {
		model = new Model("CarSequencing");
		parse(data.source());
		int[] darray = new int[nOptions]; // 计算单一零件需求总数
		for (int i = 0; i < nOptions; i++) {
			darray[i] = dscompute(matrix, i);
		}

		int max = nClasses - 1;
		CarSeq = model.intVarArray("CarSeq", nCars, 0, max, false);

		cars = model.intVarMatrix("cars", nCars, nOptions, 0, 1);

		// 表约束，限制矩阵中每一行的数据满足约束
		Tuples tp = new Tuples(true);
		for (int i = 0; i < nClasses; i++) {
			int[] row = new int[nOptions + 1];
			row[0] = i;
			for (int j = 0; j < nOptions; j++) {
				row[j + 1] = matrix[i][j];
			}
			tp.add(row);
		}
		for (int i = 0; i < nCars; i++) {
			IntVar[] row = new IntVar[nOptions + 1];
			row[0] = CarSeq[i];
			for (int j = 0; j < nOptions; j++) {
				row[j + 1] = cars[i][j];
			}
			model.table(row, tp).post();
		}
		// for (int i = 0; i < nCars; i++) {
		//
		// model.table(cars[i], tp).post();
		// }

		for (int i = 0; i < nOptions; i++) {

			// sum建模
			int numerator = optfreq[i][0];

			int denominator = optfreq[i][1];
			int module = nCars - denominator;

			for(int j = 0;j < module;j++) {
				IntVar[] array = extractor(cars, j, denominator, i);
				model.sum(array, "<=", numerator).post();
			}

			// 对每个零件进行总数约束
			IntVar[] sumarray = extractor(cars, 0, nCars, i);
			model.sum(sumarray, "=", darray[i]).post();
		}

		// 全局约束
		int[] values = new int[nClasses];
		IntVar[] expArray = new IntVar[nClasses];
		for (int i = 0; i < nClasses; i++) {
			expArray[i] = model.intVar("var_" + i, 0, demands[i], false);
			values[i] = i;
		}
		model.globalCardinality(CarSeq, values, expArray, false).post();

	}

	// 将矩阵中的一列数据提取出来
	private static IntVar[] extractor(IntVar[][] mac, int initialNumber, int amount, int num) {// 提取矩阵中第一列数据
		if ((initialNumber + amount) > mac.length) {
			amount = mac.length - initialNumber;
		}
		IntVar[] tmp = new IntVar[amount];
		for (int m = 0; m < amount; m++) {
			tmp[m] = mac[initialNumber + m][num];
		}
		return tmp;

	}

	// 计算单一零件的总需求量
	private int dscompute(int[][] matrixtmp, int num) {
		int d = 0;
		for (int m = 0; m < nClasses; m++) {
			d += matrixtmp[m][num] * demands[m];
		}
//		System.out.println(d);
		return d;

	}

	@Override
	public void configureSearch() {
		model.getSolver().setSearch(MyHeuristicSearch(CarSeq, matrix, optfreq, demands));
		model.getSolver().limitTime("20m");
		// model.getSolver().setSearch(inputOrderLBSearch(CarSeq));
	}

	@Override
	public void solve() {
		model.getSolver().solve();
//		model.getSolver().printStatistics();
		float time = model.getSolver().getTimeCount();
		timetmp[ttindex] = time;

		for (int i = 0; i < nCars; i++) {
			//将结果存储到List中
			result.add(model.getVars()[i].toString());

		}
//		System.out.println(time);
		// 打印结果
//		for (int i = 0; i < model.getNbVars(); i++) {
//			System.out.print(model.getVars()[i]);
//			if (i < nCars) {
//				System.out.println();
//			}
//			if ((i >= nCars) && ((i + 1 - nCars) % nOptions == 0) && ((i - nCars) < (nOptions * nCars))) {
//				System.out.println();
//			}
//			if ((i - nCars) >= (nOptions * nCars)) {
//				System.out.println();
//			}
//		}

	}

	public static float getAverage(float[] ft) {

		float r = 0;
		for (int i = 0; i < ft.length; i++) {
//			System.out.println("ft" + i + ft[i]);
			r += ft[i];
		}
		return r / ft.length;
	}

	public static void main(String[] args) {
		List<String> r = new ArrayList<>();
		new STGCS(r).execute(args);
		System.out.println("列表：");
		for(String tmp : r) {
			System.out.println(tmp);
		}
	}

	private int[][] parse(String source) {
		int[][] data = null;
		Scanner sc = new Scanner(source);
		nCars = sc.nextInt();
		nOptions = sc.nextInt();
		nClasses = sc.nextInt();

		optfreq = new int[nOptions][2];
		// 获取零件容量
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][0] = sc.nextInt();
		}
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][1] = sc.nextInt();
		}

		// 获取类型需求数量；获取零件矩阵
		demands = new int[nClasses];
		matrix = new int[nClasses][nOptions];
		for (int i = 0; i < nClasses; i++) {
			sc.nextInt();
			demands[i] = sc.nextInt();
			for (int j = 0; j < nOptions; j++) {
				matrix[i][j] = sc.nextInt();
			}
		}
		sc.close();
		return data;
	}

}
