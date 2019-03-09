
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
		int[] darray = new int[nOptions]; // ���㵥һ�����������
		for (int i = 0; i < nOptions; i++) {
			darray[i] = dscompute(matrix, i);
		}

		int max = nClasses - 1;
		CarSeq = model.intVarArray("CarSeq", nCars, 0, max, false);

		cars = model.intVarMatrix("cars", nCars, nOptions, 0, 1);

		// ��Լ�������ƾ�����ÿһ�е���������Լ��
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

			// sum��ģ
			int numerator = optfreq[i][0];

			int denominator = optfreq[i][1];
			int module = nCars - denominator;

			for(int j = 0;j < module;j++) {
				IntVar[] array = extractor(cars, j, denominator, i);
				model.sum(array, "<=", numerator).post();
			}

			// ��ÿ�������������Լ��
			IntVar[] sumarray = extractor(cars, 0, nCars, i);
			model.sum(sumarray, "=", darray[i]).post();
		}

		// ȫ��Լ��
		int[] values = new int[nClasses];
		IntVar[] expArray = new IntVar[nClasses];
		for (int i = 0; i < nClasses; i++) {
			expArray[i] = model.intVar("var_" + i, 0, demands[i], false);
			values[i] = i;
		}
		model.globalCardinality(CarSeq, values, expArray, false).post();

	}

	// �������е�һ��������ȡ����
	private static IntVar[] extractor(IntVar[][] mac, int initialNumber, int amount, int num) {// ��ȡ�����е�һ������
		if ((initialNumber + amount) > mac.length) {
			amount = mac.length - initialNumber;
		}
		IntVar[] tmp = new IntVar[amount];
		for (int m = 0; m < amount; m++) {
			tmp[m] = mac[initialNumber + m][num];
		}
		return tmp;

	}

	// ���㵥һ�������������
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
			//������洢��List��
			result.add(model.getVars()[i].toString());

		}
//		System.out.println(time);
		// ��ӡ���
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
		System.out.println("�б�");
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
		// ��ȡ�������
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][0] = sc.nextInt();
		}
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][1] = sc.nextInt();
		}

		// ��ȡ����������������ȡ�������
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
