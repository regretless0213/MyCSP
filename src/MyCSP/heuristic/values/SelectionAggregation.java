
package MyCSP.heuristic.values;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

public final class SelectionAggregation implements IntValueSelector {

	private TIntList bests;

	private java.util.Random random;

	private IntVar[] CarSeq; // 变量数组
	private int[][] matrix, optfreq; // 传递过来的0/1矩阵、容量矩阵
	private int[] demands, result, slotava; // 传递过来的每个类的需求;存储动态下每个零件已经安装了几次；每个零件对应的剩余插槽数
//	private int slots; // 总插槽数

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
		// 选择权值计算策略
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
			// 统计中间值矩阵中各零件已被赋值的次数
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
			// 计算剩余插槽数
			for (int n = 0; n < length; n++) {
				int m = 0;
				int count = 0;
				int index = 0;
				for (; m < retmp.length; m++) {
					int row = retmp[m];
					// 已赋值的部分一定满足约束
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
			Object wtmp = aggregateWeight(matrix[idx]); // 权值
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
//				System.out.println("权值策略设置有误！");
//				break;
//
//			}
			if (wtmp.getClass().isAssignableFrom(Double.class)) {
				
//				System.out.println("常规权值聚合：");
				
				double weight = (double) wtmp;
//				System.out.print(weight+" ");
				if (weight > _d) {
					bests.clear();
					bests.add(idx);
					_d = weight;
				} else if (_d == weight) {
					bests.add(idx);
				}
			} else if(wtmp.getClass().isAssignableFrom(double[].class)){// 聚合方程为Lex是的选值策略
				
//				System.out.println("Lex权值聚合：");
				
				int alleq = 1;// 判断是否数组中每个值都相等
				double[] weight = (double[]) wtmp;
//				//输出权值情况
//				System.out.print("{");
//				for(double printd:weight) {
//					System.out.print(printd+" ");
//				}
//				System.out.println("}");
				for (int i = 0; i < weight.length; i++) {
					if (weight[i] > _da[i]) {// 有一个值大于预存值就保存该值的index并结束循环
						alleq = 0;
						bests.clear();
						bests.add(idx);
						_da = weight;
						break;
					} else if (weight[i] < _da[i]) {// 有一个值小于预存值则该值不作考虑直接结束循环
						alleq = 0;
						break;
					}
				}
				if (alleq == 1) {
					bests.add(idx);
				}
			}else {
				System.out.println(wtmp.getClass()+" Object类别匹配出错！");
			}
//			System.out.println(idx+"变量");
		}
		
//		System.out.println("最优值列表大小"+bests.size());
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
//		System.out.println("选择变量"+currentVar);
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
				System.out.println("聚合方程设置有误！");
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
		double weight = -1; // 权值
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
			System.out.println("权值策略设置有误！");
			break;

		}
		return weight;
	}



	private double demand(int optnum) {
		double d = 0;
		// System.out.println("计算需求");
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


	// 新版
	/*静态*/
	/**常数*/
	private double weight(int v) {
		double w = v;
//		System.out.print(w + " ");
		return w;
	}
	/**容量*/
	private double capacity(int v, int index) {
		double w = v * (double)optfreq[index][1] / optfreq[index][0];
		return w;

	}
	/*动态*/
	/**剩余需求*/
	private double redemand(int v, int index) {
		// 动态剩余需求如何计算
		double w = v * (demand(index) - result[index]);
//		System.out.print(w + " ");
		return w;
	}
	/**松弛度*/
	private double slack(int v, int index) {
		double w = v * (sumofArray(slotava) - slotava[index] + loadcompute(index));
//		System.out.print(w + " ");
		return w;
	}
	/**利用率*/
	private double usagerate(int v, int index) {
		int tmp = slotava[index];
		if (tmp == 0) {
			tmp = 1;
		}
		double w = v * (loadcompute(index) / tmp);
//		System.out.print(w + " ");
		return w;
	}
	/**负载值*/
	private double load(int v, int index) {
		double w = v * loadcompute(index);
//		System.out.print(w + " ");
		return w;
	}
	/**新权值*/
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
