package fr.inria.zvtm.common.protocol;


/**
 * Encoding constants for the Metisse protocol.
 * @author Julien Altieri
 *
 */
public class Proto {
	/*****************************************************************************
	 *
	 * Message types
	 *
	 *****************************************************************************/

	/* server -> client */
	public static final int   rfbFramebufferUpdate          = 0;
	public static final int   rfbSetColourMapEntries        = 1;
	public static final int   rfbBell                       = 2;
	public static final int   rfbServerCutText              = 3;
	public static final int   rfbConfigureWindow            = 4;
	public static final int   rfbUnmapWindow                = 5;
	public static final int   rfbDestroyWindow              = 6;
	public static final int   rfbRestackWindow              = 7;
	public static final int   rfbServKeyEvent				= 8;
	public static final int   rfbDoublePointerEvent         = 9;
	public static final int   rfbConfigureWall              = 10;
	public static final int   ping							= 11;

	/* client -> server */
	public static final int   rfbSetPixelFormat             = 0;
	public static final int   rfbFixColourMapEntries        = 1;	/* not currently supported */
	public static final int   rfbSetEncodings               = 2;
	public static final int   rfbFramebufferUpdateRequest   = 3;
	public static final int   rfbKeyEvent                   = 4;
	public static final int   rfbPointerEvent               = 5;
	public static final int   rfbClientCutText              = 6;
	public static final int   rfbWindowUpdateRequest        = 7;
	public static final int   rfbFacadesDescription         = 8;
	public static final int   pong							= 12;




	/*-----------------------------------------------------------------------------
	 * Protocol Version
	 *
	 * The server always sends 12 bytes to start which identifies the latest RFB
	 * protocol version number which it supports.  These bytes are interpreted
	 * as a string of 12 ASCII characters in the format "RFB xxx.yyy\n" where
	 * xxx and yyy are the major and minor version numbers (for version 3.3
	 * this is "RFB 003.003\n").
	 *
	 * The client then replies with a similar 12-byte message giving the version
	 * number of the protocol which should actually be used (which may be different
	 * to that quoted by the server).
	 *
	 * It is intended that both clients and servers may provide some level of
	 * backwards compatibility by this mechanism.  Servers in particular should
	 * attempt to provide backwards compatibility, and even forwards compatibility
	 * to some extent.  For example if a client demands version 3.1 of the
	 * protocol, a 3.0 server can probably assume that by ignoring requests for
	 * encoding types it doesn't understand, everything will still work OK.  This
	 * will probably not be the case for changes in the major version number.
	 *
	 * The format string below can be used in sprintf or sscanf to generate or
	 * decode the version string respectively.
	 */
	public static final String rfbProtocolVersionFormat = "METISSE %03d.%03d\n"; // NOT USED, for information only
	public static final int   rfbProtocolMajorVersion   = 1;
	public static final int   rfbProtocolMinorVersion   = 0;

	/*-----------------------------------------------------------------------------
	 * Authentication
	 *
	 * Once the protocol version has been decided, the server then sends a 32-bit
	 * word indicating whether any authentication is needed on the connection.
	 * The value of this word determines the authentication scheme in use.  For
	 * version 3.0 of the protocol this may have one of the following values:
	 */
	public static final int   rfbConnFailed             = 0; /* For some reason the connection failed (e.g. the server
	 * cannot support the desired protocol version).  This is
	 * followed by a string describing the reason (where a
	 * string is specified as a 32-bit length followed by that
	 * many ASCII characters). */
	public static final int   rfbNoAuth                 = 1; /* No authentication is needed. */
	public static final int   rfbMetisseAuth            = 2; /* The Metisse authentication scheme is to be used.  A 16-byte
	 * challenge follows, which the client encrypts as
	 * appropriate using the password and sends the resulting
	 * 16-byte response.  If the response is correct, the
	 * server sends the 32-bit word rfbMetisseAuthOK.  If a simple
	 * failure happens, the server sends rfbMetisseAuthFailed and
	 * closes the connection. If the server decides that too
	 * many failures have occurred, it sends rfbMetisseAuthTooMany
	 * and closes the connection.  In the latter case, the
	 * server should not allow an immediate reconnection by
	 * the client. */

	public static final int   rfbMetisseAuthOK              = 0;
	public static final int   rfbMetisseAuthFailed          = 1;
	public static final int   rfbMetisseAuthTooMany         = 2;

	/*****************************************************************************
	 *
	 * Encoding types
	 *
	 *****************************************************************************/

	public static final int   rfbEncodingRaw            = 0;
	public static final int   rfbEncodingCopyRect       = 1;
	public static final int   rfbEncodingRRE            = 2;
	public static final int   rfbEncodingCoRRE          = 4;
	public static final int   rfbEncodingHextile        = 5;
	public static final int   rfbEncodingZlib           = 6;
	public static final int   rfbEncodingTight          = 7;
	public static final int   rfbEncodingZlibHex        = 8;
	public static final int   rfbEncodingRawShm         = 9;

	/*
	 * Special encoding numbers:
	 *   0xFFFFFF00 .. 0xFFFFFF0F -- encoding-specific compression levels;
	 *   0xFFFFFF10 .. 0xFFFFFF1F -- mouse cursor shape data;
	 *   0xFFFFFF20 .. 0xFFFFFF2F -- various protocol extensions;
	 *   0xFFFFFF30 .. 0xFFFFFFDF -- not allocated yet;
	 *   0xFFFFFFE0 .. 0xFFFFFFEF -- quality level for JPEG compressor;
	 *   0xFFFFFFF0 .. 0xFFFFFFFF -- cross-encoding compression levels.
	 */

	public static final int   rfbEncodingCompressLevel0  = 0xFFFFFF00;
	public static final int   rfbEncodingCompressLevel1  = 0xFFFFFF01;
	public static final int   rfbEncodingCompressLevel2  = 0xFFFFFF02;
	public static final int   rfbEncodingCompressLevel3  = 0xFFFFFF03;
	public static final int   rfbEncodingCompressLevel4  = 0xFFFFFF04;
	public static final int   rfbEncodingCompressLevel5  = 0xFFFFFF05;
	public static final int   rfbEncodingCompressLevel6  = 0xFFFFFF06;
	public static final int   rfbEncodingCompressLevel7  = 0xFFFFFF07;
	public static final int   rfbEncodingCompressLevel8  = 0xFFFFFF08;
	public static final int   rfbEncodingCompressLevel9  = 0xFFFFFF09;

	public static final int   rfbEncodingXCursor         = 0xFFFFFF10;
	public static final int   rfbEncodingARGBCursor      = 0xFFFFFF12;
	public static final int   rfbEncodingPointerPos      = 0xFFFFFF18;

	public static final int   rfbEncodingLastRect        = 0xFFFFFF20;
	public static final int   rfbEncodingWindowShape     = 0xFFFFFF21;
	public static final int   rfbEncodingFacades         = 0xFFFFFF22;

	public static final int   rfbEncodingQualityLevel0   = 0xFFFFFFE0;
	public static final int   rfbEncodingQualityLevel1   = 0xFFFFFFE1;
	public static final int   rfbEncodingQualityLevel2   = 0xFFFFFFE2;
	public static final int   rfbEncodingQualityLevel3   = 0xFFFFFFE3;
	public static final int   rfbEncodingQualityLevel4   = 0xFFFFFFE4;
	public static final int   rfbEncodingQualityLevel5   = 0xFFFFFFE5;
	public static final int   rfbEncodingQualityLevel6   = 0xFFFFFFE6;
	public static final int   rfbEncodingQualityLevel7   = 0xFFFFFFE7;
	public static final int   rfbEncodingQualityLevel8   = 0xFFFFFFE8;
	public static final int   rfbEncodingQualityLevel9   = 0xFFFFFFE9;

	/*-----------------------------------------------------------------------------
	 * RestackWindow - 
	 */
	public static final int   rfbWindowFlagsOverrideRedirect = (1 << 0);
	public static final int   rfbWindowFlagsInputOnly        = (1 << 1);
	public static final int   rfbWindowFlagsUnmanaged        = (1 << 2);
	public static final int   rfbWindowFlagsNetChecking      = (1 << 3);
	public static final int   rfbWindowFlagsEwmhDesktop      = (1 << 4);
	public static final int   rfbWindowFlagsTransient        = (1 << 5);
}
