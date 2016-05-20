package fr.inria.zvtm.cluster.tests;

import org.kohsuke.args4j.Option;

public class ClusteredViewBenchmarkOptions {

	@Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
	public int blockWidth = 200;

	@Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
	public int blockHeight = 200;

	@Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
	public int numRows = 2;

	@Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
	public int numCols = 2;
	
	@Option(name = "-mw", aliases = {"--master-width"}, usage = "master view block width")
	public int masterWidth = 400;

	@Option(name = "-mh", aliases = {"--master-height"}, usage = "master view block height")
	public int masterHeight = 400;

	@Option(name = "-gn", aliases = {"--glyph-number"}, usage = "Number of glyph to display")
	public int glyphNumber = 50;
	
	@Option(name = "-d", aliases = {"--double-view"}, usage = "Create two clustered view")
	public boolean doubleView = false;
}