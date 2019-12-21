package ua.edu.ucu.stream;

import ua.edu.ucu.function.IntBinaryOperator;
import ua.edu.ucu.function.IntConsumer;
import ua.edu.ucu.function.IntPredicate;
import ua.edu.ucu.function.IntToIntStreamFunction;
import ua.edu.ucu.function.IntUnaryOperator;

import java.util.*;

public class AsIntStream implements IntStream {

    private final Iterator<Integer> iterator;

    private AsIntStream(final Iterator<Integer> it) {
        this.iterator = it;
    }

    private void ensureNonEmpty() {
        if (!iterator.hasNext()) {
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
                if (current > copied.length - 1) {
                    throw new NoSuchElementException();
                }
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
        while (iterator.hasNext()) {
            sum += iterator.next();
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
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        return count;
    }

    @Override
    public Integer sum() {
        ensureNonEmpty();
        int sum = 0;
        while (iterator.hasNext()) {
            sum += iterator.next();
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
                while (toConsume == null &&
                        AsIntStream.this.iterator.hasNext()) {
                    Integer current = AsIntStream.this.iterator.next();
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
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        Iterator<Integer> it = new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return AsIntStream.this.iterator.hasNext();
            }

            @Override
            public Integer next() {
                return mapper.apply(AsIntStream.this.iterator.next());
            }
        };
        return new AsIntStream(it);
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        return new AsIntStream(new StreamingIterator(func));
    }

    private class StreamingIterator implements Iterator<Integer> {

        private final IntToIntStreamFunction func;
        private AsIntStream substream;

        private StreamingIterator(final IntToIntStreamFunction func) {
            this.func = func;
        }

        //find next substream which will be used
        private void seekSubStream() {
            substream = null;
            // go to source iterator, convert, check
            while (substream == null && AsIntStream.
                    this.iterator.hasNext()) {
                IntStream stream = func.applyAsIntStream(AsIntStream.
                        this.iterator.next());
                substream = (AsIntStream) of(stream.toArray());
                if (!substream.iterator.hasNext()) {
                    substream = null;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (substream == null || !substream.iterator.hasNext()) {
                seekSubStream();
            }
            return substream != null && substream.iterator.hasNext();
        }

        @Override
        public Integer next() {
            if (hasNext()) {
                return substream.iterator.next();
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        int value = identity;
        while (iterator.hasNext()) {
            value = op.apply(value, iterator.next());
        }
        return value;
    }

    @Override
    public int[] toArray() {
        final List<Integer> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
