package eu.danieldk.treetbl.learn;

import eu.danieldk.treetbl.dtree.DependencyTree;
import eu.danieldk.treetbl.util.BinaryFunctor;

public interface ScoreFun
	extends BinaryFunctor<Double, DependencyTree, DependencyTree> {}
