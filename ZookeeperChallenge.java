import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Myanimals.Animal;

public class ZookeeperChallenge {

    private static final String ARRIVING_ANIMALS_FILE = "arrivingAnimals.txt";
    private static final String ANIMAL_NAMES_FILE = "animalNames.txt";
    private static final String ZOO_POPULATION_FILE = "zooPopulation.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
    private static final Map<String, Integer> speciesCounter = new HashMap<>();
    private static final Map<String, List<Animal>> habitats = new HashMap<>();
    private static List<String> animalNames = new ArrayList<>();

    public static void main(String[] args) {
        try {
            loadAnimalNames();
            processArrivingAnimals();
            writeZooPopulationReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load animal names from the file
    private static void loadAnimalNames() throws IOException {
        animalNames = Files.readAllLines(Paths.get(ANIMAL_NAMES_FILE));
        if (animalNames.isEmpty()) {
            throw new IOException("Animal names file is empty.");
        }
    }

    // Process the arriving animals and categorize them into habitats
    private static void processArrivingAnimals() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(ARRIVING_ANIMALS_FILE));
        for (String line : lines) {
            if (line.trim().isEmpty())
                continue; // Skip empty lines
            Animal animal = parseAnimal(line);
            String habitat = animal.species + " Habitat";
            habitats.computeIfAbsent(habitat, k -> new ArrayList<>()).add(animal);
        }
    }

    // Parse the animal description and create an Animal object
    private static Animal parseAnimal(String description) {
        String[] parts = description.split(", ");
        String ageSexSpecies = parts[0];
        String[] ageSexSpeciesParts = ageSexSpecies.split(" ");
        int age = Integer.parseInt(ageSexSpeciesParts[0]);
        String sex = ageSexSpeciesParts[3];
        String species = ageSexSpeciesParts[4];

        String birthSeason = parts[1].split(" ")[2];
        String color = parts[2];
        double weight = Double.parseDouble(parts[3].split(" ")[0]);
        String origin = parts[4];

        LocalDate birthDate = genBirthDay(birthSeason, age);
        String name = animalNames.remove(0);
        String id = genUniqueID(species);

        return new Animal(id, name, birthDate, color, sex, weight, species, origin);
    }

    // Generate the birthday based on the birth season and age
    private static LocalDate genBirthDay(String birthSeason, int age) {
        LocalDate now = LocalDate.now();
        LocalDate birthDate = now.minusYears(age);
        if (birthSeason != null) {
            int month = switch (birthSeason.toLowerCase()) {
                case "spring" -> 3;
                case "summer" -> 6;
                case "fall" -> 9;
                case "winter" -> 12;
                default -> birthDate.getMonthValue();
            };
            birthDate = birthDate.with(ChronoField.MONTH_OF_YEAR, month).with(ChronoField.DAY_OF_MONTH, 21);
        }
        return birthDate;
    }

    // Generate a unique ID for each animal
    private static String genUniqueID(String species) {
        int count = speciesCounter.getOrDefault(species, 0) + 1;
        speciesCounter.put(species, count);
        return species.substring(0, 2).toUpperCase() + String.format("%02d", count);
    }

    // Write the zoo population report to a file
    private static void writeZooPopulationReport() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ZOO_POPULATION_FILE))) {
            for (Map.Entry<String, List<Animal>> entry : habitats.entrySet()) {
                writer.write(entry.getKey() + ":\n");
                for (Animal animal : entry.getValue()) {
                    writer.write(animal.toString() + "\n");
                }
                writer.write("\n");
            }
        }
    }

    // Animal class representing each animal's attributes
    static class Animal {
        String id;
        String name;
        LocalDate birthDate;
        String color;
        String sex;
        double weight;
        String species;
        String origin;

        Animal(String id, String name, LocalDate birthDate, String color, String sex, double weight, String species,
                String origin) {
            this.id = id;
            this.name = name;
            this.birthDate = birthDate;
            this.color = color;
            this.sex = sex;
            this.weight = weight;
            this.species = species;
            this.origin = origin;
        }

        @Override
        public String toString() {
            return id + "; " + name + "; birth date: " + DATE_FORMAT.format(birthDate) + "; " + color + "; " + sex
                    + "; " + weight + " pounds; from " + origin;
        }
    }
}
