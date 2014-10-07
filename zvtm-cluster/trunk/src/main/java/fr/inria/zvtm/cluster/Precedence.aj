/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

// aspect MiscIntroduction has no dependencies
// aspect ObjIdIntroduction has no dependencies
// aspect AutoReplay requires ObjIdIntroduction, and requires
// that networking be available

/**
 * Makes precedence explicit.
 */
public aspect Precedence {
    declare precedence:
        MiscIntroduction,
        ObjIdIntroduction,
        AutoReplay,
        *;
}

