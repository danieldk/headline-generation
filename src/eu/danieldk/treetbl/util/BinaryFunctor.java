package eu.danieldk.treetbl.util;

public interface BinaryFunctor<R, A1, A2> {
	public R call(A1 arg1, A2 arg2);
}
