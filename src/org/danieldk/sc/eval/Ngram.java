package org.danieldk.sc.eval;

import java.util.Arrays;

public class Ngram {
	private String[] d_ngram;

	public Ngram(String[] ngram) {
		d_ngram = ngram.clone();
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (this.getClass() != otherObject.getClass())
			return false;
		
		Ngram otherNgram = (Ngram) otherObject;
		return Arrays.equals(d_ngram, otherNgram.d_ngram);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(d_ngram);
	}
	
	public String[] toArray() {
		return d_ngram.clone();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < d_ngram.length; ++i) {
			sb.append(d_ngram[i]);
			if (i != d_ngram.length - 1)
				sb.append(" ");
		}
		
		return sb.toString();
	}
}
