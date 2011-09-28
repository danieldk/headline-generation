package eu.danieldk.treetbl.eval;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

public class Rouge {
	private static int[][] buildLCSMatrix(String[] seq1, String[] seq2) {
		int[][] matrix = new int[seq1.length + 1][seq2.length + 1];
		
		for (int i = 1; i <= seq1.length; ++i)
			for (int j = 1; j <= seq2.length; ++j) {
				if (seq1[i - 1].equals(seq2[j - 1]))
					matrix[i][j] = matrix[i - 1][j - 1] + 1;
				else
					matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
			}

		return matrix;
	}
	
	private static double[][] buildWLCSMatrix(String[] seq1, String[] seq2,
			UnaryFunctor<Double, Double> fun) {
		double[][] matrix = new double[seq1.length + 1][seq2.length + 1];
		double[][] weightMatrix = new double[seq1.length + 1][seq2.length + 1];

		for (int i = 1; i <= seq1.length; ++i)
			for (int j = 1; j <= seq2.length; ++j) {
				if (seq1[i - 1].equals(seq2[j - 1])) {
					double k = weightMatrix[i - 1][j - 1];
					matrix[i][j] = matrix[i - 1][j - 1] + fun.call(k + 1.0) -
						fun.call(k);
					weightMatrix[i][j] = k + 1.0;
				}
				else {
					matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
					weightMatrix[i][j] = 0.0;
				}	
			}
		
		return matrix;
	}
	
	private static int combination(int n, int k) {
		BigInteger bn = BigInteger.valueOf(n);
		BigInteger bk = BigInteger.valueOf(k);
		
		// C(n,k) = n! / (k!(n - k)!)
		BigInteger result = factorial(bn).divide(
				factorial(bk).multiply(factorial(bn.subtract(bk))));
		
		return result.intValue();
	}
	
	private static BigInteger factorial(BigInteger n) {
		// Catch invalid arguments to factorial.
		if (n.compareTo(BigInteger.ZERO) < 0)
			throw new RuntimeErrorException(
					new Error("Can not calculate the factorial of a number below 0!"));
		
		// If the argument is 0 or 1, we are done.
		if (n.equals(BigInteger.ZERO) || n.equals(BigInteger.ONE))
			return BigInteger.ONE;
		
		return n.multiply(factorial(n.subtract(BigInteger.ONE)));
	}
	
	private static double fMeasure(double precision, double recall,
			double beta) {
		return ((1 + Math.pow(beta, 2.0)) * recall * precision) /
			(recall + Math.pow(beta, 2.0) * precision);
	}
	
	private static Map<Ngram, Integer> mapIntersection(Map<Ngram, Integer> x,
			Map<Ngram, Integer> y)
	{
		Map<Ngram, Integer> intersect = new HashMap<Ngram, Integer>();
		
		for (Entry<Ngram, Integer> entry: x.entrySet()) {
			if (!y.containsKey(entry.getKey()))
				continue;
			
			Ngram key = entry.getKey();
			int elemIntersect = Math.min(entry.getValue(), y.get(key));
			intersect.put(key, elemIntersect);
		}
		
		return intersect;
	}

	private static Map<Ngram, Integer> ngrams(List<String> sentence, int n) {
		Map<Ngram, Integer> results = new HashMap<Ngram, Integer>();
		
		for (int i = 0; i < sentence.size() - (n - 1); ++i) {
			String[] ngram = new String[n];
			for (int j = 0; j < n; ++j)
				ngram[j] = sentence.get(i + j);

			Ngram ngramObj = new Ngram(ngram);
			if (!results.containsKey(ngramObj))
				results.put(ngramObj, 0);
			results.put(ngramObj, results.get(ngramObj) + 1);
		}

		return results;
	}
	
	private static Map<Ngram, Integer> skipBigrams(List<String> sentence,
			int maxSkipDistance) {
		Map<Ngram, Integer> results = new HashMap<Ngram, Integer>();
		
		for (int i = 0; i < sentence.size() - 1; ++i)
			for (int j = 0; j <= maxSkipDistance && i + j + 1 < sentence.size(); ++j) {
				String[] ngramStr = {sentence.get(i), sentence.get(i + j + 1)};
				Ngram ngram = new Ngram(ngramStr);
				if (!results.containsKey(ngram))
					results.put(ngram, 0);
				results.put(ngram, results.get(ngram) + 1);
			}
		
		return results;
	}
	
	private static double wlcsLength(String[] seq1, String[] seq2,
			UnaryFunctor<Double, Double> fun) {
		double[][] matrix = buildWLCSMatrix(seq1, seq2, fun);
		return matrix[seq1.length][seq2.length];
	}

	public static int lcsLength(String[] seq1, String[] seq2) {
		int[][] matrix = buildLCSMatrix(seq1, seq2);
		return matrix[seq1.length][seq2.length];
	}
	
	public static double rougeL(String[] refSentence,
			String[] candidateSentence, double beta) {
		int lcs = lcsLength(refSentence, candidateSentence);
		
		// If the longest common substring has length 0, we give
		// a score of 0.0 (to avoid NaNs).
		if (lcs == 0)
			return 0.0;

		double precision = (double) lcs / candidateSentence.length;
		double recall = (double) lcs / refSentence.length;

		return fMeasure(precision, recall, beta);
	}
	
	public static double rougeN(List<String> refSentence, 
			List<String> candidateSentence, int n) {
		Map<Ngram, Integer> refNgrams = ngrams(refSentence, n);
		Map<Ngram, Integer> candidateNgrams = ngrams(candidateSentence, n);
		
		// Create the intersection
		Map<Ngram, Integer> mapIntersect = mapIntersection(refNgrams, candidateNgrams);

		int intersectSize = 0;
		for (Entry<Ngram, Integer> entry: mapIntersect.entrySet())
			intersectSize += entry.getValue();
		
		int nRefNgrams = 0;
		for (Entry<Ngram, Integer> entry: refNgrams.entrySet())
			nRefNgrams += entry.getValue();
		
		if (intersectSize == 0)
			return 0.0;

		return ((double) intersectSize) / nRefNgrams;
	}
	
	public static double rougeS(List<String> refSentence,
			List<String> candidateSentence, int maxSkipDistance,
			double beta) {
		if (refSentence.size() < 2 || candidateSentence.size() < 2)
			return 0.0;
		
		Map<Ngram, Integer> refNgrams = skipBigrams(refSentence, maxSkipDistance);
		Map<Ngram, Integer> candidateNgrams =
				skipBigrams(candidateSentence, maxSkipDistance);
		
		// Create the intersection
		Map<Ngram, Integer> mapIntersect = mapIntersection(refNgrams, candidateNgrams);
		int intersectSize = 0;
		for (Entry<Ngram, Integer> entry: mapIntersect.entrySet())
			intersectSize += entry.getValue();

		double precision = (double) intersectSize /
			combination(refSentence.size(), 2);
		double recall = (double) intersectSize /
		combination(candidateSentence.size(), 2);
		
		return fMeasure(precision, recall, beta);
	}
	
	public static double rougeSU(List<String> refSentence,
			List<String> candidateSentence, int maxSkipDistance,
			double beta) {
		List<String> newRef = new ArrayList<String>(refSentence);
		List<String> newCan = new ArrayList<String>(candidateSentence);
		
		newRef.add(0, "@");
		newCan.add(0, "@");

		return rougeS(newRef, newCan, maxSkipDistance, beta);
	}
	
	public static double rougeW(String[] refSentence,
			String[] candidateSentence, double beta,
			UnaryFunctor<Double, Double> fun,
			UnaryFunctor<Double, Double> invFun) {
		double wlcs = wlcsLength(refSentence, candidateSentence, fun);
		
		// If the longest common substring has length 0, we give
		// a score of 0.0 (to avoid NaNs).
		if (wlcs == 0.0)
			return 0.0;
		
		double precision = invFun.call(wlcs /
				fun.call((double) refSentence.length));
		double recall = invFun.call(wlcs /
				fun.call((double) candidateSentence.length));
		
		return fMeasure(precision, recall, beta);
	}
	
	public static void main(String[] args) {
		String[] origA = {"police", "killed", "the", "gunman"};
		List<String> orig = Arrays.asList(origA);
		String[] test1A = {"police", "kill", "the", "gunman"};
		List<String> test1 = Arrays.asList(test1A);
		String[] test2A = {"the", "gunman", "kill", "police"};
		List<String> test2 = Arrays.asList(test2A);

		/*
		String[] test3A = {"the", "gunman", "police", "killed"};

		String[] xA = {"A", "B", "C", "D", "E", "F", "G"};
		String[] yA = {"A", "B", "C", "D", "H", "I", "K"};
		String[] zA = {"A", "H", "B", "K", "C", "I", "D"};
		
		String[] aA = {"A", "B", "A", "A"};
		String[] bA = {"A", "C", "A", "A"};
		*/
		
		String[] rA = {"Spain", "wins", "the", "European", "soccer", "championship"};
		List<String> r = Arrays.asList(rA);
		String[] dA = {"Yesterday", ",", "Spain", "won", "the", "European", "soccer", "championship", "."};
		List<String> d = Arrays.asList(dA);
		String[] c1A = {"Yesterday", ",", "Spain", "won", "the", "European", "soccer", "championship"};
		List<String> c1 = Arrays.asList(c1A);
		String[] c2A = {"Yesterday", ",", "Spain", "won", "the", "European", "soccer", "."};
		List<String> c2 = Arrays.asList(c2A);

		System.out.println(rougeN(orig, test1, 1));
		System.out.println(rougeN(orig, test2, 1));
		System.out.println(rougeN(test1, orig, 1));
		System.out.println(rougeN(test2, orig, 1));
		
		/*
		System.out.println(lcsLength(orig, test1));
		System.out.println(lcsLength(orig, test2));

		System.out.println(rougeL(orig, test1, 1.0));
		System.out.println(rougeL(orig, test2, 1.0));
		
		double[][] matrix = buildWLCSMatrix(orig, test3,
				new UnaryFunctor<Double, Double>() {
			public Double call(Double arg) {
				return Math.pow(arg, 2.0);
			}
		});

		for (double[] row: matrix) {
			for (double col: row)
				System.out.print(col + " ");
			System.out.println();
		}
		*/
		
		/*
		System.out.println(rougeW(x, y, 1.0, new UnaryFunctor<Double, Double>() {
			public Double call(Double arg) {
				return Math.pow(arg, 2.0);
			}
		}, new UnaryFunctor<Double, Double>() {
			public Double call(Double arg) {
				return Math.pow(arg, 0.5);
			}
		}));
		
		System.out.println(rougeW(x, z, 1.0, new UnaryFunctor<Double, Double>() {
			public Double call(Double arg) {
				return Math.pow(arg, 2.0);
			}
		}, new UnaryFunctor<Double, Double>() {
			public Double call(Double arg) {
				return Math.pow(arg, 0.5);
			}
		}));
		*/
		
/*		System.out.println(rougeS(orig, test1, Integer.MAX_VALUE, 1.0));
		System.out.println(rougeS(orig, test2, Integer.MAX_VALUE, 1.0));
		System.out.println(rougeS(orig, test3, Integer.MAX_VALUE, 1.0));*/

//		System.out.println(rougeS(a, b, Integer.MAX_VALUE, 1.0));
		System.out.println(rougeSU(r, d, Integer.MAX_VALUE, 1.0));
		System.out.println(rougeSU(r, c1, Integer.MAX_VALUE, 1.0));
		System.out.println(rougeS(r, c2, Integer.MAX_VALUE, 1.0));

	}
}
