package parsers;

public class ParserRVC {
    private ParserRVC() {}

    protected static String quadrant0(String instruction) {
        return switch (instruction.substring(0, 3)) {
            case "001" -> "c.fld";
            case "010" -> "c.lw";
            case "011" -> "c.flw";
            case "101" -> "c.fsd";
            case "110" -> "c.sw";
            case "111" -> "c.fsw";
            default -> throw error(instruction);
        };
    }

    protected static String quadrant1(String instruction, String str3) {
        return switch (Integer.parseInt(str3, 2)) {
            case 0 -> "c.sub";
            case 1 -> "c.xor";
            case 2 -> "c.or";
            case 3 -> "c.and";
            case 4 -> "c.subw";
            case 5 -> "c.addw";
            default -> throw error(instruction);
        };
    }

    protected static String quadrant2(String instruction) {
        return switch (Integer.parseInt(instruction.substring(0, 3), 2)) {
            case 0 -> "c.slli";
            case 1 -> "c.fldsp";
            case 2 -> "c.lwsp";
            case 3 -> "c.flwsp";
            case 5 -> "c.fsdsp";
            case 6 -> "c.swsp";
            case 7 -> "c.fswsp";
            default -> throw error(instruction);
        };
    }

    protected static ParserException error(String instruction) {
        return new ParserException("RVC", instruction);
    }
}
