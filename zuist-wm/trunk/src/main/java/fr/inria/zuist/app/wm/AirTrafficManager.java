/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

class AirTrafficManager {
    
    WorldExplorer application;
    
    AirTrafficManager(WorldExplorer app, boolean show){
        this.application = app;
        if (show){
            System.out.print("Loading air traffic information...");
            loadAirports();
            loadTraffic();
            System.out.println(" OK");
        }
    }
    
    void loadAirports(){
        
    }
    
    void loadTraffic(){
        
    }

    
}
