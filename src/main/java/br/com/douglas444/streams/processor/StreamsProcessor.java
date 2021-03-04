package br.com.douglas444.streams.processor;

import br.com.douglas444.streams.datastructures.Sample;

import java.util.Optional;

public interface StreamsProcessor {

    Optional<Integer> process(final Sample sample);

    String getLog();

}
