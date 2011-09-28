package eu.danieldk.treetbl.eval;

import java.util.List;

public class NgramEval {
	public static double nGramPrecision(List<String> refSentence,
			List<String> candidateSentence, int n) {
		return Rouge.rougeN(candidateSentence, refSentence, n);
	}
}
