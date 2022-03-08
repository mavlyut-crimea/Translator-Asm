package parsers;

public class Section {
    protected final int name, type, flags, addr, offset, size, link, info, addralign, entsize;

    protected Section(int[] param) {
        name = param[0];
        type = param[1];
        flags = param[2];
        addr = param[3];
        offset = param[4];
        size = param[5];
        link = param[6];
        info = param[7];
        addralign = param[8];
        entsize = param[9];
    }

    @Override
    public String toString() {
//        return name + " " + type + " " + flags + " " + addr + " " +
//                offset + " " + size + " " + link + " " + info + " " +
//                addralign + " " + entsize;
        return String.format("name = %2d, addr = %5d, offset = %4d, size = %4d", name, addr, offset, size);
    }
}
