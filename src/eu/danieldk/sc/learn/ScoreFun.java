package eu.danieldk.sc.learn;

import eu.danieldk.sc.dtree.DependencyTree;
import eu.danieldk.sc.util.BinaryFunctor;

public interface ScoreFun
	extends BinaryFunctor<Double, DependencyTree, DependencyTree> {}
