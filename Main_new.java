import parsers.ParserELF;

public class Main_new {
    public static void main(String[] args) {
        new ParserELF(args[0], args[1]).parse();
    }
}
