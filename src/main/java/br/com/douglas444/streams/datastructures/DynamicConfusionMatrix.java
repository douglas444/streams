package br.com.douglas444.streams.datastructures;

import java.util.*;

public class DynamicConfusionMatrix {

    private final List<Integer> rowLabels;
    private final List<Integer> knownColumnLabels;
    private final List<Integer> noveltyColumnLabels;

    //Number of columns
    private int knownColumnsCount;
    private int noveltyColumnsCount;

    //Indices for matrix access
    private final HashMap<Integer, Integer> knownColumnIndexByLabel;
    private final HashMap<Integer, Integer> noveltyColumnIndexByLabel;
    private final HashMap<Integer, Integer> rowIndexByLabel;

    //Matrix
    private final List<List<Integer>> knownColumnsMatrix;
    private final List<List<Integer>> noveltyColumnsMatrix;
    private final List<Integer> unknownColumn;

    public DynamicConfusionMatrix() {

        this.rowLabels = new ArrayList<>();
        this.knownColumnLabels = new ArrayList<>();
        this.noveltyColumnLabels = new ArrayList<>();

        this.knownColumnsCount = 0;
        this.noveltyColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.noveltyColumnIndexByLabel = new HashMap<>();
        this.rowIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.noveltyColumnsMatrix = new ArrayList<>();
        this.unknownColumn = new ArrayList<>();

    }

    public DynamicConfusionMatrix(List<Integer> knownLabels) {

        this.rowLabels = new ArrayList<>();
        this.knownColumnLabels = new ArrayList<>();
        this.noveltyColumnLabels = new ArrayList<>();

        this.knownColumnsCount = 0;
        this.noveltyColumnsCount = 0;

        this.knownColumnIndexByLabel = new HashMap<>();
        this.noveltyColumnIndexByLabel = new HashMap<>();
        this.rowIndexByLabel = new HashMap<>();

        this.knownColumnsMatrix = new ArrayList<>();
        this.noveltyColumnsMatrix = new ArrayList<>();
        this.unknownColumn = new ArrayList<>();

        knownLabels.forEach(this::addKnownLabel);
    }

    public boolean isLabelKnown(final Integer label) {
        return this.knownColumnLabels.contains(label);
    }

    public void addKnownLabel(final Integer label) {
        this.addKnownColumn(label);
        if (!this.rowLabels.contains(label)) {
            this.addRow(label);
        }
    }

    private void addRow(final Integer label) {
        this.rowIndexByLabel.put(label, rowLabels.size());
        this.rowLabels.add(label);
        this.knownColumnsMatrix.add(new ArrayList<>(Collections.nCopies(knownColumnsCount, 0)));
        this.noveltyColumnsMatrix.add(new ArrayList<>(Collections.nCopies(noveltyColumnsCount, 0)));
        this.unknownColumn.add(0);

    }

    private void addKnownColumn(final Integer label) {

        this.knownColumnLabels.add(label);
        this.knownColumnIndexByLabel.put(label, this.knownColumnsCount++);
        this.knownColumnsMatrix.forEach(row -> row.add(0));

    }

    private void addNoveltyColumn(final Integer label) {

        this.noveltyColumnLabels.add(label);
        this.noveltyColumnIndexByLabel.put(label, this.noveltyColumnsCount++);
        this.noveltyColumnsMatrix.forEach(row -> row.add(0));

    }

    public void updatedDelayed(final int realLabel, final int predictedLabel, final boolean isNovel) {

        if (!this.rowLabels.contains(realLabel)) {

            throw new RuntimeException("Invalid value for parameter realLabel");

        }

        final int rowIndex = this.rowIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(rowIndex);
        this.unknownColumn.set(rowIndex, value - 1);

        this.addPrediction(realLabel, predictedLabel, isNovel);

    }

    public void addUnknown(final int realLabel) {

        if (!this.rowLabels.contains(realLabel)) {

            this.addRow(realLabel);

        }

        final int rowIndex = this.rowIndexByLabel.get(realLabel);
        final int value = this.unknownColumn.get(rowIndex);
        this.unknownColumn.set(rowIndex, value + 1);

    }

    public void addPrediction(final int realLabel, final int predictedLabel, final boolean isNovel) {


        if (!this.rowLabels.contains(realLabel)) {

            this.addRow(realLabel);

        }

        final int rowIndex = this.rowIndexByLabel.get(realLabel);

        if (isNovel) {

            if (!this.noveltyColumnLabels.contains(predictedLabel)) {
                this.addNoveltyColumn(predictedLabel);
            }

            final int columnIndex = this.noveltyColumnIndexByLabel.get(predictedLabel);
            final int count = this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex);
            this.noveltyColumnsMatrix.get(rowIndex).set(columnIndex, count + 1);

        } else {

            if (!this.knownColumnIndexByLabel.containsKey(predictedLabel)) {
                throw new IllegalArgumentException("Predicted label is not known");

            }

            final int columnIndex = this.knownColumnIndexByLabel.get(predictedLabel);
            final int count = this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
            this.knownColumnsMatrix.get(rowIndex).set(columnIndex, count + 1);

        }

    }

    @Override
    public String toString() {

        final List<Integer> sortedKnownColumnLabels = new ArrayList<>(this.knownColumnLabels);
        sortedKnownColumnLabels.sort(Comparator.comparingInt(rowLabels::indexOf));

        final int[][] matrix = new int[this.rowLabels.size() + 1][this.knownColumnsCount + this.noveltyColumnsCount + 2];

        for (int i = 0; i < sortedKnownColumnLabels.size(); ++i) {
            matrix[0][i + 1] = sortedKnownColumnLabels.get(i);
        }

        for (int i = 0; i < this.noveltyColumnLabels.size(); ++i) {
            matrix[0][i + sortedKnownColumnLabels.size() + 1] = this.noveltyColumnLabels.get(i);
        }

        for (int i = 0; i < this.rowLabels.size(); ++i) {
            matrix[i + 1][0] = this.rowLabels.get(i);
        }


        for (int i = 0; i < this.rowLabels.size(); ++i) {
            for (int j = 0; j < this.knownColumnsCount; ++j) {

                final int row = this.rowLabels.get(i);
                final int column = sortedKnownColumnLabels.get(j);

                final int rowIndex = this.rowIndexByLabel.get(row);
                final int columnIndex = this.knownColumnIndexByLabel.get(column);

                matrix[i + 1][j + 1] = this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
            }
        }

        for (int i = 0; i < this.unknownColumn.size(); ++i) {
            matrix[i + 1][this.knownColumnsCount + this.noveltyColumnsCount + 1] = this.unknownColumn.get(i);
        }

        for (int i = 0; i < this.rowLabels.size(); ++i) {
            for (int j = 0; j < this.noveltyColumnsCount; ++j) {

                final int row = this.rowLabels.get(i);
                final int column = this.noveltyColumnLabels.get(j);

                final int rowIndex = this.rowIndexByLabel.get(row);
                final int columnIndex = this.noveltyColumnIndexByLabel.get(column);

                matrix[i + 1][j + this.knownColumnsCount + 1] =
                        this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex);
            }

        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                if (i == 0 && j == 0) {
                    stringBuilder.append(String.format("   %6s", ""));
                } else if (i == 0 && j > this.knownColumnsCount && j < this.knownColumnsCount + this.noveltyColumnsCount + 1) {
                    stringBuilder.append(String.format("|PN%6d", this.noveltyColumnIndexByLabel.get(matrix[i][j])));
                } else if (i == 0 && j > this.knownColumnsCount) {
                    stringBuilder.append(String.format("|%1sUNKNOWN", ""));
                } else if (j == 0 && i > this.knownColumnsCount){
                    stringBuilder.append(String.format("|CN%6d", matrix[i][j]));
                } else if (i == 0 || j == 0){
                    stringBuilder.append(String.format("|CK%6d", matrix[i][j]));
                } else {
                    stringBuilder.append(String.format("|  %6d", matrix[i][j]));
                }
            }
            stringBuilder.append("|\n");
        }

        return stringBuilder.toString();
    }

    public HashMap<Integer, List<Integer>> calculateNoveltyAssociationByRow() {

        final HashMap<Integer, List<Integer>> noveltyAssociationByRow = new HashMap<>();

        for (Integer noveltyColumnLabel: this.noveltyColumnLabels) {

            int max = 0;
            int label = -1;

            for (Integer rowLabel: this.rowLabels) {
                final int row = this.rowIndexByLabel.get(rowLabel);
                final int column = this.noveltyColumnIndexByLabel.get(noveltyColumnLabel);
                if (this.noveltyColumnsMatrix.get(row).get(column) > max) {
                    max = this.noveltyColumnsMatrix.get(row).get(column);
                    label = rowLabel;
                }
            }

            if (label != -1) {
                List<Integer> novelties;
                if ((novelties = noveltyAssociationByRow.get(label)) != null) {
                    novelties.add(noveltyColumnLabel);
                } else {
                    novelties = new ArrayList<>();
                    novelties.add(noveltyColumnLabel);
                    noveltyAssociationByRow.put(label, novelties);
                }
            }
        }

        return noveltyAssociationByRow;

    }

    public int measureTP(final int label, final HashMap<Integer, List<Integer>> noveltyAssociationByRow) {

        int sum = 0;
        final int rowIndex = this.rowIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {
            final int columnIndex = this.knownColumnIndexByLabel.get(label);
            sum += this.knownColumnsMatrix.get(rowIndex).get(columnIndex);
        }

        final List<Integer> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        sum += novelties.stream()
                .map(this.noveltyColumnIndexByLabel::get)
                .map(noveltyIndex -> this.noveltyColumnsMatrix.get(rowIndex).get(noveltyIndex))
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int measureFP(final int label, final HashMap<Integer, List<Integer>> noveltyAssociationByRow) {

        int sum = 0;

        if (this.knownColumnLabels.contains(label)) {

            final int columnIndex = this.knownColumnIndexByLabel.get(label);

            sum += this.rowLabels.stream()
                    .filter(rowLabel -> rowLabel != label)
                    .map(this.rowIndexByLabel::get)
                    .map(rowIndex -> this.knownColumnsMatrix.get(rowIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        final List<Integer> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        sum += novelties.stream()
                .map(this.noveltyColumnIndexByLabel::get)
                .map(noveltyIndex ->

                    this.rowLabels.stream()
                            .filter(rowLabel -> rowLabel != label)
                            .map(this.rowIndexByLabel::get)
                            .map(rowIndex -> this.noveltyColumnsMatrix.get(rowIndex).get(noveltyIndex))
                            .reduce(0, Integer::sum)

                )
                .reduce(0,  Integer::sum);

        return sum;

    }

    public int measureFN(final int label, final HashMap<Integer, List<Integer>> noveltyAssociationByRow) {

        int sum = 0;

        final int rowIndex = this.rowIndexByLabel.get(label);

        if (this.knownColumnLabels.contains(label)) {

            sum += this.knownColumnLabels.stream()
                    .filter(columnLabel -> columnLabel != label)
                    .map(this.knownColumnIndexByLabel::get)
                    .map(columnIndex -> this.knownColumnsMatrix.get(rowIndex).get(columnIndex))
                    .reduce(0, Integer::sum);

        }

        final List<Integer> novelties = noveltyAssociationByRow.get(label);
        if (novelties == null) {
            return sum;
        }

        sum += this.noveltyColumnLabels.stream()
                .filter(columnLabel -> !novelties.contains(columnLabel))
                .map(this.noveltyColumnIndexByLabel::get)
                .map(columnIndex -> this.noveltyColumnsMatrix.get(rowIndex).get(columnIndex))
                .reduce(0, Integer::sum);

        return sum;
    }


    public int measureTN(final int label, final HashMap<Integer, List<Integer>> noveltyAssociationByRow) {

        return this.rowLabels.stream()
                .filter(rowLabel -> rowLabel != label)
                .map(rowLabel -> this.measureTP(rowLabel, noveltyAssociationByRow))
                .reduce(0, Integer::sum);

    }

    public int numberOfExplainedSamplesPerLabel(final int label) {

        final int rowIndex = this.rowIndexByLabel.get(label);

        int sum = this.knownColumnsMatrix.get(rowIndex)
                .stream()
                .reduce(0, Integer::sum);

        sum += this.noveltyColumnsMatrix.get(rowIndex)
                .stream()
                .reduce(0, Integer::sum);

        return sum;
    }

    public int numberOfExplainedSamples() {
        return this.rowLabels.stream().map(this::numberOfExplainedSamplesPerLabel).reduce(0, Integer::sum);
    }

    public double measureCER() {

        double sum = 0;
        final int totalExplainedSamples = this.numberOfExplainedSamples();
        final HashMap<Integer, List<Integer>> association = calculateNoveltyAssociationByRow();

        sum += this.rowLabels.stream().map(rowLabel -> {

            final int fp = this.measureFP(rowLabel, association);
            final int fn = this.measureFN(rowLabel, association);
            final int tn = this.measureTN(rowLabel, association);
            final int tp = this.measureTP(rowLabel, association);

            final int numberOfExplainedSamples = this.numberOfExplainedSamplesPerLabel(rowLabel);

            if (numberOfExplainedSamples == 0) {
                return 0.0;
            }

            final double rate = numberOfExplainedSamples / (double) totalExplainedSamples;

            return rate * (fp / (double) Math.max(1, fp + tn))
                    + rate * (fn / (double) Math.max(1, fn + tp));


        }).reduce(0.0, Double::sum);

        return sum / 2;
    }

    public double measureUnkR() {

        return this.rowLabels.stream()
                .map(rowLabel -> {

                    final double unexplained = this.unknownColumn.get(this.rowIndexByLabel.get(rowLabel));
                    final double explained = this.numberOfExplainedSamplesPerLabel(rowLabel);

                    if (explained == 0) {
                        if (unexplained == 0) {
                            return 0.0;
                        } else {
                            return 1.0;
                        }
                    } else {
                        return unexplained / (explained + unexplained);
                    }

                })
                .reduce(0.0, Double::sum) / this.rowLabels.size();

    }
}
