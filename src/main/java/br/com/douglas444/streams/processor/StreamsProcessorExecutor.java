package br.com.douglas444.streams.processor;

import br.com.douglas444.streams.datastructures.Sample;

import java.io.IOException;

public class StreamsProcessorExecutor {

    private boolean alive;

    public StreamsProcessorExecutor() {
        this.alive = false;
    }

    public void interrupt() {
        this.alive = false;
    }

    public boolean start(final StreamsProcessor streamsProcessor,
                         final StreamsFileReader... streamsFileReader) throws IOException {
        return start(streamsProcessor, 0, streamsFileReader);
    }
    public boolean start(final StreamsProcessor streamsProcessor, final int classifierLoggingTimestampInterval, final StreamsFileReader... streamsFileReader)
            throws IOException {

        this.alive = true;
        Sample sample;
        int timestamp = 0;

        for (StreamsFileReader f : streamsFileReader) {
            while (this.alive && (sample = f.next()) != null) {
                ++timestamp;
                streamsProcessor.process(sample);
                if (classifierLoggingTimestampInterval > 0 && timestamp % classifierLoggingTimestampInterval == 0) {
                    System.out.println(streamsProcessor.getLog());
                }
            }
        }

        return alive;
    }

}
