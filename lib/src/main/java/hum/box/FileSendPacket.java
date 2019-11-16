package hum.box;

import hum.core.SendPacket;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author hum
 */
public class FileSendPacket extends SendPacket<FileInputStream> {

    public FileSendPacket(File file) {
        length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}