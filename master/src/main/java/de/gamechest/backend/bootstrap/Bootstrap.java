package de.gamechest.backend.bootstrap;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        if ( Float.parseFloat( System.getProperty( "java.class.version" ) ) < 52.0 )
        {
            System.err.println( "*** ERROR *** ByteCloud requires Java 8 or above to function! Please download and install it!" );
            System.out.println( "You can check your Java version with the command: java -version" );
            return;
        }

        Launcher.main( args );
    }
}
