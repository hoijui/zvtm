// $ANTLR : "DOT.g" -> "DOTTreeParser.java"$

package net.claribole.zgrviewer.dot;

import java.io.StringReader;
import antlr.CommonAST;
import antlr.debug.misc.ASTFrame;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


/**
 * Evaluate an GraphViz AST generated with the correct parser in order to create
 * the corresponding Java data structure
 * @author Eric Mounhem
 */
public class DOTTreeParser extends antlr.TreeParser       implements DOTTreeParserTokenTypes
 {

	public String cleanAttribute(String value) {
		if(value.startsWith("\"") && value.endsWith("\""))
			return value.substring(1, value.length() - 1).trim();
		else return value;
	}
public DOTTreeParser() {
	tokenNames = _tokenNames;
}

	public final Graph  graph(AST _t) throws RecognitionException, Exception {
		Graph graph;
		
		AST graph_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST d = null;
		AST g = null;
		
			graph = new Graph();
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case DIGRAPH:
			{
				graph.directed = true;
				AST __t1278 = _t;
				AST tmp1_AST_in = (AST)_t;
				match(_t,DIGRAPH);
				_t = _t.getFirstChild();
				d = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				stmt_list(_t,graph);
				_t = _retTree;
				_t = __t1278;
				_t = _t.getNextSibling();
				graph.id=d.getText();
						graph.removeNode(graph.genericNode);
					graph.removeNode(graph.genericRecord);
					graph.removeEdge(graph.genericEdge);
					
				break;
			}
			case GRAPH:
			{
				graph.directed = false;
				AST __t1279 = _t;
				AST tmp2_AST_in = (AST)_t;
				match(_t,GRAPH);
				_t = _t.getFirstChild();
				g = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				stmt_list(_t,graph);
				_t = _retTree;
				_t = __t1279;
				_t = _t.getNextSibling();
				graph.id=g.getText();
						graph.removeNode(graph.genericNode);
					graph.removeNode(graph.genericRecord);
					graph.removeEdge(graph.genericEdge);
				
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return graph;
	}
	
	public final void stmt_list(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST stmt_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t1282 = _t;
			AST tmp3_AST_in = (AST)_t;
			match(_t,LCUR);
			_t = _t.getFirstChild();
			{
			_loop1284:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					stmt(_t,g);
					_t = _retTree;
				}
				else {
					break _loop1284;
				}
				
			} while (true);
			}
			_t = __t1282;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final String  id(AST _t) throws RecognitionException {
		String r;
		
		AST id_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST i = null;
		r = "";
		
		try {      // for error handling
			i = (AST)_t;
			match(_t,ID);
			_t = _t.getNextSibling();
			r=i.getText();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final void stmt(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST stmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SUBGRAPH:
			case ID:
			case LCUR:
			case LBR:
			case RBR:
			case D_EDGE_OP:
			case ND_EDGE_OP:
			case COLON:
			{
				node_edge_subgraph_stmt(_t,g);
				_t = _retTree;
				break;
			}
			case GRAPH:
			case NODE:
			case EDGE:
			{
				attr_stmt(_t,g);
				_t = _retTree;
				break;
			}
			case LABEL:
			case EQUAL:
			{
				String a="", v="";
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EQUAL:
				{
					AST __t1287 = _t;
					AST tmp4_AST_in = (AST)_t;
					match(_t,EQUAL);
					_t = _t.getFirstChild();
					a=id(_t);
					_t = _retTree;
					v=id(_t);
					_t = _retTree;
					_t = __t1287;
					_t = _t.getNextSibling();
					break;
				}
				case LABEL:
				{
					a="label";
					AST __t1288 = _t;
					AST tmp5_AST_in = (AST)_t;
					match(_t,LABEL);
					_t = _t.getFirstChild();
					v=id(_t);
					_t = _retTree;
					_t = __t1288;
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				if(g instanceof Graph)
					  		((Graph) g).changeOption(a, v);
					  	else
					  		((SubGraph) g).changeOption(a, v);
					  	
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void node_edge_subgraph_stmt(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST node_edge_subgraph_stmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Node node=null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case D_EDGE_OP:
			case ND_EDGE_OP:
			{
				node=edges(_t,g);
				_t = _retTree;
				break;
			}
			case SUBGRAPH:
			case LCUR:
			{
				node=subgraph(_t,g);
				_t = _retTree;
				break;
			}
			case ID:
			case LBR:
			case RBR:
			case COLON:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ID:
				case COLON:
				{
					node=node_id(_t,g);
					_t = _retTree;
					break;
				}
				case RBR:
				{
					{
					AST __t1294 = _t;
					AST tmp6_AST_in = (AST)_t;
					match(_t,RBR);
					_t = _t.getFirstChild();
					node=record_id(_t,g);
					_t = _retTree;
					a_list_record(_t,g, node);
					_t = _retTree;
					_t = __t1294;
					_t = _t.getNextSibling();
					
								DotUtils.expandRects((Record) node);
						
					}
					break;
				}
				default:
					if (_t==null) _t=ASTNULL;
					if (((_t.getType()==LBR))&&( (g instanceof Graph)?((Graph) g).record:((SubGraph) g).getRootGraph().record )) {
						AST __t1291 = _t;
						AST tmp7_AST_in = (AST)_t;
						match(_t,LBR);
						_t = _t.getFirstChild();
						node=node_id(_t,g);
						_t = _retTree;
						a_list_record(_t,g, node);
						_t = _retTree;
						_t = __t1291;
						_t = _t.getNextSibling();
						
									DotUtils.expandRects((Record) node);
							
					}
					else if ((_t.getType()==LBR)) {
						AST __t1292 = _t;
						AST tmp8_AST_in = (AST)_t;
						match(_t,LBR);
						_t = _t.getFirstChild();
						node=node_id(_t,g);
						_t = _retTree;
						a_list_node(_t,node);
						_t = _retTree;
						_t = __t1292;
						_t = _t.getNextSibling();
					}
				else {
					throw new NoViableAltException(_t);
				}
				}
				}
				
					    	if(g instanceof Graph)
					    		((Graph) g).addNode(node);
					    	else
					    		((SubGraph) g).addNode(node);
					
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void attr_stmt(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST attr_stmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case GRAPH:
			{
				AST __t1320 = _t;
				AST tmp9_AST_in = (AST)_t;
				match(_t,GRAPH);
				_t = _t.getFirstChild();
				attr_list_graph(_t,g);
				_t = _retTree;
				_t = __t1320;
				_t = _t.getNextSibling();
				break;
			}
			case NODE:
			{
				AST __t1321 = _t;
				AST tmp10_AST_in = (AST)_t;
				match(_t,NODE);
				_t = _t.getFirstChild();
				attr_list_generic_node(_t,g);
				_t = _retTree;
				_t = __t1321;
				_t = _t.getNextSibling();
				break;
			}
			case EDGE:
			{
				AST __t1322 = _t;
				AST tmp11_AST_in = (AST)_t;
				match(_t,EDGE);
				_t = _t.getFirstChild();
				attr_list_generic_edge(_t,g);
				_t = _retTree;
				_t = __t1322;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final Node  edges(AST _t,
		Object g
	) throws RecognitionException, Exception {
		Node n;
		
		AST edges_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		n=null;
		
		try {      // for error handling
			Node start=null, end=null; Edge edge=null;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case D_EDGE_OP:
			{
				AST __t1297 = _t;
				AST tmp12_AST_in = (AST)_t;
				match(_t,D_EDGE_OP);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case D_EDGE_OP:
				case ND_EDGE_OP:
				{
					start=edges(_t,g);
					_t = _retTree;
					break;
				}
				case ID:
				case COLON:
				{
					start=node_id(_t,g);
					_t = _retTree;
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				end=node_id(_t,g);
				_t = _retTree;
				
							n=end;
							edge = new Edge(g, start, end);
							if(g instanceof Graph)
								((Graph) g).addEdge(edge);
							else
								((SubGraph) g).addEdge(edge);
					
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LBR:
				{
					attr_list_edge(_t,edge);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t1297;
				_t = _t.getNextSibling();
				break;
			}
			case ND_EDGE_OP:
			{
				AST __t1300 = _t;
				AST tmp13_AST_in = (AST)_t;
				match(_t,ND_EDGE_OP);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case D_EDGE_OP:
				case ND_EDGE_OP:
				{
					start=edges(_t,g);
					_t = _retTree;
					break;
				}
				case ID:
				case COLON:
				{
					start=node_id(_t,g);
					_t = _retTree;
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				end=node_id(_t,g);
				_t = _retTree;
				
							n=end;
							if(g instanceof Graph) {
								edge = new Edge((Graph) g, start, end);
								((Graph) g).addEdge(edge);
							}
							else {
								edge = new Edge((SubGraph) g, start, end);
								((SubGraph) g).addEdge(edge);
							}
					
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LBR:
				{
					attr_list_edge(_t,edge);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t1300;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return n;
	}
	
	public final Node  subgraph(AST _t,
		Object g
	) throws RecognitionException, Exception {
		Node n;
		
		AST subgraph_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		n=null; /*n=new SubGraph(g);*/ String i="";
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SUBGRAPH:
			{
				AST __t1316 = _t;
				AST tmp14_AST_in = (AST)_t;
				match(_t,SUBGRAPH);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ID:
				{
					i=id(_t);
					_t = _retTree;
					
												if(i.startsWith("cluster"))
													n = new Cluster(g);
												else
													n = new SubGraph(g);
											
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LCUR:
					{
						stmt_list(_t,n);
						_t = _retTree;
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					
											//n.id = i;
											/*if(i.startsWith("cluster_")) {
												n.id = i.substring(8);
												Cluster c = (Cluster) n;
												n = c;*/
											if(n instanceof Cluster)
												n.id = i.substring(7);
											else
												n.id = i;
										
					break;
				}
				case LCUR:
				{
					n = new SubGraph(g);
					stmt_list(_t,n);
					_t = _retTree;
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t1316;
				_t = _t.getNextSibling();
				break;
			}
			case LCUR:
			{
				n = new SubGraph(g);
				stmt_list(_t,n);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return n;
	}
	
	public final Node  node_id(AST _t,
		Object g
	) throws RecognitionException, Exception {
		Node n;
		
		AST node_id_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST r = null;
		n=null; String i="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				i=id(_t);
				_t = _retTree;
				break;
			}
			case COLON:
			{
				AST __t1310 = _t;
				AST tmp15_AST_in = (AST)_t;
				match(_t,COLON);
				_t = _t.getFirstChild();
				i=id(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ID:
				{
					r = (AST)_t;
					match(_t,ID);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				case COLON:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case COLON:
				{
					compass(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t1310;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					if(g instanceof Graph) {
						if(((Graph) g).record)
							n = new Record((Graph) g, i);
						else
							n = new BasicNode((Graph) g, i);
					} else {
						if(((SubGraph) g).getRootGraph().record)
							n = new Record((SubGraph) g, i);
						else
							n = new BasicNode((SubGraph) g, i);
						}
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return n;
	}
	
	public final void a_list_record(AST _t,
		Object g, Node n
	) throws RecognitionException, Exception {
		
		AST a_list_record_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String v="";
		
		try {      // for error handling
			{
			int _cnt1358=0;
			_loop1358:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ID:
				case EQUAL:
				{
					String a = "";
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQUAL:
					{
						AST __t1356 = _t;
						AST tmp16_AST_in = (AST)_t;
						match(_t,EQUAL);
						_t = _t.getFirstChild();
						a=id(_t);
						_t = _retTree;
						v=id(_t);
						_t = _retTree;
						_t = __t1356;
						_t = _t.getNextSibling();
						break;
					}
					case ID:
					{
						v="true";
						a=id(_t);
						_t = _retTree;
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					
							((Record)n).changeOption(a, v);
						
					break;
				}
				case LABEL:
				{
					AST __t1357 = _t;
					AST tmp17_AST_in = (AST)_t;
					match(_t,LABEL);
					_t = _t.getFirstChild();
					v=id(_t);
					_t = _retTree;
					_t = __t1357;
					_t = _t.getNextSibling();
					
					//		System.err.println("record: " + v);
							
							DOTRecordLexer lexer = new DOTRecordLexer(new StringReader(v));
							DOTRecordParser parser = new DOTRecordParser(lexer);
							parser.record();
							CommonAST ast = (CommonAST) parser.getAST();
							
					/*        System.out.println(ast.toStringTree());
							ASTFrame frame = new ASTFrame(v, ast);
							frame.setVisible(true);*/
							
					DOTRecordWalker walker = new DOTRecordWalker();
					walker.record(ast, g, (Record) n);
						
					break;
				}
				default:
				{
					if ( _cnt1358>=1 ) { break _loop1358; } else {throw new NoViableAltException(_t);}
				}
				}
				_cnt1358++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void a_list_node(AST _t,
		Node n
	) throws RecognitionException, Exception {
		
		AST a_list_node_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="", v="";
		
		try {      // for error handling
			{
			int _cnt1352=0;
			_loop1352:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LABEL||_t.getType()==ID||_t.getType()==EQUAL)) {
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case EQUAL:
					{
						AST __t1350 = _t;
						AST tmp18_AST_in = (AST)_t;
						match(_t,EQUAL);
						_t = _t.getFirstChild();
						a=id(_t);
						_t = _retTree;
						v=id(_t);
						_t = _retTree;
						_t = __t1350;
						_t = _t.getNextSibling();
						break;
					}
					case ID:
					{
						v="true";
						a=id(_t);
						_t = _retTree;
						break;
					}
					case LABEL:
					{
						a="label";
						AST __t1351 = _t;
						AST tmp19_AST_in = (AST)_t;
						match(_t,LABEL);
						_t = _t.getFirstChild();
						v=id(_t);
						_t = _retTree;
						_t = __t1351;
						_t = _t.getNextSibling();
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					
							n.changeOption(a, v);
						
				}
				else {
					if ( _cnt1352>=1 ) { break _loop1352; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt1352++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final Record  record_id(AST _t,
		Object g
	) throws RecognitionException, Exception {
		Record n;
		
		AST record_id_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST r = null;
		n=null; String i="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				i=id(_t);
				_t = _retTree;
				break;
			}
			case COLON:
			{
				AST __t1305 = _t;
				AST tmp20_AST_in = (AST)_t;
				match(_t,COLON);
				_t = _t.getFirstChild();
				i=id(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ID:
				{
					r = (AST)_t;
					match(_t,ID);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				case COLON:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case COLON:
				{
					compass(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t1305;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					if(g instanceof Graph)
						n=new Record((Graph) g, i);
					else
						n = new Record((SubGraph) g, i);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return n;
	}
	
	public final void attr_list_edge(AST _t,
		Edge e
	) throws RecognitionException, Exception {
		
		AST attr_list_edge_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t1324 = _t;
			AST tmp21_AST_in = (AST)_t;
			match(_t,LBR);
			_t = _t.getFirstChild();
			{
			_loop1326:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LABEL||_t.getType()==ID||_t.getType()==EQUAL)) {
					a_list_edge(_t,e);
					_t = _retTree;
				}
				else {
					break _loop1326;
				}
				
			} while (true);
			}
			_t = __t1324;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void compass(AST _t) throws RecognitionException {
		
		AST compass_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST c = null;
		
		try {      // for error handling
			AST __t1314 = _t;
			AST tmp22_AST_in = (AST)_t;
			match(_t,COLON);
			_t = _t.getFirstChild();
			c = (AST)_t;
			match(_t,ID);
			_t = _t.getNextSibling();
			_t = __t1314;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void attr_list_graph(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST attr_list_graph_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t1336 = _t;
			AST tmp23_AST_in = (AST)_t;
			match(_t,LBR);
			_t = _t.getFirstChild();
			{
			_loop1338:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LABEL||_t.getType()==ID||_t.getType()==EQUAL)) {
					a_list_graph(_t,g);
					_t = _retTree;
				}
				else {
					break _loop1338;
				}
				
			} while (true);
			}
			_t = __t1336;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void attr_list_generic_node(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST attr_list_generic_node_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t1328 = _t;
			AST tmp24_AST_in = (AST)_t;
			match(_t,LBR);
			_t = _t.getFirstChild();
			{
			_loop1330:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LABEL||_t.getType()==ID||_t.getType()==EQUAL)) {
					a_list_generic_node(_t,g);
					_t = _retTree;
				}
				else {
					break _loop1330;
				}
				
			} while (true);
			}
			_t = __t1328;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void attr_list_generic_edge(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST attr_list_generic_edge_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t1332 = _t;
			AST tmp25_AST_in = (AST)_t;
			match(_t,LBR);
			_t = _t.getFirstChild();
			{
			_loop1334:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LABEL||_t.getType()==ID||_t.getType()==EQUAL)) {
					a_list_generic_edge(_t,g);
					_t = _retTree;
				}
				else {
					break _loop1334;
				}
				
			} while (true);
			}
			_t = __t1332;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void a_list_edge(AST _t,
		Edge e
	) throws RecognitionException, Exception {
		
		AST a_list_edge_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="", v="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t1341 = _t;
				AST tmp26_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				a=id(_t);
				_t = _retTree;
				v=id(_t);
				_t = _retTree;
				_t = __t1341;
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				v="true";
				a=id(_t);
				_t = _retTree;
				break;
			}
			case LABEL:
			{
				a="label";
				AST __t1342 = _t;
				AST tmp27_AST_in = (AST)_t;
				match(_t,LABEL);
				_t = _t.getFirstChild();
				v=id(_t);
				_t = _retTree;
				_t = __t1342;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					e.changeOption(a, v);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void a_list_generic_node(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST a_list_generic_node_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="", v="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t1361 = _t;
				AST tmp28_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				a=id(_t);
				_t = _retTree;
				v=id(_t);
				_t = _retTree;
				_t = __t1361;
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				v="true";
				a=id(_t);
				_t = _retTree;
				break;
			}
			case LABEL:
			{
				a="label";
				AST __t1362 = _t;
				AST tmp29_AST_in = (AST)_t;
				match(_t,LABEL);
				_t = _t.getFirstChild();
				v=id(_t);
				_t = _retTree;
				_t = __t1362;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					String value = cleanAttribute(v);
					if(g instanceof Graph) {
						if(a.equals("shape"))
							((Graph) g).record = value.endsWith("record");
						if(((Graph) g).record)
							((Graph) g).genericRecord.changeOption(a, v);
						else
							((Graph) g).genericNode.changeOption(a, v);
					}
					else {
						if(a.equals("shape"))
							((SubGraph) g).genericGraph.record = value.endsWith("record");
						if(((SubGraph) g).genericGraph.record)
							((SubGraph) g).genericRecord.changeOption(a, v);
						else
							((SubGraph) g).genericNode.changeOption(a, v);
					}
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void a_list_generic_edge(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST a_list_generic_edge_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="", v="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t1365 = _t;
				AST tmp30_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				a=id(_t);
				_t = _retTree;
				v=id(_t);
				_t = _retTree;
				_t = __t1365;
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				v="true";
				a=id(_t);
				_t = _retTree;
				break;
			}
			case LABEL:
			{
				a="label";
				AST __t1366 = _t;
				AST tmp31_AST_in = (AST)_t;
				match(_t,LABEL);
				_t = _t.getFirstChild();
				v=id(_t);
				_t = _retTree;
				_t = __t1366;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
					if(g instanceof Graph)
						((Graph) g).genericEdge.changeOption(a, v);
					else
						((SubGraph) g).genericEdge.changeOption(a, v);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void a_list_graph(AST _t,
		Object g
	) throws RecognitionException, Exception {
		
		AST a_list_graph_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="", v="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t1345 = _t;
				AST tmp32_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				a=id(_t);
				_t = _retTree;
				v=id(_t);
				_t = _retTree;
				_t = __t1345;
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				v="true";
				a=id(_t);
				_t = _retTree;
				break;
			}
			case LABEL:
			{
				a="label";
				AST __t1346 = _t;
				AST tmp33_AST_in = (AST)_t;
				match(_t,LABEL);
				_t = _t.getFirstChild();
				v=id(_t);
				_t = _retTree;
				_t = __t1346;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
				  	//System.err.println(a + " = " + v + " (" + (g instanceof Graph) + ")");
					if(g instanceof Graph)
						((Graph) g).changeOption(a, v);
					else
						((SubGraph) g).changeOption(a, v);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"graph\"",
		"\"digraph\"",
		"\"subgraph\"",
		"\"node\"",
		"\"edge\"",
		"\"strict\"",
		"\"label\"",
		"an identifier",
		"LCUR",
		"SEMI",
		"RCUR",
		"EQUAL",
		"HTML",
		"LBR",
		"RBR",
		"COMMA",
		"D_EDGE_OP",
		"ND_EDGE_OP",
		"COLON",
		"WS",
		"CMT",
		"CPP_COMMENT",
		"ESC",
		"LT",
		"GT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 7773648L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	}
	
