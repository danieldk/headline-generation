package eu.danieldk.treetbl.eval;

public class NgramEval {
	public static double nGramPrecision(String[] refSentence,
			String[] candidateSentence, int n) {
		return Rouge.rougeN(candidateSentence, refSentence, n);
	}
}
