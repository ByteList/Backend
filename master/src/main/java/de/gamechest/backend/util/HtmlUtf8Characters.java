package de.gamechest.backend.util;

/**
 * Created by ByteList on 15.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class HtmlUtf8Characters {

    private static final String[] converts = {
            "äa:&#uml;", "ÄA:&#uml;", "öo:&#uml;", "ÖO:&#uml;", "üu:&#uml;", "ÜU:&#uml;",
            "ß:&szlig;", "€:&euro;", "©:&copy;", "•:&bull;", "™:&trade;", "§:&sect;"
    };

    public static String convertToHtmlCharacters(String str) {
        for (String string : converts) {
            String[] conv = string.split(":");

            if(conv[1].equals("&#uml;")) {
                str = str.replace(conv[0].split("")[0], conv[1].replace("#", conv[0].split("")[1]));
            } else {
                str = str.replace(conv[0], conv[1]);
            }
        }
        return str;
    }
}
