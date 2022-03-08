package parsers;

public class SymTabNode {
    private final int symbol, value, size;
    private final String type, bind, vis, index, name;

    protected SymTabNode(String name) {
        this.symbol = 0;
        this.value = 0;
        this.size = 0;
        this.type = "FUNC";
        this.bind = null;
        this.vis = null;
        this.index = null;
        this.name = name;
    }

    protected SymTabNode(
            final int i, final String name, final int value, final int size, final int info, final int other
    ) {
        this.symbol = i;
        this.value = value;
        this.size = size;
        this.type = ParserSymtab.getType(info & 0xf);
        this.bind = ParserSymtab.getBind(info >> 4);
        this.vis = ParserSymtab.getVis(other & 0x3);
        this.index = ParserSymtab.getIndex(other >> 8);
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (name.startsWith("LOC")) {
            return "";
        }
        return String.format(
                "[%4d] 0x%-15X %5d %-8s %-8s %-8s %6s %s\n",
                symbol, value, size, type, bind, vis, index, name
        );
    }
}
