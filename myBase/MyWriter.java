package myBase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyWriter {
    private final BufferedWriter in;
    private final String name;

    public MyWriter(String name) throws IOException {
        in = new BufferedWriter(new FileWriter(name, StandardCharsets.UTF_8));
        this.name = name;
    }

    public void write(String str) throws IOException {
        in.write(str);
    }

    public void newLine() throws IOException {
        in.newLine();
    }

    @Override
    public String toString() {
        return name;
    }

    public void close() throws IOException {
        in.close();
    }
}
