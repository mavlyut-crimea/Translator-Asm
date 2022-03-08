package myBase;

public class MyPair<F, S> {
    private final F first;
    private final S second;

    public MyPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return String.format("%s %s", first, second);
    }
}
