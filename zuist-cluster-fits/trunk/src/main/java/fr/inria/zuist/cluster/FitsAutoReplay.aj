/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.FitsImageDescription;

aspect FitsAutoReplay extends AbstractAutoReplay {
     public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         execution(public void FitsImageDescription.setScaleMethod(FitsImage.ScaleMethod)) ||
         execution(public void FitsImageDescription.setColorFilter(FitsImage.ColorFilter))
        );
}

