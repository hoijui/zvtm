package fr.inria.zvtm.cluster;

// aspect ObjIdIntroduction has no dependencies
// aspect AutoReplay requires ObjIdIntroduction, and requires
// that networking be available

/**
 * Makes precedence explicit.
 */
public aspect Precedence {
	declare precedence: ObjIdIntroduction, AutoReplay, *;
}

