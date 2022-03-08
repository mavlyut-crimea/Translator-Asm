import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class SymTabLine {
    String name;
    int st_value, st_size, st_info, st_shndx, st_other;
    String type, bind, vis, index;

    public void calc() {
        if (st_info == 0) {
            type = "NOTYPE";
        } else if (st_info == 1) {
            type = "OBJECT";
        } else if (st_info == 2) {
            type = "FUNC";
        } else if (st_info == 3) {
            type = "SECTION";
        } else if (st_info == 4) {
            type = "FILE";
        } else if (st_info == 13) {
            type = "LOPROC";
        } else if (st_info == 15) {
            type = "HIPROC";
        }

        if (st_shndx == 0) {
            bind = "LOCAL";
        } else if (st_shndx == 1) {
            bind = "GLOBAL";
        } else if (st_shndx == 2) {
            bind = "WEAK";
        } else if (st_shndx == 13) {
            bind = "LOPROC";
        } else if (st_shndx == 15) {
            bind = "HIPROC";
        }

        vis = "DEFAULT";
        if ((st_other >> 1) % 2 == 1) {
            vis = "HIDDEN";
        }

        int x = (st_other >> 8);
        if (x == 0) {
            index = "UND";
        } else if (x >= 1 && x <= 9) {
            index = Integer.valueOf(x).toString();
        } else {
            index = "ABS";
        }
    }
}

public class MainVK {
    static int[] hex;
    static final int SIZE = 256;
    static final int HEX = 16;
    static final int BYTE = 8;
    static int e_shoff, e_shentsize, e_shnum, e_shstrndx, shstrtab_off,
            text_addr, text_off, text_size,
            symtab_addr, symtab_off, symtab_size,
            strtab_off;
    static SymTabLine[] symtab;
    static Map<Integer, Integer> symtab_dict;

    public static void main (String[] args) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
            try {
                byte[] bytes = in.readAllBytes();
                hex = new int[bytes.length];
                for (int i = 0; i < bytes.length; ++i) {
                    hex[i] = bytes[i];
                    if (hex[i] < 0) {
                        hex[i] += 256;
                    }
                }
                e_shoff = eval(32, 4);
                e_shentsize = eval(46, 2);
                e_shnum = eval(48, 2);
                e_shstrndx = eval(50, 2);

                shstrtab_off = eval(e_shoff + e_shentsize * e_shstrndx + 16, 4);
                for (int i = 0, ind = e_shoff; i < e_shnum; ++i, ind += e_shentsize) {
                    int name = eval(ind, 4);
                    StringBuilder sb = new StringBuilder();
                    int cur = shstrtab_off + name;
                    while (hex[cur] != 0) {
                        sb.append((char)hex[cur]);
                        ++cur;
                    }

                    if (sb.toString().equals(".text")) {
                        text_addr = eval(ind + 12, 4);
                        text_off = eval(ind + 16, 4);
                        text_size = eval(ind + 20, 4);
                    }
                    if (sb.toString().equals(".symtab")) {
                        symtab_addr = eval(ind + 12, 4);
                        symtab_off = eval(ind + 16, 4);
                        symtab_size = eval(ind + 20, 4);
                    }
                    if (sb.toString().equals(".strtab")) {
                        strtab_off = eval(ind + 16, 4);
                    }
                }

                symtab = new SymTabLine[symtab_size/16];
                for (int i = 0, ind = symtab_off; ind - symtab_off < symtab_size; ++i, ind += 16) {
                    SymTabLine res = new SymTabLine();
                    StringBuilder mask = new StringBuilder();
                    for (int j = ind; j < ind+16; ++j) {
                        int temp = hex[j];
                        for (int z = 0; z < BYTE; ++z) {
                            mask.append((char)('0' + temp % 2));
                            temp /= 2;
                        }
                    }

                    int name = Integer.parseInt((new StringBuilder(mask.substring(0, 32))).reverse().toString(), 2);
                    res.st_value = Integer.parseInt((new StringBuilder(mask.substring(32, 64))).reverse().toString(), 2);
                    res.st_size = Integer.parseInt((new StringBuilder(mask.substring(64, 96))).reverse().toString(), 2);
                    res.st_info = Integer.parseInt((new StringBuilder(mask.substring(96, 100))).reverse().toString(), 2);
                    res.st_shndx = Integer.parseInt((new StringBuilder(mask.substring(100, 104))).reverse().toString(), 2);
                    res.st_other = Integer.parseInt((new StringBuilder(mask.substring(104, 128))).reverse().toString(), 2);
                    res.calc();

                    int cur = strtab_off + name;
                    StringBuilder sb = new StringBuilder();
                    while (hex[cur] != 0) {
                        sb.append((char)hex[cur]);
                        ++cur;
                    }
                    res.name = sb.toString();

                    symtab[i] = res;
                }

                symtab_dict = new HashMap<>();
                for (int i = 0; i < symtab.length; ++i) {
                    symtab_dict.put(symtab[i].st_value, i);
                }
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not find: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Input error: " + e.getMessage());
        }

        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(args[1]),
                            StandardCharsets.UTF_8
                    )
            );
            try {
                printText(out);
                out.newLine();
                printSymTab(out);
            } finally {
                out.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open output file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Output error: " + e.getMessage());
        }
    }

    private static void printSymTab (BufferedWriter out) throws IOException {
        out.write(".symtab");
        out.newLine();
        out.write(String.format("%s %-15s %7s %-8s %-8s %-8s %6s %s",
                "Symbol", "Value", "Size", "Type", "Bind", "Vis", "Index", "Name"));
        out.newLine();
        for (int i = 0; i < symtab.length; ++i) {
            out.write(String.format("[%4d] 0x%-15X %5d %-8s %-8s %-8s %6s %s",
                    i, symtab[i].st_value, symtab[i].st_size, symtab[i].type,
                    symtab[i].bind, symtab[i].vis, symtab[i].index, symtab[i].name));
            out.newLine();
        }
    }

    private static void printText (BufferedWriter out) throws IOException {
        out.write(".text");
        out.newLine();
        for (int i = 0; i < text_size; i += 4) {
            out.write(translateLine(text_off + i));
            out.newLine();
        }
    }

    private static int eval (int ind, int size) {
        int res = 0;
        for (int i = 0, mult = 1; i < size; ++i, mult *= SIZE) {
            res += hex[ind + i] * mult;
        }
        return res;
    }

    private static String toParam (String r) {
        StringBuilder res = new StringBuilder();
        int x = Integer.parseInt((new StringBuilder(r)).reverse().toString(), 2);
        if (x == 0) {
            res.append("zero");
        } else if (x == 1) {
            res.append("ra");
        } else if (x == 2) {
            res.append("sp");
        } else if (x == 3) {
            res.append("gp");
        } else if (x == 4) {
            res.append("tp");
        } else if (x >= 5 && x <= 7) {
            res.append("t" + (char)('0' + x - 5));
        } else if (x == 8 || x == 9) {
            res.append("s" + (char)('0' + x - 8));
        } else if (x >= 10 && x <= 17) {
            res.append("a" + (char)('0' + x - 10));
        } else if (x >= 18 && x <= 27) {
            res.append("s" + (char)('0' + x - 16));
        } else if (x >= 28 && x <= 31) {
            res.append("t" + (char)('0' + x - 25));
        }
        return res.toString();
    }

    private static int toImm (String imm) {
        return (1024 + Integer.parseInt((new StringBuilder(imm)).reverse().toString(), 2)) % 2048 - 1024;
    }

    private static String translateLine (int ind) {
        StringBuilder mask = new StringBuilder();
        for (int j = ind; j < ind+4; ++j) {
            int temp = hex[j];
            for (int z = 0; z < BYTE; ++z) {
                mask.append((char)('0' + temp % 2));
                temp /= 2;
            }
        }

        StringBuilder op = new StringBuilder();
        StringBuilder param = new StringBuilder();
        int addr = text_addr + ind - text_off;

        String opcode = mask.substring(0, 7);
        if (opcode.equals("1100110")) {
            //R - 32I, 32M
            String rd = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String rs2 = mask.substring(20, 25);
            String funct7 = mask.substring(25, 32);
            param.append(toParam(rd));
            param.append(", ");
            param.append(toParam(rs1));
            param.append(", ");
            param.append(toParam(rs2));
            if (funct3.equals("000") && funct7.equals("0000000")) {
                op.append("add");
            } else if (funct3.equals("000") && funct7.equals("0000010")) {
                op.append("sub");
            } else if (funct3.equals("100") && funct7.equals("0000000")) {
                op.append("sll");
            } else if (funct3.equals("010") && funct7.equals("0000000")) {
                op.append("slt");
            } else if (funct3.equals("110") && funct7.equals("0000000")) {
                op.append("sltu");
            } else if (funct3.equals("001") && funct7.equals("0000000")) {
                op.append("xor");
            } else if (funct3.equals("101") && funct7.equals("0000000")) {
                op.append("srl");
            } else if (funct3.equals("101") && funct7.equals("0000010")) {
                op.append("sra");
            } else if (funct3.equals("011") && funct7.equals("0000000")) {
                op.append("or");
            } else if (funct3.equals("111") && funct7.equals("0000000")) {
                op.append("and");
            } else if (funct3.equals("000") && funct7.equals("1000000")) {
                op.append("mul");
            } else if (funct3.equals("100") && funct7.equals("1000000")) {
                op.append("mulh");
            } else if (funct3.equals("010") && funct7.equals("1000000")) {
                op.append("mulhsu");
            } else if (funct3.equals("110") && funct7.equals("1000000")) {
                op.append("mulhu");
            } else if (funct3.equals("001") && funct7.equals("1000000")) {
                op.append("div");
            } else if (funct3.equals("101") && funct7.equals("1000000")) {
                op.append("divu");
            } else if (funct3.equals("011") && funct7.equals("1000000")) {
                op.append("rem");
            } else if (funct3.equals("111") && funct7.equals("1000000")) {
                op.append("remu");
            }
            op.append("");
        } else if (opcode.equals("1100000")) {
            //I - 1
            String rd = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String imm = mask.substring(20, 32);

            param.append(toParam(rd));
            param.append(", ");
            param.append(toImm(imm));
            param.append("(");
            param.append(toParam(rs1));
            param.append(")");

            if (funct3.equals("000")) {
                op.append("lb");
            } else if (funct3.equals("100")) {
                op.append("lh");
            } else if (funct3.equals("010")) {
                op.append("lw");
            } else if (funct3.equals("001")) {
                op.append("lbu");
            } else if (funct3.equals("101")) {
                op.append("lhu");
            }
        } else if (opcode.equals("1100100")) {
            //I - 2, shamt
            String rd = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String imm = mask.substring(20, 32);
            int x = Integer.parseInt((new StringBuilder(imm)).reverse().toString(), 2);
            x -= (x & 0b100000000000) << 1;
            if (funct3.equals("000")) {
                op.append("addi");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else if (funct3.equals("010")) {
                op.append("slti");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else if (funct3.equals("110")) {
                op.append("sltiu");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else if (funct3.equals("001")) {
                op.append("xori");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else if (funct3.equals("011")) {
                op.append("ori");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else if (funct3.equals("111")) {
                op.append("andi");
                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(x);
            } else {
                //shamt
                String shamt = mask.substring(20, 25);
                String funct7 = mask.substring(25, 32);

                x = Integer.parseInt((new StringBuilder(shamt)).reverse().toString(), 2);
                x -= (x & 0b100000000000) << 1;

                param.append(toParam(rd));
                param.append(", ");
                param.append(toParam(rs1));
                param.append(", ");
                param.append(String.format("0x%x", x));

                if (funct3.equals("100") && funct7.equals("0000000")) {
                    op.append("slli");
                } else if (funct3.equals("101") && funct7.equals("0000000")) {
                    op.append("srli");
                } else if (funct3.equals("101") && funct7.equals("0000010")) {
                    op.append("srai");
                }
            }
        } else if (opcode.equals("1100111")) {
            //I - csr
            String rd = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1_zimm = mask.substring(15, 20);
            String csr = mask.substring(20, 32);

            if (!mask.toString().equals("11001110000000000000000000000000") &&
                    !mask.toString().equals("11001110000000000000100000000000")) {

                param.append(toParam(rd));
                param.append(", ");
                param.append(toImm(csr));
                param.append("(");
                param.append(toParam(rs1_zimm));
                param.append(")");
            }

            if (funct3.equals("100")) {
                op.append("csrrw");
            } else if (funct3.equals("010")) {
                op.append("csrrs");
            } else if (funct3.equals("110")) {
                op.append("csrrc");
            } else if (funct3.equals("101")) {
                op.append("csrrwi");
            } else if (funct3.equals("011")) {
                op.append("csrrsi");
            } else if (funct3.equals("111")) {
                op.append("csrrci");
            } else if (mask.toString().equals("11001110000000000000000000000000")) {
                op.append("ecall");
            } else if (mask.toString().equals("11001110000000000000100000000000")) {
                op.append("ebreak");
            }
        } else if (opcode.equals("1110011")) {
            //I - JALR
            String rd = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String imm = mask.substring(20, 32);

            param.append(toParam(rd));
            param.append(", ");
            param.append(toImm(imm));
            param.append("(");
            param.append(toParam(rs1));
            param.append(")");


            op.append("jalr ");
        } else if (opcode.equals("1110110")) {
            //U - LUI
            String rd = mask.substring(7, 12);
            String imm = mask.substring(12, 32);
            op.append("lui ");
            int x = Integer.parseInt((new StringBuilder(imm)).reverse().toString(), 2);

            param.append(toParam(rd));
            param.append(", ");
            param.append(String.format("0x%x", x));
        } else if (opcode.equals("1110100")) {
            //U - AUIPC
            String rd = mask.substring(7, 12);
            String imm = mask.substring(12, 32);
            op.append("auipc ");

            int x = Integer.parseInt((new StringBuilder(imm)).reverse().toString(), 2);
            param.append(toParam(rd));
            param.append(", ");
            param.append(String.format("0x%x", x));
        } else if (opcode.equals("1100011")) {
            //B
            String imm1 = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String rs2 = mask.substring(20, 25);
            String imm2 = mask.substring(25, 32);

            StringBuilder temp = new StringBuilder("0" + imm1.substring(1) + imm2.substring(0, 6)
                    + imm1.charAt(0) + imm2.charAt(6));
            int x = Integer.parseInt(temp.reverse().toString(), 2);

            param.append(toParam(rs1));
            param.append(", ");
            param.append(toParam(rs2));
            param.append(", ");
            param.append(toImm(temp.toString()));
            if (funct3.equals("000")) {
                op.append("beq");
            } else if (funct3.equals("100")) {
                op.append("bne");
            } else if (funct3.equals("001")) {
                op.append("blt");
            } else if (funct3.equals("101")) {
                op.append("bge");
            } else if (funct3.equals("011")) {
                op.append("bltu");
            } else if (funct3.equals("111")) {
                op.append("bgeu");
            }
        } else if (opcode.equals("1111011")) {
            //J
            String rd = mask.substring(7, 12);
            String imm = mask.substring(12, 32);
            op.append("jal ");
            int x = Integer.parseInt((new StringBuilder(imm)).reverse().toString(), 2);

            param.append(toParam(rd));
            param.append(", ");
            param.append(toImm(imm));
        } else if (opcode.equals("1100010")) {
            //S
            String imm1 = mask.substring(7, 12);
            String funct3 = mask.substring(12, 15);
            String rs1 = mask.substring(15, 20);
            String rs2 = mask.substring(20, 25);
            String imm2 = mask.substring(25, 32);

            param.append(toParam(rs2));
            param.append(", ");
            param.append(toImm(imm1));
            param.append("(");
            param.append(toParam(rs1));
            param.append(")");

            if (funct3.equals("000")) {
                op.append("sb");
            } else if (funct3.equals("100")) {
                op.append("sh");
            } else if (funct3.equals("010")) {
                op.append("sw");
            }
        }

        if (symtab_dict.containsKey(addr) && symtab[symtab_dict.get(addr)].type == "FUNC") {
            return String.format("%08x %20s: %s %s",
                    addr, symtab[symtab_dict.get(addr)].name, op, param);
        } else {
            return String.format("%08x %20s: %s %s",
                    addr, "", op, param);
        }
    }
}
