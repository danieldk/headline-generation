package org.danieldk.sc.learn;

import org.danieldk.sc.dtree.DependencyTree;
import org.danieldk.sc.util.BinaryFunctor;

public interface ScoreFun
	extends BinaryFunctor<Double, DependencyTree, DependencyTree> {}
