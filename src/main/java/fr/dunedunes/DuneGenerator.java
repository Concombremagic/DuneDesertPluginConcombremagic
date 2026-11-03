
package fr.dunedunes;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.HeightMap;
import org.bukkit.Material;

import java.util.List;
import java.util.Random;

public final class DuneGenerator extends ChunkGenerator {

    private static final int BASE_Y = 72;
    private static final int SPACING = 360;
    private static final double DUNE_CHANCE = 0.28;

    private static final BiomeProvider DESERT_BIOMES = new BiomeProvider() {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return Biome.DESERT;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(Biome.DESERT);
        }
    };

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return DESERT_BIOMES;
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
            WorldInfo info,
            Random random,
            int chunkX,
            int chunkZ,
            ChunkData data
    ) {
        long seed = info.getSeed();
        int minY = data.getMinHeight();
        int maxY = data.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                int surfaceY = duneHeight(seed, worldX, worldZ);

                surfaceY = Math.max(
                        minY + 8,
                        Math.min(maxY - 8, surfaceY)
                );

                data.setBlock(x, minY, z, Material.BEDROCK);

                for (int y = minY + 1; y <= surfaceY; y++) {
                    int depth = surfaceY - y;

                    if (depth < 4) {
                        data.setBlock(
                                x,
                                y,
                                z,
                                surfaceMaterial(seed, worldX, y, worldZ)
                        );
                    } else {
                        data.setBlock(x, y, z, Material.SANDSTONE);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(
            WorldInfo info,
            Random random,
            int x,
            int z,
            HeightMap heightMap
    ) {
        return duneHeight(info.getSeed(), x, z) + 1;
    }

    private static int duneHeight(long seed, int x, int z) {

        // Grandes ondulations naturelles plus prononcées.
        double broad = valueNoise(seed + 111L, x, z, 200.0);
        double medium = valueNoise(seed + 222L, x, z, 85.0);
        double detail = valueNoise(seed + 333L, x, z, 32.0);

        double ground = BASE_Y
                + broad * 12.0
                + medium * 6.0
                + detail * 2.5;

        double highestDune = 0.0;

        int cellX = Math.floorDiv(x, SPACING);
        int cellZ = Math.floorDiv(z, SPACING);

        for (int gx = cellX - 1; gx <= cellX + 1; gx++) {
            for (int gz = cellZ - 1; gz <= cellZ + 1; gz++) {

                // Toutes les cellules ne contiennent pas une dune.
                if (unitHash(seed + 777L, gx, gz) > DUNE_CHANCE) {
                    continue;
                }

                double centerX = gx * SPACING
                        + SPACING / 2.0
                        + hashNoise(seed + 1234L, gx, gz) * 65.0;

                double centerZ = gz * SPACING
                        + SPACING / 2.0
                        + hashNoise(seed + 5678L, gx, gz) * 65.0;

                // Dunes très larges.
                double radius = 75.0
                        + unitHash(seed + 333L, gx, gz) * 65.0;

                // Hauteur supplémentaire de 7 à 19 blocs.
                double duneHeight = 7.0
                        + unitHash(seed + 444L, gx, gz) * 12.0;

                double dx = x - centerX;
                double dz = z - centerZ;

                double angle = Math.atan2(dz, dx);
                double distance = Math.sqrt(dx * dx + dz * dz);

                // Forme légèrement irrégulière.
                double phase = unitHash(seed + 555L, gx, gz)
                        * Math.PI * 2.0;

                double irregularity =
                        1.0
                        + 0.10 * Math.sin(angle * 3.0 + phase)
                        + 0.06 * Math.sin(angle * 5.0 + phase * 0.7);

                double effectiveRadius = radius * irregularity;

                if (distance < effectiveRadius) {

                    double t = distance / effectiveRadius;

                    // Pente progressive et sommet arrondi.
                    double shape = 0.5 * (1.0 + Math.cos(Math.PI * t));
                    shape = Math.pow(shape, 1.2);

                    double height = duneHeight * shape;

                    highestDune = Math.max(highestDune, height);
                }
            }
        }

        return (int) Math.round(ground + highestDune);
    }

    // Bruit interpolé pour un relief sans marches d'escalier.
    private static double valueNoise(
            long seed,
            double x,
            double z,
            double scale
    ) {
        double nx = x / scale;
        double nz = z / scale;

        int x0 = (int) Math.floor(nx);
        int z0 = (int) Math.floor(nz);

        double fx = nx - x0;
        double fz = nz - z0;

        fx = fx * fx * (3.0 - 2.0 * fx);
        fz = fz * fz * (3.0 - 2.0 * fz);

        double a = hashNoise(seed, x0, z0);
        double b = hashNoise(seed, x0 + 1, z0);
        double c = hashNoise(seed, x0, z0 + 1);
        double d = hashNoise(seed, x0 + 1, z0 + 1);

        double top = a + (b - a) * fx;
        double bottom = c + (d - c) * fx;

        return top + (bottom - top) * fz;
    }

    private static Material surfaceMaterial(
            long seed,
            int x,
            int y,
            int z
    ) {
        double r = unitHash(seed, x, y, z);

        if (r < 0.50) return Material.SAND;
        if (r < 0.70) return Material.SANDSTONE;
        if (r < 0.90) return Material.SUSPICIOUS_SAND;

        return Material.END_STONE;
    }

    private static double hashNoise(long seed, int x, int z) {
        long h = seed;

        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;

        h = mix64(h);

        return ((h >>> 11) * 0x1.0p-52) - 1.0;
    }

    private static double unitHash(long seed, int x, int z) {
        long h = seed
                ^ ((long) x * 0x9E3779B97F4A7C15L)
                ^ ((long) z * 0xC2B2AE3D27D4EB4FL);

        h = mix64(h);

        return (h >>> 11) * 0x1.0p-53;
    }

    private static double unitHash(long seed, int x, int y, int z) {
        long h = seed
                ^ ((long) x * 0x9E3779B97F4A7C15L)
                ^ ((long) y * 0xC2B2AE3D27D4EB4FL)
                ^ ((long) z * 0x165667B19E3779F9L);

        h = mix64(h);

        return (h >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;

        return z ^ (z >>> 31);
    }
}
