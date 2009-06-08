/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fms;

public class Launcher {
    
    static final String TEST_1 = "1";
    static final String TEST_2 = "2";
    
    public static void main(String[] args){
		String testType = TEST_1;
		for (int i=0;i<args.length;i++){
			if (args[i].equals(TEST_1) || args[i].equals(TEST_2)){
			    testType = args[i];
			}
		}
        if (testType.equals(TEST_2)){
            new Test2();            
        }
        else {
            new Test();
        }
    }
    
}
