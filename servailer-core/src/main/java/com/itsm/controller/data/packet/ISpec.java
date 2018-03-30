package com.itsm.controller.data.packet;

/**
 * Created by anpiakhota on 15.12.16.
 */
@Deprecated
public interface ISpec {

    char DIRECTION_TO = '>';
    char DIRECTION_FROM = '<';

    /**
     * The pattern named group to evaluate with regex matcher.
     * Plus sign within parentheses should be substituted with real data.
     */
    @Deprecated String DATA_GROUP = "(?<data>+)";
    @Deprecated char TYPE_IDENTIFIER = ':';
    @Deprecated char PARAMETER_DELIMITER = ',';
    @Deprecated char ESCAPE_CHARACTER = '\u007D';
    @Deprecated
    String packetPattern = "^(\\x5E)" + // header
            "(\\x3C|\\x3E)" +  // direction
            "(?<command>SR|ST|MC|VR|CA|CV|CT|DV|CH|RP)" +  // command
            "(?<number>\\p{XDigit}{1,4}+) " +    // data number
            "(\\(\\)|\\((?<data>[ILFDCS]:.{1,},?)\\))" +    // data
            "(?<checksum>\\p{ASCII}{2})" +   // checksum
            "(\\x24)$"; // footer;

    /* https://regex101.com/
        ^(\x5E)(\x3C|\x3E)(?<command>SR|ST|MC|VR|CA|CV|CT|DV|CH|RP)(?<number>\d{1,4}+)(?<data>\(\)|\((?<parameter>[ILFDCS]:.{1,},?)\))(?<checksum>.{2})(\x24)$
    */

    interface Command {

        char PING = 'P';
        char START = 'S';
        char STOP = 'X';
        char VERSION = 'V';
        char CALIBRATION = 'C';
        char BUFFER = 'B';
        char SOFT_RELOAD = 'L';
        char REQUEST_REPEAT = 'R';

    }

}



