package org.danieldk.sc.dtree.io;

import java.io.InputStream;

import org.danieldk.sc.dtree.DependencyTree;

public interface DependencyTreeReader {
	public DependencyTree readTree(InputStream is) throws Exception;
}
