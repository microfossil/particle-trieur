package particletrieur.services;

import particletrieur.models.project.Project;
import particletrieur.models.project.Particle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class StatisticsService {
    
    private Project project;      
    
    public StatisticsService(Project project) {
        this.project = project;
    }

    private List<Double> uniqueIndexValues(int indexType) {
        List<Double> uniqueIndices;
        if (indexType == 1) {
            uniqueIndices = project.getParticles().stream().map(Particle::getIndex1).distinct().sorted().collect(Collectors.toList());
        }
        else {
            uniqueIndices = project.getParticles().stream().map(Particle::getIndex2).distinct().sorted().collect(Collectors.toList());
        }
        return uniqueIndices;
    }

    private List<String> uniqueSampleValues() {
        return project.getParticles().stream().map(Particle::getSampleID).distinct().sorted().collect(Collectors.toList());
    }

    private List<String> uniqueLabelValues() {
        return project.getParticles().stream().map(Particle::getClassification).distinct().sorted().collect(Collectors.toList());
    }

    private List<String> getProjectLabels() {
        return project.getTaxons().keySet().stream().sorted().collect(Collectors.toList());
    }

    private List<String> getProjectMorphometricLabels() {
        return project.getTaxons().values().stream().filter(t -> t.getIsClass()).map(t -> t.getCode()).sorted().collect(Collectors.toList());
    }

    public <T> LinkedHashMap<T,Double> createTable(List<T> rowNames) {
        LinkedHashMap<T,Double> table = new LinkedHashMap<>();
        for (T rowName : rowNames) {
            table.put(rowName, 0.0);
        }
        return table;
    }

    public <T> LinkedHashMap<T,Double> fillTable(
            List<T> rowNames,
            List<Particle> particles,
            Function<Particle,T> rowFunction) {
        LinkedHashMap<T, Double> table = createTable(rowNames);
        for(Particle particle : particles) {
            T rowKey = rowFunction.apply(particle);
            if (table.containsKey(rowKey)) {
                Double value = table.get(rowKey);
                table.put(rowKey, value + 1);
            }
        }
        return table;
    }

    public <T,U> LinkedHashMap<T,LinkedHashMap<U,Double>> createTable(List<T> rowNames, List<U> columnNames) {
        LinkedHashMap<T,LinkedHashMap<U,Double>> table = new LinkedHashMap<>();
        for (T rowName : rowNames) {
            LinkedHashMap<U,Double> row = new LinkedHashMap<>();
            for (U columnName : columnNames) {
                row.put(columnName, 0.0);
            }
            table.put(rowName, row);
        }
        return table;
    }

    public <T,U> LinkedHashMap<T, LinkedHashMap<U,Double>> fillTable(
            List<T> rowNames,
            List<U> columnNames,
            List<Particle> particles,
            Function<Particle,T> rowFunction,
            Function<Particle,U> colFunction) {
        LinkedHashMap<T, LinkedHashMap<U,Double>> table = createTable(rowNames, columnNames);
        for(Particle particle : particles) {
            T rowKey = rowFunction.apply(particle);
            U colKey = colFunction.apply(particle);
            if (table.containsKey(rowKey)) {
                LinkedHashMap<U,Double> row = table.get(rowKey);
                if (row.containsKey(colKey)) {
                    Double value = table.get(rowKey).get(colKey);
                    table.get(rowKey).put(colKey, value + 1);
                }
            }
        }
        return table;
    }

    public <T,U> void relativise(LinkedHashMap<T, LinkedHashMap<U,Double>> table, boolean byColumn) {
        if (!byColumn) {
            for (LinkedHashMap<U, Double> row : table.values()) {
                double count = row.values().stream().mapToDouble(Double::doubleValue).sum();
                for (Map.Entry<U, Double> val : row.entrySet()) {
                    val.setValue(val.getValue() / count);
                }
            }
        }
        else {
            LinkedHashMap<U,Double> counts = new LinkedHashMap<>();
            for (LinkedHashMap<U, Double> row : table.values()) {
                for (Map.Entry<U, Double> val : row.entrySet()) {
                    counts.put(val.getKey(),counts.getOrDefault(val.getKey(),0.0) + val.getValue());
                }
            }
            for (LinkedHashMap<U, Double> row : table.values()) {
                for (Map.Entry<U, Double> val : row.entrySet()) {
                    val.setValue(val.getValue() / counts.get(val.getKey()));
                }
            }
        }
    }

    public LinkedHashMap<String,Double> classCounts() {
        LinkedHashMap<String,Double> table = fillTable(
                getProjectLabels(),
                project.particles,
                p -> p.getClassification());
        return table;
    }

    public LinkedHashMap<String,Double> sampleCounts() {
        LinkedHashMap<String,Double> table = fillTable(
                uniqueSampleValues(),
                project.particles,
                p -> p.getSampleID());
        return table;
    }

    public LinkedHashMap<Double,Double> index1Counts() {
        LinkedHashMap<Double,Double> table = fillTable(
                uniqueIndexValues(1),
                project.particles,
                Particle::getIndex1);
        return table;
    }

    public LinkedHashMap<Double,Double> index2Counts() {
        LinkedHashMap<Double,Double> table = fillTable(
                uniqueIndexValues(2),
                project.particles,
                Particle::getIndex2);
        return table;
    }

    public LinkedHashMap<Double,LinkedHashMap<String,Double>> indexByLabel(int indexType, boolean calculateRelative) {
        LinkedHashMap<Double,LinkedHashMap<String,Double>> table = fillTable(
                uniqueIndexValues(indexType),
                getProjectMorphometricLabels(),
                project.particles,
                p -> p.getIndexN(indexType),
                p -> p.getClassification());
        if (calculateRelative) {
            relativise(table, false);
        }
        return table;
    }

    public LinkedHashMap<String,LinkedHashMap<Double,Double>> labelByIndex(int indexType, boolean calculateRelative) {
        LinkedHashMap<String,LinkedHashMap<Double,Double>> table = fillTable(
                getProjectMorphometricLabels(),
                uniqueIndexValues(indexType),
                project.particles,
                p -> p.getClassification(),
                p -> p.getIndexN(indexType));
        if (calculateRelative) {
            relativise(table, true);
        }
        return table;
    }

    public LinkedHashMap<String,LinkedHashMap<String,Double>> sampleByLabel(boolean calculateRelative) {
        LinkedHashMap<String, LinkedHashMap<String,Double>> table = fillTable(
                uniqueSampleValues(),
                uniqueLabelValues(),
                project.particles,
                p -> p.getSampleID(),
                p -> p.getClassification());
        if (calculateRelative) {
            relativise(table, false);
        }
        return table;
    }

    public LinkedHashMap<String,LinkedHashMap<String,Double>> labelBySample(boolean calculateRelative) {
        LinkedHashMap<String,LinkedHashMap<String,Double>> table = fillTable(
                getProjectMorphometricLabels(),
                uniqueSampleValues(),
                project.particles,
                p -> p.getClassification(),
                p -> p.getSampleID());
        if (calculateRelative) {
            relativise(table, true);
        }
        return table;
    }
}
