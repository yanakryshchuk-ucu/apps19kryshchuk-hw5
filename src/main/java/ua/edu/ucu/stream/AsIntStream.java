package ua.edu.ucu.stream;

import ua.edu.ucu.function.*;

import java.util.*;


public class AsIntStream implements IntStream {

    private final Iterator<Integer> it;

    private AsIntStream(final Iterator<Integer> it) {
        this.it = it;
    }

    private void ensureNonEmpty() {
        if (!it.hasNext()) {
            throw new IllegalArgumentException();
        }
    }

    public static IntStream of(int... values) {
        final int[] copied = Arrays.copyOf(values, values.length);
        final Iterator<Integer> it = new Iterator<Integer>() {
            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < copied.length;
            }

            @Override
            public Integer next() {
                return copied[current++];
            }
        };
        return new AsIntStream(it);
    }

    @Override
    public Double average() {
        ensureNonEmpty();
        double sum = 0;
        int count = 0;
        while (it.hasNext()) {
            sum += it.next();
            count++;
        }
        return sum / count;
    }

    @Override
    public Integer max() {
        ensureNonEmpty();
        int m = Integer.MIN_VALUE;
        return this.reduce(m, Math::max);
    }

    @Override
    public Integer min() {
        ensureNonEmpty();
        int m = Integer.MAX_VALUE;
        return this.reduce(m, Math::min);

    }

    @Override
    public long count() {
        int count = 0;
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    @Override
    public Integer sum() {
        ensureNonEmpty();
        int sum = 0;
        while (it.hasNext()) {
            sum += it.next();
        }
        return sum;
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
        // create buffer - put no more elements than I have
        Iterator<Integer> it = new Iterator<Integer>() {
            private Integer toConsume = null;

            @Override
            public boolean hasNext() {
                if (toConsume == null) {
                    seekNext();
                }
                return toConsume != null;

            }

            private void seekNext() {
                while (toConsume == null && AsIntStream.this.it.hasNext()) {
                    Integer current = AsIntStream.this.it.next();
                    if (predicate.test(current)) {
                        toConsume = current;
                    }
                }
            }

            @Override
            public Integer next() {
                if (toConsume == null) {
                    seekNext();
                }
                if (toConsume == null) {
                    throw new IllegalArgumentException();
                }
                Integer consume = toConsume;
                toConsume = null;
                return consume;
            }
        };
        return new AsIntStream(it);
    }

    @Override
    public void forEach(IntConsumer action) {
        while (it.hasNext()) {
            action.accept(it.next());
        }
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        Iterator<Integer> it = new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return AsIntStream.this.it.hasNext();
            }

            @Override
            public Integer next() {
                return mapper.apply(AsIntStream.this.it.next());
            }
        };
        return new AsIntStream(it);
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        Iterator<Integer> it = new Iterator<Integer>() {
            private AsIntStream substream;

            //find next substream which will be used
            private void seekSubStream() {
                substream = null;
                // go to source iterator, convert, check
                while (substream == null && AsIntStream.this.it.hasNext()) {
                    IntStream stream = func.applyAsIntStream(AsIntStream.this.it.next());
                    substream = (AsIntStream) of(stream.toArray());
                    if (!substream.it.hasNext()) {
                        substream = null;
                    }
                }

            }

            @Override
            public boolean hasNext() {
                if (substream == null || !substream.it.hasNext()) {
                    seekSubStream();
                }
                return substream != null && substream.it.hasNext();
            }

            @Override
            public Integer next() {
                if (hasNext()) {
                    return substream.it.next();
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        return new AsIntStream(it);
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        int value = identity;
        while (it.hasNext()) {
            value = op.apply(value, it.next());
        }
        return value;
    }

    @Override
    public int[] toArray() {
        final List<Integer> list = new LinkedList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
