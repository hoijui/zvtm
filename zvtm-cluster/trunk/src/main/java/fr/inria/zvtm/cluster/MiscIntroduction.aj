package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;

import java.io.Serializable;

/**
 * Miscellaneous introductions
 */
public aspect MiscIntroduction {
	declare parents: LongPoint implements java.io.Serializable;
	declare parents: Location implements java.io.Serializable;
}

