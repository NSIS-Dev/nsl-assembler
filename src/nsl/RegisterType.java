/*
 * RegisterType.java
 */

package nsl;

/**
 * Defines types of NSIS registers/variables.
 *
 * @author Stuart
 */
public enum RegisterType {
	/** Built in NSIS registers ($0-$9, $R0-$R9). */
	Register,
	/** User defined variable (e.g. $MyVar). */
	Variable,
	/** Other registers/variables (such as $INSTDIR, $OUTDIR). */
	Other
}
