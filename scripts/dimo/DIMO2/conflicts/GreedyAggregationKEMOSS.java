
package conflicts;

import java.util.Vector;

import conflicts.sets.Aggregation;

public class GreedyAggregationKEMOSS {

	private FileProblem problem;
	private Population pop;
	private Controller con;
	
	/* type = 1: normal delta error is optimized 
	 * type = 2: delta error averaged over all solution pairs is optimized*/
	public void start(String filename, int k, int a, String outputfilename) {
		Vector<String> toPrint = new Vector<String>(); // to store output
		
		java.util.Calendar calendar = new java.util.GregorianCalendar();
		long milliseconds = calendar.getTimeInMillis();
		
		init(filename);
		this.con = new Controller(this.problem, this.pop);
		
		Aggregation output = con.greedyAggregationAlgorithmForGivenK(k, a);

		double[][] weights = output.getAggregation();
				
		/* Compute delta error in the end correctly again to avoid mistakes within algorithm.
		 * This can happen since the other delta error (max./avg.) is probably carried along
		 * incorrectly if the other error (avg./max.) is optimized.
		 */
		double[][] objValuesWithIDs = this.problem.getPoints();
		double[][] objValues = new double[objValuesWithIDs.length][objValuesWithIDs[0].length-1];
		for (int i=0; i<objValues.length; i++) {
			for (int j=0; j<objValues[0].length; j++) {
				objValues[i][j] = objValuesWithIDs[i][j+1];
			}
		}
		double[][] aggObjValues = new double[objValues.length][weights.length];
		for (int i=0; i<aggObjValues.length; i++) {
			for (int j=0; j<aggObjValues[0].length; j++) {
				aggObjValues[i][j] = 0;
				for (int l=0; l<objValues[0].length; l++) {
					aggObjValues[i][j] += weights[j][l] * objValues[i][l];
				}
			}
		}

		double[] deltaError = this.con.greedyAlgo.computeDeltaError(aggObjValues, objValues);
			
		/* prepare printing results */
		toPrint.add("Greedy algorithm for finding the best aggregation with " + k + " objectives");
		if (a==1) {
			toPrint.add(" where the overall maximum delta error is optimized.");
		} else if (a==2) {
			toPrint.add(" where the delta error averaged over all solution pairs is optimized.");
		}
		toPrint.add("");
		if (deltaError[0] != output.getMaxError()) {
			toPrint.add("Gives an overall error of delta = " + deltaError[0] + " *");
		} else {
			toPrint.add("Gives an overall error of delta = " + deltaError[0]);
		}
		if (deltaError[1] != output.getAverageError()) {
			toPrint.add("  and an average error of delta = " + deltaError[1] + " *");
		} else {
			toPrint.add("  and an average error of delta = " + deltaError[1]);
		}
		if (deltaError[0] != output.getMaxError() || deltaError[1] != output.getAverageError()) {
			toPrint.add("* error has been incorrectly carried along during algorithm and has been recomputed in the end");
		}
		toPrint.add("");
		toPrint.add("Weights for the aggregated objectives:");
		
		for (int i=0; i<weights.length; i++) {
			String newline = "";
			for (int j=0; j<weights[0].length; j++) {
				newline = newline + weights[i][j] + " ";
			}
			toPrint.add(newline);
			toPrint.add(" ...aggregated objective " + i);
		}
		java.util.Calendar calendar2 = new java.util.GregorianCalendar();
		long millis = calendar2.getTimeInMillis();
		toPrint.add("Elapsed time during computation: " + (millis-milliseconds) + " milliseconds");
		
		/* finally output everything */
		Output.print(toPrint, outputfilename);
	}

	
	private void init(String filename){
		this.problem = new FileProblem(filename);
		this.pop = new FilePopulation(problem);
	}

	/**
	 * Performs the greedy algorithm for kEMOSS where aggregation is allowed. The maximum
	 * and average error and the computed weights for the aggregated objectives are written
	 * to stdout or to a specified file 'outputfilename'.
	 *
	 * @param args
	 * 			args[0]: name of file, with information about the individuals
	 * 						data format: "id objectivevalue1 objectivevalue2 ..."
	 * 			args[1]: given k 
	 *          args[2]: optional; if the letter 'a' is used, the averaged delta
	 *                      error is used instead of the maximum delta error
	 *          an additional last argument '-o outputfilename' will indicate
	 *             that all output is written to the file 'outputfilename'
	 *             
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 2 || args.length > 5) {
			System.out.println("Wrong usage.");
			System.out.println();
			System.out.println("Usage:");
			System.out.println("   GreedyAggregationKEMOSS filename k [-o outputfilename]");
			System.out.println("   or");
			System.out.println("   GreedyAggregationKEMOSS filename k a [-o outputfilename]");
			System.out.println();
			System.out.println("   (where a is the single character 'a'");
			System.out.println("   indicating that the average delta error has to be optimized)");
			System.out.println();
			System.out.println("   Adding '-o outputfilename' as last argument will result");
			System.out.println("      in writing all output to the file 'outputfilename'");
			System.out.println("      instead of writing to standard output.");
			
		} else {
			String outputfilename = ""; // standard: output written to stdout
			if (args[args.length - 2].compareTo("-o") == 0) {
				outputfilename = args[args.length - 1];
			}
			
			GreedyAggregationKEMOSS gkemoss = new GreedyAggregationKEMOSS();
			String filename = args[0];
			if (args.length == 2 || args.length == 4) {
				gkemoss.start(filename, new Double(args[1]).intValue(),1, outputfilename);
			} else {
				gkemoss.start(filename, new Double(args[1]).intValue(),2, outputfilename);
			}
		}
	}

}
