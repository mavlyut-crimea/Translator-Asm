package myBase;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MyReader {
    private final DataInputStream out;
    private final String name;

    public MyReader(String name) throws IOException {
        out = new DataInputStream(new FileInputStream(name));
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public byte[] readAllBytes() throws IOException {
        return out.readAllBytes();
    }

    public void close() throws IOException {
        out.close();
    }
}
