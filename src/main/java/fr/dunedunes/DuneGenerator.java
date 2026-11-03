
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

    // Le générateur construit lui-même le terrain.
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
        final long seed = info.getSeed();
        final int minY = data.getMinHeight();
        final int maxY = data.getMaxHeight();

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
                                x, y, z,
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

    // Dunes arrondies de 20 blocs maximum.
    // Grandes zones plates entre les dunes.
    private static int duneHeight(long seed, int x, int z) {

        final int spacing = 130;
        double highestDune = 0.0;

        int cellX = Math.floorDiv(x, spacing);
        int cellZ = Math.floorDiv(z, spacing);

        for (int gx = cellX - 1; gx <= cellX + 1; gx++) {
            for (int gz = cellZ - 1; gz <= cellZ + 1; gz++) {

                double offsetX =
                        hashNoise(seed + 1234L, gx, gz) * 15.0;

                double offsetZ =
                        hashNoise(seed + 5678L, gx, gz) * 15.0;

                double centerX =
                        gx * spacing + spacing / 2.0 + offsetX;

                double centerZ =
                        gz * spacing + spacing / 2.0 + offsetZ;

                double radius =
                        20.0 + (hashNoise(seed + 9876L, gx, gz) + 1.0) * 4.0;

                double dx = x - centerX;
                double dz = z - centerZ;

                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance < radius) {

                    double t = distance / radius;

                    double shape =
                            0.5 * (1.0 + Math.cos(Math.PI * t));

                    double height = 20.0 * shape;

                    highestDune = Math.max(highestDune, height);
                }
            }
        }

        return (int) Math.round(72.0 + highestDune);
    }

    // Répartition indépendante bloc par bloc : 50/20/20/10.
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

    private static double unitHash(
            long seed,
            int x,
            int y,
            int z
    ) {
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
