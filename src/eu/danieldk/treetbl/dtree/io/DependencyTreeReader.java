package eu.danieldk.treetbl.dtree.io;

import java.io.InputStream;

import eu.danieldk.treetbl.dtree.DependencyTree;

public interface DependencyTreeReader {
	public DependencyTree readTree(InputStream is) throws Exception;
}
