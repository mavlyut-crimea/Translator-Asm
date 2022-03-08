package parsers;

public class ParserSymtab {
    private ParserSymtab() {}

    protected static String getIndex(final int other) {
        return switch (other) {
            case 0 -> "UNDEF";
            case 0xff00 -> "LOPROC";
            case 0xff01 -> "AFTER";
            case 0xff02 -> "AMD64_LCOMMON";
            case 0xff1f -> "HIPROC";
            case 0xff20 -> "LOOS";
            case 0xff3f -> "HIOS";
            case 0xfff1 -> "ABS";
            case 0xfff2 -> "COMMON";
            case 0xffff -> "XINDEX";
            default -> other + "";
        };
    }

    protected static String getVis(final int other) {
        return switch (other) {
            case 0 -> "DEFAULT";
            case 1 -> "INTERNAL";
            case 2 -> "HIDDEN";
            case 3 -> "PROTECTED";
            case 4 -> "EXPORTED";
            case 5 -> "SINGLETON";
            case 6 -> "ELIMINATE";
            default -> "UNKNOWN";
        };
    }

    protected static String getBind(final int info) {
        return switch (info) {
            case 0 -> "LOCAL";
            case 1 -> "GLOBAL";
            case 2 -> "WEAK";
            case 10 -> "LOOS";
            case 12 -> "HIOS";
            case 13 -> "LOPROC";
            case 15 -> "HIPROC";
            default -> "UNKNOWN";
        };
    }

    protected static String getType(final int info) {
        return switch (info) {
            case 0 -> "NOTYPE";
            case 1 -> "OBJECT";
            case 2 -> "FUNC";
            case 3 -> "SECTION";
            case 4 -> "FILE";
            case 5 -> "COMMON";
            case 6 -> "TLS";
            case 10 -> "LOOS";
            case 12 -> "HIOS";
            case 13 -> "LOPROC";
            case 14 -> "SPARC_REGISTER";
            case 15 -> "HIPROC";
            default -> "UNKNOWN";
        };
    }
}
