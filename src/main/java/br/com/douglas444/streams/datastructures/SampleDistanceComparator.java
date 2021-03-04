package br.com.douglas444.streams.datastructures;

import java.util.Comparator;

public class SampleDistanceComparator implements Comparator<Sample> {

    private Sample target;

    public SampleDistanceComparator(final Sample target) {
        this.target = target;
    }

    /** Compares the distances of the two samples passed as argument to the
     * the target sample defined as a class attribute.
     *
     * @return Returns 0 if p1 and p2 have the same distance to the target
     * sample, -1 if p1 are closer to the target sample, and returns 1 if p1
     * are closer to the target sample.
     */
    @Override
    public int compare(final Sample p1, final Sample p2) {

        final double d1 = p1.distance(this.target);
        final double d2 = p2.distance(this.target);

        return Double.compare(d1, d2);
    }

    public Sample getTarget() {
        return target;
    }

    public void setTarget(Sample target) {
        this.target = target;
    }

}
