/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package MyCSP.model;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.util.Scanner;

import static MyCSP.heuristic.Search.MyHeuristicSearch;
//import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

import MyCSP.model.data.DataSet.*;

/**
 * CSPLib prob001:<br/>
 * "A number of cars are to be produced; they are not identical, because
 * different options are available as variants on the basic model. <br/>
 * The assembly line has different stations which install the various options
 * (air-conditioning, sun-roof, etc.). These stations have been designed to
 * handle at most a certain percentage of the cars passing along the assembly
 * line. Furthermore, the cars requiring a certain option must not be bunched
 * together, otherwise the station will not be able to cope. Consequently, the
 * cars must be arranged in a sequence so that the K of each station is never
 * exceeded. <br/>
 * For instance, if a particular station can only cope with at most half of the
 * cars passing along the line, the sequence must be built so that at most 1 car
 * in any 2 requires that option. <br/>
 * The problem has been shown to be NP-complete (Gent 1999)" <br/>
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class CarSequencing extends AbstractProblem {

	@Option(name = "-d", aliases = "--data", usage = "Car sequencing data.", required = false)
//	Data data = Data.P4_72;
	CSPLib data = CSPLib.random09;
//	MyData data = MyData.md05;

	IntVar[] cars;

	int nCars, nClasses, nOptions;

	int[] demands;
	int[][] optfreq, matrix, options, idleConfs;

	@Override
	public void buildModel() {
		model = new Model("CarSequencing");
		parse(data.source());
		// System.out.println(data.source());
		prepare();
		int max = nClasses - 1;
		cars = model.intVarArray("cars", nCars, 0, max, false);
		IntVar[] expArray = new IntVar[nClasses];

		for (int optNum = 0; optNum < options.length; optNum++) {
			int nbConf = options[optNum].length;
			for (int seqStart = 0; seqStart < (cars.length - optfreq[optNum][1]); seqStart++) {
				IntVar[] carSequence = extractor(cars, seqStart, optfreq[optNum][1]);

				// configurations that include given option may be chosen
				IntVar[] atMost = new IntVar[nbConf];
				for (int i = 0; i < nbConf; i++) {
					// optfreq[optNum][0] times AT MOST
					atMost[i] = model.intVar("atmost_" + optNum + "_" + seqStart + "_" + nbConf, 0, optfreq[optNum][0],
							true);
				}
				// System.out.println(atMost[0].getName()+":
				// "+atMost[0].getLB()+"--"+atMost[0].getUB());
				model.globalCardinality(carSequence, options[optNum], atMost, false).post();

				IntVar[] atLeast = model.intVarArray("atleast_" + optNum + "_" + seqStart, idleConfs[optNum].length, 0,
						max, true);
				model.globalCardinality(carSequence, idleConfs[optNum], atLeast, false).post();

				// all others configurations may be chosen
				IntVar sum = model.intVar("sum", optfreq[optNum][1] - optfreq[optNum][0], 99999999, true);
				model.sum(atLeast, "=", sum).post();
				// System.out.println(sum);
			}
		}

		int[] values = new int[expArray.length];
		for (int i = 0; i < expArray.length; i++) {
			expArray[i] = model.intVar("var_" + i, 0, demands[i], false);
			values[i] = i;
			// System.out.println(expArray[i]+" "+values[i]);
		}

		model.globalCardinality(cars, values, expArray, false).post();
	}

	private static IntVar[] extractor(IntVar[] cars, int initialNumber, int amount) {
		if ((initialNumber + amount) > cars.length) {
			amount = cars.length - initialNumber;
		}
		IntVar[] tmp = new IntVar[amount];
		System.arraycopy(cars, initialNumber, tmp, 0, initialNumber + amount - initialNumber);
		return tmp;
	}

	@Override
	public void configureSearch() {

		model.getSolver().setSearch(MyHeuristicSearch(cars, matrix, optfreq, demands));
		model.getSolver().limitTime("20m");
	}

	@Override
	public void solve() {
		model.getSolver().solve();
        model.getSolver().printStatistics();
//        for (int i = 0; i < nCars;i++) {
//            System.out.println(model.getVars()[i]);
//        }
		/*
		 * for (int i = 0; i < matrix.length; i++) { for (int j = 0; j <
		 * matrix[i].length; j++) { System.out.print(matrix[i][j]+ "  "); }
		 * System.out.println(); }
		 */
	}

	public static void main(String[] args) {
		new CarSequencing().execute(args);
	}

	private int[][] parse(String source) {
		int[][] data = null;
		Scanner sc = new Scanner(source);
		nCars = sc.nextInt();
		nOptions = sc.nextInt();
		nClasses = sc.nextInt();

		optfreq = new int[nOptions][2];
		// get frequencies
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][0] = sc.nextInt();
		}
		for (int i = 0; i < nOptions; i++) {
			optfreq[i][1] = sc.nextInt();
		}

		// get the demand and options
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

	private void prepare() {
		options = new int[nOptions][];
		idleConfs = new int[nOptions][];
		for (int i = 0; i < matrix[0].length; i++) {
			int nbNulls = 0;
			int nbOnes = 0;
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[j][i] == 1)
					nbOnes++;
				else
					nbNulls++;
			}
			options[i] = new int[nbOnes];
			idleConfs[i] = new int[nbNulls];
			int countOnes = 0;
			int countNulls = 0;
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[j][i] == 1) {
					options[i][countOnes] = j;
					countOnes++;
				} else {
					idleConfs[i][countNulls] = j;
					countNulls++;
				}
			}
		}
		/*
		 * for (int j = 0; j < options.length; j++) { for (int i = 0;i <
		 * options[j].length; i++) { System.out.print(options[j][i] +"  "); }
		 * System.out.println(); } System.out.println("================="); for (int j =
		 * 0; j < idleConfs.length; j++) { for (int i = 0;i < idleConfs[j].length; i++)
		 * { System.out.print(idleConfs[j][i] +"  "); } System.out.println(); }
		 */
//		for (int j = 0; j < optfreq.length; j++) {
//			for (int i = 0; i < optfreq[j].length; i++) {
//				System.out.print(optfreq[j][i] + "  ");
//			}
//			System.out.println();
//		}
	}

	/////////////////////////////////// DATA
	/////////////////////////////////// //////////////////////////////////////////////////


}

