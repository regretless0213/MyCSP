package MyCSP.experiment;

import java.util.ArrayList;
import java.util.List;

import MyCSP.model.CombineTool;

public class CombineToolTest {

	public int[][] geneAllowedTuple(int n, int m) {
		// n为容量约束中的p，m为容量约束中的q
		ArrayList<int[]> list = new ArrayList<int[]>();
		list.add(new int[m]);

		CombineTool ct = new CombineTool();
		int[][] com = ct.genResult(n, m);
		int[][] tuples = new int[com.length + 1][m];
		for (int i = 0; i < com.length; i++) {
			for (int j = 0; j < com[i].length; j++) {
				tuples[i + 1][com[i][j]] = 1;
			}
		}

		return tuples;
	}

	private int getSum(List<Integer> array) {
		int sum = 0;
		for (int t : array) {
			sum += t;
		}
		return sum;
	}

	// 构建基础数据
	public int[][] getDataBase(int n, int m) {
		List<List<Integer>> classData = new ArrayList<List<Integer>>();
		List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < 2; i++) {
			tmp.add(i);
		}
		for (int i = 0; i < m; i++) {
			classData.add(tmp);
		}
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		descartes(classData, result, 0, new ArrayList<Integer>());
		// System.out.println(sum(classData.get(0)));
		// classData.remove(0);
		if (result.size() > 0) {
			
			for (int index = 0; index < result.size(); index++) {
				if (getSum(result.get(index)) > n) {
					result.remove(index);
				}
			}
			
			int[][] resultMatrix = new int[result.size()][m];
			for (int i = 0; i < result.size(); i++) {
				for (int j = 0; j < m; j++) {
					resultMatrix[i][j] = result.get(i).get(j);
				}
			}
			return resultMatrix;
		} else {
			System.out.println("笛卡尔积函数未能成功生成结果数据！");
			return null;
		}

	}

	// 笛卡尔积函数
	private static void descartes(List<List<Integer>> dimvalue, List<List<Integer>> result, int layer,
			List<Integer> curList) {
		if (layer < dimvalue.size() - 1) {
			if (dimvalue.get(layer).size() == 0) {
				descartes(dimvalue, result, layer + 1, curList);
			} else {
				for (int i = 0; i < dimvalue.get(layer).size(); i++) {
					List<Integer> list = new ArrayList<Integer>(curList);
					list.add(dimvalue.get(layer).get(i));
					descartes(dimvalue, result, layer + 1, list);
				}
			}
		} else if (layer == dimvalue.size() - 1) {
			if (dimvalue.get(layer).size() == 0) {
				result.add(curList);
			} else {
				for (int i = 0; i < dimvalue.get(layer).size(); i++) {
					List<Integer> list = new ArrayList<Integer>(curList);
					list.add(dimvalue.get(layer).get(i));
					result.add(list);
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CombineToolTest ctt = new CombineToolTest();

		int[][] result = ctt.geneAllowedTuple(2, 3);
		System.out.println("Old Result:");
		for (int[] tmp : result) {
			for (int t : tmp) {
				System.out.print(t + " ");
			}
			System.out.println();
		}
		
		int[][] newResult = ctt.getDataBase(2, 3);
		System.out.println("New Result:");
		for (int[] tmp : newResult) {
			for (int t : tmp) {
				System.out.print(t + " ");
			}
			System.out.println();
		}

	}

}
