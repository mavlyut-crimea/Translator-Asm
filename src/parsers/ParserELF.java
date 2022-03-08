package parsers;

import myBase.MyReader;
import myBase.MyWriter;
import myBase.MyPair;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static parsers.ParserCommands.decToBin;

public class ParserELF {
    private int[] bytes;
    private final MyReader in;
    private final MyWriter out;
    private final Map<String, Section> sections = new LinkedHashMap<>();
    private final Map<Integer, SymTabNode> symTabNodes = new LinkedHashMap<>();

    public ParserELF(String fileIn, String fileOut) {
        try {
            in = new MyReader(fileIn);
            enterData();
        } catch (IOException e) {
            throw new ParserException(String.format("Can't open file \"%s\"", fileIn));
        }
        try {
            out = new MyWriter(fileOut);
        } catch (IOException e) {
            throw new ParserException(String.format("Can't open file \"%s\"", fileOut));
        }
    }

    public void parse() {
        parseHeader();
        parseSymtab();
        try {
            parseAndDumpText();
        } catch (IOException e) {
            throw new ParserException(".text", "Can't write to file", e.getMessage());
        }
        try {
            dumpSymtab();
        } catch (IOException e) {
            throw new ParserException(".symtab", "Can't write to file", e.getMessage());
        }
        try {
            out.close();
        } catch (IOException e) {
            throw new ParserException(String.format("Error while closing file \"%s\"", out));
        }
    }

    private void enterData() {
        final byte[] bytes1;
        try {
            bytes1 = in.readAllBytes();
            in.close();
        } catch (IOException e) {
            throw new ParserException(
                    String.format("file \"%s\" format not supported: %s", in, e.getMessage())
            );
        }
        bytes = new int[bytes1.length];
        for (int i = 0; i < bytes1.length; i++) {
            bytes[i] = ((bytes1[i] < 0) ? 256 : 0) + bytes1[i];
        }
    }

    private void parseHeader() {
        final int e_shoff = cnt(32, 4);
        final int e_shentsiz = cnt(46, 2);
        final int e_shnum = cnt(48, 2);
        final int e_shstrndx = cnt(50, 2);
        final int go = e_shoff + e_shentsiz * e_shstrndx;
        final int sh_offset12 = cnt(go + 16, 4);
        for (int j = 0, tmp = e_shoff; j < e_shnum; j++, tmp += 40) {
            String name = getName(sh_offset12 + cnt(tmp, 4));
            int[] param = new int[10];
            for (int r = 0; r < 10; r++) {
                param[r] = cnt(tmp + 4 * r, 4);
            }
            sections.put(name, new Section(param));
        }
    }

    private void parseAndDumpText() throws IOException {
        Section Text = sections.get(".text");
        out.write(".text\n");
        int ind = Text.offset;
        final int size = Text.size;
        final int addr = Text.addr;
        for (int i = 0; i < size; i += 2) {
            MyPair<String[], Boolean> ans = ParserCommands.parseCommand(bytes, ind);
            out.write(String.format("0x%08x", addr + i));
            String label = (symTabNodes.containsKey(addr + i) && symTabNodes.get(addr + i).getType().equals("FUNC")) ?
                    symTabNodes.get(addr + i).getName() : "";
            out.write(String.format(" %10s%s", label, label.isEmpty() ? " " : ":"));
            int len = ans.getFirst().length;
            for (int j = 0; j < len; j++) {
                out.write(String.format(" %s", ans.getFirst()[j]));
                if (j != 0 && j != len - 1) {
                    out.write(",");
                }
            }
            out.newLine();
            ind += 2;
            if (ans.getSecond()) {
                i += 2;
                ind += 2;
            }
        }
        out.newLine();
    }

    private void parseSymtab() {
        final Section Symtab = sections.get(".symtab");
        final int ind = Symtab.offset;
        final int num = Symtab.size / 16;
        for (int i = 0; i < num; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 4; j++) {
                sb.append(new StringBuilder(decToBin(bytes[ind + i * 16 + 12 + j])).reverse());
            }
            final String name = getName(cnt(ind + i * 16, 4) + sections.get(".strtab").offset);
            final int value = cnt(ind + i * 16 + 4, 4);
            final int size = cnt(ind + i * 16 + 8, 4);
            final int info = Integer.parseInt(new StringBuilder(sb.substring(0, 8)).reverse().toString(), 2);
            final int other = Integer.parseInt(new StringBuilder(sb.substring(8)).reverse().toString(), 2);
            symTabNodes.put(value, new SymTabNode(i, name, value, size, info, other));
        }
    }

    private void dumpSymtab() throws IOException {
        out.write(".symtab\n");
        out.write(String.format("%s %-12s %7s %-8s %-8s %-8s %6s %s\n",
                "Symbol", "Value", "Size", "Type", "Bind", "Vis", "Index", "Name"));
        for (SymTabNode node : symTabNodes.values()) {
            out.write(node.toString());
        }
    }

    protected int cnt(final int left, final int num) {
        int ans = 0;
        for (int i = num - 1; i >= 0; i--) {
            ans = ans * 256 + bytes[left + i];
        }
        return ans;
    }

    protected String getName(int left) {
        final StringBuilder sb = new StringBuilder();
        while (bytes[left] != 0) {
            sb.append((char) bytes[left++]);
        }
        return sb.toString();
    }
}
