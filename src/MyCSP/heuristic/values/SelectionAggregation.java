
package MyCSP.heuristic.values;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

public final class SelectionAggregation implements IntValueSelector {

	private TIntList bests;

	private java.util.Random random;

	private IntVar[] CarSeq; // ��������
	private int[][] matrix, optfreq; // ���ݹ�����0/1������������
	private int[] demands, result, slotava; // ���ݹ�����ÿ���������;�洢��̬��ÿ������Ѿ���װ�˼��Σ�ÿ�������Ӧ��ʣ������
//	private int slots; // �ܲ����

	private enum WeightStrategy {
		constant, capacity, redemand, load, slack, usagerate, neweight
	}

	private WeightStrategy ws;

	private enum AggregationStrategy {
		Sum, Euc, Lex
	}

	private AggregationStrategy as;

	public SelectionAggregation(IntVar[] vars, long seed, int[][] options, int[][] frequency, int[] nums) {
		bests = new TIntArrayList();
		random = new java.util.Random(seed);
		matrix = options;
		optfreq = frequency;
		demands = nums;
		CarSeq = vars;
//		slots = 0;
		slotava = new int[optfreq.length];

		for (int j = 0; j < slotava.length; j++) {
			slotava[j] = sumofArray(demands);
		}
		// System.out.println(slots);
		// ѡ��Ȩֵ�������
		ws = WeightStrategy.load;
		as = AggregationStrategy.Sum;
	}

	private int sumofArray(int[] array) {
		int sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	@Override
	public int selectValue(IntVar var) {
		// TODO Auto-generated method stub
		// IntVar best = null;
		bests.clear();
		// System.out.println("selectvalue");
		DynamicInformation di = new DynamicInformation(CarSeq);
		int[] retmp = null;
		if (di.getICarSeq() != null) {
			retmp = di.getICarSeq();

			// System.out.print("retmp=");
			// for (int out : retmp) {
			// System.out.print(out + " ");
			// }
			// System.out.println();
		}

		int length = matrix[0].length;
		// System.out.println(length);
		result = new int[length];
		if (retmp != null) {
			// ͳ���м�ֵ�����и�����ѱ���ֵ�Ĵ���
			for (int m = 0; m < retmp.length; m++) {
				int row = retmp[m];
				// System.out.println(row);
				for (int n = 0; n < length; n++) {
					// System.out.println(result[n]);
					result[n] += matrix[row][n];
//					System.out.print("result" + n + " " + result[n]);
				}
//				System.out.println();
			}
			// ����ʣ������
			for (int n = 0; n < length; n++) {
				int m = 0;
				int count = 0;
				int index = 0;
				for (; m < retmp.length; m++) {
					int row = retmp[m];
					// �Ѹ�ֵ�Ĳ���һ������Լ��
					if (optfreq[n][0] == 1) {
						if (matrix[row][n] == 1) {
							m += optfreq[n][1] - 1;
						}
					} else if (optfreq[n][0] == 2) {
						if (matrix[row][n] == 1) {
							count++;
							if (count < 2) {
								index = m;
							} else {
								if (m - index > optfreq[n][1] - 1) {
									count = 1;
									index = m;
								} else {
									m = index + optfreq[n][1];
								}
								count = 0;
							}
						}
					}
				}
				slotava[n] = sumofArray(demands) - m;
				if (slotava[n] < 0) {
					slotava[n] = 0;
				}

			}
		}

		double _d = 0;
		double[] _da = new double[optfreq.length];
		int up = var.getUB();
		for (int idx = var.getLB(); idx <= up; idx = var.nextValue(idx)) {
			// System.out.print("value ");
			// System.out.print(idx + " ");
			Object wtmp = aggregateWeight(matrix[idx]); // Ȩֵ
//			double weight = -1;
//			switch (ws) {
//			case capacity:
//				weight = capacity(matrix[idx]);
//				break;
//			case constant:
//				weight = weight(matrix[idx]);
//				break;
//			case load:
//				weight = load(matrix[idx]);
//				break;
//			case neweight:
//				weight = neweight(matrix[idx]);
//				break;
//			case redemand:
//				weight = redemand(matrix[idx]);
//				break;
//			case slack:
//				weight = slack(matrix[idx]);
//				break;
//			case usagerate:
//				weight = usagerate(matrix[idx]);
//				break;
//			default:
//				System.out.println("Ȩֵ������������");
//				break;
//
//			}
			if (wtmp.getClass().isAssignableFrom(Double.class)) {
				
//				System.out.println("����Ȩֵ�ۺϣ�");
				
				double weight = (double) wtmp;
//				System.out.print(weight+" ");
				if (weight > _d) {
					bests.clear();
					bests.add(idx);
					_d = weight;
				} else if (_d == weight) {
					bests.add(idx);
				}
			} else if(wtmp.getClass().isAssignableFrom(double[].class)){// �ۺϷ���ΪLex�ǵ�ѡֵ����
				
//				System.out.println("LexȨֵ�ۺϣ�");
				
				int alleq = 1;// �ж��Ƿ�������ÿ��ֵ�����
				double[] weight = (double[]) wtmp;
//				//���Ȩֵ���
//				System.out.print("{");
//				for(double printd:weight) {
//					System.out.print(printd+" ");
//				}
//				System.out.println("}");
				for (int i = 0; i < weight.length; i++) {
					if (weight[i] > _da[i]) {// ��һ��ֵ����Ԥ��ֵ�ͱ����ֵ��index������ѭ��
						alleq = 0;
						bests.clear();
						bests.add(idx);
						_da = weight;
						break;
					} else if (weight[i] < _da[i]) {// ��һ��ֵС��Ԥ��ֵ���ֵ��������ֱ�ӽ���ѭ��
						alleq = 0;
						break;
					}
				}
				if (alleq == 1) {
					bests.add(idx);
				}
			}else {
				System.out.println(wtmp.getClass()+" Object���ƥ�����");
			}
//			System.out.println(idx+"����");
		}
		
//		System.out.println("����ֵ�б��С"+bests.size());
		// System.out.println();
//		if (bests.size() > 0) {
//			int currentVar = bests.get(random.nextInt(bests.size()));
//			return currentVar;
//		}else {
//			System.out.println("weight error!");
//			System.exit(0);
//			return up;
//		}
		// System.out.println();
		int currentVar = up;
		try {
			currentVar = bests.get(random.nextInt(bests.size()));
		} catch (Exception e) {
			System.out.println("weight error!");
		}
//		System.out.println("ѡ�����"+currentVar);
		return currentVar;

	}

	private Object aggregateWeight(int[] va) {
		int length = va.length;
		double weight = 0;
		double[] lexweight = new double[length];
		for (int i = 0; i < length; i++) {
			double wtmp = getWeight(va[i], i);
			switch (as) {
			case Euc:
				weight += Math.pow(wtmp, 2);
				break;
			case Lex:
				lexweight[i] = wtmp;
				break;
			case Sum:
				weight += wtmp;
				break;
			default:
				System.out.println("�ۺϷ�����������");
				break;

			}
		}
		switch (as) {
		case Lex:
			return sortArray(lexweight);
		default:
			return weight;

		}
	}
	private double[] sortArray(double[] array) {
		Arrays.sort(array);
		int n = array.length;
		double[] result = new double[n];
		for(int i = 0;i<n;i++) {
			result[i] = array[n-1-i];
		}
		return result;
	}

	private double getWeight(int v, int index) {
		double weight = -1; // Ȩֵ
		switch (ws) {
		case capacity:
			weight = capacity(v, index);
			break;
		case constant:
			weight = weight(v);
			break;
		case load:
			weight = load(v, index);
			break;
		case neweight:
			weight = neweight(v, index);
			break;
		case redemand:
			weight = redemand(v, index);
			break;
		case slack:
			weight = slack(v, index);
			break;
		case usagerate:
			weight = usagerate(v, index);
			break;
		default:
			System.out.println("Ȩֵ������������");
			break;

		}
		return weight;
	}



	private double demand(int optnum) {
		double d = 0;
		// System.out.println("��������");
		for (int i = 0; i < matrix.length; i++) {
			d += matrix[i][optnum] * demands[i];
			// System.out.print("matrix "+ matrix[i][optnum] + " demands "+ demands[i]);
		}
//		System.out.println(" d=" + d);
		return d;
	}

	private double loadcompute(int optnum) {
		double l = (demand(optnum) - result[optnum]) * optfreq[optnum][1] / optfreq[optnum][0];
		return l;
	}


	// �°�
	/*��̬*/
	/**����*/
	private double weight(int v) {
		double w = v;
//		System.out.print(w + " ");
		return w;
	}
	/**����*/
	private double capacity(int v, int index) {
		double w = v * (double)optfreq[index][1] / optfreq[index][0];
		return w;

	}
	/*��̬*/
	/**ʣ������*/
	private double redemand(int v, int index) {
		// ��̬ʣ��������μ���
		double w = v * (demand(index) - result[index]);
//		System.out.print(w + " ");
		return w;
	}
	/**�ɳڶ�*/
	private double slack(int v, int index) {
		double w = v * (sumofArray(slotava) - slotava[index] + loadcompute(index));
//		System.out.print(w + " ");
		return w;
	}
	/**������*/
	private double usagerate(int v, int index) {
		int tmp = slotava[index];
		if (tmp == 0) {
			tmp = 1;
		}
		double w = v * (loadcompute(index) / tmp);
//		System.out.print(w + " ");
		return w;
	}
	/**����ֵ*/
	private double load(int v, int index) {
		double w = v * loadcompute(index);
//		System.out.print(w + " ");
		return w;
	}
	/**��Ȩֵ*/
	private double neweight(int v, int index) {
		int tmp = slotava[index];
		if (tmp == 0) {
			tmp = 1;
		}
		double w = v * (sumofArray(slotava) * loadcompute(index)
				/ (sumofArray(slotava) - slotava[index] + loadcompute(index)));

//		System.out.print(w + " ");
		return w;
	}
}
