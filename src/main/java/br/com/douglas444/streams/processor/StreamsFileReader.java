package br.com.douglas444.streams.processor;

import br.com.douglas444.streams.datastructures.Sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class StreamsFileReader {

    private final String separator;
    private final BufferedReader bufferedReaderData;
    private final BufferedReader bufferedReaderLabel;
    private Boolean labelIsNaN;
    private HashMap<String, Integer> labelEnumeration;

    public StreamsFileReader(final String separator,
                             final Reader dataReader,
                             final Reader labelReader) {

        this.separator = separator;
        this.bufferedReaderData = new BufferedReader(dataReader);
        this.bufferedReaderLabel = new BufferedReader(labelReader);
        this.labelIsNaN = null;
    }

    public StreamsFileReader(final String separator, final Reader reader) {

        this.separator = separator;
        this.bufferedReaderData = new BufferedReader(reader);
        this.bufferedReaderLabel = null;
        this.labelIsNaN = null;

    }

    public Sample next() throws IOException, NumberFormatException {

        String line = this.bufferedReaderData.readLine();

        if (line == null) {
            this.bufferedReaderData.close();

            if (this.bufferedReaderLabel != null) {
                this.bufferedReaderLabel.close();
            }

            return null;
        }

        final String[] splittedLine = line.split(this.separator);

        final int numberOfFeatures = this.bufferedReaderLabel != null
                ? splittedLine.length : splittedLine.length - 1;

        final double[] x = new double[numberOfFeatures];

        for (int i = 0; i < numberOfFeatures; ++i) {
            x[i] = Double.parseDouble(splittedLine[i]);
        }

        final String y;

        if (this.bufferedReaderLabel != null) {
            line = this.bufferedReaderLabel.readLine();
            y = line;
        } else {
            y = splittedLine[splittedLine.length - 1];
        }

        if (this.labelIsNaN == null) {
            if (!isNumeric(y)) {
                this.labelIsNaN = true;
                this.labelEnumeration = new HashMap<>();
            } else {
                this.labelIsNaN = false;
            }
        }

        if (this.labelIsNaN) {
            labelEnumeration.putIfAbsent(y, labelEnumeration.size());
            return new Sample(x, labelEnumeration.get(y));
        } else {
            return new Sample(x, (int) Double.parseDouble(y));
        }

    }

    public List<Sample> next(final int n) throws IOException, NumberFormatException {

        final List<Sample> samples = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            samples.add(next());
        }
        return samples;
    }

    private boolean isNumeric(final String str) {

        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (str == null) {
            return false;
        }
        return pattern.matcher(str).matches();
    }

}
