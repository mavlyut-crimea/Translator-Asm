package parsers;

public class ParserRiscV {
    private ParserRiscV() {}

    protected static String parseIL(final String func3) {
        return switch (func3) {
            case "000" -> "lb";
            case "001" -> "lh";
            case "010" -> "lw";
            case "100" -> "lbu";
            case "101" -> "lhu";
            default -> throw new ParserException("I", func3);
        };
    }

    protected static String parseICsr(final String func3) {
        return switch (func3) {
            case "001" -> "csrrw";
            case "010" -> "csrrs";
            case "011" -> "csrrc";
            case "101" -> "csrrwi";
            case "110" -> "csrrsi";
            case "111" -> "csrrci";
            default -> throw new ParserException("I", func3);
        };
    }

    protected static String parseISr(final String func3, final String func7) {
        return switch (func3) {
            case "000" -> "addi";
            case "001" -> {
                if (ParserCommands.isZeroes(func7)) {
                    yield "slli";
                } else {
                    throw new ParserException("I", func7);
                }
            }
            case "010" -> "slti";
            case "011" -> "sltiu";
            case "100" -> "xori";
            case "101" -> switch (func7) {
                case "0000000" -> "srli";
                case "0100000" -> "srai";
                default -> throw new ParserException("I", func7);
            };
            case "110" -> "ori";
            case "111" -> "andi";
            default -> throw new ParserException("I", func3);
        };
    }

    protected static String parseS(final String func3) {
        return switch (func3) {
            case "000" -> "sb";
            case "001" -> "sh";
            case "010" -> "sw";
            default -> throw new ParserException("S", func3);
        };
    }

    protected static String parseU(final String opcode) {
        return switch (opcode) {
            case "0110111" -> "lui";
            case "0010111" -> "auipc";
            default -> throw new ParserException("U", opcode);
        };
    }

    protected static String parseB(final String func3) {
        return switch (func3) {
            case "000" -> "beq";
            case "001" -> "bne";
            case "100" -> "blt";
            case "101" -> "bge";
            case "110" -> "bltu";
            case "111" -> "bgeu";
            default -> throw new ParserException("B", func3);
        };
    }

    protected static String parseR(final String func7, final String func3) {
        return switch (func7) {
            case "0000000" -> switch (func3) {
                case "000" -> "add";
                case "001" -> "sll";
                case "010" -> "slt";
                case "011" -> "sltu";
                case "100" -> "xor";
                case "101" -> "srl";
                case "110" -> "or";
                case "111" -> "and";
                default -> throw new ParserException("R", func3);
            };
            case "0100000" -> switch (func3) {
                case "000" -> "sub";
                case "101" -> "sra";
                default -> throw new ParserException("R", func3);
            };
            case "0000001" -> switch (func3) {
                case "000" -> "mul";
                case "001" -> "mulh";
                case "010" -> "mulhsu";
                case "011" -> "mulhu";
                case "100" -> "div";
                case "101" -> "divu";
                case "110" -> "rem";
                case "111" -> "remu";
                default -> throw new ParserException("R", func3);
            };
            default -> throw new ParserException("R", func7);
        };
    }
}
