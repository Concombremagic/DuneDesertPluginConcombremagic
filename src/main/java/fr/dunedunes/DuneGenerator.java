
package fr.dunedunes;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.HeightMap;

import java.util.List;
import java.util.Random;

public class DuneGenerator extends ChunkGenerator {

    private static final int BASE_Y = 80;
    private static final int SPACING = 420;
    private static final double DUNE_CHANCE = 0.28;

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo info, int x, int y, int z) {
                return Biome.DESERT;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo info) {
                return List.of(Biome.DESERT);
            }
        };
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public void generateNoise(
            WorldInfo worldInfo,
            Random random,
            int chunkX,
            int chunkZ,
            ChunkData chunkData
    ) {
        int minY = chunkData.getMinHeight();
        int maxY = chunkData.getMaxHeight();
        long seed = worldInfo.getSeed();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                int surfaceY = duneHeight(worldX, worldZ, seed);
                surfaceY = Math.max(minY + 1, Math.min(maxY - 1, surfaceY));

                for (int y = minY; y <= surfaceY; y++) {

                    if (y == minY) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else if (y >= surfaceY - 3) {
                        chunkData.setBlock(
                                x, y, z,
                                surfaceMaterial(worldX, y, worldZ, seed)
                        );
                    } else {
                        chunkData.setBlock(x, y, z, Material.SANDSTONE);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(
            WorldInfo worldInfo,
            Random random,
            int x,
            int z,
            HeightMap heightMap
    ) {
        return duneHeight(x, z, worldInfo.getSeed()) + 1;
    }

    private int duneHeight(int x, int z, long seed) {

        double broad = valueNoise(seed + 111L, x, z, 240.0);
        double medium = valueNoise(seed + 222L, x, z, 100.0);
        double detail = valueNoise(seed + 333L, x, z, 38.0);
        double extra = valueNoise(seed + 888L, x, z, 160.0);

        double ground = BASE_Y
                + broad * 22.0
                + medium * 11.0
                + detail * 4.0
                + extra * 8.0;

        int cellX = Math.floorDiv(x, SPACING);
        int cellZ = Math.floorDiv(z, SPACING);

        double duneHeight = 0.0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                int cx = cellX + dx;
                int cz = cellZ + dz;

                long cellSeed = mix64(
                        seed
                                + cx * 341873128712L
                                + cz * 132897987541L
                );

                if (unitHash(cellSeed, 1) > DUNE_CHANCE) {
                    continue;
                }

                double centerX = cx * SPACING
                        + unitHash(cellSeed, 2) * 150.0 - 75.0;

                double centerZ = cz * SPACING
                        + unitHash(cellSeed, 3) * 150.0 - 75.0;

                double radius = 100.0
                        + unitHash(cellSeed, 4) * 80.0;

                double height = 12.0
                        + unitHash(cellSeed, 5) * 16.0;

                double distanceX = x - centerX;
                double distanceZ = z - centerZ;

                double distance = Math.sqrt(
                        distanceX * distanceX
                                + distanceZ * distanceZ
                );

                double irregularity = valueNoise(
                        cellSeed + 99L,
                        x,
                        z,
                        55.0
                ) * 0.18;

                double normalized = distance / radius;
                normalized += irregularity;

                if (normalized < 1.0) {

                    double shape = 1.0 - normalized;
                    shape = shape * shape * (3.0 - 2.0 * shape);

                    duneHeight = Math.max(
                            duneHeight,
                            shape * height
                    );
                }
            }
        }

        return (int) Math.round(ground + duneHeight);
    }

    private Material surfaceMaterial(int x, int y, int z, long seed) {

        long hash = mix64(
                seed
                        + x * 341873128712L
                        + y * 132897987541L
                        + z * 42317861L
        );

        double value = (hash >>> 11) / (double) (1L << 53);

        if (value < 0.50) {
            return Material.SAND;
        } else if (value < 0.70) {
            return Material.SANDSTONE;
        } else if (value < 0.90) {
            return Material.SUSPICIOUS_SAND;
        } else {
            return Material.END_STONE;
        }
    }

    private double valueNoise(
            long seed,
            double x,
            double z,
            double scale
    ) {
        double nx = x / scale;
        double nz = z / scale;

        int x0 = (int) Math.floor(nx);
        int z0 = (int) Math.floor(nz);

        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double tx = smooth(nx - x0);
        double tz = smooth(nz - z0);

        double a = hashNoise(seed, x0, z0);
        double b = hashNoise(seed, x1, z0);
        double c = hashNoise(seed, x0, z1);
        double d = hashNoise(seed, x1, z1);

        double top = lerp(a, b, tx);
        double bottom = lerp(c, d, tx);

        return lerp(top, bottom, tz);
    }

    private double hashNoise(long seed, int x, int z) {
        long hash = mix64(
                seed
                        + x * 341873128712L
                        + z * 132897987541L
        );

        return ((hash >>> 11) / (double) (1L << 53)) * 2.0 - 1.0;
    }

    private double unitHash(long seed, int salt) {
        long hash = mix64(seed + salt * 0x9E3779B97F4A7C15L);
        return (hash >>> 11) / (double) (1L << 53);
    }

    private long mix64(long value) {
        value = (value ^ (value >>> 30))
                * 0xBF58476D1CE4E5B9L;

        value = (value ^ (value >>> 27))
                * 0x94D049BB133111EBL;

        return value ^ (value >>> 31);
    }

    private double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
