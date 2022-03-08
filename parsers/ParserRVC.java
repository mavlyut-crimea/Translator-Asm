package parsers;

public class ParserRVC {
    private ParserRVC() {}

    protected static String quadrant0(String instruction) {
        return switch (instruction.substring(0, 3)) {
            case "001" -> "fld";
            case "010" -> "lw";
            case "011" -> "flw";
            case "101" -> "fsd";
            case "110" -> "sw";
            case "111" -> "fsw";
            default -> throw error(instruction);
        };
    }

    protected static String quadrant1(String instruction, String str3) {
        return switch (Integer.parseInt(str3, 2)) {
            case 0 -> "sub";
            case 1 -> "xor";
            case 2 -> "or";
            case 3 -> "and";
            case 4 -> "subw";
            case 5 -> "addw";
            default -> throw error(instruction);
        };
    }

    protected static String quadrant2(String instruction) {
        return switch (Integer.parseInt(instruction.substring(0, 3), 2)) {
            case 0 -> "slli";
            case 1 -> "fldsp";
            case 2 -> "lwsp";
            case 3 -> "flwsp";
            case 5 -> "fsdsp";
            case 6 -> "swsp";
            case 7 -> "fswsp";
            default -> throw error(instruction);
        };
    }

    protected static ParserException error(String instruction) {
        return new ParserException("RVC", instruction);
    }
}
