package fr.inria.zvtm.clustering;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate method whose local calls should be remotely
 * replayed by network slaves. For now, we plan to support 
 * only methods taking an unique, serializable parameter.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface ClusterAutoReplay {}

